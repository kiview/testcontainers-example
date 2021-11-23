package com.groovycoder.testcontainersexample;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Testcontainers
public class SpecialOfferNotifierJupiterTests {

    @Container
    private KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"));

    @Test
    void notifierSendsRecordsToKafkaTopic() {
        String topic = "notification";
        SpecialOfferNotifier notifier = new SpecialOfferNotifier(kafka.getBootstrapServers(), "clientFoobar", topic);

        // use consumer to check received records
        KafkaConsumer<String, String> consumer = createConsumer(topic);

        String notificationMessage = "foobar";
        notifier.sendNotification(notificationMessage);


        await().untilAsserted(() -> {
            ConsumerRecords<String, String> records = consumer.poll(100);
            assertFalse(records.isEmpty());
            assertEquals(notificationMessage, records.iterator().next().value());
        });

    }

    private KafkaConsumer<String, String> createConsumer(String topic) {
        Map<String, Object> kafkaConfig = new HashMap<>();
        kafkaConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        kafkaConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "tc-" + UUID.randomUUID());
        kafkaConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        kafkaConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        kafkaConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(kafkaConfig);
        consumer.subscribe(Arrays.asList(topic));
        return consumer;
    }


}
