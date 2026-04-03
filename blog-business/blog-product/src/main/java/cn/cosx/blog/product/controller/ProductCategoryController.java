package cn.cosx.blog.product.controller;

import cn.cosx.blog.api.product.vo.ProductCategoryInfo;
import cn.cosx.blog.base.response.Response;
import cn.cosx.blog.product.converter.ProductConverter;
import cn.cosx.blog.product.domain.entity.ProductCategory;
import cn.cosx.blog.product.domain.service.ProductCategoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品分类控制器
 *
 * @author cosx
 */
@RestController
@RequestMapping("/product/category")
public class ProductCategoryController {

    @Resource
    private ProductCategoryService productCategoryService;

    /**
     * 查询所有分类树结构
     *
     * @return 分类树结构（顶层分类包含子分类）
     */
    @GetMapping("/tree")
    public Response<List<ProductCategoryInfo>> getCategoryTree() {
        List<ProductCategory> categoryTree = productCategoryService.findAllCategoryTree();
        List<ProductCategoryInfo> categoryInfoList = ProductConverter.INSTANCE.categoryList2CategoryInfoList(categoryTree);
        return Response.of(categoryInfoList);
    }
}
