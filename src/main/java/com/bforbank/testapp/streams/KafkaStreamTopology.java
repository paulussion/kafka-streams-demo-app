package com.bforbank.testapp.streams;

import com.bforbank.testapp.streams.topology.builders.MaterializedViewTopologyBuilder;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaStreamTopology {

    private static final Logger log = LoggerFactory.getLogger(KafkaStreamTopology.class);

    @Autowired
    MaterializedViewTopologyBuilder materializedViewTopologyBuilder;

    @Bean
    public Topology mainTopology(StreamsBuilder builder) {


        //businessMetricsTopologyBuilder.buildInstantPaymentMetricsTopology(builder);

        materializedViewTopologyBuilder.buildCustomerWithAccountsTopology(builder);

        /*
        KStream<String, CardOrderReply> cardOrderReplyKStream =
                businessLogicTopologyBuilder.buildCardCommandLogicTopology(builder);

        schedulerTopologyBuilder.buildErrorHandlingSchedulerTopology(builder, cardOrderReplyKStream);
        */
        Topology topology = builder.build();
        log.debug("Here is my topology definition :\n{}", topology.describe().toString());
        return topology;
    }
}
