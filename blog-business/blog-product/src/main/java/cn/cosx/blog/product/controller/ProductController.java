package cn.cosx.blog.product.controller;

import cn.cosx.blog.api.product.request.ProductPageQueryRequest;
import cn.cosx.blog.api.product.vo.ProductInfo;
import cn.cosx.blog.base.result.Result;
import cn.cosx.blog.product.domain.service.ProductService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 *
 * @author cosx
 */
@RestController
@RequestMapping("/product")
public class ProductController {


    @Resource
    private ProductService productService;

    /**
     * 分页查询商品列表
     */
    @PostMapping("/page/queryIntoList")
    public Result<List<ProductInfo>> pageQueryProductInfo(@RequestBody ProductPageQueryRequest request){
        List<ProductInfo> productInfoList = productService.pageQueryProductInfo(request);
        return Result.success(productInfoList);
    }

}
