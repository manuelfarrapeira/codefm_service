package org.web.codefm.infrastructure.users;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.User;
import org.web.codefm.infrastructure.entity.mariadb.codefm.UserEntity;
import org.web.codefm.infrastructure.jpa.codefm.UserJPARepository;
import org.web.codefm.infrastructure.mapper.UserMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    private UserRepositoryImpl userRepository;

    @Mock
    private UserJPARepository userJPARepository;

    @Mock
    private UserMapper userMapper;

    @BeforeEach
    void beforeEach() {
        this.userRepository = new UserRepositoryImpl(this.userJPARepository, this.userMapper);
    }

    @Nested
    class FindByLogin {

        @Test
        void when_user_exists_expect_user_returned() {
            final String nombre = "Manuel";
            final UserEntity entity = new UserEntity();
            final User user = new User();

            when(UserRepositoryImplTest.this.userJPARepository.findByLogin(nombre)).thenReturn(Optional.of(entity));
            when(UserRepositoryImplTest.this.userMapper.toModel(entity)).thenReturn(user);

            final User result = UserRepositoryImplTest.this.userRepository.findByLogin(nombre);

            assertThat(result).isEqualTo(user);
        }

        @Test
        void when_user_does_not_exist_expect_null_returned() {
            final String nombre = "Desconocido";

            when(UserRepositoryImplTest.this.userJPARepository.findByLogin(nombre)).thenReturn(Optional.empty());

            final User result = UserRepositoryImplTest.this.userRepository.findByLogin(nombre);

            assertThat(result).isNull();
        }

        @Test
        void when_login_is_null_expect_null_returned() {
            when(UserRepositoryImplTest.this.userJPARepository.findByLogin(null)).thenReturn(Optional.empty());

            final User result = UserRepositoryImplTest.this.userRepository.findByLogin(null);

            assertThat(result).isNull();
        }
    }
}