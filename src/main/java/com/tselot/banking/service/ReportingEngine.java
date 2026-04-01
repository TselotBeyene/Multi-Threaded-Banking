package com.tselot.banking.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ReportingEngine<T> {
    public String generate(List<T> rows, Function<T, String> mapper) {
        return rows.stream().map(mapper).collect(Collectors.joining(System.lineSeparator()));
    }
}
