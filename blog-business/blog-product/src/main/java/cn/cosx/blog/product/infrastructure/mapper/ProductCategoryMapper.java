package cn.cosx.blog.product.infrastructure.mapper;

import cn.cosx.blog.product.domain.entity.ProductCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类表 Mapper 接口
 *
 * @author cosx
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {

}
