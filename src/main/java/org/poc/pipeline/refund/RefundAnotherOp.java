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
import org.poc.pipeline.refund.dto.RefundPaymentMethodStepResponse;
import org.poc.pipeline.refund.dto.RefundPointsStepResponse;
import org.poc.pipeline.refund.dto.RefundTransactionPaymentStepRequest;
import org.poc.pipeline.refund.dto.interfaces.IRefundPaymentMethodStepRequest;
import org.poc.pipeline.refund.dto.interfaces.IRefundTransactionStepRequest;
import org.poc.pipeline.refund.factories.RefundAnotherPipelineFactory;

import java.util.Optional;
import java.util.UUID;

public class RefundAnotherOp {

    public RefundPaymentMethodStepResponse processRefund(IRefundTransactionStepRequest request) {

        String orderId = request.order().orderId();
        OrderLineStatus orderLineStatus = OrderLineStatusRepo.get(orderId);

        Pipeline<IRefundTransactionStepRequest, RefundPaymentMethodStepResponse> pipeline = new RefundAnotherPipelineFactory().create();

        setStartStageIfOrderLineHasManualAction(orderLineStatus, pipeline);

        try {
            return pipeline.execute(request);
        } catch (PipelineExecutionError e) {
            System.out.println("Pipeline execution failed: " + e.getErrorInfo().message());
            handlePipelineError(e, pipeline, request.order().orderId());
            throw e;
        }
    }

    private void setStartStageIfOrderLineHasManualAction(
            OrderLineStatus orderLineStatus,
            Pipeline<IRefundTransactionStepRequest, RefundPaymentMethodStepResponse> pipeline
    ) {
        Optional<ManualActionEntity> manualActionOpt = orderLineStatus.manualActionId().flatMap(ManualActionRepo::get);

        if (manualActionOpt.isPresent()) {
            ManualActionEntity manualAction = manualActionOpt.get();

            PipelineExecutionEntity pipelineExecution = PipelineExecutionRepo
                    .get(manualAction.pipelineExecutionId())
                    .orElseThrow(() -> new IllegalStateException("Pipeline execution not found for ID: " + manualAction.pipelineExecutionId()));

            if (!pipelineExecution.pipelineId().equals(pipeline.pipelineId())) {
                throw new IllegalStateException("Manual action pipeline ID does not match current pipeline ID");
            }

            pipeline.setStartStep(pipelineExecution.nextStep());
        }
    }

    private void handlePipelineError(PipelineExecutionError e, Pipeline<?, ?> pipeline, String orderId) {
        RefundOperationName operationName = RefundOperationName.from(e.getErrorInfo().operationName());

        if (doesOperationRequireManualAction(operationName)) {
            PipelineExecutionEntity pipelineExecution = PipelineExecutionRepo.create(new PipelineExecutionEntity(
                    UUID.randomUUID().toString(),
                    pipeline.pipelineId(),
                    pipeline.getNextStepNumberAfterStage(e.getErrorInfo().stage())));

            new RegisterManualActionOp().execute(
                    orderId,
                    pipelineExecution.executionId(),
                    e.getErrorInfo().step(),
                    e.getErrorInfo().message(),
                    operationName.value());
        }
    }

    private boolean doesOperationRequireManualAction(RefundOperationName operationName) {
        return operationName == RefundOperationName.REFUND_PAYMENT ||
               operationName == RefundOperationName.REFUND_PERSONAL_CREDIT;
    }
}
