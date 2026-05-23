package org.web.codefm.kafka.producers;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import org.web.codefm.domain.entity.User;
import org.web.codefm.kafka.UserEvent;
import org.web.codefm.kafka.mappers.UserEventMapper;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExampleKafkaProducerImplTest {

    @Mock
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Mock
    private UserEventMapper mapper;

    private ExampleKafkaProducerImpl producer;

    @BeforeEach
    void beforeEach() {
        this.producer = new ExampleKafkaProducerImpl(this.kafkaTemplate, this.mapper);
        ReflectionTestUtils.setField(this.producer, "topic", "test-topic");
    }

    @Nested
    class SendMessage {

        @Test
        void when_user_is_valid_expect_kafka_send() {
            final User user = User.builder()
                    .id(1)
                    .login("testuser")
                    .name("Test User")
                    .build();
            final UserEvent event = new UserEvent(1, "testuser", "Test User");
            final RecordMetadata metadata = new RecordMetadata(new TopicPartition("test-topic", 0), 0L, 0, 0L, 0, 0);
            final ProducerRecord<String, UserEvent> producerRecord = new ProducerRecord<>("test-topic", "1", event);
            final SendResult<String, UserEvent> sendResult = new SendResult<>(producerRecord, metadata);
            final CompletableFuture<SendResult<String, UserEvent>> future = CompletableFuture.completedFuture(sendResult);
            when(ExampleKafkaProducerImplTest.this.mapper.toEvent(user)).thenReturn(event);
            when(ExampleKafkaProducerImplTest.this.kafkaTemplate.send(anyString(), anyString(), any(UserEvent.class)))
                    .thenReturn(future);

            ExampleKafkaProducerImplTest.this.producer.sendMessage(user);

            verify(ExampleKafkaProducerImplTest.this.kafkaTemplate).send("test-topic", "1", event);
            verify(ExampleKafkaProducerImplTest.this.mapper).toEvent(user);
        }

        @Test
        void when_kafka_send_fails_expect_send_attempt() {
            final User user = User.builder()
                    .id(1)
                    .login("testuser")
                    .name("Test User")
                    .build();
            final UserEvent event = new UserEvent(1, "testuser", "Test User");
            final CompletableFuture<SendResult<String, UserEvent>> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Kafka error"));
            when(ExampleKafkaProducerImplTest.this.mapper.toEvent(user)).thenReturn(event);
            when(ExampleKafkaProducerImplTest.this.kafkaTemplate.send(anyString(), anyString(), any(UserEvent.class)))
                    .thenReturn(future);

            ExampleKafkaProducerImplTest.this.producer.sendMessage(user);

            verify(ExampleKafkaProducerImplTest.this.kafkaTemplate).send("test-topic", "1", event);
            verify(ExampleKafkaProducerImplTest.this.mapper).toEvent(user);
        }

        @Test
        void when_user_is_null_expect_no_send() {
            ExampleKafkaProducerImplTest.this.producer.sendMessage(null);

            verify(ExampleKafkaProducerImplTest.this.kafkaTemplate, never()).send(anyString(), anyString(), any());
            verify(ExampleKafkaProducerImplTest.this.mapper, never()).toEvent(any());
        }
    }
}
