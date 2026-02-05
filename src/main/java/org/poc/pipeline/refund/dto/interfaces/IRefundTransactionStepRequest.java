package org.poc.pipeline.refund.dto.interfaces;

import org.poc.pipeline.order.dto.OrderAssociatedIdsAndCreationDate;
import org.poc.pipeline.order.dto.OrderLineDetails;
import org.poc.pipeline.order.dto.ReturnPublicId;

import java.util.List;
import java.util.Optional;

public interface IRefundTransactionStepRequest {
    OrderAssociatedIdsAndCreationDate order();
    List<OrderLineDetails> orderLines();
    Optional<ReturnPublicId> returnPublicId();
}
