package org.poc.pipeline.pipeline;

public record StepDefinition(
        String operationName,
        Integer order,
        Integer stage
) {}