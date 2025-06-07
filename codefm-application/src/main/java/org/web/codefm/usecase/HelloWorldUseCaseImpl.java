package org.web.codefm.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.consul.EjemploConsul;
import org.web.codefm.domain.entity.Usuario;
import org.web.codefm.domain.repository.UsuarioRepository;
import org.web.codefm.domain.service.HelloWorldService;
import org.web.codefm.domain.usecase.HelloWorldUseCase;


@Slf4j
@Service
@RequiredArgsConstructor
public class HelloWorldUseCaseImpl implements HelloWorldUseCase {

    private final HelloWorldService helloWorldService;

    private final UsuarioRepository usuarioRepository;

    private final EjemploConsul ejemploConsul;

    @Override
    public String helloWorld(String usuario) {

        log.info("ejemplo consul: " + ejemploConsul.getparametro());

        Usuario user = usuarioRepository.findByName(usuario);

        return helloWorldService.helloWorld(user);
    }

}
