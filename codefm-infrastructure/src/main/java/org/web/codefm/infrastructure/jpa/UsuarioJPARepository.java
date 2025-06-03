package org.web.codefm.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.web.codefm.infrastructure.entity.mariadb.usuarios.UsuarioEntity;

import java.util.Optional;

@Repository
public interface UsuarioJPARepository extends JpaRepository<UsuarioEntity, Integer> {

    Optional<UsuarioEntity> findByUsuario(String usuario);

}

