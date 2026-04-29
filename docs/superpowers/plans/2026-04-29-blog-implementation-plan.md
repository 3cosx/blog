# 博客系统实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在博客系统中实现用户扩展信息管理和文章发布功能

**Architecture:** 复用现有 blog-user 模块添加 user_profile 相关功能，新建 blog-article 模块处理文章、评论、点赞、收藏。采用 DDD 架构风格，遵循现有项目分层模式。

**Tech Stack:** Spring Boot 3.5.6, MyBatis-Plus, Sa-Token, MySQL

---

## 第一阶段：用户扩展信息（blog-user 扩展）

### Task 1: 创建 UserProfile 实体类和 Mapper

**Files:**
- Create: `blog-business/blog-user/src/main/java/cn/cosx/blog/user/domain/entity/UserProfile.java`
- Create: `blog-business/blog-user/src/main/java/cn/cosx/blog/user/infrastructure/mapper/UserProfileMapper.java`
- Create: `blog-common/blog-api/src/main/java/cn/cosx/blog/api/user/vo/UserProfileInfo.java`

**Steps:**

- [ ] **Step 1: 创建 UserProfile 实体类**

```java
package cn.cosx.blog.user.domain.entity;

import cn.cosx.blog.database.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户扩展信息表实体类
 */
@Setter
@Getter
@TableName("user_profile")
public class UserProfile extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 个人简介
     */
    @TableField("bio")
    private String bio;

    /**
     * 技能标签（JSON数组）
     */
    @TableField("skills")
    private String skills;

    /**
     * 项目经历（JSON数组）
     */
    @TableField("projects")
    private String projects;

    /**
     * 教育背景（JSON数组）
     */
    @TableField("education")
    private String education;

    /**
     * 社交链接（JSON）
     */
    @TableField("social_links")
    private String socialLinks;
}
```

- [ ] **Step 2: 创建 UserProfileMapper**

```java
package cn.cosx.blog.user.infrastructure.mapper;

import cn.cosx.blog.user.domain.entity.UserProfile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}
```

- [ ] **Step 3: 创建 UserProfileInfo VO**

```java
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
```

- [ ] **Step 4: 提交代码**
```bash
git add blog-business/blog-user/src/main/java/cn/cosx/blog/user/domain/entity/UserProfile.java blog-business/blog-user/src/main/java/cn/cosx/blog/user/infrastructure/mapper/UserProfileMapper.java blog-common/blog-api/src/main/java/cn/cosx/blog/api/user/vo/UserProfileInfo.java
git commit -m "feat(user): add UserProfile entity and mapper"
```

---

### Task 2: 创建 UserProfileService 和实现

**Files:**
- Create: `blog-business/blog-user/src/main/java/cn/cosx/blog/user/domain/service/UserProfileService.java`
- Create: `blog-business/blog-user/src/main/java/cn/cosx/blog/user/domain/service/impl/UserProfileServiceImpl.java`

**Steps:**

- [ ] **Step 1: 创建 UserProfileService 接口**

```java
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
```

- [ ] **Step 2: 创建 UserProfileServiceImpl**

```java
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
```

- [ ] **Step 3: 提交代码**
```bash
git add blog-business/blog-user/src/main/java/cn/cosx/blog/user/domain/service/UserProfileService.java blog-business/blog-user/src/main/java/cn/cosx/blog/user/domain/service/impl/UserProfileServiceImpl.java
git commit -m "feat(user): add UserProfileService"
```

---

### Task 3: 添加用户扩展信息 API

**Files:**
- Modify: `blog-business/blog-user/src/main/java/cn/cosx/blog/user/controller/UserController.java`

**Steps:**

- [ ] **Step 1: 添加更新扩展信息和查询接口到 UserController**

```java
@PostMapping("/getUserProfile")
public Result<UserProfileInfo> getUserProfile(@RequestParam Long userId) {
    UserProfileInfo info = userProfileService.getUserProfileInfo(userId);
    return Result.success(info);
}

@PostMapping("/updateUserProfile")
public Result<Boolean> updateUserProfile(@RequestBody UserProfile param) {
    String userId = (String) StpUtil.getLoginId();
    param.setUserId(Long.parseLong(userId));
    Boolean result = userProfileService.update(param);
    return Result.success(result);
}
```

