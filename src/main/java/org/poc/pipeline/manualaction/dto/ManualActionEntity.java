package org.poc.pipeline.manualaction.dto;

public record ManualActionEntity(
            ManualActionId manualActionId,
            String orderId,
            String pipelineId,
            Integer step,
            Integer nextStep,
            String errorMessage,
            String cause
) {}