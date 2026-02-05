package org.poc.pipeline.refund;

import org.poc.pipeline.manualaction.RegisterManualActionOp;
import org.poc.pipeline.manualaction.dto.ManualActionEntity;
import org.poc.pipeline.pipeline.Pipeline;
import org.poc.pipeline.pipeline.exceptions.PipelineExecutionError;
import org.poc.pipeline.refund.dto.RefundOperationName;
import org.poc.pipeline.refund.dto.RefundPointsStepResponse;
import org.poc.pipeline.refund.dto.RefundTransactionPaymentStepRequest;
import org.poc.pipeline.refund.dto.interfaces.IRefundTransactionStepRequest;
import org.poc.pipeline.refund.factories.RefundCompletePipelineFactory;

public class RefundWithManualActionOp {

    public RefundPointsStepResponse processRefund(RefundTransactionPaymentStepRequest request, ManualActionEntity manualAction) {
        Pipeline<IRefundTransactionStepRequest, RefundPointsStepResponse> pipeline = getManualActionPipeline(manualAction);

        try {
            return pipeline.execute(request);
        } catch (PipelineExecutionError e) {
            System.out.println("Pipeline execution failed: " + e.getErrorInfo().message());
            handlePipelineError(e, pipeline.pipelineId(), request.order().orderId());
            throw e;
        }
    }

    private Pipeline<IRefundTransactionStepRequest, RefundPointsStepResponse> getManualActionPipeline(ManualActionEntity manualAction) {
        // TODO: get pipeline from manual action pipeline id
        return null;
    }

    private void handlePipelineError(PipelineExecutionError e, String pipelineId, String orderId) {
        RefundOperationName operationName = RefundOperationName.from(e.getErrorInfo().operationName());

        if (operationName == RefundOperationName.REFUND_PAYMENT || operationName == RefundOperationName.REFUND_PERSONAL_CREDIT) {
            new RegisterManualActionOp().execute(
                    orderId,
                    pipelineId,
                    e.getErrorInfo().stage(),
                    e.getErrorInfo().stage()+1,
                    e.getErrorInfo().message(),
                    operationName.value());
        }
    }
}
