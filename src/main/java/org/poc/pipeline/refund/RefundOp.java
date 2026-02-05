package org.poc.pipeline.refund;

import org.poc.pipeline.manualaction.RegisterManualActionOp;
import org.poc.pipeline.manualaction.dto.ManualActionEntity;
import org.poc.pipeline.manualaction.dto.ManualActionRepo;
import org.poc.pipeline.order.OrderLineStatusRepo;
import org.poc.pipeline.order.dto.OrderLineStatus;
import org.poc.pipeline.pipeline.Pipeline;
import org.poc.pipeline.pipeline.exceptions.PipelineExecutionError;
import org.poc.pipeline.refund.dto.RefundOperationName;
import org.poc.pipeline.refund.dto.RefundPointsStepResponse;
import org.poc.pipeline.refund.dto.RefundTransactionPaymentStepRequest;
import org.poc.pipeline.refund.dto.interfaces.IRefundTransactionStepRequest;
import org.poc.pipeline.refund.factories.RefundCompletePipelineFactory;

import java.util.Optional;

public class RefundOp {

    public RefundPointsStepResponse processRefund(RefundTransactionPaymentStepRequest request) {
        RefundWithoutManualActionOp refundWithoutManualActionOp = new RefundWithoutManualActionOp();
        RefundWithManualActionOp refundWithManualActionOp = new RefundWithManualActionOp();

        String orderId = request.order().orderId();
        OrderLineStatus orderLineStatus = OrderLineStatusRepo.get(orderId);

        Optional<ManualActionEntity> manualAction = orderLineStatus.manualActionId().flatMap(ManualActionRepo::get);

        return manualAction
                .map(action -> refundWithManualActionOp.processRefund(request, action))
                .orElseGet(() -> refundWithoutManualActionOp.processRefund(request));
    }
}
