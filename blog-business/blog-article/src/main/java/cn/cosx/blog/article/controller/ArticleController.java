package cn.cosx.blog.article.controller;

import cn.cosx.blog.api.article.vo.ArticleDetailInfo;
import cn.cosx.blog.api.article.vo.ArticleListInfo;
import cn.cosx.blog.api.article.vo.CommentInfo;
import cn.cosx.blog.article.domain.entity.Article;
import cn.cosx.blog.article.domain.service.ArticleService;
import cn.cosx.blog.article.domain.service.CommentService;
import cn.cosx.blog.base.result.Result;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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