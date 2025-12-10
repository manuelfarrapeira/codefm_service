package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.User;
import org.web.codefm.infrastructure.entity.mariadb.users.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toModel(UserEntity entity);


}

