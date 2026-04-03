package cn.cosx.blog.user.domain.service.impl;

import cn.cosx.blog.api.user.enums.UserOperateTypeEnum;
import cn.cosx.blog.api.user.vo.UserInfo;
import cn.cosx.blog.base.exception.BizException;
import cn.cosx.blog.base.exception.RepoErrorCode;
import cn.cosx.blog.base.response.Response;
import cn.cosx.blog.lock.DistributeLock;
import cn.cosx.blog.user.constant.UserConstants;
import cn.cosx.blog.user.converter.UserConverter;
import cn.cosx.blog.user.domain.entity.User;
import cn.cosx.blog.user.domain.entity.UserOperateStream;
import cn.cosx.blog.user.domain.service.UserOperateStreamService;
import cn.cosx.blog.user.domain.service.UserService;

import cn.cosx.blog.user.infrastructure.exception.UserErrorCodeEnum;
import cn.cosx.blog.user.infrastructure.exception.UserException;
import cn.cosx.blog.user.infrastructure.mapper.UserMapper;
import cn.cosx.blog.user.param.UserAuthParam;
import cn.cosx.blog.user.param.UserModifyParam;
import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.support.json.JSONUtils;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheRefresh;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 用户信息表 Service 实现类
 *
 * @author cosx
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserOperateStreamService userOperateStreamService;

    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLock(keyExpression = "#email", scene = "USER_REGISTER")
    public Response<UserInfo> register(String email) {
        //执行注册
        String nickName = generateNickName(email);

        User user = new User();
        user.register(email, nickName);
        boolean flag = this.save(user);
        Assert.isTrue(flag,"注册失败请重新尝试");

        // 记录用户操作流水
        String stream = userOperateStreamService.insertStream(user, UserOperateTypeEnum.REGISTER);
        Assert.notNull(stream, () -> new BizException(RepoErrorCode.UPDATE_FAILED));

        // 转换为UserInfo返回
        UserInfo userInfo = UserConverter.INSTANCE.user2UserInfo(user);
        return Response.of(userInfo);
    }

    @Override
    @Cached(name = ":user:cache:id", cacheType = CacheType.BOTH ,key = "#userId", cacheNullValue = true)
    @CacheRefresh(refresh = 60,timeUnit = TimeUnit.MINUTES)
    public User findById(String userId) {
        return userMapper.selectById(userId);
    }

    @Override
    @CacheInvalidate(name = ":user:cache:id", key = "param.userId")
    public Boolean modify(UserModifyParam param) {
        UserOperateStream userOperateStream = new UserOperateStream();
        User user = userMapper.selectById(param.getUserId());
        Assert.notNull(user, () -> new UserException(UserErrorCodeEnum.USER_NOT_EXIST));

        if (StringUtils.isNotBlank(param.getNickName())) {
            user.setNickName(param.getNickName());
        }

        if (StringUtils.isNotBlank(param.getAvatar())) {
            user.setProfilePhotoUrl(param.getAvatar());
        }

        if (StringUtils.isNotBlank(param.getEmail())) {
            user.setEmail(param.getEmail());
        }

        if (StringUtils.isNotBlank(param.getPhone())) {
            user.setTelephone(param.getPhone());
        }
        if (!updateById(user)) {
            throw new UserException(UserErrorCodeEnum.UPDATE_FAILED);
        }
        userOperateStream.setUserId(param.getUserId());
        userOperateStream.setOperateTime(new Date());
        userOperateStream.setType(UserOperateTypeEnum.UPDATE.name());
        userOperateStream.setParam(JSONUtils.toJSONString(param));

        userOperateStreamService.insertStream(user,UserOperateTypeEnum.UPDATE);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheInvalidate(name = ":user:cache:id", key = "#userId")
    public Boolean auth(String userId, UserAuthParam param) {
        User user = userMapper.selectById(userId);
        Assert.notNull(user, () -> new UserException(UserErrorCodeEnum.USER_NOT_EXIST));

        // 设置实名认证信息
        user.setRealName(param.getRealName());
        user.setIdCardNo(param.getIdCardNo());
        user.setCertification(true);

        // 设置手机号
        if (StringUtils.isNotBlank(param.getPhone())) {
            user.setTelephone(param.getPhone());
        }

        if (!updateById(user)) {
            throw new UserException(UserErrorCodeEnum.UPDATE_FAILED);
        }

        // 记录用户操作流水
        userOperateStreamService.insertStream(user, UserOperateTypeEnum.CERTIFICATION);
        return true;
    }


    public String generateNickName(String email) {
        return UserConstants.NIKE_NAME_PREFIX + email.substring(0,email.lastIndexOf("@"));
    }
}
