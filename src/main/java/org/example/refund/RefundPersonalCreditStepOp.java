package org.example.refund;

import org.example.order.dto.PaymentMethod;
import org.example.pipeline.StepOperation;
import org.example.refund.dto.RefundPaymentMethodStepResponse;
import org.example.refund.dto.RefundTransactionPaymentStepResponse;

public class RefundPersonalCreditStepOp implements StepOperation<RefundTransactionPaymentStepResponse, RefundPaymentMethodStepResponse> {
        @Override
        public RefundPaymentMethodStepResponse execute(RefundTransactionPaymentStepResponse input) {

            PaymentMethod paymentMethod = input.orderPaymentInfo().paymentMethod();
            if (paymentMethod != PaymentMethod.PERSONAL_CREDIT) {
                System.out.println("Skipping personal credit refund - payment method is: " + paymentMethod);
                return transformForSkip(input);
            }

            // Perform actual personal credit refund logic here

            return transformForSkip(input);
        }

        @Override
        public RefundPaymentMethodStepResponse transformForSkip(RefundTransactionPaymentStepResponse input) {
            return new RefundPaymentMethodStepResponse(
                    input.order(),
                    input.orderLines(),
                    input.returnPublicId());
        }
    }