package com.bforbank.testapp.mock.data.api;

import com.bforbank.demo.*;
import com.bforbank.testapp.mock.data.producers.KafkaProducers;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@RestController
public class FakeCardOrderReplyController {

    @Autowired
    private KafkaProducers kafkaProducers;

    record FakeCardOrderReply(String requestId, OrderStatus orderStatus){
        CardOrderReply toAvro() {
            return new CardOrderReply(requestId, orderStatus);
        }
    }

    @PostMapping("/fake-card-order-reply")
    public ResponseEntity<Void> generateFakeTransactions(
            @RequestBody FakeCardOrderReply fakeCardOrderReply
    ) {
        String customerId = fakeCardOrderReply.requestId.split("@")[0];
        kafkaProducers.publishCardOrderReply(customerId, fakeCardOrderReply.toAvro());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
