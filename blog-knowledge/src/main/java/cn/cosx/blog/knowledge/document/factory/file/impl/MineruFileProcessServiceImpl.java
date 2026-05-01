package cn.cosx.blog.knowledge.document.factory.file.impl;

import cn.cosx.blog.knowledge.document.domain.entity.KnowledgeDocument;
import cn.cosx.blog.knowledge.document.factory.file.FileProcessService;
import cn.cosx.blog.knowledge.document.infra.enums.DocumentStatus;
import cn.cosx.blog.knowledge.document.infra.file.MineruClient;
import cn.cosx.blog.knowledge.document.infra.file.MinioUtils;
import cn.cosx.blog.knowledge.document.infra.file.MultimodalClient;
import cn.cosx.blog.knowledge.document.service.IKnowledgeDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
public abstract class MineruFileProcessServiceImpl implements FileProcessService {

    @Autowired
    private IKnowledgeDocumentService knowledgeDocumentService;

    @Autowired
    private MineruClient mineruClient;

    @Autowired
    private MinioUtils minioUtils;

    @Autowired
    private MultimodalClient multimodalClient;

    @Value("${temp.dir:/tmp/knowledge}")
    private String tempDir;

    private static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[\\]\\(([^)]+)\\)");

    @Override
    public void processDocument(KnowledgeDocument document, MultipartFile file) {
        Long docId = document.getDocId();
        try {
            // 4. 更新状态为CONVERTING
            knowledgeDocumentService.updateStatus(docId, DocumentStatus.CONVERTING);
            log.info("[Document] 开始处理文档，docId: {}", docId);

            // 5. 调用Mineru进行文档处理
            String taskId = submitMineruTask(file);
            log.info("[Document] Mineru任务提交成功，docId: {}, taskId: {}", docId, taskId);

            // 6. 等待Mineru处理完成
            boolean completed = mineruClient.waitForCompletion(taskId, 30, java.util.concurrent.TimeUnit.MINUTES);
            if (!completed) {
                // 获取失败原因
                String errMsg = mineruClient.getErrorMessage(taskId);
                throw new RuntimeException("Mineru处理失败: " + (errMsg != null ? errMsg : "未知原因"));
            }

            // 7. 下载并解压Mineru返回的zip文件
            Path zipPath = Paths.get(tempDir, docId.toString(), "result.zip");
            mineruClient.downloadResult(taskId, zipPath);
            log.info("[Document] Mineru结果下载成功，docId: {}, path: {}", docId, zipPath);

            // 8. 解压zip文件
            Path extractPath = Paths.get(tempDir, docId.toString(), "extract");
            unzipFile(zipPath, extractPath);
            log.info("[Document] 解压成功，docId: {}, path: {}", docId, extractPath);

            // 9. 查找并处理markdown文件中的图片
            File mdFile = findMarkdownFile(extractPath);
            if (mdFile != null) {
                processMarkdownImages(mdFile, docId);
            }

            // 10. 上传转换后的文档到MinIO
            Path convertedDocPath = findConvertedDocument(extractPath);
            String convertedObjectName = String.format("documents/%d/converted", docId);
            String convertedDocUrl;
            if (convertedDocPath != null && convertedDocPath.toFile().exists()) {
                convertedDocUrl = minioUtils.uploadFile(convertedDocPath, convertedObjectName + "/document.md");
            } else if (mdFile != null) {
                convertedDocUrl = minioUtils.uploadFile(mdFile.toPath(), convertedObjectName + "/document.md");
            } else {
                throw new RuntimeException("未找到转换后的文档");
            }

            // 11. 更新转换后的文档URL和状态为CONVERTED
            knowledgeDocumentService.updateConvertedDocUrl(docId, convertedDocUrl);
            knowledgeDocumentService.updateStatus(docId, DocumentStatus.CONVERTED);
            log.info("[Document] 文档处理完成，docId: {}, convertedUrl: {}", docId, convertedDocUrl);

            log.info("[Document] 已发布文档切分事件，docId: {}", docId);

            // 12. 清理临时文件
            cleanupTempFiles(docId);

        } catch (Exception e) {
            log.error("[Document] 处理文档失败，docId: {}", docId, e);
            knowledgeDocumentService.updateStatus(docId, DocumentStatus.INIT);
        }
    }

    /**
     * 提交Mineru任务
     */
    private String submitMineruTask(MultipartFile file) throws Exception {
        // 1. 先上传文件到MinIO
        String originalFileName = file.getOriginalFilename();
        String objectName = String.format("temp/mineru/%d/%s", System.currentTimeMillis(), originalFileName);
        minioUtils.uploadFile(file, objectName);
        log.info("[Document] 文件已上传至MinIO，objectName: {}", objectName);

        // 2. 生成预签名URL（让Mineru能够从公网访问）
        String fileUrl = minioUtils.getPresignedObjectUrl(objectName, 60, java.util.concurrent.TimeUnit.MINUTES);
        log.info("[Document] 预签名URL生成成功，URL: {}", fileUrl);

        // 3. 提交Mineru任务
        return mineruClient.submitTask(fileUrl);
    }

    /**
     * 解压zip文件
     */
    private void unzipFile(Path zipPath, Path extractPath) throws Exception {
        extractPath.toFile().mkdirs();
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = extractPath.resolve(entry.getName());
                if (entry.isDirectory()) {
                    entryPath.toFile().mkdirs();
                } else {
                    entryPath.getParent().toFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(entryPath.toFile())) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    /**
     * 查找markdown文件
     */
    private File findMarkdownFile(Path dir) {
        File[] mdFiles = dir.toFile().listFiles((d, name) -> name.endsWith(".md"));
        if (mdFiles != null && mdFiles.length > 0) {
            return mdFiles[0];
        }
        // 递归查找
        File[] subDirs = dir.toFile().listFiles(File::isDirectory);
        if (subDirs != null) {
            for (File subDir : subDirs) {
                File mdFile = findMarkdownFile(subDir.toPath());
                if (mdFile != null) {
                    return mdFile;
                }
            }
        }
        return null;
    }

    /**
     * 查找转换后的文档
     */
    private Path findConvertedDocument(Path dir) {
        // 优先查找.md文件
        File mdFile = findMarkdownFile(dir);
        if (mdFile != null) {
            return mdFile.toPath();
        }
        return null;
    }

    /**
     * 处理markdown中的图片
     * 1. 调用多模态大模型描述图片
     * 2. 上传图片到MinIO
     * 3. 修改![]()为新的URL
     */
    private void processMarkdownImages(File mdFile, Long docId) throws Exception {
        String content = new String(Files.readAllBytes(mdFile.toPath()));
        Matcher matcher = IMAGE_PATTERN.matcher(content);

        // 获取mdFile所在的目录，用于解析相对路径图片
        Path mdFileDir = mdFile.getParentFile().toPath();

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String imageUrl = matcher.group(1);
            log.info("[Document] 发现图片: {}", imageUrl);

            // 下载图片到本地
            Path imagePath = downloadImage(imageUrl, mdFileDir, docId);
            if (imagePath != null && imagePath.toFile().exists()) {
                // 调用多模态大模型描述图片
                String description = multimodalClient.generateImageDescription(imagePath);

                // 上传图片到MinIO
                String imageObjectName = String.format("documents/%d/images/%s", docId, imagePath.getFileName());
                String uploadedUrl = minioUtils.uploadFile(imagePath, imageObjectName);

                // 替换原文中的图片引用
                String replacement = "![](" + uploadedUrl + ") " + description;
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                log.info("[Document] 图片处理完成: {} -> {}", imageUrl, uploadedUrl);
            }
        }
        matcher.appendTail(sb);

        // 写回文件
        Files.write(mdFile.toPath(), sb.toString().getBytes());
    }

    /**
     * 下载图片
     */
    private Path downloadImage(String imageUrl, Path mdFileDir, Long docId) {
        try {
            Path imagePath = Paths.get(tempDir, docId.toString(), "images", UUID.randomUUID().toString() + ".png");
            imagePath.getParent().toFile().mkdirs();

            // 如果是本地文件路径或minio URL，直接复制
            // 如果是外部URL，使用HTTP下载
            if (imageUrl.startsWith("http")) {
                try (InputStream in = new java.net.URL(imageUrl).openStream()) {
                    Files.copy(in, imagePath);
                }
            } else {
                // 本地文件 - 相对于mdFile所在目录解析
                Path sourcePath = mdFileDir.resolve(imageUrl);
                Files.copy(sourcePath, imagePath);
            }
            return imagePath;
        } catch (Exception e) {
            log.error("[Document] 下载图片失败: {}", imageUrl, e);
            return null;
        }
    }

    /**
     * 清理临时文件
     */
    private void cleanupTempFiles(Long docId) {
        try {
            Path tempPath = Paths.get(tempDir, docId.toString());
            if (tempPath.toFile().exists()) {
                deleteDirectory(tempPath.toFile());
                log.info("[Document] 清理临时文件成功，docId: {}", docId);
            }
        } catch (Exception e) {
            log.warn("[Document] 清理临时文件失败，docId: {}", docId, e);
        }
    }

    /**
     * 递归删除目录
     */
    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }



}
