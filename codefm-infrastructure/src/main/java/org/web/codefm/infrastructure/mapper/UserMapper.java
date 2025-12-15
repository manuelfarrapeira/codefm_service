package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.User;
import org.web.codefm.infrastructure.entity.mariadb.codefm.UserEntity;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = false))
public interface UserMapper {

    User toModel(UserEntity entity);


}

