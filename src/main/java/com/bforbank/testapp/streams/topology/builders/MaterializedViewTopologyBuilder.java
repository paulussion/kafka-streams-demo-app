package com.bforbank.testapp.streams.topology.builders;

import com.bforbank.demo.Account;
import com.bforbank.demo.AccountAggregation;
import com.bforbank.demo.Customer;
import com.bforbank.demo.CustomerWithAccounts;
import com.bforbank.testapp.streams.serdes.AppSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class MaterializedViewTopologyBuilder {

    @Autowired
    private AppSerdes appSerdes;

    @Value("${kafka.topics.customers-topic-name:customers}")
    private String customersTopicName;
    @Value("${kafka.topics.accounts-topic-name:accounts}")
    private String accountsTopicName;
    @Value("${kafka.topics.customers-with-accounts-topic-name:customers-with-accounts}")
    private String customersWithAccountsTopicName;


    public void buildCustomerWithAccountsTopology(StreamsBuilder builder) {
        // On stream les évènements du topic "Source" : "accounts"
        KTable<String, AccountAggregation> accountListPerCustomer =
                builder.stream(accountsTopicName, consumedAsAvroAccountAggregations())
                        // on regroupe les comptes par client (key = customerId)
                        .groupByKey()
                        // on aggrège les comptes pour construire notre liste de comptes
                        .aggregate(
                                () -> AccountAggregation.newBuilder().setAccounts(new ArrayList<>()).build(),
                                MaterializedViewTopologyBuilder::aggregateAccount,
                                Named.as("aggregate-accounts-in-empty-customer"),
                                Materialized.with(Serdes.String(), appSerdes.accountAggregationSerde())
                        );

        // On construit une KTable du topic "Source" : "customers"
        builder.table(customersTopicName, consumedAsAvroCustomers(), Materialized.as("customers-store"))
                // on réalise une jointure des customers avec leur liste de comptes
                .join(accountListPerCustomer,
                        MaterializedViewTopologyBuilder::mergeCustomerAndAccountAggregation,
                        Named.as("customer-and-accounts-aggregation-join")
                )
                // on convertit notre KTable en KStream
                .toStream(Named.as("customers-with-accounts-table-to-stream"))
                // on forward nos clients avec leur liste de compte dans le topic "Sink"
                .to(customersWithAccountsTopicName, producedAsAvroCustomerWithAccounts());
    }

    private static CustomerWithAccounts mergeCustomerAndAccountAggregation(Customer customer, AccountAggregation aggregation) {
        return CustomerWithAccounts.newBuilder()
                        .setId(customer.getId())
                        .setFirstName(customer.getFirstName())
                        .setLastName(customer.getLastName())
                        .setAccounts(aggregation.getAccounts())
                        .build();
    }

    private static AccountAggregation aggregateAccount(String key, Account account, AccountAggregation aggregation) {
        aggregation.getAccounts().add(account);
        return aggregation;
    }

    private Consumed<String, Account> consumedAsAvroAccountAggregations() {
        return Consumed.with(Serdes.String(), appSerdes.accountSerde()).withName("accounts-source");
    }

    private Consumed<String, Customer> consumedAsAvroCustomers() {
        return Consumed.with(Serdes.String(), appSerdes.customerSerde())
                .withName("customers-table");
    }

    private Produced<String, CustomerWithAccounts> producedAsAvroCustomerWithAccounts() {
        return Produced.with(Serdes.String(), appSerdes.customerWithAccountsSerde())
                .withName("customers-with-accounts-sink");
    }
}
