package org.web.codefm.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.consul.ConsulExample;
import org.web.codefm.domain.entity.User;
import org.web.codefm.domain.kafka.ExampleKafkaProducer;
import org.web.codefm.domain.repository.ReactorExecutorExampleRepository;
import org.web.codefm.domain.repository.UserRepository;
import org.web.codefm.domain.service.HelloWorldService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HelloWorldUseCaseImplTest {
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

    @InjectMocks
    HelloWorldUseCaseImpl useCase;

    @Test
    void returnsGreetingWhenUserExists() {
        User user = new User();
        user.setName("Manuel");
        when(userRepository.findByLogin("Manuel")).thenReturn(user);
        when(helloWorldService.helloWorld(user)).thenReturn("Hello Manuel");
        when(consulExample.getparameter()).thenReturn("value");

        String result = useCase.helloWorld("Manuel");

        assertEquals("Hello Manuel", result);
    }

    @Test
    void returnsUserNotFoundWhenUserDoesNotExist() {
        when(userRepository.findByLogin("Desconocido")).thenReturn(null);
        when(helloWorldService.helloWorld(null)).thenReturn("user not found");
        when(consulExample.getparameter()).thenReturn("value");

        String result = useCase.helloWorld("Desconocido");

        assertEquals("user not found", result);
    }

    @Test
    void returnsGreetingWhenUserNameIsNull() {
        User user = new User();
        user.setName(null);
        when(userRepository.findByLogin(null)).thenReturn(user);
        when(helloWorldService.helloWorld(user)).thenReturn("Hello null");
        when(consulExample.getparameter()).thenReturn("value");

        String result = useCase.helloWorld(null);

        assertEquals("Hello null", result);
    }
}