同时在 UserController 中添加：
```java
@Resource
private UserProfileService userProfileService;
```

- [ ] **Step 2: 提交代码**
```bash
git add blog-business/blog-user/src/main/java/cn/cosx/blog/user/controller/UserController.java
git commit -m "feat(user): add user profile API endpoints"
```

---

## 第二阶段：文章模块（新建 blog-article）

### Task 4: 创建 blog-article 模块

**Files:**
- Create: `blog-business/blog-article/pom.xml`
- Create: `blog-business/blog-article/src/main/java/cn/cosx/blog/article/BlogArticleApplication.java`

**Steps:**

- [ ] **Step 1: 创建模块目录结构和 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>cn.cosx</groupId>
        <artifactId>blog-business</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>blog-article</artifactId>

    <properties>
        <application.name>blog-article</application.name>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>cn.cosx</groupId>
            <artifactId>blog-base</artifactId>
        </dependency>

        <dependency>
            <groupId>cn.cosx</groupId>
            <artifactId>blog-api</artifactId>
        </dependency>

        <dependency>
            <groupId>cn.cosx</groupId>
            <artifactId>blog-database</artifactId>
        </dependency>

        <dependency>
            <groupId>cn.cosx</groupId>
            <artifactId>blog-rpc</artifactId>
        </dependency>

        <dependency>
            <groupId>cn.cosx</groupId>
            <artifactId>blog-cache</artifactId>
        </dependency>

        <dependency>
            <groupId>cn.cosx</groupId>
            <artifactId>blog-lock</artifactId>
        </dependency>

        <dependency>
            <groupId>cn.cosx</groupId>
            <artifactId>blog-web</artifactId>
        </dependency>

        <dependency>
            <groupId>cn.cosx</groupId>
            <artifactId>blog-sa-token</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <skip>true</skip>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 在 blog-business/pom.xml 中添加模块**

在 `<modules>` 中添加：
```xml
<module>blog-article</module>
```

- [ ] **Step 3: 创建 Spring Boot 启动类**

```java
package cn.cosx.blog.article;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "cn.cosx.blog")
public class BlogArticleApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogArticleApplication.class, args);
    }
}
```

- [ ] **Step 4: 提交代码**
```bash
git add blog-business/blog-article
git add blog-business/pom.xml
git commit -m "feat(article): create blog-article module"
```

---

### Task 5: 创建文章相关实体类和 Mapper

**Files:**
- Create: `blog-business/blog-article/src/main/java/cn/cosx/blog/article/domain/entity/Article.java`
- Create: `blog-business/blog-article/src/main/java/cn/cosx/blog/article/domain/entity/ArticleImage.java`
- Create: `blog-business/blog-article/src/main/java/cn/cosx/blog/article/domain/entity/Comment.java`
- Create: `blog-business/blog-article/src/main/java/cn/cosx/blog/article/domain/entity/ArticleLike.java`
- Create: `blog-business/blog-article/src/main/java/cn/cosx/blog/article/domain/entity/ArticleCollect.java`
- Create: `blog-business/blog-article/src/main/java/cn/cosx/blog/article/infrastructure/mapper/ArticleMapper.java`
- Create: `blog-business/blog-article/src/main/java/cn/cosx/blog/article/infrastructure/mapper/ArticleImageMapper.java`
- Create: `blog-business/blog-article/src/main/java/cn/cosx/blog/article/infrastructure/mapper/CommentMapper.java`
- Create: `blog-business/blog-article/src/main/java/cn/cosx/blog/article/infrastructure/mapper/ArticleLikeMapper.java`
- Create: `blog-business/blog-article/src/main/java/cn/cosx/blog/article/infrastructure/mapper/ArticleCollectMapper.java`

**Steps:**

- [ ] **Step 1: 创建 Article 实体**

