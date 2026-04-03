package cn.cosx.blog.product.infrastructure.mapper;

import cn.cosx.blog.product.domain.entity.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品主表 Mapper 接口
 *
 * @author cosx
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 根据分类ID列表和游标分页查询商品
     *
     * @param categoryIds 分类ID列表
     * @param status 商品状态
     * @param lastId 上一页最后一条记录ID
     * @param pageSize 分页大小
     * @return 商品列表
     */
    List<Product> selectByCategoryIdsWithCursor(
            @Param("categoryIds") List<String> categoryIds,
            @Param("status") String status,
            @Param("lastId") Long lastId,
            @Param("pageSize") Integer pageSize);
}
