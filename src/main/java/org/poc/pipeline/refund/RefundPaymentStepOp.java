package org.poc.pipeline.refund;

import org.poc.pipeline.order.OrderPaymentInfoRepo;
import org.poc.pipeline.order.dto.OrderPaymentInfo;
import org.poc.pipeline.pipeline.dto.StepOperation;
import org.poc.pipeline.pipeline.exceptions.StepOperationExecutionError;
import org.poc.pipeline.refund.dto.RefundTransactionPaymentStepResponse;
import org.poc.pipeline.refund.dto.interfaces.IRefundTransactionStepRequest;

public class RefundPaymentStepOp implements StepOperation<IRefundTransactionStepRequest, RefundTransactionPaymentStepResponse> {

    @Override
    public RefundTransactionPaymentStepResponse execute(IRefundTransactionStepRequest input) {
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
    public RefundTransactionPaymentStepResponse transformForSkip(IRefundTransactionStepRequest input) {
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

    private OrderPaymentInfo getOrderPaymentInfo(IRefundTransactionStepRequest input) {
        return OrderPaymentInfoRepo.get(input.order().orderId());
    }
}