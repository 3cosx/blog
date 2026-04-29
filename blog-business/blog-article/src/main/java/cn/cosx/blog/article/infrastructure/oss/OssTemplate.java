package cn.cosx.blog.article.infrastructure.oss;

import org.springframework.web.multipart.MultipartFile;

/**
 * OSS上传模板接口
 */
public interface OssTemplate {

    /**
     * 上传图片文件
     *
     * @param file MultipartFile
     * @return 上传后的URL
     */
    String uploadImage(MultipartFile file);
}
