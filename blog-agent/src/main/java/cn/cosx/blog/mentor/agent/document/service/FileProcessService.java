package cn.cosx.blog.mentor.agent.document.service;

import cn.cosx.blog.mentor.agent.document.entity.KnowledgeDocument;
import cn.cosx.blog.mentor.agent.document.enums.DocumentStatus;
import org.springframework.ai.content.Media;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import org.springframework.ai.chat.messages.UserMessage;

import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.MimeTypeUtils;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
@Slf4j
public class FileProcessService {

    private final Tika tika = new Tika();

    private static final String MINERU_API_URL = "https://mineru.net/api/v4/extract/task";
    private static final String MINERU_RESULT_URL = "https://mineru.net/api/v4/extract/result/";
    private static final String MINERU_FILE_PARSE_URL = "https://mineru.net/api/v4/extract/file_parse";
    private static final String CONVERTED_FILE_DIR = "converted/";
    private static final int MAX_POLL_ATTEMPTS = 60; // 最大轮询次数
    private static final long POLL_INTERVAL_MS = 10000; // 轮询间隔10秒

    @Value("${mineru.api-key}")
    private String mineruApiKey;

    @Value("${mineru.api-url}")
    private String mineruApiUrl;

    @Value("${mineru.model-version}")
    private String modelVersion;

    @Value("${mineru.use-async-api:false}")
    private boolean useAsyncApi;

    @Value("${spring.ai.openai.api-key}")
    private String chatModelApiKey;

    @Value("${spring.ai.openai.base-url}")
    private String chatModelBaseUrl;

    @Value("${mineru.api-url:https://mineru.net}")
    private String fileParseApiUrl;

    @Value("${mineru.connect-timeout:60000}")
    private int connectTimeout;

    @Value("${mineru.response-timeout:300000}")
    private int responseTimeout;

    @Autowired
    private KnowledgeDocumentService knowledgeDocumentService;

    @Autowired
    private cn.cosx.blog.mentor.agent.utils.FileStorageService fileStorageService;



    /**
     * 处理文档转换 - Markdown 格式
     * 1. 从 MinIO 下载文件
     * 2. 调用文档解析接口获取md/zip
     * 3. 转换后的文档保存在minio上
     * 3. 更新文档状态和转换后的 URL
     *
     * @param document 文档对象
     */
    public void processDocument(KnowledgeDocument document, InputStream inputStream) {
        // 使用 Tika 检测文件 MIME 类型
        try {
            String detectedType = tika.detect(inputStream);
            log.info("Tika 检测到文件类型: {}, 文档标题: {}", detectedType, document.getDocTitle());

            if ("application/pdf".equals(detectedType)) {
                log.info("检测到 PDF 文件，开始处理: {}", document.getDocTitle());
                processPdfDocument(document, inputStream);
            } else {
                // 其他类型走默认流程
                processDocumentToMarkdownFromZip(document, inputStream);
            }
        } catch (Exception e) {
            log.error("Tika 检测文件类型失败，文档标题: {}", document.getDocTitle(), e);
            // 检测失败时走默认流程
            processDocumentToMarkdownFromZip(document, inputStream);
        }
    }

    /**
     * 处理 PDF 文档
     * 可以针对 PDF 进行特殊处理逻辑
     *
     * @param document 文档对象
     * @param inputStream 文件输入流
     */
    public void processPdfDocument(KnowledgeDocument document, InputStream inputStream) {
        log.info("开始处理 PDF 文档: {}", document.getDocTitle());
        // TODO: PDF 特殊处理逻辑
        processDocumentToMarkdownFromZip(document, inputStream);
    }


