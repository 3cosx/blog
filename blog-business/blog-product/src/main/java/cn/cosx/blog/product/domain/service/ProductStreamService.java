package cn.cosx.blog.product.domain.service;

import cn.cosx.blog.api.product.enums.ProductStreamTypeEnum;
import cn.cosx.blog.product.domain.entity.Product;
import cn.cosx.blog.product.domain.entity.ProductStream;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商品流水表 Service 接口
 *
 * @author cosx
 */
public interface ProductStreamService extends IService<ProductStream> {

    /**
     * 插入商品流水
     *
     * @param product 商品实体
     * @param type 流水类型
     * @param quantity 变动数量
     * @return 流水ID
     */
    Long insertStream(Product product, ProductStreamTypeEnum type, Long quantity);
}
