package cn.cosx.blog.article.controller;

import cn.cosx.blog.api.article.request.ArticleCreateRequest;
import cn.cosx.blog.api.article.request.ArticleListRequest;
import cn.cosx.blog.api.article.request.CommentCreateRequest;
import cn.cosx.blog.api.article.request.CommentListRequest;
import cn.cosx.blog.api.article.vo.ArticleDetailInfo;
import cn.cosx.blog.api.article.vo.ArticleListInfo;
import cn.cosx.blog.api.article.vo.CommentInfo;
import cn.cosx.blog.article.domain.entity.Article;
import cn.cosx.blog.article.domain.service.ArticleService;
import cn.cosx.blog.article.domain.service.CommentService;
import cn.cosx.blog.base.result.Result;
import cn.cosx.blog.file.service.OssTemplate;
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

    @Resource
    private OssTemplate ossTemplate;

    /**
     * 获取文章列表（公开）
     */
    @GetMapping("/list")
    public Result<List<ArticleListInfo>> list(ArticleListRequest request) {
        return Result.success(articleService.pageQuery(request.getLastId(), request.getPageSize()));
    }

    /**
     * 获取文章详情（公开）
     */
    @GetMapping("/{id}")
    public Result<ArticleDetailInfo> detail(@PathVariable Long id) {
        return Result.success(articleService.getDetail(id));
    }

    /**
     * 获取文章一级评论列表（分页）
     */
    @GetMapping("/{id}/comments")
    public Result<List<CommentInfo>> comments(@PathVariable Long id, CommentListRequest request) {
        return Result.success(commentService.getRootComments(id, request.getOffset(), request.getLimit()));
    }

    /**
     * 获取某楼所有评论（一次性查询，不递归）
     */
    @GetMapping("/{id}/comments/{topId}")
    public Result<List<CommentInfo>> buildingComments(@PathVariable Long id, @PathVariable Long topId) {
        return Result.success(commentService.getCommentsByTopId(topId));
    }

    /**
     * 发布评论或回复（需登录）
     */
    @PostMapping("/comment")
    public Result<Long> addComment(@RequestBody CommentCreateRequest request) {
        StpUtil.checkLogin();
        Long userId = Long.parseLong((String) StpUtil.getLoginId());

        // 一级评论
        if (request.getPid() == null || request.getPid() == 0) {
            return Result.success(commentService.addRootComment(request.getArticleId(), userId, request.getContent()).getId());
        }

        // 二级及以上评论
        return Result.success(commentService.addReply(request.getTopId(), request.getPid(), userId, request.getContent()).getId());
    }

    /**
     * 创建文章（需登录）
     */
    @PostMapping("/create")
    public Result<Long> create(@RequestBody ArticleCreateRequest request) {
        StpUtil.checkLogin();
        Long userId = Long.parseLong((String) StpUtil.getLoginId());

        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCoverImageUrl(request.getCoverImageUrl());
        article.setAuthorId(userId);
        article.setStatus(request.getStatus());

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
        String url = ossTemplate.uploadImage(file);
        return Result.success(url);
    }
}