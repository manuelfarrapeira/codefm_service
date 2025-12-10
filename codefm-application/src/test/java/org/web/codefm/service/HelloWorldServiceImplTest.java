package org.web.codefm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class HelloWorldServiceImplTest {

    @Test
    void returnsGreetingWhenUserIsNotNull() {
        HelloWorldServiceImpl service = new HelloWorldServiceImpl();
        User user = new User();
        user.setName("Manuel");
        String result = service.helloWorld(user);
        assertEquals("Hi! Manuel", result);
    }

    @Test
    void returnsUserNotFoundWhenUserIsNull() {
        HelloWorldServiceImpl service = new HelloWorldServiceImpl();
        String result = service.helloWorld(null);
        assertEquals("User not found", result);
    }

    @Test
    void returnsGreetingWhenUserNameIsNull() {
        HelloWorldServiceImpl service = new HelloWorldServiceImpl();
        User user = new User();
        user.setName(null);
        String result = service.helloWorld(user);
        assertEquals("Hi! null", result);
    }

}