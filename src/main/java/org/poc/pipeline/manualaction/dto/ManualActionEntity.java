package org.poc.pipeline.manualaction.dto;

public record ManualActionEntity(
            ManualActionId manualActionId,
            String orderId,
            String pipelineId,
            Integer stage,
            Integer nextStage,
            String errorMessage,
            String cause
    ) {}