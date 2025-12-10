package org.web.codefm.kafka.consumers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.web.codefm.kafka.UserEvent;


@Component
@Slf4j
public class ExampleKafkaConsumer {

    @KafkaListener(topics = "${kafka.topic.user}")
    public void handleMessage(@Payload UserEvent usuarioEvent) {

        log.info("Usser message received: {}", usuarioEvent);
    }
}
