package org.example.pipeline;

public record StepDefinition(
        String operationName,
        Integer order,
        Integer stage
) {}