    /**
     * 处理文档转换为 ZIP 格式
     * 1. 调用文档解析接口获取 ZIP（包含 Markdown 和图片）
     * 2. 保存 ZIP 到本地磁盘
     * 3. 解压 ZIP 文件
     * 4. 上传解压后的 md 和图片到 MinIO
     * 5. 替换 md 中的图片地址为 MinIO 地址
     * 6. 调用 LLM 生成图片描述并更新 md
     * 7. 保存 md 的 MinIO 地址到 convertedUrl
     * 8. 异步清理本地临时文件
     *
     * @param document 文档对象
     */
    public void processDocumentToMarkdownFromZip(KnowledgeDocument document, InputStream inputStream) {
        log.info("开始处理文档转换为 ZIP，documentId: {}", document.getDocTitle());

        // 更新状态为转换中
        document.setStatus(DocumentStatus.CONVERTING);
        boolean result = knowledgeDocumentService.updateById(document);
        Assert.isTrue(result, "文件CONVERTING状态更新失败");

        String zipFilePath = null;
        String extractDir = null;

        try {
            // 生成一串数字，避免文件名的中文乱码
            String docTitle = document.getDocTitle() + document.getDocTitle().hashCode();

            // 1. 调用文档解析获取 ZIP 格式响应
            byte[] zipBytes;
            if (useAsyncApi) {
                // 使用异步任务 API
                zipBytes = parseDocumentToZipAsync(docTitle, inputStream);
            } else {
                // 使用同步文件解析 API
                zipBytes = parseDocumentToZip(docTitle, inputStream);
            }

            // 2. 保存 ZIP 到本地临时目录
            String tempDir = System.getProperty("java.io.tmpdir");
            String uniqueId = UUID.randomUUID().toString();
            zipFilePath = tempDir + File.separator + uniqueId + ".zip";
            extractDir = tempDir + File.separator + uniqueId + "_extracted";

            Files.write(Paths.get(zipFilePath), zipBytes);
            log.info("ZIP 文件已保存到本地: {}", zipFilePath);

            // 3. 解压 ZIP 文件
            extractZip(zipFilePath, extractDir);
            log.info("ZIP 文件已解压到: {}", extractDir);

            // 4. 上传解压后的 md 和图片到 MinIO，并处理 md 内容
            String mdMinioUrl = processExtractedFiles(document, extractDir);

            // 5. 更新文档状态为已转换，保存 md 的 MinIO 地址
            document.setStatus(DocumentStatus.CONVERTED);
            document.setConvertedDocUrl(mdMinioUrl);
            result = knowledgeDocumentService.updateById(document);
            Assert.isTrue(result, "文件CONVERTED状态更新失败");

            log.info("文档 ZIP 转换完成，documentId: {}, mdUrl: {}", document.getDocTitle(), mdMinioUrl);
        } catch (Exception e) {
            log.error("文档 ZIP 转换失败，documentId: {}", document.getDocTitle(), e);
            // 转换失败，状态回滚为 UPLOADED
            document.setStatus(DocumentStatus.UPLOADED);
            result = knowledgeDocumentService.updateById(document);
            Assert.isTrue(result, "文件UPLOADED状态更新失败");
            throw new RuntimeException("文档 ZIP 转换失败: " + e.getMessage(), e);
        } finally {
            closeQuietly(inputStream);
            // 异步清理临时文件
            cleanupTempFilesAsync(zipFilePath, extractDir);
        }
    }

