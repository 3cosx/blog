package cn.cosx.blog.knowledge.document.file;

import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * MinIO客户端封装
 */
@Slf4j
@Component
public class MinioUtils {


    @Value("${minio.bucketName:know-engine}")
    private String bucketName;

    @Resource
    private MinioClient minioClient;

    @Value("${minio.endpoint:http://112.126.84.219:9000}")
    private String endpoint;

    /**
     * 确保桶存在，不存在则创建
     */
    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("[MinIO] 创建桶成功: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("[MinIO] 检查或创建桶失败: {}", bucketName, e);
            throw new RuntimeException("检查或创建桶失败: " + bucketName, e);
        }
    }

    /**
     * 上传文件
     *
     * @param filePath 文件路径
     * @param objectName 对象名称（包含路径）
     * @return 访问URL
     */
    public String uploadFile(Path filePath, String objectName) {
        try {
            ensureBucketExists();
            File file = filePath.toFile();
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .filename(file.getAbsolutePath())
                    .build();
            minioClient.uploadObject(uploadObjectArgs);

            return String.format("%s/%s/%s", endpoint, bucketName, objectName);
        } catch (Exception e) {
            log.error("[MinIO] 上传文件失败: {}", objectName, e);
            throw new RuntimeException("上传文件失败: " + objectName, e);
        }
    }

    /**
     * 上传MultipartFile
     *
     * @param file       MultipartFile
     * @param objectName 对象名称
     * @return 访问URL
     */
    public String uploadFile(MultipartFile file, String objectName) {
        try {
            ensureBucketExists();
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();
            minioClient.putObject(putObjectArgs);

            return String.format("%s/%s/%s", endpoint, bucketName, objectName);

        } catch (Exception e) {
            log.error("[MinIO] 上传文件失败: {}", objectName, e);
            throw new RuntimeException("上传文件失败: " + objectName, e);
        }
    }

    /**
     * 上传InputStream
     *
     * @param inputStream 输入流
     * @param objectName  对象名称
     * @param contentType 内容类型
     * @param size        文件大小
     * @return 访问URL
     */
    public String uploadFile(InputStream inputStream, String objectName, String contentType, long size) {
        try {
            ensureBucketExists();
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build();
            minioClient.putObject(putObjectArgs);

            return String.format("%s/%s/%s", endpoint, bucketName, objectName);
        } catch (Exception e) {
            log.error("[MinIO] 上传文件失败: {}", objectName, e);
            throw new RuntimeException("上传文件失败: " + objectName, e);
        }
    }

    /**
     * 获取预签名URL
     *
     * @param objectName 对象名称
     * @param expiry     过期时间
     * @param unit       时间单位
     * @return 预签名URL
     */
    public String getPresignedObjectUrl(String objectName, long expiry, TimeUnit unit) {
        try {
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .method(Method.GET)
                    .expiry((int) unit.toSeconds(expiry))
                    .build();
            return minioClient.getPresignedObjectUrl(args);
        } catch (Exception e) {
            log.error("[MinIO] 获取预签名URL失败: {}", objectName, e);
            throw new RuntimeException("获取预签名URL失败: " + objectName, e);
        }
    }

    /**
     * 下载文件
     *
     * @param objectName 对象名称
     * @param outputPath 输出路径
     */
    public void downloadFile(String objectName, Path outputPath) {
        try {
            StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();
            minioClient.statObject(statObjectArgs);

            DownloadObjectArgs downloadObjectArgs = DownloadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .filename(outputPath.toString())
                    .build();
            minioClient.downloadObject(downloadObjectArgs);
            log.info("[MinIO] 下载文件成功: {} -> {}", objectName, outputPath);
        } catch (Exception e) {
            log.error("[MinIO] 下载文件失败: {}", objectName, e);
            throw new RuntimeException("下载文件失败: " + objectName, e);
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param objectName 对象名称
     * @return 是否存在
     */
    public boolean exists(String objectName) {
        try {
            StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();
            minioClient.statObject(statObjectArgs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) {
        try {
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();
            minioClient.removeObject(removeObjectArgs);
            log.info("[MinIO] 删除文件成功: {}", objectName);
        } catch (Exception e) {
            log.error("[MinIO] 删除文件失败: {}", objectName, e);
            throw new RuntimeException("删除文件失败: " + objectName, e);
        }
    }

    /**
     * 下载文件并返回字节数组
     *
     * @param objectName 对象名称
     * @return 文件字节数组
     */
    public byte[] downloadFileAsBytes(String objectName) {
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();
            try (InputStream inputStream = minioClient.getObject(getObjectArgs)) {
                return inputStream.readAllBytes();
            }
        } catch (Exception e) {
            log.error("[MinIO] 下载文件失败: {}", objectName, e);
            throw new RuntimeException("下载文件失败: " + objectName, e);
        }
    }
}
