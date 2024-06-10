package com.bforbank.testapp.mock.data.producers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.SendResult;

import java.util.function.BiConsumer;

public class KafkaProducersUtils {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducersUtils.class);

    static <K, V> BiConsumer<SendResult<K, V>, Throwable> handleSendResult() {
        return (r, ex) -> {
            if (null == ex) {
                var producerRecord = r.getProducerRecord();
                var recordMetadata = r.getRecordMetadata();
                log.info("Event successfully sent to topic {} : {} / {} (partition {} / offset {})",
                        recordMetadata.topic(), producerRecord.key(), producerRecord.value(),
                        recordMetadata.partition(), recordMetadata.offset());
            } else {
                log.error("Failed to send event to kafka broker", ex);
            }
        };
    }
}
