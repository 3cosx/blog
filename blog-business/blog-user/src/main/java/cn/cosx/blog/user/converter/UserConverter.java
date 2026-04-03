package cn.cosx.blog.user.converter;

import cn.cosx.blog.api.user.response.UserQueryResponse;
import cn.cosx.blog.api.user.vo.UserInfo;
import cn.cosx.blog.user.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserConverter {

    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    @Mapping(source = "user.id",target = "userId")
    UserInfo user2UserInfo(User user);

}
