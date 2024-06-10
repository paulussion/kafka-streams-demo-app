package com.bforbank.testapp.streams.processors.card;

import com.bforbank.demo.*;
import org.apache.kafka.streams.processor.api.ContextualFixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class HandleCardOrderReplyProcessor extends ContextualFixedKeyProcessor<String, CardOrderReply, CardAggregate> {

    private static final Logger log = LoggerFactory.getLogger(HandleCardOrderReplyProcessor.class);

    private final String cardAggregateStoreName;
    private KeyValueStore<String, CardAggregate> cardAggregateStore;
    private FixedKeyProcessorContext<String, CardAggregate> context;

    public HandleCardOrderReplyProcessor(String cardAggregateStoreName) {
        this.cardAggregateStoreName = cardAggregateStoreName;
    }

    @Override
    public void init(FixedKeyProcessorContext<String, CardAggregate> context) {
        this.context = context;
        cardAggregateStore = context.getStateStore(this.cardAggregateStoreName);
    }

    @Override
    public void process(FixedKeyRecord<String, CardOrderReply> fixedKeyRecord) {
        String customerId = fixedKeyRecord.key();
        CardOrderReply cardOrderReply = fixedKeyRecord.value();
        List<String> identifiers = Arrays.asList(cardOrderReply.getRequestId().split("@"));
        if(identifiers.size() != 3 || !identifiers.get(0).equals(customerId)) {
            log.error("Invalid card order reply - invalid requestId : {} / {}", customerId, cardOrderReply);
        } else {
            String accountId = identifiers.get(1);
            String cardId = identifiers.get(2);

            String storeKey = customerId + "@" + accountId;
            CardAggregate cardAggregate = cardAggregateStore.get(storeKey);
            if(cardAggregate == null || !cardAggregate.getId().equals(cardId)) {
                log.error("Invalid card order reply - unknown card : {} / {}", customerId, cardOrderReply);
            }

            cardAggregate.setOrderStatus(cardOrderReply.getOrderStatus());
            cardAggregateStore.put(storeKey, cardAggregate);
            context.forward(fixedKeyRecord.withValue(cardAggregate));
        }
    }
}
