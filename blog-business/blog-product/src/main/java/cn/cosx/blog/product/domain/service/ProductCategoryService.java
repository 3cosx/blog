package cn.cosx.blog.product.domain.service;

import cn.cosx.blog.product.domain.entity.ProductCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品分类表 Service 接口
 *
 * @author cosx
 */
public interface ProductCategoryService extends IService<ProductCategory> {

    /**
     * 根据ID查询分类
     *
     * @param categoryId 分类ID
     * @return 分类实体
     */
    ProductCategory findById(String categoryId);

    /**
     * 查询子分类列表
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<ProductCategory> findByParentId(String parentId);

    /**
     * 查询所有分类
     *
     * @return 所有分类列表
     */
    List<ProductCategory> findAll();

    /**
     * 查询所有分类树结构
     *
     * @return 分类树结构（顶层分类包含子分类）
     */
    List<ProductCategory> findAllCategoryTree();
}
