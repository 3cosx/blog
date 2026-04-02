package cn.cosx.blog.user.domain.service.impl;

import cn.cosx.blog.user.domain.entity.UserOperateStream;
import cn.cosx.blog.user.domain.service.UserOperateStreamService;
import cn.cosx.blog.user.infrastructure.mapper.UserOperateStreamMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 用户操作流水表 Service 实现类
 *
 * @author cosx
 */
@Service
public class UserOperateStreamServiceImpl extends ServiceImpl<UserOperateStreamMapper, UserOperateStream> implements UserOperateStreamService {

}
