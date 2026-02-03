package org.example.refund;

import org.example.order.OrderPaymentInfoRepo;
import org.example.order.dto.OrderPaymentInfo;
import org.example.pipeline.StepOperation;
import org.example.pipeline.exceptions.StepOperationExecutionError;
import org.example.refund.dto.RefundTransactionPaymentStepRequest;
import org.example.refund.dto.RefundTransactionPaymentStepResponse;

public class RefundPaymentStepOp implements StepOperation<RefundTransactionPaymentStepRequest, RefundTransactionPaymentStepResponse> {
    @Override
    public RefundTransactionPaymentStepResponse execute(RefundTransactionPaymentStepRequest input) {
        try {
            OrderPaymentInfo paymentInfo = getOrderPaymentInfo(input);
            // Perform actual refund payment logic here
            return new RefundTransactionPaymentStepResponse(
                input.order(),
                input.orderLines(),
                paymentInfo,
                input.returnPublicId()
            );
        } catch (Exception e) {
            throw new StepOperationExecutionError(e.getMessage());
        }
    }

    @Override
    public RefundTransactionPaymentStepResponse transformForSkip(RefundTransactionPaymentStepRequest input) {
        return new RefundTransactionPaymentStepResponse(
                input.order(),
                input.orderLines(),
                getOrderPaymentInfo(input),
                input.returnPublicId());
    }

    @Override
    public void rollback(RefundTransactionPaymentStepResponse output) {
        System.out.println("Rollback called for refund payment step for order: " + output.order().orderId() + ", requires manual intervention");
    }

    private OrderPaymentInfo getOrderPaymentInfo(RefundTransactionPaymentStepRequest input) {
        return OrderPaymentInfoRepo.get(input.order().orderId());
    }
}