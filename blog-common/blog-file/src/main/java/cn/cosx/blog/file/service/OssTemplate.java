package cn.cosx.blog.file.service;

/**
 * OSS 文件存储服务接口
 */
public interface OssTemplate {

    /**
     * 上传图片文件
     *
     * @param file 上传的文件
     * @return 上传后的URL
     */
    String uploadImage(org.springframework.web.multipart.MultipartFile file);
}