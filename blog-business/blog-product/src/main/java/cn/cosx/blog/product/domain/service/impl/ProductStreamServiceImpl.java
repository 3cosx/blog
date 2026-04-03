package cn.cosx.blog.product.domain.service.impl;

import cn.cosx.blog.api.product.enums.ProductStreamTypeEnum;
import cn.cosx.blog.product.domain.entity.Product;
import cn.cosx.blog.product.domain.entity.ProductStream;
import cn.cosx.blog.product.domain.service.ProductStreamService;
import cn.cosx.blog.product.infrastructure.mapper.ProductStreamMapper;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 商品流水表 Service 实现类
 *
 * @author cosx
 */
@Service
public class ProductStreamServiceImpl extends ServiceImpl<ProductStreamMapper, ProductStream> implements ProductStreamService {

    @Resource
    private ProductStreamMapper productStreamMapper;

    @Override
    public String insertStream(Product product, ProductStreamTypeEnum type, Long quantity) {
        ProductStream stream = null;
        Long saleableStock = product.getSaleableStock() != null ? product.getSaleableStock() : 0L;

        switch (type) {
            case IN:
                stream = ProductStream.createInStream(product.getId(), quantity, saleableStock - quantity, product.getIdentifier());
                break;
            case OUT:
                stream = ProductStream.createOutStream(product.getId(), quantity, saleableStock + quantity, product.getIdentifier());
                break;
            case LOCK:
                stream = ProductStream.createLockStream(product.getId(), quantity, saleableStock + quantity, product.getIdentifier());
                break;
            default:
                break;
        }

        if (stream != null) {
            boolean flag = this.save(stream);
            Assert.isTrue(flag, "流水记录失败");
            return stream.getId();
        }
        return null;
    }
}
