package org.example.manualaction.dto;

public record ManualActionEntity(
            ManualActionId manualActionId,
            String orderId,
            String pipelineId,
            Integer stage,
            String errorMessage,
            ManualActionCause cause
    ) {}