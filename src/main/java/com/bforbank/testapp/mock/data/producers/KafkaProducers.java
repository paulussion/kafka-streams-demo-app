package com.bforbank.testapp.mock.data.producers;

import com.bforbank.demo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.bforbank.testapp.mock.data.producers.KafkaProducersUtils.handleSendResult;

@Component
public class KafkaProducers {

    @Value("${kafka.topics.customers-topic-name:customers}")
    private String customersTopicName;

    @Value("${kafka.topics.accounts-topic-name:accounts}")
    private String accountsTopicName;

    @Autowired
    private KafkaTemplate<String, Customer> kafkaTemplateCustomer;

    @Autowired
    private KafkaTemplate<String, Account> kafkaTemplateAccount;

    public void publishCustomer(String customerId, Customer customer) {
        var future = kafkaTemplateCustomer.send(customersTopicName, customerId, customer);
        future.whenComplete(handleSendResult());
    }

    public void publishAccount(String customerId, Account account) {
        var future = kafkaTemplateAccount.send(accountsTopicName, customerId, account);
        future.whenComplete(handleSendResult());
    }
}
