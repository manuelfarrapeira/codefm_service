package org.web.codefm.kafka.consumers;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.web.codefm.kafka.UserEvent;

import static org.assertj.core.api.Assertions.assertThatCode;

class ExampleKafkaConsumerTest {

    private final ExampleKafkaConsumer consumer = new ExampleKafkaConsumer();

    @Nested
    class HandleMessage {

        @Test
        void when_user_event_is_received_expect_no_exception() {
            final UserEvent event = new UserEvent(1, "testuser", "Test User");
            final ThrowingCallable action = () -> ExampleKafkaConsumerTest.this.consumer.handleMessage(event);

            assertThatCode(action).doesNotThrowAnyException();
        }

        @Test
        void when_event_is_null_expect_no_exception() {
            final ThrowingCallable action = () -> ExampleKafkaConsumerTest.this.consumer.handleMessage(null);

            assertThatCode(action).doesNotThrowAnyException();
        }
    }
}