```java
package cn.cosx.blog.article.domain.entity;

import cn.cosx.blog.database.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 文章表实体类
 */
@Setter
@Getter
@TableName("article")
public class Article extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 作者ID
     */
    @TableField("author_id")
    private Long authorId;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 内容
     */
    @TableField("content")
    private String content;

    /**
     * 封面图URL
     */
    @TableField("cover_image_url")
    private String coverImageUrl;

    /**
     * 浏览量
     */
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 评论数
     */
    @TableField("comment_count")
    private Integer commentCount;

    /**
     * 状态：0=草稿，1=已发布
     */
    @TableField("status")
    private Integer status;
}
```

- [ ] **Step 2: 创建 ArticleImage 实体**

```java
package cn.cosx.blog.article.domain.entity;

import cn.cosx.blog.database.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 文章图片关联表实体类
 */
@Setter
@Getter
@TableName("article_image")
public class ArticleImage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    @TableField("article_id")
    private Long articleId;

    /**
     * 图片URL
     */
    @TableField("image_url")
    private String imageUrl;
}
```

- [ ] **Step 3: 创建 Comment 实体**

```java
package cn.cosx.blog.article.domain.entity;

import cn.cosx.blog.database.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 评论表实体类
 */
@Setter
@Getter
@TableName("comment")
public class Comment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    @TableField("article_id")
    private Long articleId;

    /**
     * 评论者ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 父评论ID
     */
    @TableField("parent_id")
    private Long parentId;
}
```

- [ ] **Step 4: 创建 ArticleLike 实体**

```java
package cn.cosx.blog.article.domain.entity;

import cn.cosx.blog.database.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 文章点赞表实体类
 */
@Setter
@Getter
@TableName("article_like")
public class ArticleLike extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    @TableField("article_id")
    private Long articleId;

    /**
     * 点赞用户ID
     */
    @TableField("user_id")
    private Long userId;
}
```

- [ ] **Step 5: 创建 ArticleCollect 实体**

```java
package cn.cosx.blog.article.domain.entity;

import cn.cosx.blog.database.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 文章收藏表实体类
 */
@Setter
@Getter
@TableName("article_collect")
public class ArticleCollect extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    @TableField("article_id")
    private Long articleId;

    /**
     * 收藏用户ID
     */
    @TableField("user_id")
    private Long userId;
}
```

- [ ] **Step 6: 创建所有 Mapper 接口**（继承 BaseMapper）

```java
// ArticleMapper.java
package cn.cosx.blog.article.infrastructure.mapper;

import cn.cosx.blog.article.domain.entity.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}

// 类似创建 ArticleImageMapper, CommentMapper, ArticleLikeMapper, ArticleCollectMapper
```

- [ ] **Step 7: 提交代码**
```bash
git add blog-business/blog-article/src/main/java/cn/cosx/blog/article/domain/entity/ blog-business/blog-article/src/main/java/cn/cosx/blog/article/infrastructure/mapper/
git commit -m "feat(article): add article related entities and mappers"
```

---

### Task 6: 创建 ArticleService 和实现

**Files:**
- Create: `blog-business/blog-article/src/main/java/cn/cosx/blog/article/domain/service/ArticleService.java`
- Create: `blog-business/blog-article/src/main/java/cn/cosx/blog/article/domain/service/impl/ArticleServiceImpl.java`

**Steps:**

- [ ] **Step 1: 创建 ArticleService 接口**

```java
package cn.cosx.blog.article.domain.service;

import cn.cosx.blog.api.article.vo.ArticleDetailInfo;
import cn.cosx.blog.api.article.vo.ArticleListInfo;
import cn.cosx.blog.article.domain.entity.Article;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ArticleService extends IService<Article> {

    /**
     * 分页查询文章列表
     */
    Page<ArticleListInfo> pageQuery(Integer pageNum, Integer pageSize);

    /**
     * 获取文章详情
     */
    ArticleDetailInfo getDetail(Long articleId);

    /**
     * 创建文章
     */
    Article createArticle(Article article);

    /**
     * 更新文章
     */
    Boolean updateArticle(Article article);

    /**
     * 删除文章
     */
    Boolean deleteArticle(Long articleId);

    /**
     * 点赞文章
     */
    Boolean likeArticle(Long articleId, Long userId);

    /**
     * 收藏文章
     */
    Boolean collectArticle(Long articleId, Long userId);

    /**
     * 获取用户收藏的文章列表
     */
    List<ArticleListInfo> getCollectedArticles(Long userId);
}
```

