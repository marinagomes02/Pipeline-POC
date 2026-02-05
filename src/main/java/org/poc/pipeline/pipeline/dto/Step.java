package org.poc.pipeline.pipeline.dto;

public record Step<I, O>(
        StepOperation<I, O> operation,
        String operationName,
        Integer position,
        Integer stage
) {}