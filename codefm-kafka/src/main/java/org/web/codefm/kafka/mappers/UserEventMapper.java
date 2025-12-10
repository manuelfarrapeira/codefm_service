package org.web.codefm.kafka.mappers;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.User;
import org.web.codefm.kafka.UserEvent;

@Mapper(componentModel = "spring")
public interface UserEventMapper {

    UserEvent toEvent(User user);

    User toEntity(UserEvent event);
}
