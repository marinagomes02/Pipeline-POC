package org.poc.pipeline.refund;

import org.poc.pipeline.pipeline.StepOperation;
import org.poc.pipeline.refund.dto.RefundPaymentMethodStepResponse;
import org.poc.pipeline.refund.dto.RefundPointsStepResponse;

public class RefundPointsStepOp implements StepOperation<RefundPaymentMethodStepResponse, RefundPointsStepResponse> {
    @Override
    public RefundPointsStepResponse execute(RefundPaymentMethodStepResponse input) {

        // Perform actual points refund logic here

        return transformForSkip(input);
    }

    @Override
    public RefundPointsStepResponse transformForSkip(RefundPaymentMethodStepResponse input) {
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