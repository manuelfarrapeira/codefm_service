package org.web.codefm.infrastructure.users;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.User;
import org.web.codefm.infrastructure.entity.mariadb.users.UserEntity;
import org.web.codefm.infrastructure.jpa.UserJPARepository;
import org.web.codefm.infrastructure.mapper.UserMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @Mock
    UserJPARepository userJPARepository;

    @Mock
    UserMapper userMapper;

    @InjectMocks
    UserRepositoryImpl userRepositoryImpl;

    @Test
    void returnsUserWhenUserExists() {
        String nombre = "Manuel";
        UserEntity entity = new UserEntity();
        User user = new User();
        when(userJPARepository.findByLogin(nombre)).thenReturn(Optional.of(entity));
        when(userMapper.toModel(entity)).thenReturn(user);

        User result = userRepositoryImpl.findByLogin(nombre);

        assertEquals(user, result);
    }

    @Test
    void returnsNullWhenUserDoesNotExist() {
        String nombre = "Desconocido";
        when(userJPARepository.findByLogin(nombre)).thenReturn(Optional.empty());

        User result = userRepositoryImpl.findByLogin(nombre);

        assertNull(result);
    }

    @Test
    void returnsNullWhenUserLoginIsNull() {
        when(userJPARepository.findByLogin(null)).thenReturn(Optional.empty());

        User result = userRepositoryImpl.findByLogin(null);

        assertNull(result);
    }
}