package org.web.codefm.domain.kafka;

import org.web.codefm.domain.entity.Usuario;

public interface ExampleKafkaProducer {

    void sendMessage(Usuario usuario);

}
