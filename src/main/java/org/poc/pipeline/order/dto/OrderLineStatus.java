package org.poc.pipeline.order.dto;

import org.poc.pipeline.manualaction.dto.ManualActionId;

import java.util.Optional;

public record OrderLineStatus(
        Status status,
        Optional<ManualActionId> manualActionId
) {}