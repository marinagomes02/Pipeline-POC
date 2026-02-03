package org.example.pipeline;

public record Step(
        Integer order,
        Integer stage,
        String operationName,
        StepOperation<?, ?> operation
) {}