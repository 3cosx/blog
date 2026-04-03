package cn.cosx.blog.product.converter;

import cn.cosx.blog.api.product.vo.ProductCategoryInfo;
import cn.cosx.blog.api.product.vo.ProductInfo;
import cn.cosx.blog.product.domain.entity.Product;
import cn.cosx.blog.product.domain.entity.ProductCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 商品转换器
 *
 * @author cosx
 */
@Mapper
public interface ProductConverter {

    ProductConverter INSTANCE = Mappers.getMapper(ProductConverter.class);

    @Mapping(source = "id", target = "id")
    ProductInfo product2ProductInfo(Product product);

    @Mapping(source = "id", target = "id")
    ProductCategoryInfo category2CategoryInfo(ProductCategory category);

    /**
     * 批量转换分类列表
     */
    List<ProductCategoryInfo> categoryList2CategoryInfoList(List<ProductCategory> categories);
}
