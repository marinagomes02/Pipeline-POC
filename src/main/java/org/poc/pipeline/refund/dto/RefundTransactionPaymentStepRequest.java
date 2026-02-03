package org.poc.pipeline.refund.dto;

import org.poc.pipeline.order.dto.OrderAssociatedIdsAndCreationDate;
import org.poc.pipeline.order.dto.OrderLineDetails;
import org.poc.pipeline.order.dto.ReturnPublicId;

import java.util.List;
import java.util.Optional;

public record RefundTransactionPaymentStepRequest(
            OrderAssociatedIdsAndCreationDate order,
            List<OrderLineDetails> orderLines,
            Optional<ReturnPublicId> returnPublicId
    ) {}