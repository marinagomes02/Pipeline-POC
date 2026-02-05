package org.poc.pipeline.refund;

import org.poc.pipeline.pipeline.dto.StepOperation;
import org.poc.pipeline.refund.dto.RefundPaymentMethodStepResponse;
import org.poc.pipeline.refund.dto.RefundPointsStepResponse;
import org.poc.pipeline.refund.dto.interfaces.IRefundPointsStepRequest;
import org.poc.pipeline.refund.dto.interfaces.IRefundPointsStepResponse;

public class RefundPointsStepOp implements StepOperation<IRefundPointsStepRequest, RefundPointsStepResponse> {
    @Override
    public RefundPointsStepResponse execute(IRefundPointsStepRequest input) {

        // Perform actual points refund logic here

        return transformForSkip(input);
    }

    @Override
    public RefundPointsStepResponse transformForSkip(IRefundPointsStepRequest input) {
        return new RefundPointsStepResponse(
                input.returnPublicId(),
                input.orderLines(),
                input.order()
        );
    }

    @Override
    public void rollback(RefundPointsStepResponse output) {
        System.out.println("Rollback called for refund points step for order: " + output.order().orderId() + ", requires manual intervention");
    }
}