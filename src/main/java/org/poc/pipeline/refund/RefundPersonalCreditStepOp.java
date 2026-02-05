package org.poc.pipeline.refund;

import org.poc.pipeline.order.dto.PaymentMethod;
import org.poc.pipeline.pipeline.dto.StepOperation;
import org.poc.pipeline.pipeline.exceptions.StepOperationExecutionError;
import org.poc.pipeline.refund.dto.RefundPaymentMethodStepResponse;
import org.poc.pipeline.refund.dto.RefundTransactionPaymentStepResponse;
import org.poc.pipeline.refund.dto.interfaces.IRefundPaymentMethodStepRequest;

public class RefundPersonalCreditStepOp implements StepOperation<IRefundPaymentMethodStepRequest, RefundPaymentMethodStepResponse> {
        @Override
        public RefundPaymentMethodStepResponse execute(IRefundPaymentMethodStepRequest input) {

            PaymentMethod paymentMethod = input.orderPaymentInfo().paymentMethod();
            throw new StepOperationExecutionError("Personal credit refund step is not implemented yet");
            /*if (paymentMethod != PaymentMethod.PERSONAL_CREDIT) {
                System.out.println("Skipping personal credit refund - payment method is: " + paymentMethod);
                return transformForSkip(input);
            }

            // Perform actual personal credit refund logic here

            return transformForSkip(input);    */
        }

        @Override
        public RefundPaymentMethodStepResponse transformForSkip(IRefundPaymentMethodStepRequest input) {
            return new RefundPaymentMethodStepResponse(
                    input.order(),
                    input.orderLines(),
                    input.returnPublicId());
        }

        @Override
        public void rollback(RefundPaymentMethodStepResponse output) {
            System.out.println("Rollback called for personal credit refund step for order: " + output.order().orderId() + ", requires manual intervention");
        }
    }