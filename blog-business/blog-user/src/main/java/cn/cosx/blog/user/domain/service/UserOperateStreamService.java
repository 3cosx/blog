package cn.cosx.blog.user.domain.service;

import cn.cosx.blog.api.user.enums.UserOperateTypeEnum;
import cn.cosx.blog.user.domain.entity.User;
import cn.cosx.blog.user.domain.entity.UserOperateStream;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户操作流水表 Service 接口
 *
 * @author cosx
 */
public interface UserOperateStreamService extends IService<UserOperateStream> {

    String insertStream(User user, UserOperateTypeEnum type);
}
