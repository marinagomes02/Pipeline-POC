package org.poc.pipeline.refund.dto;

import org.poc.pipeline.order.dto.OrderAssociatedIdsAndCreationDate;
import org.poc.pipeline.order.dto.OrderLineDetails;
import org.poc.pipeline.order.dto.ReturnPublicId;
import org.poc.pipeline.refund.dto.interfaces.IRefundPointsStepRequest;

import java.util.List;
import java.util.Optional;

public record RefundPaymentMethodStepResponse(
            OrderAssociatedIdsAndCreationDate order,
            List<OrderLineDetails> orderLines,
            Optional<ReturnPublicId> returnPublicId
) implements IRefundPointsStepRequest {}