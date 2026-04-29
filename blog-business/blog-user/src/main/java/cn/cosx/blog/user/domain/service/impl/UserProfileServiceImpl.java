package cn.cosx.blog.user.domain.service.impl;

import cn.cosx.blog.api.user.vo.UserProfileInfo;
import cn.cosx.blog.user.domain.entity.User;
import cn.cosx.blog.user.domain.entity.UserProfile;
import cn.cosx.blog.user.domain.service.UserProfileService;
import cn.cosx.blog.user.domain.service.UserService;
import cn.cosx.blog.user.infrastructure.mapper.UserProfileMapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements UserProfileService {

    @Resource
    private UserService userService;

    @Override
    public UserProfile getByUserId(Long userId) {
        return lambdaQuery().eq(UserProfile::getUserId, userId).one();
    }

    @Override
    public Boolean update(UserProfile userProfile) {
        UserProfile existing = getByUserId(userProfile.getUserId());
        if (existing == null) {
            return false;
        }
        userProfile.setId(existing.getId());
        return updateById(userProfile);
    }

    @Override
    public UserProfileInfo getUserProfileInfo(Long userId) {
        User user = userService.findById(String.valueOf(userId));
        if (user == null) {
            return null;
        }

        UserProfileInfo info = new UserProfileInfo();
        info.setUserId(userId);
        info.setNickName(user.getNickName());
        info.setProfilePhotoUrl(user.getProfilePhotoUrl());

        UserProfile profile = getByUserId(userId);
        if (profile != null) {
            info.setBio(profile.getBio());
            info.setSkills(JSON.parseArray(profile.getSkills(), String.class));
            info.setProjects(JSON.parseArray(profile.getProjects(), UserProfileInfo.ProjectInfo.class));
            info.setEducation(JSON.parseArray(profile.getEducation(), UserProfileInfo.EducationInfo.class));
            info.setSocialLinks(JSON.parseObject(profile.getSocialLinks(), UserProfileInfo.SocialLinks.class));
        }

        return info;
    }
}