- [ ] **Step 2: 创建 ArticleServiceImpl**

实现包括：
- 分页查询已发布的文章列表
- 获取文章详情（增加浏览量）
- 点赞功能（去重，更新文章 like_count）
- 收藏功能（去重）

- [ ] **Step 3: 提交代码**
```bash
git add blog-business/blog-article/src/main/java/cn/cosx/blog/article/domain/service/ArticleService.java blog-business/blog-article/src/main/java/cn/cosx/blog/article/domain/service/impl/ArticleServiceImpl.java
git commit -m "feat(article): add ArticleService"
```

---

### Task 7: 创建评论服务

**Files:**
- Create: `blog-business/blog-article/src/main/java/cn/cosx/blog/article/domain/service/CommentService.java`
- Create: `blog-business/blog-article/src/main/java/cn/cosx/blog/article/domain/service/impl/CommentServiceImpl.java`

**Steps:**

- [ ] **Step 1: 创建 CommentService 接口**

```java
package cn.cosx.blog.article.domain.service;

import cn.cosx.blog.api.article.vo.CommentInfo;
import cn.cosx.blog.article.domain.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CommentService extends IService<Comment> {

    /**
     * 获取文章评论列表
     */
    List<CommentInfo> getComments(Long articleId);

    /**
     * 添加评论
     */
    Comment addComment(Long articleId, Long userId, String content, Long parentId);
}
```

- [ ] **Step 2: 创建 CommentServiceImpl**

- [ ] **Step 3: 提交代码**
```bash
git add blog-business/blog-article/src/main/java/cn/cosx/blog/article/domain/service/CommentService.java blog-business/blog-article/src/main/java/cn/cosx/blog/article/domain/service/impl/CommentServiceImpl.java
git commit -m "feat(article): add CommentService"
```

---

### Task 8: 创建 ArticleController

**Files:**
- Create: `blog-business/blog-article/src/main/java/cn/cosx/blog/article/controller/ArticleController.java`

**Steps:**

- [ ] **Step 1: 创建 ArticleController**

```java
package cn.cosx.blog.article.controller;

import cn.cosx.blog.api.article.vo.ArticleDetailInfo;
import cn.cosx.blog.api.article.vo.ArticleListInfo;
import cn.cosx.blog.api.article.vo.CommentInfo;
import cn.cosx.blog.article.domain.entity.Article;
import cn.cosx.blog.article.domain.service.ArticleService;
import cn.cosx.blog.article.domain.service.CommentService;
import cn.cosx.blog.base.result.Result;
import cn.cosx.blog.entity.Comment;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Resource
    private ArticleService articleService;

    @Resource
    private CommentService commentService;

    /**
     * 获取文章列表（公开）
     */
    @GetMapping("/list")
    public Result<Page<ArticleListInfo>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(articleService.pageQuery(pageNum, pageSize));
    }

    /**
     * 获取文章详情（公开）
     */
    @GetMapping("/{id}")
    public Result<ArticleDetailInfo> detail(@PathVariable Long id) {
        return Result.success(articleService.getDetail(id));
    }

    /**
     * 获取文章评论（公开）
     */
    @GetMapping("/{id}/comments")
    public Result<List<CommentInfo>> comments(@PathVariable Long id) {
        return Result.success(commentService.getComments(id));
    }

    /**
     * 创建文章（需登录）
     */
    @PostMapping("/create")
    public Result<Long> create(@RequestBody Article article) {
        StpUtil.checkLogin();
        Long userId = Long.parseLong((String) StpUtil.getLoginId());
        article.setAuthorId(userId);
        Article created = articleService.createArticle(article);
        return Result.success(created.getId());
    }

    /**
     * 更新文章（需登录）
     */
    @PostMapping("/{id}/update")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody Article article) {
        StpUtil.checkLogin();
        article.setId(id);
        return Result.success(articleService.updateArticle(article));
    }

    /**
     * 删除文章（需登录）
     */
    @PostMapping("/{id}/delete")
    public Result<Boolean> delete(@PathVariable Long id) {
        StpUtil.checkLogin();
        return Result.success(articleService.deleteArticle(id));
    }

    /**
     * 点赞文章（需登录）
     */
    @PostMapping("/{id}/like")
    public Result<Boolean> like(@PathVariable Long id) {
        StpUtil.checkLogin();
        Long userId = Long.parseLong((String) StpUtil.getLoginId());
        return Result.success(articleService.likeArticle(id, userId));
    }

    /**
     * 收藏文章（需登录）
     */
    @PostMapping("/{id}/collect")
    public Result<Boolean> collect(@PathVariable Long id) {
        StpUtil.checkLogin();
        Long userId = Long.parseLong((String) StpUtil.getLoginId());
        return Result.success(articleService.collectArticle(id, userId));
    }

    /**
     * 上传文章图片（需登录）
     */
    @PostMapping("/image/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        StpUtil.checkLogin();
        // TODO: 实现图片上传到云存储，返回URL
        return Result.success("https://your-oss-url.com/" + file.getOriginalFilename());
    }
}
```

