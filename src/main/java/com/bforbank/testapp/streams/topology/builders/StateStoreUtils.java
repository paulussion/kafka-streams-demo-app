package com.bforbank.testapp.streams.topology.builders;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

public class StateStoreUtils {

    public static <K, V> StoreBuilder<KeyValueStore<K, V>> buildKeyValueStore(
            final String storeName,
            final Serde<K> keySerde,
            final Serde<V> valueSerde
    ) {
        return Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(storeName),
                keySerde,
                valueSerde);
    }
}
