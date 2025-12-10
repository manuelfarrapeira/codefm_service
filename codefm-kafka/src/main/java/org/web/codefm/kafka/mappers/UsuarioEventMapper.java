package org.web.codefm.kafka.mappers;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.Usuario;
import org.web.codefm.kafka.UserEvent;

@Mapper(componentModel = "spring")
public interface UsuarioEventMapper {

    UserEvent toEvent(Usuario usuario);

    Usuario toEntity(UserEvent event);
}
