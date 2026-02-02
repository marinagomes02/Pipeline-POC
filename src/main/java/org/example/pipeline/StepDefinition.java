package org.example.pipeline;

import org.example.pipeline.dto.OperationName;

public record StepDefinition(
        OperationName operationName,
        Integer order,
        Integer stage
) {}