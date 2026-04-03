package cn.cosx.blog.gateway.auth;

import cn.cosx.blog.api.user.enums.UserPermissionEnum;
import cn.cosx.blog.api.user.enums.UserRoleEnum;
import cn.cosx.blog.api.user.enums.UserStateEnum;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SaTokenConfiguration {


    @Bean
    public SaReactorFilter saReactorFilter() {
        return new SaReactorFilter()
                .addInclude("/**")
//                .addExclude("/")
                .setAuth(obj->{
                    SaRouter.match("/**").notMatch("/auth/**").check(r -> StpUtil.checkLogin());

                    SaRouter.match("/user/**",r-> StpUtil.checkPermissionOr(UserPermissionEnum.NORMAL.name(),UserPermissionEnum.FROZEN.name(),UserPermissionEnum.ADMIN.name()));
                })
                .setError(err -> getSaResult(err))
                ;
    }

    private SaResult getSaResult(Throwable throwable) {
        switch (throwable) {
            case NotLoginException notLoginException:
                log.error("请先登录");
                return SaResult.error("请先登录");
            case NotRoleException notRoleException:
                if (UserRoleEnum.ADMIN.name().equals(notRoleException.getRole())) {
                    log.error("请勿越权使用！");
                    return SaResult.error("请勿越权使用！");
                }
                log.error("您无权限进行此操作！");
                return SaResult.error("您无权限进行此操作！");
            case NotPermissionException notPermissionException:
                if (UserStateEnum.AUTH.name().equals(notPermissionException.getPermission())) {
                    log.error("请先完成实名认证！");
                    return SaResult.error("请先完成实名认证！");
                }
                log.error("您无权限进行此操作！");
                return SaResult.error("您无权限进行此操作！");
            default:
                return SaResult.error(throwable.getMessage());
        }
    }
}
