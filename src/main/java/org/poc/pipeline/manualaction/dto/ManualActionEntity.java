package org.poc.pipeline.manualaction.dto;

public record ManualActionEntity(
            ManualActionId manualActionId,
            String orderId,
            String pipelineExecutionId,
            Integer step,
            String errorMessage,
            String cause
) {}