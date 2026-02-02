package org.example.pipeline.dto;

public record StepEntity(
        String stepId,
        String pipelineId,
        OperationName operationName,
        Integer order,
        Integer stage
) {}
