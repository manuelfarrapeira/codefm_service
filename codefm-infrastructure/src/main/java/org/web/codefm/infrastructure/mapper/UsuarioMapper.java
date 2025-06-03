package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.Usuario;
import org.web.codefm.infrastructure.entity.mariadb.usuarios.UsuarioEntity;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

//    @Mapping(target = "id", source = "id")
//    @Mapping(target = "usuario", source = "usuario")
//    @Mapping(target = "nombre", source = "nombre")
    Usuario toModel(UsuarioEntity entity);


}

