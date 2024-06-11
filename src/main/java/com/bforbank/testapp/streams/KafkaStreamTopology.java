package com.bforbank.testapp.streams;

import com.bforbank.demo.CardOrderReply;
import com.bforbank.testapp.streams.serdes.AppSerdes;
import com.bforbank.testapp.streams.topology.builders.BusinessLogicTopologyBuilder;
import com.bforbank.testapp.streams.topology.builders.BusinessMetricsTopologyBuilder;
import com.bforbank.testapp.streams.topology.builders.MaterializedViewTopologyBuilder;
import com.bforbank.testapp.streams.topology.builders.SchedulerTopologyBuilder;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaStreamTopology {

    private static final Logger log = LoggerFactory.getLogger(KafkaStreamTopology.class);

    @Autowired
    BusinessMetricsTopologyBuilder businessMetricsTopologyBuilder;
    @Autowired
    MaterializedViewTopologyBuilder materializedViewTopologyBuilder;
    @Autowired
    BusinessLogicTopologyBuilder businessLogicTopologyBuilder;
    @Autowired
    SchedulerTopologyBuilder schedulerTopologyBuilder;

    @Bean
    public Topology mainTopology(StreamsBuilder builder) {


        businessMetricsTopologyBuilder.buildInstantPaymentMetricsTopology(builder);

        // materializedViewTopologyBuilder.buildCustomerWithAccountsTopology(builder);

        //KStream<String, CardOrderReply> cardOrderReplyKStream =
        //        businessLogicTopologyBuilder.buildCardCommandLogicTopology(builder);

        //schedulerTopologyBuilder.buildErrorHandlingSchedulerTopology(builder, cardOrderReplyKStream);

        Topology topology = builder.build();
        log.debug("Here is my topology definition :\n{}", topology.describe().toString());
        return topology;
    }



}
