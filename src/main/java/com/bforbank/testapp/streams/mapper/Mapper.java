package com.bforbank.testapp.streams.mapper;

import java.util.function.Function;

public interface Mapper<A, B> extends Function<A, B> {
    @Override
    default B apply(final A a) {
        return from(a);
    }

    B from(final A a);
}
