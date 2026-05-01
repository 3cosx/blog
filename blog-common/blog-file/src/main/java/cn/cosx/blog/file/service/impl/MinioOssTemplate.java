package cn.cosx.blog.file.service.impl;

import cn.cosx.blog.file.config.MinioProperties;
import cn.cosx.blog.file.service.OssTemplate;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class MinioOssTemplate implements OssTemplate {

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioProperties minioProperties;

    private static final String[] IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp", "bmp"};

    @Override
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (!isImageExtension(extension)) {
            throw new IllegalArgumentException("不支持的图片格式: " + extension);
        }

        String objectName = generateUniqueFileName(extension);

        ensureBucketExists();

        File tempFile = null;
        try {
            tempFile = convertMultipartFileToFile(file);
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .filename(tempFile.getAbsolutePath())
                    .contentType(file.getContentType())
                    .build();
            minioClient.uploadObject(uploadObjectArgs);

            String url = String.format("%s/%s/%s", minioProperties.getEndpoint(), minioProperties.getBucketName(), objectName);
            log.info("[MinIO] 上传图片成功: {} -> {}", originalFilename, url);
            return url;
        } catch (Exception e) {
            log.error("[MinIO] 上传图片失败: {}", originalFilename, e);
            throw new RuntimeException("上传图片失败: " + originalFilename, e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build());
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());
                log.info("[MinIO] 创建 Bucket 成功: {}", minioProperties.getBucketName());
            }
        } catch (Exception e) {
            log.error("[MinIO] 检查或创建 Bucket 失败: {}", minioProperties.getBucketName(), e);
            throw new RuntimeException("检查或创建 Bucket 失败: " + minioProperties.getBucketName(), e);
        }
    }

    private boolean isImageExtension(String extension) {
        if (extension == null) return false;
        extension = extension.toLowerCase();
        for (String ext : IMAGE_EXTENSIONS) {
            if (ext.equals(extension)) return true;
        }
        return false;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return null;
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private String generateUniqueFileName(String extension) {
        return "images/" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }

    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("upload-", "." + getFileExtension(multipartFile.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
        }
        return tempFile;
    }
}
