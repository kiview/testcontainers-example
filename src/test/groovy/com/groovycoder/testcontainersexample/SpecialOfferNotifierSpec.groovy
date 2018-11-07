package com.groovycoder.testcontainersexample

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.rnorth.ducttape.unreliables.Unreliables
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Specification

import java.util.concurrent.TimeUnit

@Testcontainers
class SpecialOfferNotifierSpec extends Specification {

    KafkaContainer kafka = new KafkaContainer()

    def "kafka works"() {

        given: "the notifier"
        def topic = "notification"
        def notifier = new SpecialOfferNotifier(kafka.bootstrapServers, "clientFoobar", topic)

        and: "a consumer for testing"
        KafkaConsumer<String, String> consumer = createConsumer(topic)

        when: "sending notification"
        def notificationMessage = "Special offer, Docker stickers!"
        notifier.sendNotification(notificationMessage)

        then: "test consumer received record after some time"
        ConsumerRecords<String, String> records = pollRecords(consumer)
        consumer.unsubscribe()

        and: "received records contains sent notification"
        records.first().value() == notificationMessage
    }

    private KafkaConsumer<String, String> createConsumer(String topic) {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(
                [
                        (ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG): kafka.bootstrapServers,
                        (ConsumerConfig.GROUP_ID_CONFIG)         : "tc-" + UUID.randomUUID(),
                        (ConsumerConfig.AUTO_OFFSET_RESET_CONFIG): "earliest"
                ],
                new StringDeserializer(),
                new StringDeserializer())
        consumer.subscribe(Arrays.asList(topic))
        return consumer
    }

    private ConsumerRecords<String, String> pollRecords(consumer) {
        ConsumerRecords<String, String> records
        Unreliables.retryUntilTrue(10, TimeUnit.SECONDS, {
            records = consumer.poll(100)
            return !records.isEmpty()
        })
        return records
    }

}
