package cn.cosx.blog.user.domain.service;

import cn.cosx.blog.api.user.vo.UserProfileInfo;
import cn.cosx.blog.user.domain.entity.UserProfile;

public interface UserProfileService {

    /**
     * 获取用户扩展信息
     */
    UserProfile getByUserId(Long userId);

    /**
     * 更新用户扩展信息
     */
    Boolean update(UserProfile userProfile);

    /**
     * 获取用户完整信息（基础+扩展）
     */
    UserProfileInfo getUserProfileInfo(Long userId);
}