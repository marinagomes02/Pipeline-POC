package org.example.order.dto;

import org.example.manualaction.dto.ManualActionId;

import java.util.Optional;

public record OrderLineStatus(
        Status status,
        Optional<ManualActionId> manualActionId
) {}