    /**
     * 解压 ZIP 文件到指定目录
     */
    private void extractZip(String zipFilePath, String extractDir) throws IOException {
        Path extractPath = Paths.get(extractDir);
        Files.createDirectories(extractPath);

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = extractPath.resolve(entry.getName());

                // 安全检查：防止 ZIP 路径遍历攻击
                if (!entryPath.normalize().startsWith(extractPath.normalize())) {
                    log.warn("跳过不安全的 ZIP 条目: {}", entry.getName());
                    continue;
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }

    /**
     * 处理解压后的文件：上传 md 和图片到 MinIO，替换图片地址，生成图片描述
     */
    private String processExtractedFiles(KnowledgeDocument document, String extractDir) throws Exception {
        Path extractPath = Paths.get(extractDir);

        // 查找所有的 md 文件和图片文件
        Path mdFile = null;
        java.util.List<Path> imageFiles = new java.util.ArrayList<>();

        try (Stream<Path> paths = Files.walk(extractPath)) {
            for (Path path : paths.toList()) {
                if (Files.isRegularFile(path)) {
                    String fileName = path.getFileName().toString().toLowerCase();
                    if (fileName.endsWith(".md")) {
                        mdFile = path;
                    } else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") ||
                            fileName.endsWith(".jpeg") || fileName.endsWith(".gif") ||
                            fileName.endsWith(".webp") || fileName.endsWith(".bmp")) {
                        imageFiles.add(path);
                    }
                }
            }
        }

        if (mdFile == null) {
            throw new RuntimeException("解压后的文件夹中未找到 Markdown 文件");
        }

        log.info("找到 Markdown 文件: {}, 图片文件数量: {}", mdFile, imageFiles.size());

        // 上传图片到 MinIO，并建立本地文件名到 MinIO URL 的映射
        java.util.Map<String, String> imageUrlMap = new java.util.HashMap<>();
        String baseObjectName = CONVERTED_FILE_DIR + document.getDocTitle() + "/";

        for (Path imagePath : imageFiles) {
            String imageName = imagePath.getFileName().toString();
            byte[] imageBytes = Files.readAllBytes(imagePath);
            String contentType = getImageContentType(imageName);
            String objectName = baseObjectName + "images/" + imageName;
            String imageUrl = fileStorageService.uploadFile(objectName, imageBytes, contentType);
            imageUrlMap.put(imageName, imageUrl);
            log.info("图片已上传到 MinIO: {} -> {}", imageName, imageUrl);
        }

        // 读取 md 文件内容
        String mdContent = Files.readString(mdFile, StandardCharsets.UTF_8);

        // 替换 md 中的图片地址为 MinIO 地址，并生成图片描述
        String processedMdContent = processMarkdownImages(mdContent, imageUrlMap);

        // 上传处理后的 md 文件到 MinIO
        String mdObjectName = baseObjectName + mdFile.getFileName().toString();
        String mdUrl = fileStorageService.uploadFile(mdObjectName, processedMdContent.getBytes(StandardCharsets.UTF_8), "text/markdown");
        log.info("Markdown 文件已上传到 MinIO: {}", mdUrl);

        return mdUrl;
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

    /**
     * 处理 Markdown 中的图片标签：替换地址并生成图片描述
     * 匹配格式: ![](xxx.png) 或 ![alt](xxx.png)
     */
    private String processMarkdownImages(String mdContent, java.util.Map<String, String> imageUrlMap) {
        // 匹配图片标签的正则表达式: ![alt](path)
        Pattern pattern = Pattern.compile("!\\[(.*?)\\]\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(mdContent);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String altText = matcher.group(1);
            String imagePath = matcher.group(2);

            // 提取图片文件名
            String imageName = Paths.get(imagePath).getFileName().toString();

            // 获取 MinIO 上的图片 URL
            String minioUrl = imageUrlMap.get(imageName);
            if (minioUrl == null) {
                // 如果找不到对应的 MinIO URL，保持原样
                log.warn("未找到图片 {} 对应的 MinIO URL", imageName);
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }

            // 生成图片描述（mock 实现）
            String imageDescription = generateImageDescription(minioUrl);

            // 构建新的图片标签: ![描述](minio_url)
            String newImageTag = "![" + imageDescription + "](" + minioUrl + ")";
            matcher.appendReplacement(result, Matcher.quoteReplacement(newImageTag));

            log.info("图片标签已处理: {} -> {}", imagePath, minioUrl);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 生成图片描述
     * 需要注意的是，如果你用的是外部的模型，这个url需要是公网可以访问的url。否则模型需要能和MinIO进行内网通信。
     */
    public String generateImageDescription(String imageUrl) {
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

        Media media = new Media(MimeTypeUtils.IMAGE_PNG, java.net.URI.create(imageUrl));
        UserMessage userMessage = UserMessage.builder()
                .text("请描述这张图片的内容，包括场景、对象、布局、颜色、文字信息，直接输出纯文本描述，不要多余说明。")
                .media(java.util.List.of(media))
                .build();
        var response = chatModel.call(new org.springframework.ai.chat.prompt.Prompt(java.util.List.of(userMessage)));
        return response.getResult().getOutput().getText();
    }

    /**
     * 异步清理临时文件
     */
    private void cleanupTempFilesAsync(String zipFilePath, String extractDir) {
        if (zipFilePath == null && extractDir == null) {
            return;
        }

        Thread.startVirtualThread(() -> {
            try {
                // 删除 ZIP 文件
                if (zipFilePath != null) {
                    Files.deleteIfExists(Paths.get(zipFilePath));
                    log.info("临时 ZIP 文件已删除: {}", zipFilePath);
                }

                // 删除解压目录
                if (extractDir != null) {
                    deleteDirectory(Paths.get(extractDir));
                    log.info("临时解压目录已删除: {}", extractDir);
                }
            } catch (Exception e) {
                log.warn("清理临时文件失败", e);
            }
        });
    }

    /**
     * 递归删除目录
     */
    private void deleteDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted((a, b) -> -a.compareTo(b)) // 反向排序，先删除子文件/目录
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("删除文件失败: {}", path, e);
                        }
                    });
        }
    }

    /**
     * 调用文件解析接口
     * 使用 Apache HttpClient 5 替代 HttpURLConnection，提供更好的超时控制和连接管理
     *
     * @param fileName   文件名
     * @param fileStream 文件输入流
     * @return 解析结果
     */
    private String parseDocumentToMarkdown(String fileName, InputStream fileStream) {
        String url = fileParseApiUrl + "/file_parse";

        // 配置请求超时
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(Timeout.ofMilliseconds(connectTimeout)).setResponseTimeout(Timeout.ofMilliseconds(responseTimeout)).build();

        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build()) {

            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Accept", "application/json");

            // 构建 multipart 请求体
            org.apache.hc.core5.http.HttpEntity multipartEntity = MultipartEntityBuilder.create().addBinaryBody("files", fileStream, org.apache.hc.core5.http.ContentType.APPLICATION_OCTET_STREAM, fileName).addTextBody("backend", "pipeline").addTextBody("response_format_zip", "false").addTextBody("return_images", "false").addTextBody("return_model_output", "false").addTextBody("return_middle_json", "false").build();

            httpPost.setEntity(multipartEntity);

            log.info("开始调用文件解析接口: {}", url);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                log.info("文件解析接口响应状态码: {}", statusCode);

                org.apache.hc.core5.http.HttpEntity responseEntity = response.getEntity();
                String responseBody = responseEntity != null ? EntityUtils.toString(responseEntity, "UTF-8") : "";

                if (statusCode == 200) {
                    log.info("文件解析接口调用成功，响应体长度: {}", responseBody.length());
                    return responseBody;
                } else {
                    log.error("文件解析接口调用失败，状态码: {}, 响应: {}", statusCode, responseBody);
                    throw new RuntimeException("文件解析接口调用失败: HTTP " + statusCode + ", " + responseBody);
                }
            }
        } catch (Exception e) {
            log.error("调用文件解析接口异常", e);
            throw new RuntimeException("调用文件解析接口失败: " + e.getMessage(), e);
        } finally {
            closeQuietly(fileStream);
        }
    }

    /**
     * 调用文件解析接口，获取 ZIP 格式响应
     * 使用 Apache HttpClient 5，支持流式下载大文件
     *
     * @param fileName   文件名
     * @param fileStream 文件输入流
     * @return ZIP 文件字节数组
     */
    private byte[] parseDocumentToZip(String fileName, InputStream fileStream) {
        String url = fileParseApiUrl + "/file_parse";

        // 配置请求超时
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(Timeout.ofMilliseconds(connectTimeout)).setResponseTimeout(Timeout.ofMilliseconds(responseTimeout)).build();

        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build()) {

            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + mineruApiKey);
            // 构建 multipart 请求体，启用 ZIP 格式和返回图片
            org.apache.hc.core5.http.HttpEntity multipartEntity = MultipartEntityBuilder.create()
                    .addBinaryBody("files", fileStream, org.apache.hc.core5.http.ContentType.APPLICATION_OCTET_STREAM, fileName)
                    .addTextBody("backend", "pipeline").addTextBody("response_format_zip", "true")
                    .addTextBody("return_images", "true").addTextBody("return_model_output", "false")
                    .addTextBody("return_middle_json", "false").build();

            httpPost.setEntity(multipartEntity);

            log.info("开始调用文件解析接口（ZIP 模式）: {}", url);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                log.info("文件解析接口响应状态码: {}", statusCode);

                HttpEntity responseEntity = response.getEntity();
                if (statusCode == 200 && responseEntity != null) {
                    // 读取响应体为字节数组（ZIP 文件）
                    byte[] zipBytes = EntityUtils.toByteArray(responseEntity);
                    log.info("文件解析接口调用成功，ZIP 文件大小: {} bytes", zipBytes.length);
                    return zipBytes;
                } else {
                    String responseBody = responseEntity != null ? EntityUtils.toString(responseEntity, "UTF-8") : "";
                    log.error("文件解析接口调用失败，状态码: {}, 响应: {}", statusCode, responseBody);
                    throw new RuntimeException("文件解析接口调用失败: HTTP " + statusCode + ", " + responseBody);
                }
            }

        } catch (Exception e) {
            log.error("调用文件解析接口异常", e);
            throw new RuntimeException("调用文件解析接口失败: " + e.getMessage(), e);
        } finally {
            closeQuietly(fileStream);
        }
    }

    /**
     * 使用异步任务方式调用 MinerU API
     * 1. 上传文件到 MinIO 获取 URL
     * 2. 提交提取任务
     * 3. 轮询任务结果
     * 4. 下载 ZIP 文件
     *
     * @param fileName   文件名
     * @param fileStream 文件输入流
     * @return ZIP 文件字节数组
     */
    private byte[] parseDocumentToZipAsync(String fileName, InputStream fileStream) {
        log.info("开始异步调用 MinerU API 处理文档: {}", fileName);

        String tempFileName = "temp/" + UUID.randomUUID().toString() + "_" + fileName;
        String fileUrl = null;

        try {
            // 1. 上传文件到 MinIO 获取 URL
            byte[] fileBytes = fileStream.readAllBytes();
            fileUrl = fileStorageService.uploadFile(tempFileName, fileBytes, getContentType(fileName));
            log.info("文件已上传到 MinIO，URL: {}", fileUrl);

            // 2. 提交提取任务
            String taskId = submitExtractionTask(fileUrl);
            log.info("提取任务已提交，任务 ID: {}", taskId);

            // 3. 轮询任务结果
            String resultUrl = pollTaskResult(taskId);
            log.info("任务完成，结果 URL: {}", resultUrl);

            // 4. 下载 ZIP 文件
            return downloadZipFromUrl(resultUrl);

        } catch (Exception e) {
            log.error("异步调用 MinerU API 失败", e);
            throw new RuntimeException("异步调用 MinerU API 失败: " + e.getMessage(), e);
        } finally {
            closeQuietly(fileStream);
            // 异步清理临时文件
            cleanupTempFileAsync(tempFileName);
        }
    }

    /**
     * 获取文件的 Content-Type
     */
    private String getContentType(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".pdf")) return "application/pdf";
        if (lowerName.endsWith(".doc")) return "application/msword";
        if (lowerName.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lowerName.endsWith(".txt")) return "text/plain";
        if (lowerName.endsWith(".md")) return "text/markdown";
        return "application/octet-stream";
    }

    /**
     * 异步清理临时文件
     */
    private void cleanupTempFileAsync(String objectName) {
        if (objectName == null) {
            return;
        }

        Thread.startVirtualThread(() -> {
            try {
                fileStorageService.deleteFile(objectName);
                log.info("临时文件已删除: {}", objectName);
            } catch (Exception e) {
                log.warn("清理临时文件失败: {}", objectName, e);
            }
        });
    }

    /**
     * 提交提取任务
     */
    private String submitExtractionTask(String fileUrl) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        // 构建请求体
        JsonNode requestJson = mapper.createObjectNode()
                .put("url", fileUrl)
                .put("model_version", modelVersion != null ? modelVersion : "vlm");

        String requestBody = mapper.writeValueAsString(requestJson);
        log.info("提交提取任务请求: {}", requestBody);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectTimeout))
                .setResponseTimeout(Timeout.ofMilliseconds(responseTimeout))
                .build();

        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build()) {
            HttpPost httpPost = new HttpPost(MINERU_API_URL);
            httpPost.setHeader("Authorization", "Bearer " + mineruApiKey);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Accept", "*/*");
            httpPost.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(requestBody, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                
                log.info("提交提取任务响应状态码: {}, 响应体: {}", statusCode, responseBody);

                if (statusCode == 200) {
                    JsonNode responseJson = mapper.readTree(responseBody);
                    if (responseJson.get("code").asInt() == 0) {
                        return responseJson.get("data").get("task_id").asText();
                    } else {
                        throw new RuntimeException("提交提取任务失败: " + responseJson.get("msg").asText());
                    }
                } else {
                    throw new RuntimeException("提交提取任务失败: HTTP " + statusCode);
                }
            }
        }
    }

    /**
     * 轮询任务结果
     */
    private String pollTaskResult(String taskId) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String resultUrl = MINERU_RESULT_URL + taskId;

        for (int attempt = 0; attempt < MAX_POLL_ATTEMPTS; attempt++) {
            log.info("轮询任务结果，任务 ID: {}, 尝试次数: {}", taskId, attempt + 1);

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectTimeout))
                    .setResponseTimeout(Timeout.ofMilliseconds(responseTimeout))
                    .build();

            try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build()) {
                HttpPost httpPost = new HttpPost(resultUrl);
                httpPost.setHeader("Authorization", "Bearer " + mineruApiKey);
                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setHeader("Accept", "*/*");

                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    int statusCode = response.getCode();
                    String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                    
                    log.info("轮询任务结果响应状态码: {}, 响应体: {}", statusCode, responseBody);

                    if (statusCode == 200) {
                        JsonNode responseJson = mapper.readTree(responseBody);
                        if (responseJson.get("code").asInt() == 0) {
                            JsonNode data = responseJson.get("data");
                            String status = data.get("status").asText();
                            
                            if ("completed".equals(status)) {
                                // 任务完成，返回结果 URL
                                return data.get("result_url").asText();
                            } else if ("failed".equals(status)) {
                                throw new RuntimeException("任务处理失败: " + data.get("error_msg").asText());
                            }
                            // 任务仍在处理中，继续轮询
                        } else {
                            throw new RuntimeException("轮询任务结果失败: " + responseJson.get("msg").asText());
                        }
                    } else {
                        throw new RuntimeException("轮询任务结果失败: HTTP " + statusCode);
                    }
                }
            }

            // 等待一段时间后再次轮询
            Thread.sleep(POLL_INTERVAL_MS);
        }

        throw new RuntimeException("任务处理超时，最大轮询次数已达: " + MAX_POLL_ATTEMPTS);
    }

    /**
     * 从 URL 下载 ZIP 文件
     */
    private byte[] downloadZipFromUrl(String url) throws Exception {
        log.info("开始下载 ZIP 文件: {}", url);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectTimeout))
                .setResponseTimeout(Timeout.ofMilliseconds(responseTimeout))
                .build();

        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Authorization", "Bearer " + mineruApiKey);
            httpPost.setHeader("Accept", "*/*");

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                
                if (statusCode == 200) {
                    byte[] zipBytes = EntityUtils.toByteArray(response.getEntity());
                    log.info("ZIP 文件下载成功，大小: {} bytes", zipBytes.length);
                    return zipBytes;
                } else {
                    String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                    log.error("下载 ZIP 文件失败，状态码: {}, 响应: {}", statusCode, responseBody);
                    throw new RuntimeException("下载 ZIP 文件失败: HTTP " + statusCode);
                }
            }
        }
    }

    /**
     * 安静关闭输入流，忽略异常
     *
     * @param inputStream 输入流
     */
    private void closeQuietly(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception ignored) {
                // 忽略关闭异常
            }
        }
    }
}