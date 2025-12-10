package org.web.codefm.kafka.producers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.web.codefm.domain.entity.Usuario;
import org.web.codefm.domain.kafka.ExampleKafkaProducer;
import org.web.codefm.kafka.UserEvent;
import org.web.codefm.kafka.mappers.UsuarioEventMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExampleKafkaProducerImpl implements ExampleKafkaProducer {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private final UsuarioEventMapper mapper;

    @Value("${kafka.topic.user}")
    private String topic;

    public void sendMessage(Usuario usuario) {

        UserEvent userEvent = mapper.toEvent(usuario);

        log.info("Send message to topic {}: {}", topic, userEvent);
        kafkaTemplate.send(topic, String.valueOf(userEvent.getId()), userEvent)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Message sent successfully: offset={}", result.getRecordMetadata().offset());
                    } else {
                        log.error("Error sending message", ex);
                    }
                });
    }
}
