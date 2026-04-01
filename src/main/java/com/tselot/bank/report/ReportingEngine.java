package com.tselot.bank.report;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ReportingEngine<T> {
    public String generate(Collection<T> source, Function<T, String> mapper) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(mapper);
        return source.stream().map(mapper).collect(Collectors.joining(System.lineSeparator()));
    }
}
