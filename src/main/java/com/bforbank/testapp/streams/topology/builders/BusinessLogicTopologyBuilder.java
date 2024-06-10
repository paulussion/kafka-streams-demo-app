package com.bforbank.testapp.streams.topology.builders;

import com.bforbank.demo.*;
import com.bforbank.testapp.streams.processors.card.HandleCardOrderReplyProcessor;
import com.bforbank.testapp.streams.processors.card.HandleNewCurrentAccountProcessor;
import com.bforbank.testapp.streams.serdes.AppSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BusinessLogicTopologyBuilder {

    @Autowired
    private AppSerdes appSerdes;

    @Value("${kafka.topics.customers-with-accounts-topic-name:customers-with-accounts}")
    private String customersWithAccountsTopicName;

    @Value("${kafka.topics.card-orders-topic-name:card-orders}")
    private String cardOrdersTopicName;
    @Value("${kafka.topics.card-orders-reply-topic-name:card-orders-reply}")
    private String cardOrdersReplyTopicName;
    @Value("${kafka.topics.cards-topic-name:cards}")
    private String cardsTopicName;
    @Value("${kafka.state-stores.card-aggregate-store-name:card-aggregate-store}")
    private String cardAggregateStoreName;

    public KStream<String, CardOrderReply> buildCardCommandLogicTopology(StreamsBuilder builder) {
        // create a state store dedicated to card aggregates
        builder.addStateStore(StateStoreUtils.buildKeyValueStore(cardAggregateStoreName, Serdes.String(), appSerdes.cardAggregateSerde()));

        // stream customer with accounts topic
        KStream<String, CardAggregate> newCardAggregateKStream =
                builder.stream(customersWithAccountsTopicName, consumedAsAvroCustomerWithAccounts())
                        // keep only current accounts in account list
                        .mapValues(BusinessLogicTopologyBuilder::removeNonCurrentAccountsFromAccountList, Named.as("remove-non-current-accounts-from-account-list"))
                        // filter empty lists
                        .filterNot((_, v) -> v.getAccounts().isEmpty(), Named.as("filter-empty-accounts-list"))
                        // use processor to enrich state store and map to card-order
                        .processValues(() -> new HandleNewCurrentAccountProcessor(cardAggregateStoreName), Named.as("new-current-account-processor"), cardAggregateStoreName);

        newCardAggregateKStream
                // map card aggregate to card order
                .mapValues(BusinessLogicTopologyBuilder::cardAggregateToCardOrder, Named.as("map-new-card-aggregate-to-card-order"))
                // send card order event to sink topic
                .to(cardOrdersTopicName, producedAsAvroCardOrder());

        // stream card order replies
        KStream<String, CardOrderReply> cardOrderReplyKStream =
                builder.stream(cardOrdersReplyTopicName, consumedAsAvroCardOrderReply());

        cardOrderReplyKStream
                // filter technical errors
                .filterNot((_, v) -> v.getOrderStatus() == OrderStatus.TECHNICAL_ERROR, Named.as("filter-technical-errors"))
                // enrich state store aggregates with card order replies
                .processValues(() -> new HandleCardOrderReplyProcessor(cardAggregateStoreName), Named.as("card-order-reply-processor"), cardAggregateStoreName)
                // merge card aggregate streams
                .merge(newCardAggregateKStream, Named.as("card-aggregates-merge"))
                // map card aggregates to card
                .mapValues(BusinessLogicTopologyBuilder::cardAggregateToCard, Named.as("map-card-aggregate-to-card-order"))
                // send card events to sink topic
                .to(cardsTopicName, producedAsAvroCard());

        return cardOrderReplyKStream;
    }

    private static CustomerWithAccounts removeNonCurrentAccountsFromAccountList(CustomerWithAccounts customerWithAccounts) {
        customerWithAccounts.setAccounts(
                customerWithAccounts.getAccounts().stream()
                        .filter(a -> a.getType() == AccountType.CURRENT_ACCOUNT)
                        .toList()
        );
        return customerWithAccounts;
    }

    private static CardOrder cardAggregateToCardOrder(CardAggregate v) {
        return CardOrder.newBuilder()
                .setAttachedAccountId(v.getAttachedAccountId())
                .setCartType(v.getCartType())
                .setIsVirtual(v.getIsVirtual())
                .setRequestId(v.getHolderId() + "@" + v.getAttachedAccountId() + "@" + v.getId())
                .setHolderFirstName(v.getHolderFirstName())
                .setHolderLastName(v.getHolderLastName())
                .build();
    }

    private static Card cardAggregateToCard(CardAggregate v) {
        return Card.newBuilder()
                .setAttachedAccountId(v.getAttachedAccountId())
                .setCartType(v.getCartType())
                .setIsVirtual(v.getIsVirtual())
                .setId(v.getId())
                .setOrderStatus(v.getOrderStatus())
                .setHolderId(v.getHolderId())
                .build();
    }

    private Consumed<String, CustomerWithAccounts> consumedAsAvroCustomerWithAccounts() {
        return Consumed.with(Serdes.String(), appSerdes.customerWithAccountsSerde()).withName("customers-with-accounts-source");
    }

    private Consumed<String, CardOrderReply> consumedAsAvroCardOrderReply() {
        return Consumed.with(Serdes.String(), appSerdes.cardOrderReplySerde())
                .withName("card-orders-reply-source");
    }

    private Produced<String, CardOrder> producedAsAvroCardOrder() {
        return Produced.with(Serdes.String(), appSerdes.cardOrderSerde()).withName("card-orders-sink");
    }

    private Produced<String, Card> producedAsAvroCard() {
        return Produced.with(Serdes.String(), appSerdes.cardSerde()).withName("cards-sink");
    }
}
