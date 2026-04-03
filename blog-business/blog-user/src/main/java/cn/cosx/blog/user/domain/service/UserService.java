package cn.cosx.blog.user.domain.service;

import cn.cosx.blog.api.user.vo.UserInfo;
import cn.cosx.blog.base.response.Response;
import cn.cosx.blog.user.domain.entity.User;
import cn.cosx.blog.user.param.UserAuthParam;
import cn.cosx.blog.user.param.UserModifyParam;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户信息表 Service 接口
 *
 * @author cosx
 */
public interface UserService extends IService<User> {

     Response<UserInfo> register(String email);

    User findById(String userId);

    Boolean modify(UserModifyParam param);

    /**
     * 用户实名认证
     *
     * @param userId 用户ID
     * @param param 认证参数
     * @return 认证结果
     */
    Boolean auth(String userId, UserAuthParam param);
}
