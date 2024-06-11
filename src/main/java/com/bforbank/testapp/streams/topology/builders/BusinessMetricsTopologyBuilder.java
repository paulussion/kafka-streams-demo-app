package com.bforbank.testapp.streams.topology.builders;

import com.bforbank.demo.InstantPaymentMetric;
import com.bforbank.demo.Transaction;
import com.bforbank.demo.TransactionType;
import com.bforbank.testapp.streams.serdes.AppSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class BusinessMetricsTopologyBuilder {

    @Autowired
    private AppSerdes appSerdes;

    @Value("${kafka.topics.transactions-topic-name:transactions}")
    private String transactionsTopicName;
    @Value("${kafka.topics.instant-payment-metrics-topic-name:instant-payment-metrics}")
    private String instantPaymentMetricsTopicName;

    public void buildInstantPaymentMetricsTopology(StreamsBuilder builder) {
        // On stream les évènements du topic "Source" : "transaction"
        builder.stream(transactionsTopicName, consumedAsAvroTransactions())
                // on filtre les transactions qui ne sont pas des instant payment
                .filter((_, v) -> TransactionType.INSTANT_PAYMENT_TRANSFER == v.getType(), Named.as("filter-not-instant-payment"))
                // on regroupe les transaction par identifiant du compte
                .groupByKey()
                // on aggrège les transactions pour construire notre métrique
                .aggregate(() -> new InstantPaymentMetric("", 0L, Instant.now()),
                        BusinessMetricsTopologyBuilder::aggregateTransaction,
                        Named.as("instant-payments-aggregation-to-statistics"),
                        Materialized.as("instant-payments-store")
                )
                // on convertit notre KTable en KStream
                .toStream(Named.as("statistics-table-to-stream"))
                // on forward nos métriques dans le topic "Sink"
                .to(instantPaymentMetricsTopicName, producedAsAvroMetric());
    }

    private static InstantPaymentMetric aggregateTransaction(String key, Transaction transaction, InstantPaymentMetric aggregate) {
        return InstantPaymentMetric.newBuilder()
                .setCustomerId(transaction.getCustomerId())
                .setNbOfInstantPayment(aggregate.getNbOfInstantPayment() + 1L)
                .setLastPaymentTimestamp(transaction.getTimestamp())
                .build();
    }

    private Consumed<String, Transaction> consumedAsAvroTransactions() {
        return Consumed.with(Serdes.String(), appSerdes.transactionSerde())
                .withName("transactions-source");
    }

    private Produced<String, InstantPaymentMetric> producedAsAvroMetric() {
        return Produced.with(Serdes.String(), appSerdes.statisticSerde())
                .withName("instant-payment-metrics-sink");
    }
}
