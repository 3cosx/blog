package cn.cosx.blog.mentor.agent.document.service.impl;

import cn.cosx.blog.mentor.agent.document.entity.KnowledgeDocument;
import cn.cosx.blog.mentor.agent.document.enums.DocumentStatus;
import cn.cosx.blog.mentor.agent.document.rag.event.DocumentChunkEvent;
import cn.cosx.blog.mentor.agent.document.service.DocumentUploadService;
import cn.cosx.blog.mentor.agent.document.service.FileProcessService;
import cn.cosx.blog.mentor.agent.document.service.KnowledgeDocumentService;
import cn.cosx.blog.mentor.agent.utils.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文档上传服务实现
 */
@Service
@Slf4j
public class DocumentUploadServiceImpl implements DocumentUploadService {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private KnowledgeDocumentService documentService;

    @Autowired
    private FileProcessService fileProcessService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${spring.ai.openai.api-key}")
    private String chatModelApiKey;

    @Value("${spring.ai.openai.base-url}")
    private String chatModelBaseUrl;

    private final Tika tika = new Tika();
    private static final String CONVERTED_FILE_DIR = "converted/";

    @Override
    public KnowledgeDocument uploadDocument(MultipartFile file, String docTitle, String uploadUser, String accessibleBy) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        KnowledgeDocument document = null;
        try {
            log.info("【步骤1】开始处理文件: {}", originalFilename);
            
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String objectName = "documents/" + UUID.randomUUID().toString() + extension;

            String docUrl = fileStorageService.uploadFile(file, objectName);
            log.info("文件上传到MinIO成功: docUrl={}", docUrl);

            document = new KnowledgeDocument();
            document.setDocTitle(docTitle != null ? docTitle : originalFilename);
            document.setUploadUser(uploadUser != null ? uploadUser : "anonymous");
            document.setDocUrl(docUrl);
            document.setConvertedDocUrl(docUrl);
            document.setStatus(DocumentStatus.UPLOADED);
            document.setAccessibleBy(accessibleBy);

            documentService.save(document);
            log.info("文档信息保存成功, docId={}", document.getDocId());

            String detectedType = tika.detect(file.getInputStream());
            log.info("Tika 检测到文件类型: {}, 文件名: {}", detectedType, originalFilename);

            if ("application/pdf".equals(detectedType)) {
                fileProcessService.processPdfDocument(document, file.getInputStream());
            } else if ("text/markdown".equals(detectedType) || ".md".equalsIgnoreCase(extension)) {
                // 处理 Markdown 文件：上传图片到 MinIO 并调用大模型关联
                processMarkdownWithImages(document, file.getInputStream());
            }

            document = documentService.getById(document.getDocId());
            if (document != null && document.getConvertedDocUrl() != null) {
                // 发布文档切分事件，触发异步文档切分流程
                eventPublisher.publishEvent(new DocumentChunkEvent(this, document.getDocId()));
                log.info("已发布文档切分事件: docId={}", document.getDocId());
            }

            log.info("【完成】文档上传全流程完成: docId={}", document.getDocId());
        } catch (Exception e) {
            log.error("文档上传失败: {}", originalFilename, e);
        }
        return document;
    }

    /**
     * 处理 Markdown 文件：提取图片、上传到 MinIO、调用大模型关联
     */
    private void processMarkdownWithImages(KnowledgeDocument document, java.io.InputStream inputStream) throws Exception {
        log.info("[MD处理] 开始处理 Markdown 文件中的图片: {}", document.getDocTitle());

        // 更新状态为转换中
        document.setStatus(DocumentStatus.CONVERTING);
        documentService.updateById(document);
        log.info("[MD处理] 文档状态已更新为 CONVERTING");

        try {
            // 读取 Markdown 内容
            log.info("[MD处理] 开始读取Markdown内容...");
            String mdContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            log.info("[MD处理] Markdown内容读取完成, 长度: {}", mdContent.length());

            // 提取并处理图片
            log.info("[MD处理] 开始处理图片...");
            String processedMdContent = processMarkdownImages(document, mdContent);
            log.info("[MD处理] 图片处理完成");

            // 上传处理后的 Markdown 到 MinIO
            log.info("[MD处理] 开始上传处理后的Markdown到MinIO...");
            String baseObjectName = CONVERTED_FILE_DIR + document.getDocTitle() + "/";
            String mdObjectName = baseObjectName + document.getDocTitle() + ".md";
            String mdUrl = fileStorageService.uploadFile(mdObjectName, processedMdContent.getBytes(StandardCharsets.UTF_8), "text/markdown");
            log.info("[MD处理] Markdown上传成功, url: {}", mdUrl);

            // 更新文档状态和转换后的 URL
            document.setStatus(DocumentStatus.CONVERTED);
            document.setConvertedDocUrl(mdUrl);
            documentService.updateById(document);
            log.info("[MD处理] 文档状态已更新为 CONVERTED");

            log.info("Markdown 文件处理完成: {}, mdUrl: {}", document.getDocTitle(), mdUrl);
        } catch (Exception e) {
            log.error("Markdown 文件处理失败: {}", document.getDocTitle(), e);
            document.setStatus(DocumentStatus.UPLOADED);
            documentService.updateById(document);
            throw e;
        }
    }

    /**
     * 处理 Markdown 中的图片：上传到 MinIO 并调用大模型生成描述
     */
    private String processMarkdownImages(KnowledgeDocument document, String mdContent) throws Exception {
        // 匹配图片标签的正则表达式: ![alt](path)
        Pattern pattern = Pattern.compile("!\\[(.*?)\\]\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(mdContent);

        StringBuffer result = new StringBuffer();
        Map<String, String> localImageMap = new HashMap<>();

        // 第一遍：收集所有本地图片路径
        while (matcher.find()) {
            String imagePath = matcher.group(2);
            // 只处理本地图片（不是http链接）
            if (!imagePath.startsWith("http://") && !imagePath.startsWith("https://")) {
                localImageMap.put(imagePath, imagePath);
            }
        }
        matcher.reset();

        // 上传本地图片到 MinIO，建立路径映射
        String baseObjectName = CONVERTED_FILE_DIR + document.getDocTitle() + "/images/";
        Map<String, String> imageUrlMap = new HashMap<>();

        for (Map.Entry<String, String> entry : localImageMap.entrySet()) {
            String localPath = entry.getKey();
            String imageName = Path.of(localPath).getFileName().toString();
            String objectName = baseObjectName + imageName;

            try {
                byte[] imageBytes = Files.readAllBytes(Path.of(localPath));
                String contentType = getImageContentType(imageName);
                String imageUrl = fileStorageService.uploadFile(objectName, imageBytes, contentType);
                imageUrlMap.put(localPath, imageUrl);
                log.info("图片已上传到 MinIO: {} -> {}", localPath, imageUrl);
            } catch (Exception e) {
                log.warn("图片上传失败，跳过: {}", localPath, e);
            }
        }

        // 第二遍：替换图片路径并生成描述
        while (matcher.find()) {
            String altText = matcher.group(1);
            String imagePath = matcher.group(2);

            String minioUrl = imageUrlMap.get(imagePath);
            if (minioUrl != null) {
                // 调用大模型生成图片描述
                String imageDescription = generateImageDescription(minioUrl);
                // 构建新的图片标签: ![描述](minio_url)
                String newImageTag = "![" + imageDescription + "](" + minioUrl + ")";
                matcher.appendReplacement(result, Matcher.quoteReplacement(newImageTag));
                log.info("图片标签已更新: {} -> {}", imagePath, minioUrl);
            } else {
                // 非本地图片或上传失败的，保持原样
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 生成图片描述
     */
    private String generateImageDescription(String imageUrl) {
        try {
            log.info("[图片描述] 开始生成图片描述, url: {}", imageUrl);
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .temperature(0.2d)
                    .model("qwen3-vl-plus")
                    .build();

            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(OpenAiApi.builder()
                            .baseUrl(chatModelBaseUrl)
                            .apiKey(new SimpleApiKey(chatModelApiKey))
                            .build())
                    .defaultOptions(options)
                    .build();

            Media media = new Media(MimeTypeUtils.IMAGE_PNG, URI.create(imageUrl));
            UserMessage userMessage = UserMessage.builder()
                    .text("请描述这张图片的内容，包括场景、对象、布局、颜色、文字信息，直接输出纯文本描述，不要多余说明。")
                    .media(java.util.List.of(media))
                    .build();
            
            log.info("[图片描述] 开始调用大模型...");
            var response = chatModel.call(new org.springframework.ai.chat.prompt.Prompt(java.util.List.of(userMessage)));
            String description = response.getResult().getOutput().getText();
            log.info("[图片描述] 大模型调用完成, 描述长度: {}", description.length());
            return description;
        } catch (Exception e) {
            log.warn("生成图片描述失败，使用空描述: {}", imageUrl, e);
            return "";
        }
    }

    /**
     * 获取图片的 Content-Type
     */
    private String getImageContentType(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".png")) return "image/png";
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) return "image/jpeg";
        if (lowerName.endsWith(".gif")) return "image/gif";
        if (lowerName.endsWith(".webp")) return "image/webp";
        if (lowerName.endsWith(".bmp")) return "image/bmp";
        return "application/octet-stream";
    }

}