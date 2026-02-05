package org.poc.pipeline.refund.factories;

import org.poc.pipeline.pipeline.Pipeline;
import org.poc.pipeline.pipeline.dto.PipelineFactory;
import org.poc.pipeline.pipeline.dto.Step;
import org.poc.pipeline.refund.RefundPaymentStepOp;
import org.poc.pipeline.refund.RefundPersonalCreditStepOp;
import org.poc.pipeline.refund.RefundPointsStepOp;
import org.poc.pipeline.refund.dto.RefundOperationName;
import org.poc.pipeline.refund.dto.RefundPointsStepResponse;
import org.poc.pipeline.refund.dto.interfaces.IRefundTransactionStepRequest;


public class RefundCompletePipelineFactory implements PipelineFactory<IRefundTransactionStepRequest, RefundPointsStepResponse> {

    private final String pipelineId = "7355ab88-4b17-4a42-9ba3-804f018715d1";

    @Override
    public Pipeline<IRefundTransactionStepRequest, RefundPointsStepResponse> create() {
        return Pipeline
                .builder(new Step<>(
                        new RefundPaymentStepOp(),
                        RefundOperationName.REFUND_PAYMENT.value(),
                        0))
                .pipe(new Step<>(
                        new RefundPersonalCreditStepOp(),
                        RefundOperationName.REFUND_PERSONAL_CREDIT.value(),
                        0))
                .pipe(new Step<>(
                        new RefundPointsStepOp(),
                        RefundOperationName.REFUND_POINTS.value(),
                        1))
                .build(pipelineId);
    }
}
