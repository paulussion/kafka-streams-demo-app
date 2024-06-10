package com.bforbank.testapp.streams.processors.retry;

import com.bforbank.demo.*;
import org.apache.kafka.streams.processor.api.ContextualFixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class CardOrderRetryProcessor extends ContextualFixedKeyProcessor<String, CardOrderReply, RetryCommand> {

    private static final Logger log = LoggerFactory.getLogger(CardOrderRetryProcessor.class);

    private final String cardAggregateStoreName;
    private KeyValueStore<String, CardAggregate> cardAggregateStore;
    private FixedKeyProcessorContext<String, RetryCommand> context;

    public CardOrderRetryProcessor(String cardAggregateStoreName) {
        this.cardAggregateStoreName = cardAggregateStoreName;
    }

    @Override
    public void init(FixedKeyProcessorContext<String, RetryCommand> context) {
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

            CardOrder order = CardOrder.newBuilder()
                    .setAttachedAccountId(cardAggregate.getAttachedAccountId())
                    .setCartType(cardAggregate.getCartType())
                    .setHolderFirstName(cardAggregate.getHolderFirstName())
                    .setHolderLastName(cardAggregate.getHolderLastName())
                    .setIsVirtual(cardAggregate.getIsVirtual())
                    .setRequestId(cardOrderReply.getRequestId())
                    .build();

            RetryCommand retryCommand = new RetryCommand(order);
            context.forward(fixedKeyRecord.withValue(retryCommand));
        }
    }
}
