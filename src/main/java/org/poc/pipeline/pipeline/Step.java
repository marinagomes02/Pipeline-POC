package org.poc.pipeline.pipeline;

public record Step(
        Integer order,
        Integer stage,
        String operationName,
        StepOperation<?, ?> operation
) {}