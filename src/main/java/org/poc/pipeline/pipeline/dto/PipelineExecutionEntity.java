package org.poc.pipeline.pipeline.dto;

public record PipelineExecutionEntity(
        String executionId,
        String pipelineId,
        String pipelineHash,
        Integer nextStep
) {
}
