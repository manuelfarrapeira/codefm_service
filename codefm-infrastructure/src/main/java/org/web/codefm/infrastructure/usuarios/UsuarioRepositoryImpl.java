package org.web.codefm.infrastructure.usuarios;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.Usuario;
import org.web.codefm.domain.repository.UsuarioRepository;
import org.web.codefm.infrastructure.jpa.UsuarioJPARepository;
import org.web.codefm.infrastructure.mapper.UsuarioMapper;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final UsuarioJPARepository usuarioJPARepository;

    private final UsuarioMapper usuarioMapper;


    @Override
    public Usuario findByName(String usuario) {

        return usuarioJPARepository.findByUsuario(usuario)
                .map(usuarioMapper::toModel)
                .orElse(null);

    }
}
