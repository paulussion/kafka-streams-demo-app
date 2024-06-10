package com.bforbank.testapp.view.connect;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "kafka-connector-settings")
public record KafkaConnectProperties(
        String url,
        @NestedConfigurationProperty
        DatabaseConnection db,
        @NestedConfigurationProperty
        Connector instantPaymentMetricsConnector
) {
        public record DatabaseConnection(
                String host,
                String name,
                String user,
                String password
        ) {
        }
        public record Connector(
                String topic,
                String name
        ) {
        }
}
