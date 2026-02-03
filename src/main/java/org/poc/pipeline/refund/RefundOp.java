package org.poc.pipeline.refund;

import org.poc.pipeline.manualaction.RegisterManualActionOp;
import org.poc.pipeline.manualaction.dto.ManualActionCause;
import org.poc.pipeline.manualaction.dto.ManualActionEntity;
import org.poc.pipeline.manualaction.dto.ManualActionRepo;
import org.poc.pipeline.order.OrderLineStatusRepo;
import org.poc.pipeline.order.dto.OrderLineStatus;
import org.poc.pipeline.pipeline.FetchOrCreatePipelineOp;
import org.poc.pipeline.pipeline.FetchPipelineByIdOp;
import org.poc.pipeline.pipeline.Pipeline;
import org.poc.pipeline.pipeline.StepDefinition;
import org.poc.pipeline.pipeline.StepOperation;
import org.poc.pipeline.pipeline.dto.PipelineBuilder;
import org.poc.pipeline.pipeline.exceptions.PipelineExecutionError;
import org.poc.pipeline.refund.dto.RefundOperationName;
import org.poc.pipeline.refund.dto.RefundPointsStepResponse;
import org.poc.pipeline.refund.dto.RefundTransactionPaymentStepRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RefundOp implements PipelineBuilder {

    public RefundPointsStepResponse processRefund(RefundTransactionPaymentStepRequest initialInput) {
        String orderId = initialInput.order().orderId();
        OrderLineStatus orderLineStatus = OrderLineStatusRepo.get(orderId);

        Optional<ManualActionEntity> manualAction = orderLineStatus.manualActionId()
                .flatMap(ManualActionRepo::get);

        Pipeline pipeline = manualAction
                .map(action -> getPipelineFromManualActionPipelineId(action.pipelineId()))
                .orElseGet(this::getPipelineWithSteps);

        manualAction.ifPresent(action -> pipeline.setStartStage(action.nextStage()));

        try {
            return pipeline.execute(initialInput);
        } catch (PipelineExecutionError e) {
            System.out.println("Pipeline execution failed: " + e.getErrorInfo().message());
            handlePipelineError(e, pipeline.pipelineId(), orderId);
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

    private Pipeline getPipelineFromManualActionPipelineId(String pipelineId) {
        return new FetchPipelineByIdOp()
                .execute(pipelineId, this::nameToStepOperation)
                .orElseThrow(() -> new IllegalStateException("Pipeline not found for id: " + pipelineId));
    }

    private Pipeline getPipelineWithSteps() {
        List<StepDefinition> stepDefinitions = createPipelineSteps();
        return new FetchOrCreatePipelineOp().execute(stepDefinitions, this::nameToStepOperation);
    }

    @Override
    public List<StepDefinition> createPipelineSteps() {
        List<StepDefinition> steps = new ArrayList<>();

        steps.add(new StepDefinition(RefundOperationName.REFUND_PAYMENT.value(), 0, 0));
        steps.add(new StepDefinition(RefundOperationName.REFUND_PERSONAL_CREDIT.value(), 1, 0));
        steps.add(new StepDefinition(RefundOperationName.REFUND_POINTS.value(), 2, 1));

        return steps;
    }

    @Override
    public StepOperation<?, ?> nameToStepOperation(String name) {
        return switch (RefundOperationName.from(name)) {
            case REFUND_PAYMENT -> new RefundPaymentStepOp();
            case REFUND_PERSONAL_CREDIT -> new RefundPersonalCreditStepOp();
            case REFUND_POINTS -> new RefundPointsStepOp();
        };
    }

    private ManualActionCause operationNameToManualActionCause(RefundOperationName operationName) {
        return switch (operationName) {
            case REFUND_PAYMENT -> ManualActionCause.REFUND_PAYMENT_ERROR;
            case REFUND_PERSONAL_CREDIT -> ManualActionCause.REFUND_PERSONAL_CREDIT_ERROR;
            default -> throw new IllegalArgumentException("Invalid manual action cause name: " + operationName);
        };
    }
}