- [ ] **Step 2: 提交代码**
```bash
git add blog-business/blog-article/src/main/java/cn/cosx/blog/article/controller/ArticleController.java
git commit -m "feat(article): add ArticleController with all endpoints"
```

---

### Task 9: 创建 API VO 类

**Files:**
- Create: `blog-common/blog-api/src/main/java/cn/cosx/blog/api/article/vo/ArticleListInfo.java`
- Create: `blog-common/blog-api/src/main/java/cn/cosx/blog/api/article/vo/ArticleDetailInfo.java`
- Create: `blog-common/blog-api/src/main/java/cn/cosx/blog/api/article/vo/CommentInfo.java`

**Steps:**

- [ ] **Step 1: 创建 ArticleListInfo**

```java
package cn.cosx.blog.api.article.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class ArticleListInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String coverImageUrl;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private String authorNickName;
    private String authorAvatar;
    private Date createTime;
}
```

- [ ] **Step 2: 创建 ArticleDetailInfo**

```java
package cn.cosx.blog.api.article.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ArticleDetailInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String content;
    private String coverImageUrl;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Long authorId;
    private String authorNickName;
    private String authorAvatar;
    private Date createTime;
    private Date updateTime;
    private List<String> imageUrls;
}
```

- [ ] **Step 3: 创建 CommentInfo**

```java
package cn.cosx.blog.api.article.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CommentInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long articleId;
    private Long userId;
    private String userNickName;
    private String userAvatar;
    private String content;
    private Long parentId;
    private Date createTime;
    private List<CommentInfo> replies;
}
```

- [ ] **Step 4: 提交代码**
```bash
git add blog-common/blog-api/src/main/java/cn/cosx/blog/api/article/
git commit -m "feat(api): add article related VO classes"
```

---

### Task 10: 完善图片上传功能

**Files:**
- Create: `blog-business/blog-article/src/main/java/cn/cosx/blog/article/infrastructure/oss/OssTemplate.java`

**Steps:**

- [ ] **Step 1: 实现 OSS 上传工具类**

参考项目中的 OSS 配置，实现图片上传到云存储的功能

- [ ] **Step 2: 提交代码**
```bash
git add blog-business/blog-article/src/main/java/cn/cosx/blog/article/infrastructure/oss/OssTemplate.java
git commit -m "feat(article): add OSS upload template"
```

---

## 自检清单

完成所有任务后，确认以下内容：

1. **Spec coverage:** 逐一检查设计文档中的每个功能点是否都有对应的实现
2. **Placeholder scan:** 检查代码中是否有 "TODO"、"TBD" 等占位符
3. **Type consistency:** 检查所有 VO、Entity、Service 方法签名是否一致
4. **API 测试:** 测试所有公开接口和需登录接口
5. **数据库验证:** 确认所有表结构正确创建

---

Plan complete and saved to `docs/superpowers/plans/2026-04-29-blog-implementation-plan.md`. Two execution options:

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

Which approach?