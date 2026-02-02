package org.example.refund.dto;

import org.example.order.dto.OrderAssociatedIdsAndCreationDate;
import org.example.order.dto.OrderLineDetails;
import org.example.order.dto.OrderPaymentInfo;
import org.example.order.dto.ReturnPublicId;

import java.util.List;
import java.util.Optional;

public record RefundTransactionPaymentStepResponse(
            OrderAssociatedIdsAndCreationDate order,
            List<OrderLineDetails> orderLines,
            OrderPaymentInfo orderPaymentInfo,
            Optional<ReturnPublicId> returnPublicId
) {}