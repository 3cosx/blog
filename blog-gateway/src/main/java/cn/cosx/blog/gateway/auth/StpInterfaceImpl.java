package cn.cosx.blog.gateway.auth;

import cn.cosx.blog.api.user.enums.UserPermissionEnum;
import cn.cosx.blog.api.user.enums.UserRoleEnum;
import cn.cosx.blog.api.user.enums.UserStateEnum;
import cn.cosx.blog.api.user.vo.UserInfo;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        UserInfo userInfo = (UserInfo)StpUtil.getSessionByLoginId(loginId).get((String) loginId);
        if(userInfo.getState().equals(UserStateEnum.INIT.getCode()) || userInfo.getState().equals(UserStateEnum.AUTH.getCode())) {
            List.of(UserPermissionEnum.NORMAL.name());
        }

        if(userInfo.getState().equals(UserStateEnum.FROZEN.getCode())){
            return List.of(UserPermissionEnum.FROZEN.name());
        }
        if(userInfo.getUserRole().equals(UserRoleEnum.ADMIN.getCode())){
            return List.of(UserPermissionEnum.ADMIN.name(),UserPermissionEnum.ADMIN.name());
        }
        return List.of(UserPermissionEnum.NONE.name());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        UserInfo userInfo = (UserInfo)StpUtil.getSessionByLoginId(loginId).get((String) loginId);
        if(userInfo.getUserRole().equals(UserRoleEnum.ADMIN.getCode())){
            return List.of(UserRoleEnum.ADMIN.getCode());
        }
        return List.of(UserRoleEnum.NORMAL.getCode());
    }
}
