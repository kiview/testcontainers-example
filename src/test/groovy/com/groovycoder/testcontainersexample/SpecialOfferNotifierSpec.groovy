package com.groovycoder.testcontainersexample

import com.groovycoder.spockdockerextension.Testcontainers
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.rnorth.ducttape.unreliables.Unreliables
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap
import spock.lang.Specification

import java.util.concurrent.TimeUnit

@Testcontainers
class SpecialOfferNotifierSpec extends Specification {

    KafkaContainer kafka = new KafkaContainer()

    def "kafka works"() {

        given:
        KafkaProducer<String, String> producer = new KafkaProducer<>(
                ImmutableMap.of(
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.bootstrapServers,
                        ProducerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString()
                ),
                new StringSerializer(),
                new StringSerializer())


        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(
                ImmutableMap.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.bootstrapServers,
                        ConsumerConfig.GROUP_ID_CONFIG, "tc-" + UUID.randomUUID(),
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
                ),
                new StringDeserializer(),
                new StringDeserializer())

        String topicName = "messages"
        consumer.subscribe(Arrays.asList(topicName))

        when:
        producer.send(new ProducerRecord<>(topicName, "testcontainers", "rulezzz")).get()

        then:
        ConsumerRecords<String, String> records
        Unreliables.retryUntilTrue(10, TimeUnit.SECONDS, {
            records = consumer.poll(100)
            return !records.isEmpty()
        })
        consumer.unsubscribe()
    }

}
