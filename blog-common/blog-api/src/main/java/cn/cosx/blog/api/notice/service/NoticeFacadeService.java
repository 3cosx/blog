package cn.cosx.blog.api.notice.service;

import cn.cosx.blog.base.response.Response;

public interface NoticeFacadeService {
    Response<String> sendAndGetCaptcha(String email);
}
