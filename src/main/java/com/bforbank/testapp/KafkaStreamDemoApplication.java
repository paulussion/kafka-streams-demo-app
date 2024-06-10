package com.bforbank.testapp;

import com.bforbank.testapp.view.connect.KafkaConnectProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafka
@EnableKafkaStreams
@EnableConfigurationProperties(KafkaConnectProperties.class)
@SpringBootApplication
public class KafkaStreamDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamDemoApplication.class, args);
	}
}
