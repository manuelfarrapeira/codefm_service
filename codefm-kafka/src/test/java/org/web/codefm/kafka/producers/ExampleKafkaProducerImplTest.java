package org.web.codefm.kafka.producers;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExampleKafkaProducerImplTest {

    @Mock
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Mock
    private UserEventMapper mapper;

    @InjectMocks
    private ExampleKafkaProducerImpl producer;

    @Test
    void sendMessage_withValidUser_shouldSendToKafkaSuccessfully() {
        ReflectionTestUtils.setField(producer, "topic", "test-topic");

        User user = User.builder()
                .id(1)
                .login("testuser")
                .name("Test User")
                .build();

        UserEvent event = new UserEvent(1, "testuser", "Test User");
        RecordMetadata metadata = new RecordMetadata(new TopicPartition("test-topic", 0), 0, 0, 0, 0L, 0, 0);
        ProducerRecord<String, UserEvent> producerRecord = new ProducerRecord<>("test-topic", "1", event);
        SendResult<String, UserEvent> sendResult = new SendResult<>(producerRecord, metadata);

        when(mapper.toEvent(user)).thenReturn(event);
        CompletableFuture<SendResult<String, UserEvent>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(anyString(), anyString(), any(UserEvent.class))).thenReturn(future);

        producer.sendMessage(user);

        verify(kafkaTemplate).send(eq("test-topic"), eq("1"), eq(event));
        verify(mapper).toEvent(user);
    }

    @Test
    void sendMessage_whenKafkaFails_shouldLogError() {
        ReflectionTestUtils.setField(producer, "topic", "test-topic");

        User user = User.builder()
                .id(1)
                .login("testuser")
                .name("Test User")
                .build();

        UserEvent event = new UserEvent(1, "testuser", "Test User");

        when(mapper.toEvent(user)).thenReturn(event);
        CompletableFuture<SendResult<String, UserEvent>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));
        when(kafkaTemplate.send(anyString(), anyString(), any(UserEvent.class))).thenReturn(future);

        producer.sendMessage(user);

        verify(kafkaTemplate).send(eq("test-topic"), eq("1"), eq(event));
        verify(mapper).toEvent(user);
    }

    @Test
    void sendMessage_withNullUser_shouldNotSendToKafka() {
        producer.sendMessage(null);

        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
        verify(mapper, never()).toEvent(any());
    }
}
