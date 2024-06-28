package com.bforbank.testapp.streams.serdes;

import com.bforbank.demo.*;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.apache.kafka.streams.StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG;


/**
 * Utility class for managing Kafka Serdes (Serializer/Deserializer) used in the application.
 * Provides Serde instances for various domain-specific classes.
 * The class initializes Serde instances for different data types used in the application
 * and configures them based on the provided Kafka properties.
 * All Serdes used in the application are centralized here.
 */
@Component
public record AppSerdes(
        Serde<Customer> customerSerde,
        Serde<Account> accountSerde,
        Serde<AccountAggregation> accountAggregationSerde,
        Serde<CustomerWithAccounts> customerWithAccountsSerde
) {
    /**
     * Constructor that initializes Serde instances for all needed classes
     * based on the provided Kafka properties.
     *
     * @param kafkaProperties The Kafka properties used for configuring Serdes.
     */
    @Autowired
    public AppSerdes(final KafkaProperties kafkaProperties) {
        this(
                SerdeFactory.ofValue(buildSerdePropertyMap(kafkaProperties)),
                SerdeFactory.ofValue(buildSerdePropertyMap(kafkaProperties)),
                SerdeFactory.ofValue(buildSerdePropertyMap(kafkaProperties)),
                SerdeFactory.ofValue(buildSerdePropertyMap(kafkaProperties))
        );
    }

    /**
     * Builds a Serde configuration map based on the provided Kafka properties.
     *
     * @param kafkaProperties The Kafka properties used for configuring Serdes.
     * @return A map containing Serde configuration properties.
     */
    private static Map<String, String> buildSerdePropertyMap(KafkaProperties kafkaProperties) {
        Map<String, String> result = kafkaProperties.getStreams().getProperties();
        // Set the default deserialization exception handler
        result.put(DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class.getName());
        return result;
    }
}
