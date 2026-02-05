package org.poc.pipeline.refund.factories;

import org.poc.pipeline.pipeline.Pipeline;
import org.poc.pipeline.pipeline.dto.PipelineFactory;
import org.poc.pipeline.pipeline.dto.Step;
import org.poc.pipeline.refund.RefundPaymentStepOp;
import org.poc.pipeline.refund.RefundPersonalCreditStepOp;
import org.poc.pipeline.refund.RefundPointsStepOp;
import org.poc.pipeline.refund.dto.RefundOperationName;
import org.poc.pipeline.refund.dto.RefundPaymentMethodStepResponse;
import org.poc.pipeline.refund.dto.RefundPointsStepResponse;
import org.poc.pipeline.refund.dto.interfaces.IRefundTransactionStepRequest;


public class RefundAnotherPipelineFactory implements PipelineFactory<IRefundTransactionStepRequest, RefundPaymentMethodStepResponse> {

    private final String pipelineId = "2816628c-e0ea-40a9-9ee4-3c7ff80539ca";

    @Override
    public Pipeline<IRefundTransactionStepRequest, RefundPaymentMethodStepResponse> create() {
        return Pipeline
                .builder(new Step<>(
                        new RefundPaymentStepOp(),
                        RefundOperationName.REFUND_PAYMENT.value(),
                        0,
                        0))
                .pipe(new Step<>(
                        new RefundPersonalCreditStepOp(),
                        RefundOperationName.REFUND_PERSONAL_CREDIT.value(),
                        1,
                        1))
                .build(pipelineId);
    }
}
