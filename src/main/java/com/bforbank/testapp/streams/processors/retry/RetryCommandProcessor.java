package com.bforbank.testapp.streams.processors.retry;

import com.bforbank.demo.CardOrder;
import com.bforbank.demo.RetryAggregate;
import com.bforbank.demo.RetryCommand;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Duration;

public class RetryCommandProcessor implements Processor<String, RetryCommand, String, CardOrder> {

    private final String retryAggregateStoreName;
    private KeyValueStore<String, RetryAggregate> retryAggregateStore;

    public RetryCommandProcessor(String retryAggregateStoreName) {
        this.retryAggregateStoreName = retryAggregateStoreName;
    }

    @Override
    public void init(ProcessorContext<String, CardOrder> context) {
        this.retryAggregateStore = context.getStateStore(retryAggregateStoreName);
        scheduleRetry(context);
    }

    private void scheduleRetry(ProcessorContext<String, CardOrder> context) {
        context.schedule(Duration.ofSeconds(30), PunctuationType.WALL_CLOCK_TIME, timestamp -> {
            try (final KeyValueIterator<String, RetryAggregate> iter = retryAggregateStore.all()) {
                while (iter.hasNext()) {
                    final KeyValue<String, RetryAggregate> entry = iter.next();
                    context.forward(new Record<>(entry.value.getKey(), entry.value.getValue(), timestamp));
                    retryAggregateStore.delete(entry.key);
                }
            }
        });
    }

    @Override
    public void process(Record<String, RetryCommand> record) {
        CardOrder toRetry = record.value().getToRetry();
        RetryAggregate existingRetryAggregate = retryAggregateStore.get(toRetry.getRequestId());
        if (existingRetryAggregate == null) {
            retryAggregateStore.put(toRetry.getRequestId(), new RetryAggregate(toRetry, record.key()));
        }
    }
}