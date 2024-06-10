package com.bforbank.testapp.mock.data.consumers;

import com.bforbank.demo.CardOrder;
import com.bforbank.demo.CardOrderReply;
import com.bforbank.demo.OrderStatus;
import com.bforbank.testapp.mock.data.producers.KafkaProducers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class KakfaConsumers {

    private static final Logger log = LoggerFactory.getLogger(KakfaConsumers.class);

    @Autowired
    KafkaProducers kafkaProducers;

    @KafkaListener(
            topics = "${kafka.topics.card-orders-topic-name:card-orders}"
    )
    public void listenBeneficiaryEvents(@Header(KafkaHeaders.RECEIVED_KEY) String key, @Payload CardOrder cardOrder) throws InterruptedException {
        log.info("Event listened : {}", cardOrder);
        Thread.sleep(5000L);
        CardOrderReply reply = CardOrderReply.newBuilder()
                .setRequestId(cardOrder.getRequestId())
                .setOrderStatus(OrderStatus.ORDERED)
                .build();

        kafkaProducers.publishCardOrderReply(key, reply);
    }
}
