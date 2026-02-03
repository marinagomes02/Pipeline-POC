package org.example.pipeline.dto;

public record StepEntity(
        String stepId,
        String pipelineId,
        String operationName,
        Integer order,
        Integer stage
) {}
