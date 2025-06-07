package org.web.codefm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.Usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class HelloWorldServiceImplTest {

    @Test
    void returnsGreetingWhenUserIsNotNull() {
        HelloWorldServiceImpl service = new HelloWorldServiceImpl();
        Usuario user = new Usuario();
        user.setNombre("Manuel");
        String result = service.helloWorld(user);
        assertEquals("Hola Manuel", result);
    }

    @Test
    void returnsUserNotFoundWhenUserIsNull() {
        HelloWorldServiceImpl service = new HelloWorldServiceImpl();
        String result = service.helloWorld(null);
        assertEquals("Usuario no encontrado", result);
    }

    @Test
    void returnsGreetingWhenUserNameIsNull() {
        HelloWorldServiceImpl service = new HelloWorldServiceImpl();
        Usuario user = new Usuario();
        user.setNombre(null);
        String result = service.helloWorld(user);
        assertEquals("Hola null", result);
    }

}