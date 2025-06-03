package org.web.codefm.domain.repository;

import org.web.codefm.domain.entity.Usuario;

public interface UsuarioRepository {

    Usuario findByName(String usuario);

}
