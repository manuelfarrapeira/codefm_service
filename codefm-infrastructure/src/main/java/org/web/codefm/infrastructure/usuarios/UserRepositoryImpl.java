package org.web.codefm.infrastructure.usuarios;

import lombok.Generated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.User;
import org.web.codefm.domain.repository.UserRepository;
import org.web.codefm.infrastructure.jpa.UserJPARepository;
import org.web.codefm.infrastructure.mapper.UsuarioMapper;

@Repository
@RequiredArgsConstructor
@Slf4j
@Generated
public class UserRepositoryImpl implements UserRepository {

    private final UserJPARepository userJPARepository;

    private final UsuarioMapper usuarioMapper;


    @Override
    public User findByName(String usuario) {

        return userJPARepository.findByLogin(usuario)
                .map(usuarioMapper::toModel)
                .orElse(null);

    }
}
