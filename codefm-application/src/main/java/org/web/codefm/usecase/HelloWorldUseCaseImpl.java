package org.web.codefm.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.consul.EjemploConsul;
import org.web.codefm.domain.entity.Usuario;
import org.web.codefm.domain.kafka.ExampleKafkaProducer;
import org.web.codefm.domain.repository.ReactorExecutorExampleRepository;
import org.web.codefm.domain.repository.UsuarioRepository;
import org.web.codefm.domain.service.HelloWorldService;
import org.web.codefm.domain.usecase.HelloWorldUseCase;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class HelloWorldUseCaseImpl implements HelloWorldUseCase {

    private final HelloWorldService helloWorldService;

    private final UsuarioRepository usuarioRepository;

    private final EjemploConsul ejemploConsul;

    private final ReactorExecutorExampleRepository reactorExecutorExampleRepository;

    private final ExampleKafkaProducer exampleKafkaProducer;

    @Override
    public String helloWorld(String usuario) {

        log.info("Ejemplo consul -> " + ejemploConsul.getparametro());

        Usuario user = usuarioRepository.findByName(usuario);

        List<String> result = reactorExecutorExampleRepository.getResult(getIds());

        log.info("Result size: " + result.size());

        exampleKafkaProducer.sendMessage(user);

        return helloWorldService.helloWorld(user);
    }


    private List<Integer> getIds() {
        return java.util.stream.IntStream.rangeClosed(1, 10000)
                .boxed()
                .toList();
    }


}
