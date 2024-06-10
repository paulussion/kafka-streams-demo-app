package com.bforbank.testapp.streams.processors.card;

import com.bforbank.demo.*;
import org.apache.kafka.streams.processor.api.ContextualFixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.UUID;

public class HandleNewCurrentAccountProcessor extends ContextualFixedKeyProcessor<String, CustomerWithAccounts, CardAggregate> {

    private final String cardAggregateStoreName;
    private KeyValueStore<String, CardAggregate> cardAggregateStore;
    private FixedKeyProcessorContext<String, CardAggregate> context;

    public HandleNewCurrentAccountProcessor(String cardAggregateStoreName) {
        this.cardAggregateStoreName = cardAggregateStoreName;
    }

    @Override
    public void init(FixedKeyProcessorContext<String, CardAggregate> context) {
        this.context = context;
        cardAggregateStore = context.getStateStore(this.cardAggregateStoreName);
    }

    @Override
    public void process(FixedKeyRecord<String, CustomerWithAccounts> fixedKeyRecord) {
        String customerId = fixedKeyRecord.key();
        CustomerWithAccounts customerWithAccounts = fixedKeyRecord.value();
        for(Account account : customerWithAccounts.getAccounts()) {
            String storeKey = customerId + "@" + account.getAccountId();
            if(cardAggregateStore.get(storeKey) == null) {
                CardAggregate cardAggregate = CardAggregate.newBuilder()
                        .setId(UUID.randomUUID().toString())
                        .setAttachedAccountId(account.getAccountId())
                        .setHolderFirstName(customerWithAccounts.getFirstName())
                        .setHolderLastName(customerWithAccounts.getLastName())
                        .setCartType(CardOffer.BASIC)
                        .setIsVirtual(true)
                        .setHolderId(customerId)
                        .setOrderStatus(OrderStatus.IN_PROGRESS)
                        .build();
                cardAggregateStore.put(storeKey, cardAggregate);
                context.forward(fixedKeyRecord.withValue(cardAggregate));
            }
        }
    }
}
