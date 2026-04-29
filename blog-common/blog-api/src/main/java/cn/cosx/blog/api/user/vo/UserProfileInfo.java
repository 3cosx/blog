package cn.cosx.blog.api.user.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String nickName;

    private String profilePhotoUrl;

    private String bio;

    private List<String> skills;

    private List<ProjectInfo> projects;

    private List<EducationInfo> education;

    private SocialLinks socialLinks;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ProjectInfo implements Serializable {
        private String name;
        private String description;
        private String url;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class EducationInfo implements Serializable {
        private String school;
        private String major;
        private String degree;
        private String period;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SocialLinks implements Serializable {
        private String github;
        private String weibo;
        private String email;
    }
}