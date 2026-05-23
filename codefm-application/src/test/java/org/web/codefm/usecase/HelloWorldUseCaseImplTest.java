package org.web.codefm.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.consul.ConsulExample;
import org.web.codefm.domain.entity.User;
import org.web.codefm.domain.kafka.ExampleKafkaProducer;
import org.web.codefm.domain.repository.ReactorExecutorExampleRepository;
import org.web.codefm.domain.repository.UserRepository;
import org.web.codefm.domain.service.HelloWorldService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HelloWorldUseCaseImplTest {

    private HelloWorldUseCaseImpl useCase;

    @Mock
    HelloWorldService helloWorldService;

    @Mock
    UserRepository userRepository;

    @Mock
    ReactorExecutorExampleRepository reactorExecutorExampleRepository;

    @Mock
    ConsulExample consulExample;

    @Mock
    ExampleKafkaProducer exampleKafkaProducer;

    @BeforeEach
    void beforeEach() {
        useCase = new HelloWorldUseCaseImpl(helloWorldService, userRepository, consulExample, reactorExecutorExampleRepository, exampleKafkaProducer);
    }

    @Nested
    class HelloWorld {

        @Test
        void when_user_exists_expect_greeting_returned() {
            final User user = new User();
            user.setName("Manuel");
            when(userRepository.findByLogin("Manuel")).thenReturn(user);
            when(helloWorldService.helloWorld(user)).thenReturn("Hello Manuel");
            when(consulExample.getparameter()).thenReturn("value");

            final String result = useCase.helloWorld("Manuel");

            assertThat(result).isEqualTo("Hello Manuel");
        }

        @Test
        void when_user_not_found_expect_not_found_message_returned() {
            when(userRepository.findByLogin("Desconocido")).thenReturn(null);
            when(helloWorldService.helloWorld(null)).thenReturn("user not found");
            when(consulExample.getparameter()).thenReturn("value");

            final String result = useCase.helloWorld("Desconocido");

            assertThat(result).isEqualTo("user not found");
        }

        @Test
        void when_user_name_is_null_expect_greeting_with_null_returned() {
            final User user = new User();
            user.setName(null);
            when(userRepository.findByLogin(null)).thenReturn(user);
            when(helloWorldService.helloWorld(user)).thenReturn("Hello null");
            when(consulExample.getparameter()).thenReturn("value");

            final String result = useCase.helloWorld(null);

            assertThat(result).isEqualTo("Hello null");
        }
    }
}
