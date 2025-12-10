package org.web.codefm.kafka.consumers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.kafka.UserEvent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class ExampleKafkaConsumerTest {

    @InjectMocks
    private ExampleKafkaConsumer consumer;

    @Test
    void handleMessage_shouldProcessUserEvent() {
        UserEvent event = new UserEvent(1, "testuser", "Test User");

        assertDoesNotThrow(() -> consumer.handleMessage(event));
    }

    @Test
    void handleMessage_withNullEvent_shouldNotThrowException() {
        assertDoesNotThrow(() -> consumer.handleMessage(null));
    }
}
