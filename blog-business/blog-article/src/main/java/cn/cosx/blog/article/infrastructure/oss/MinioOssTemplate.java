package cn.cosx.blog.article.infrastructure.oss;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * MinIO实现的OSS上传模板
 */
@Slf4j
@Component
public class MinioOssTemplate implements OssTemplate {

    @Value("${minio.endpoint:http://127.0.0.1:9000}")
    private String endpoint;

    @Value("${minio.bucketName:blog-images}")
    private String bucketName;

    @Resource
    private MinioClient minioClient;

    private static final String[] IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp", "bmp"};

    @Override
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (!isImageExtension(extension)) {
            throw new IllegalArgumentException("不支持的图片格式: " + extension);
        }

        // 生成唯一文件名
        String objectName = generateUniqueFileName(extension);

        try {
            // 确保bucket存在
            ensureBucketExists();

            // 上传文件
            File tempFile = convertMultipartFileToFile(file);
            try {
                UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .filename(tempFile.getAbsolutePath())
                        .contentType(file.getContentType())
                        .build();
                minioClient.uploadObject(uploadObjectArgs);

                String url = String.format("%s/%s/%s", endpoint, bucketName, objectName);
                log.info("[MinIO] 上传图片成功: {} -> {}", originalFilename, url);
                return url;
            } finally {
                // 清理临时文件
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
            }
        } catch (Exception e) {
            log.error("[MinIO] 上传图片失败: {}", originalFilename, e);
            throw new RuntimeException("上传图片失败: " + originalFilename, e);
        }
    }

    /**
     * 确保bucket存在，不存在则创建
     */
    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(
                        io.minio.MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("[MinIO] 创建Bucket成功: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("[MinIO] 检查或创建Bucket失败: {}", bucketName, e);
            throw new RuntimeException("检查或创建Bucket失败: " + bucketName, e);
        }
    }

    /**
     * 验证是否为支持的图片扩展名
     */
    private boolean isImageExtension(String extension) {
        if (extension == null) {
            return false;
        }
        extension = extension.toLowerCase();
        for (String ext : IMAGE_EXTENSIONS) {
            if (ext.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 生成唯一文件名
     */
    private String generateUniqueFileName(String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "images/" + uuid + "." + extension;
    }

    /**
     * 将MultipartFile转换为File
     */
    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("upload-", "." + getFileExtension(multipartFile.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
        }
        return tempFile;
    }
}
