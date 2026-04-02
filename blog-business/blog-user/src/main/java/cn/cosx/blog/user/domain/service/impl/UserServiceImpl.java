package cn.cosx.blog.user.domain.service.impl;

import cn.cosx.blog.user.domain.entity.User;
import cn.cosx.blog.user.domain.service.UserService;
import cn.cosx.blog.user.infrastructure.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 用户信息表 Service 实现类
 *
 * @author cosx
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
