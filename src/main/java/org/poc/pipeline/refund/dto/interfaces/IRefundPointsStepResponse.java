package org.poc.pipeline.refund.dto.interfaces;

import org.poc.pipeline.order.dto.OrderAssociatedIdsAndCreationDate;
import org.poc.pipeline.order.dto.OrderLineDetails;
import org.poc.pipeline.order.dto.ReturnPublicId;

import java.util.List;
import java.util.Optional;

public interface IRefundPointsStepResponse {
            Optional<ReturnPublicId> returnPublicId();
            List<OrderLineDetails> orderLines();
            OrderAssociatedIdsAndCreationDate order();
}