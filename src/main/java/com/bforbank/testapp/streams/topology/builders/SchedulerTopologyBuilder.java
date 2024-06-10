package com.bforbank.testapp.streams.topology.builders;

import com.bforbank.demo.CardOrder;
import com.bforbank.demo.CardOrderReply;
import com.bforbank.demo.OrderStatus;
import com.bforbank.demo.RetryCommand;
import com.bforbank.testapp.streams.processors.retry.CardOrderRetryProcessor;
import com.bforbank.testapp.streams.processors.retry.RetryCommandProcessor;
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
public class SchedulerTopologyBuilder {

    @Autowired
    private AppSerdes appSerdes;

    @Value("${kafka.state-stores.card-aggregate-store-name:card-aggregate-store}")
    private String cardAggregateStoreName;
    @Value("${kafka.topics.card-orders-topic-name:card-orders}")
    private String cardOrdersTopicName;

    @Value("${kafka.topics.retry-commands-topic-name:retry-commands}")
    private String retryCommandsTopicName;
    @Value("${kafka.state-stores.retry-store-name:retry-store}")
    private String retryStoreName;

    public void buildErrorHandlingSchedulerTopology(StreamsBuilder builder, KStream<String, CardOrderReply> cardOrderReplyKStream) {
        // create state store dedicated to errors
        builder.addStateStore(StateStoreUtils.buildKeyValueStore(retryStoreName, Serdes.String(), appSerdes.retryAggregateSerde()));

        cardOrderReplyKStream
                // filter reply without error
                .filter((_, v) -> v.getOrderStatus() == OrderStatus.TECHNICAL_ERROR, Named.as("filter-non-technical-error"))
                // reconstruct original command from card aggregate state store and map to retryCommand
                .processValues(() -> new CardOrderRetryProcessor(cardAggregateStoreName), Named.as("recover-card-order-for-retry"), cardAggregateStoreName)
                // send retry command to sink topic
                .to(retryCommandsTopicName, producedAsAvroRetryCommand());

        // stream retry commands
        builder.stream(retryCommandsTopicName, consumedAsAvroRetryCommand())
                // store commands in state store with a scheduler running every 30s
                .process(() -> new RetryCommandProcessor(retryStoreName), Named.as("retry-command-processor"), retryStoreName)
                // forward card orders retries (every 30s)
                .to(cardOrdersTopicName, producedAsAvroCardOrder());
    }

    private Consumed<String, RetryCommand> consumedAsAvroRetryCommand() {
        return Consumed.with(Serdes.String(), appSerdes.retryCommandSerde()).withName("retry-commands-source");
    }

    private Produced<String, RetryCommand> producedAsAvroRetryCommand() {
        return Produced.with(Serdes.String(), appSerdes.retryCommandSerde()).withName("retry-commands-sink");
    }

    private Produced<String, CardOrder> producedAsAvroCardOrder() {
        return Produced.with(Serdes.String(), appSerdes.cardOrderSerde()).withName("card-order-retry-sink");
    }
}
