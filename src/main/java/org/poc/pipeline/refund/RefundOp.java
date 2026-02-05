package org.poc.pipeline.refund;

import org.poc.pipeline.manualaction.RegisterManualActionOp;
import org.poc.pipeline.manualaction.dto.ManualActionCause;
import org.poc.pipeline.manualaction.dto.ManualActionEntity;
import org.poc.pipeline.manualaction.dto.ManualActionRepo;
import org.poc.pipeline.order.OrderLineStatusRepo;
import org.poc.pipeline.order.dto.OrderLineStatus;
import org.poc.pipeline.pipeline.Pipeline;
import org.poc.pipeline.pipeline.exceptions.PipelineExecutionError;
import org.poc.pipeline.refund.dto.RefundOperationName;
import org.poc.pipeline.refund.dto.RefundPointsStepResponse;
import org.poc.pipeline.refund.dto.RefundTransactionPaymentStepRequest;
import org.poc.pipeline.refund.dto.interfaces.IRefundTransactionStepRequest;
import org.poc.pipeline.refund.factories.RefundCompletePipelineFactory;

import java.util.Optional;

public class RefundOp {

    public RefundPointsStepResponse processRefund(RefundTransactionPaymentStepRequest request) {

        Pipeline<IRefundTransactionStepRequest, RefundPointsStepResponse> pipeline = new RefundCompletePipelineFactory().create();

        try {
            return pipeline.execute(request);
        } catch (PipelineExecutionError e) {
            System.out.println("Pipeline execution failed: " + e.getErrorInfo().message());
            handlePipelineError(e, pipeline.pipelineId(), request.order().orderId());
            throw e;
        }
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
                    operationNameToManualActionCause(operationName));
        }
    }

    private ManualActionCause operationNameToManualActionCause(RefundOperationName operationName) {
        return switch (operationName) {
            case REFUND_PAYMENT -> ManualActionCause.REFUND_PAYMENT_ERROR;
            case REFUND_PERSONAL_CREDIT -> ManualActionCause.REFUND_PERSONAL_CREDIT_ERROR;
            default -> throw new IllegalArgumentException("Invalid manual action cause name: " + operationName);
        };
    }
}
