package com.bforbank.testapp.view.connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;

import static com.bforbank.testapp.view.connect.JdbcSinkConnectorHelper.*;

@Component
public class ConnectorsConfigurer {
    private static final Logger log = LoggerFactory.getLogger(com.bforbank.testapp.view.connect.ConnectorsConfigurer.class);

    @Value("${kafka-connector-settings.url}")
    private String kafkaConnectUrl;

    private final RestClient restClient;

    private ConnectorsConfigurer() {
        restClient = RestClient.create(kafkaConnectUrl);
    }

    //@Bean
    //@DependsOnDatabaseInitialization
    Boolean createBeneficiaryConnector(
            KafkaConnectProperties props
    ) {
        String configName = props.instantPaymentMetricsConnector().name();
        log.info("Creating Kafka Connect source connector with name:{}", configName);

        HashMap<String, String> config = getBaseConnectorConfig(props.db());
        config.put(TASKS_MAX, "1");
        config.put(TOPICS, props.instantPaymentMetricsConnector().topic());
        config.put(TABLE_NAME_FORMAT, "instant_payment_metrics");
        config.put(PK_MODE, RECORD_KEY);
        config.put(PK_FIELDS, "accountId");
        config.put(INSERT_MODE, UPSERT);

        return restClient.put()
                .uri("/connectors/{configName}/config", configName)
                .body(config)
                .exchange((_, response) -> {
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        throw new IllegalStateException("Unexpected response code while creating connector : " + response);
                    }
                    return true;
                });
    }
}
