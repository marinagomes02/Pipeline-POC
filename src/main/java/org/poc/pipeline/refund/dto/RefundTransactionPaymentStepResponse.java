package org.poc.pipeline.refund.dto;

import org.poc.pipeline.order.dto.OrderAssociatedIdsAndCreationDate;
import org.poc.pipeline.order.dto.OrderLineDetails;
import org.poc.pipeline.order.dto.OrderPaymentInfo;
import org.poc.pipeline.order.dto.ReturnPublicId;
import org.poc.pipeline.refund.dto.interfaces.IRefundPaymentMethodStepRequest;
import org.poc.pipeline.refund.dto.interfaces.IRefundPointsStepRequest;

import java.util.List;
import java.util.Optional;

public record RefundTransactionPaymentStepResponse(
            OrderAssociatedIdsAndCreationDate order,
            List<OrderLineDetails> orderLines,
            OrderPaymentInfo orderPaymentInfo,
            Optional<ReturnPublicId> returnPublicId
) implements IRefundPointsStepRequest, IRefundPaymentMethodStepRequest {}