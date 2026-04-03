package cn.cosx.blog.product.infrastructure.mapper;

import cn.cosx.blog.product.domain.entity.ProductStream;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品流水表 Mapper 接口
 *
 * @author cosx
 */
@Mapper
public interface ProductStreamMapper extends BaseMapper<ProductStream> {

}
