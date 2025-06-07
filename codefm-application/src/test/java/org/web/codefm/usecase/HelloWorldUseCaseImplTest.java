package org.web.codefm.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.consul.EjemploConsul;
import org.web.codefm.domain.entity.Usuario;
import org.web.codefm.domain.repository.UsuarioRepository;
import org.web.codefm.domain.service.HelloWorldService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HelloWorldUseCaseImplTest {
    @Mock
    HelloWorldService helloWorldService;

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    EjemploConsul ejemploConsul;

    @InjectMocks
    HelloWorldUseCaseImpl useCase;

    @Test
    void returnsGreetingWhenUserExists() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Manuel");
        when(usuarioRepository.findByName("Manuel")).thenReturn(usuario);
        when(helloWorldService.helloWorld(usuario)).thenReturn("Hello Manuel");
        when(ejemploConsul.getparametro()).thenReturn("value");

        String result = useCase.helloWorld("Manuel");

        assertEquals("Hello Manuel", result);
    }

    @Test
    void returnsUserNotFoundWhenUserDoesNotExist() {
        when(usuarioRepository.findByName("Desconocido")).thenReturn(null);
        when(helloWorldService.helloWorld(null)).thenReturn("user not found");
        when(ejemploConsul.getparametro()).thenReturn("value");

        String result = useCase.helloWorld("Desconocido");

        assertEquals("user not found", result);
    }

    @Test
    void returnsGreetingWhenUserNameIsNull() {
        Usuario usuario = new Usuario();
        usuario.setNombre(null);
        when(usuarioRepository.findByName(null)).thenReturn(usuario);
        when(helloWorldService.helloWorld(usuario)).thenReturn("Hello null");
        when(ejemploConsul.getparametro()).thenReturn("value");

        String result = useCase.helloWorld(null);

        assertEquals("Hello null", result);
    }
}