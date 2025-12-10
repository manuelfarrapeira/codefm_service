package org.web.codefm.infrastructure.usuarios;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.User;
import org.web.codefm.infrastructure.entity.mariadb.users.UserEntity;
import org.web.codefm.infrastructure.jpa.UserJPARepository;
import org.web.codefm.infrastructure.mapper.UsuarioMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @Mock
    UserJPARepository userJPARepository;

    @Mock
    UsuarioMapper usuarioMapper;

    @InjectMocks
    UserRepositoryImpl userRepositoryImpl;

    @Test
    void returnsUsuarioWhenUsuarioExists() {
        String nombre = "Manuel";
        UserEntity entity = new UserEntity();
        User user = new User();
        when(userJPARepository.findByLogin(nombre)).thenReturn(Optional.of(entity));
        when(usuarioMapper.toModel(entity)).thenReturn(user);

        User result = userRepositoryImpl.findByName(nombre);

        assertEquals(user, result);
    }

    @Test
    void returnsNullWhenUsuarioDoesNotExist() {
        String nombre = "Desconocido";
        when(userJPARepository.findByLogin(nombre)).thenReturn(Optional.empty());

        User result = userRepositoryImpl.findByName(nombre);

        assertNull(result);
    }

    @Test
    void returnsNullWhenUsuarioNameIsNull() {
        when(userJPARepository.findByLogin(null)).thenReturn(Optional.empty());

        User result = userRepositoryImpl.findByName(null);

        assertNull(result);
    }
}