package org.web.codefm.infrastructure.usuarios;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.Usuario;
import org.web.codefm.infrastructure.entity.mariadb.usuarios.UsuarioEntity;
import org.web.codefm.infrastructure.jpa.UsuarioJPARepository;
import org.web.codefm.infrastructure.mapper.UsuarioMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioRepositoryImplTest {

    @Mock
    UsuarioJPARepository usuarioJPARepository;

    @Mock
    UsuarioMapper usuarioMapper;

    @InjectMocks
    UsuarioRepositoryImpl usuarioRepositoryImpl;

    @Test
    void returnsUsuarioWhenUsuarioExists() {
        String nombre = "Manuel";
        UsuarioEntity entity = new UsuarioEntity();
        Usuario usuario = new Usuario();
        when(usuarioJPARepository.findByUsuario(nombre)).thenReturn(Optional.of(entity));
        when(usuarioMapper.toModel(entity)).thenReturn(usuario);

        Usuario result = usuarioRepositoryImpl.findByName(nombre);

        assertEquals(usuario, result);
    }

    @Test
    void returnsNullWhenUsuarioDoesNotExist() {
        String nombre = "Desconocido";
        when(usuarioJPARepository.findByUsuario(nombre)).thenReturn(Optional.empty());

        Usuario result = usuarioRepositoryImpl.findByName(nombre);

        assertNull(result);
    }

    @Test
    void returnsNullWhenUsuarioNameIsNull() {
        when(usuarioJPARepository.findByUsuario(null)).thenReturn(Optional.empty());

        Usuario result = usuarioRepositoryImpl.findByName(null);

        assertNull(result);
    }
}