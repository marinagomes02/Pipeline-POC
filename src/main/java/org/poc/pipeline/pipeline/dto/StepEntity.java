package org.poc.pipeline.pipeline.dto;

public record StepEntity(
        String stepId,
        String pipelineId,
        String operationName,
        Integer order,
        Integer stage
) {}
