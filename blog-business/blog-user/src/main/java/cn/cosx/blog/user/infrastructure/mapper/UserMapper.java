package cn.cosx.blog.user.infrastructure.mapper;

import cn.cosx.blog.user.domain.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户信息表 Mapper 接口
 *
 * @author cosx
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
