package com.bforbank.testapp.mock.data.producers;

import com.bforbank.demo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.bforbank.testapp.mock.data.producers.KafkaProducersUtils.handleSendResult;

@Component
public class KafkaProducers {

    @Value("${kafka.topics.transactions-topic-name:transactions}")
    private String transactionsTopicName;

    @Value("${kafka.topics.customers-topic-name:customers}")
    private String customersTopicName;

    @Value("${kafka.topics.accounts-topic-name:accounts}")
    private String accountsTopicName;

    @Value("${kafka.topics.card-orders-reply-topic-name:card-orders-reply}")
    private String cardOrdersReplyTopicName;

    @Autowired
    private KafkaTemplate<String, Transaction> kafkaTemplateTransaction;

    @Autowired
    private KafkaTemplate<String, Customer> kafkaTemplateCustomer;

    @Autowired
    private KafkaTemplate<String, Account> kafkaTemplateAccount;

    @Autowired
    private KafkaTemplate<String, CardOrderReply> kafkaTemplateCardOrderReply;

    public void publishTransaction(String accountId, Transaction transaction) {
        var future = kafkaTemplateTransaction.send(transactionsTopicName, accountId, transaction);
        future.whenComplete(handleSendResult());
    }

    public void publishCustomer(String customerId, Customer customer) {
        var future = kafkaTemplateCustomer.send(customersTopicName, customerId, customer);
        future.whenComplete(handleSendResult());
    }

    public void publishAccount(String customerId, Account account) {
        var future = kafkaTemplateAccount.send(accountsTopicName, customerId, account);
        future.whenComplete(handleSendResult());
    }

    public void publishCardOrderReply(String customerId, CardOrderReply cardOrderReply) {
        var future = kafkaTemplateCardOrderReply.send(cardOrdersReplyTopicName, customerId, cardOrderReply);
        future.whenComplete(handleSendResult());
    }
}
