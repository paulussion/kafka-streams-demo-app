package com.bforbank.testapp.mock.data.producers;

import com.bforbank.demo.Account;
import com.bforbank.demo.CardOrderReply;
import com.bforbank.demo.Customer;
import com.bforbank.demo.Transaction;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducersConfig {
    private final KafkaProperties kafkaProperties;

    public KafkaProducersConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public KafkaTemplate<String, Transaction> kafkaTemplateTransaction() {
        Map<String, Object> props = getStringKeyAvroValueConfig();
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    @Bean
    public KafkaTemplate<String, Customer> kafkaTemplateCustomer() {
        Map<String, Object> props = getStringKeyAvroValueConfig();
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    @Bean
    public KafkaTemplate<String, Account> kafkaTemplateAccount() {
        Map<String, Object> props = getStringKeyAvroValueConfig();
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    @Bean
    public KafkaTemplate<String, CardOrderReply> kafkaTemplateCardOrderReply() {
        Map<String, Object> props = getStringKeyAvroValueConfig();
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    private Map<String, Object> getStringKeyAvroValueConfig() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        return props;
    }
}
