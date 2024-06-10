package com.bforbank.testapp.streams.serdes;

import io.confluent.kafka.serializers.subject.TopicNameStrategy;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serde;

import java.util.Map;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.KEY_SUBJECT_NAME_STRATEGY;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY;

/**
 * Factory class for creating Kafka Serdes (Serializer/Deserializer) for Avro specific records.
 * This class provides a static method for generating a Serde based on the provided parameters.
 * The SerdeFactory simplifies the configuration process by allowing customization of
 * serialization and deserialization strategies, such as naming strategies, for Avro records.
 */
public final class SerdeFactory {

    /**
     * Creates a Serde for Avro specific records with the specified configuration.
     *
     * @param serdesConfig Configuration map for the Serde.
     * @param isKey        Indicates whether the Serde is for keys or values.
     * @param subjectStrategy The strategy for naming Avro subjects.
     * @param <T>          The type of the Avro specific record.
     * @return A configured Serde for Avro specific records.
     */
    private static <T extends SpecificRecord> Serde<T> of(
            final Map<String, String> serdesConfig,
            final boolean isKey,
            final String subjectStrategy) {
        // Set the naming strategy for Avro subjects in the configuration

        serdesConfig.put(isKey ? KEY_SUBJECT_NAME_STRATEGY : VALUE_SUBJECT_NAME_STRATEGY, subjectStrategy);

        // Create a SpecificAvroSerde and configure it with the provided parameters
        final SpecificAvroSerde<T> serde = new SpecificAvroSerde<>();
        serde.configure(serdesConfig, isKey);

        // Return the configured Serde
        return serde;
    }

    /**
     * Creates a Serde for Avro specific records with the specified configuration.
     *
     * @param serdesConfig Configuration map for the Serde.
     * @param <T>          The type of the Avro specific record.
     * @return A configured Serde for Avro specific records.
     */
    public static <T extends SpecificRecord> Serde<T> ofKey(
            final Map<String, String> serdesConfig) {
        return of(serdesConfig, true, TopicNameStrategy.class.getName());
    }

    /**
     * Creates a Serde for Avro specific records with the specified configuration.
     *
     * @param serdesConfig Configuration map for the Serde.
     * @param subjectStrategy The strategy for naming Avro subjects.
     * @param <T>          The type of the Avro specific record.
     * @return A configured Serde for Avro specific records.
     */
    public static <T extends SpecificRecord> Serde<T> ofKeyWithCustomSubjectStrategy(
            final Map<String, String> serdesConfig,
            final String subjectStrategy) {
        return of(serdesConfig, true, subjectStrategy);
    }

    /**
     * Creates a Serde for Avro specific records with the specified configuration.
     *
     * @param serdesConfig Configuration map for the Serde.
     * @param <T>          The type of the Avro specific record.
     * @return A configured Serde for Avro specific records.
     */
    public static <T extends SpecificRecord> Serde<T> ofValue(
            final Map<String, String> serdesConfig) {
        return of(serdesConfig, false, TopicNameStrategy.class.getName());
    }

    /**
     * Creates a Serde for Avro specific records with the specified configuration.
     *
     * @param serdesConfig Configuration map for the Serde.
     * @param subjectStrategy The strategy for naming Avro subjects.
     * @param <T>          The type of the Avro specific record.
     * @return A configured Serde for Avro specific records.
     */
    public static <T extends SpecificRecord> Serde<T> ofValueWithCustomSubjectStrategy(
            final Map<String, String> serdesConfig,
            final String subjectStrategy) {
        return of(serdesConfig, false, subjectStrategy);
    }
}
