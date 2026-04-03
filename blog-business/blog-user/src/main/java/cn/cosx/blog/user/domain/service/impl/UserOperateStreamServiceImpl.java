package cn.cosx.blog.user.domain.service.impl;

import cn.cosx.blog.api.user.enums.UserOperateTypeEnum;
import cn.cosx.blog.user.domain.entity.User;
import cn.cosx.blog.user.domain.entity.UserOperateStream;
import cn.cosx.blog.user.domain.service.UserOperateStreamService;
import cn.cosx.blog.user.infrastructure.mapper.UserOperateStreamMapper;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 用户操作流水表 Service 实现类
 *
 * @author cosx
 */
@Service
public class UserOperateStreamServiceImpl extends ServiceImpl<UserOperateStreamMapper, UserOperateStream> implements UserOperateStreamService {


    public String insertStream(User user, UserOperateTypeEnum type) {
        UserOperateStream stream = new UserOperateStream();
        stream.setUserId(String.valueOf(user.getId()));
        stream.setOperateTime(new Date());
        stream.setType(type.name());
        stream.setParam(JSON.toJSONString(user));
        boolean result = save(stream);
        if (result) {
            return stream.getId();
        }
        return null;
    }
}
