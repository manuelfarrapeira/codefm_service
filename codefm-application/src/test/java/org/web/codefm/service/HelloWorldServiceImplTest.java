package org.web.codefm.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.User;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HelloWorldServiceImplTest {

    private final HelloWorldServiceImpl service = new HelloWorldServiceImpl();

    @Nested
    class HelloWorld {

        @Test
        void when_user_is_not_null_expect_greeting() {
            final User user = new User();
            user.setName("Manuel");

            final String result = service.helloWorld(user);

            assertThat(result).isEqualTo("Hi! Manuel");
        }

        @Test
        void when_user_is_null_expect_user_not_found() {
            final String result = service.helloWorld(null);

            assertThat(result).isEqualTo("User not found");
        }

        @Test
        void when_user_name_is_null_expect_greeting() {
            final User user = new User();
            user.setName(null);

            final String result = service.helloWorld(user);

            assertThat(result).isEqualTo("Hi! null");
        }
    }
}