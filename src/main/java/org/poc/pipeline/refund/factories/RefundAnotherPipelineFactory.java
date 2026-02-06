package org.poc.pipeline.refund.factories;

import org.poc.pipeline.pipeline.Pipeline;
import org.poc.pipeline.pipeline.dto.PipelineFactory;
import org.poc.pipeline.pipeline.dto.Step;
import org.poc.pipeline.refund.RefundPaymentStepOp;
import org.poc.pipeline.refund.RefundPersonalCreditStepOp;
import org.poc.pipeline.refund.dto.RefundOperationName;
import org.poc.pipeline.refund.dto.RefundPaymentMethodStepResponse;
import org.poc.pipeline.refund.dto.interfaces.IRefundTransactionStepRequest;


public class RefundAnotherPipelineFactory implements PipelineFactory<IRefundTransactionStepRequest, RefundPaymentMethodStepResponse> {

    private final String pipelineId = "2816628c-e0ea-40a9-9ee4-3c7ff80539ca";
    private final String pipelineHash = "add29b4f3c5070e3647dbcb9653ca684418713806d0cc62e95da87448b5f5200";

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
                .build(pipelineId, pipelineHash);
    }

    @Override
    public String getPipelineHash() {
        return pipelineHash;
    }
}
