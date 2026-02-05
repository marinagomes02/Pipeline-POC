package org.poc.pipeline.refund;

import org.poc.pipeline.manualaction.RegisterManualActionOp;
import org.poc.pipeline.manualaction.dto.ManualActionEntity;
import org.poc.pipeline.manualaction.dto.ManualActionRepo;
import org.poc.pipeline.order.OrderLineStatusRepo;
import org.poc.pipeline.order.dto.OrderLineStatus;
import org.poc.pipeline.pipeline.Pipeline;
import org.poc.pipeline.pipeline.PipelineExecutionRepo;
import org.poc.pipeline.pipeline.dto.PipelineExecutionEntity;
import org.poc.pipeline.pipeline.exceptions.PipelineExecutionError;
import org.poc.pipeline.refund.dto.RefundOperationName;
import org.poc.pipeline.refund.dto.RefundPointsStepResponse;
import org.poc.pipeline.refund.dto.RefundTransactionPaymentStepRequest;
import org.poc.pipeline.refund.dto.interfaces.IRefundTransactionStepRequest;
import org.poc.pipeline.refund.factories.RefundCompletePipelineFactory;

import java.util.Optional;
import java.util.UUID;

public class RefundWithoutManualActionOp {

    public RefundPointsStepResponse processRefund(RefundTransactionPaymentStepRequest request) {
        Pipeline<IRefundTransactionStepRequest, RefundPointsStepResponse> pipeline = new RefundCompletePipelineFactory().create();

        try {
            return pipeline.execute(request);
        } catch (PipelineExecutionError e) {
            System.out.println("Pipeline execution failed: " + e.getErrorInfo().message());
            handlePipelineError(e, pipeline, request.order().orderId());
            throw e;
        }
    }

    private void handlePipelineError(PipelineExecutionError e, Pipeline<?, ?> pipeline, String orderId) {
        RefundOperationName operationName = RefundOperationName.from(e.getErrorInfo().operationName());

        if (operationName == RefundOperationName.REFUND_PAYMENT || operationName == RefundOperationName.REFUND_PERSONAL_CREDIT) {
            new RegisterManualActionOp().execute(
                    orderId,
                    pipeline.pipelineId(),
                    e.getErrorInfo().step(),
                    pipeline.getNextStepNumberAfterStage(e.getErrorInfo().stage()),
                    e.getErrorInfo().message(),
                    operationName.value());
        }
    }
}
