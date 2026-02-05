package org.poc.pipeline.pipeline.dto;

public record PipelineExecutionEntity(
        String executionId,
        String pipelineId,
        Integer nextStep
) {
}
