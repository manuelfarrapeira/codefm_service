package org.web.codefm.domain.kafka;

import org.web.codefm.domain.entity.User;

public interface ExampleKafkaProducer {

    void sendMessage(User user);

}
