package cn.cosx.blog.mentor.agent.utils;

import com.alibaba.fastjson2.JSONObject;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@Slf4j
public class FileStorageService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    // 确保 bucket 存在
    private void createBucketIfNotExists(boolean publicRead) throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

            // 设置 bucket 策略为公共读
            if (publicRead) {
                String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]}]}";
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(policy)
                                .build()
                );
            }
        }
    }

    // 上传文件
    public String uploadFile(MultipartFile file, String objectName) throws Exception {
        log.info("[MinIO上传] 开始上传文件: objectName={}, size={}, contentType={}", 
                objectName, file.getSize(), file.getContentType());
        
        try {
            createBucketIfNotExists(true);// 这里可根据你自己的情况改成false，如果改成false，需要在这个方法最后调一次getPresignedUrl
            log.info("[MinIO上传] Bucket检查完成");
            
            log.info("[MinIO上传] 开始上传到MinIO...");
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            log.info("[MinIO上传] 文件上传成功");
            
            // 确保endpoint末尾没有斜杠，避免重复斜杠
            String cleanEndpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
            String fileUrl = String.format("%s/%s/%s", cleanEndpoint, bucketName, objectName);
            log.info("[MinIO上传] 生成文件URL: {}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("[MinIO上传] 文件上传失败: objectName={}", objectName, e);
            throw e;
        }
    }

    /**
     * 上传文件
     */
    public String uploadFile(String objectName, byte[] content, String contentType) throws Exception {
        createBucketIfNotExists(true);
        try (InputStream stream = new ByteArrayInputStream(content)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(stream, content.length, -1)
                            .contentType(contentType)
                            .build()
            );

            // 确保endpoint末尾没有斜杠，避免重复斜杠
        String cleanEndpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return String.format("%s/%s/%s", cleanEndpoint, bucketName, objectName);
        }
    }

    // 下载文件（返回 InputStream）
    public InputStream downloadFile(String objectName) throws Exception {
        GetObjectResponse response = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
        return response;
    }

    // 下载文件（返回字节数组）
    public byte[] downloadFileAsBytes(String objectName) throws Exception {
        try (InputStream is = downloadFile(objectName)) {
            return is.readAllBytes();
        }
    }

    // 删除文件
    public void deleteFile(String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }
}
