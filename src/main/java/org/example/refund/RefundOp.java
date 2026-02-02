package org.example.refund;

import org.example.manualaction.RegisterManualActionOp;
import org.example.manualaction.dto.ManualActionCause;
import org.example.manualaction.dto.ManualActionEntity;
import org.example.manualaction.dto.ManualActionRepo;
import org.example.order.OrderLineStatusRepo;
import org.example.order.dto.OrderLineStatus;
import org.example.pipeline.FetchOrCreatePipelineOp;
import org.example.pipeline.FetchPipelineByIdOp;
import org.example.pipeline.Pipeline;
import org.example.pipeline.StepDefinition;
import org.example.pipeline.dto.OperationName;
import org.example.pipeline.exceptions.PipelineExecutionError;
import org.example.refund.dto.RefundPointsStepResponse;
import org.example.refund.dto.RefundTransactionPaymentStepRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RefundOp {

    public RefundPointsStepResponse processRefund(RefundTransactionPaymentStepRequest initialInput) {
        String orderId = initialInput.order().orderId();
        OrderLineStatus orderLineStatus = OrderLineStatusRepo.get(orderId);

        Optional<ManualActionEntity> manualAction = orderLineStatus.manualActionId()
                .flatMap(ManualActionRepo::get);

        Pipeline pipeline = manualAction
                .map(action -> getPipelineFromManualActionPipelineId(action.pipelineId()))
                .orElseGet(this::getPipelineWithSteps);

        manualAction.ifPresent(action -> pipeline
                .setStartStage(action.stage()+1));

        try {
            return pipeline.execute(initialInput);
        } catch (PipelineExecutionError e) {
            System.out.println("Pipeline execution failed: " + e.getErrorInfo().message());
            if (e.getErrorInfo().operationName() == OperationName.REFUND_PAYMENT ||
                    e.getErrorInfo().operationName() == OperationName.REFUND_PERSONAL_CREDIT) {
                new RegisterManualActionOp().execute(
                        orderId,
                        pipeline.pipelineId(),
                        e.getErrorInfo().stage(),
                        e.getErrorInfo().message(),
                        operationNameToManualActionCause(e.getErrorInfo().operationName()));
            }
            throw e;
        }
    }

    private Pipeline getPipelineFromManualActionPipelineId(String pipelineId) {
        return new FetchPipelineByIdOp()
                .execute(pipelineId)
                .orElseThrow(() -> new IllegalStateException("Pipeline not found for id: " + pipelineId));
    }

    private Pipeline getPipelineWithSteps() {
        List<StepDefinition> stepDefinitions = createPipelineSteps();
        return new FetchOrCreatePipelineOp().execute(stepDefinitions);
    }

    private List<StepDefinition> createPipelineSteps() {
        List<StepDefinition> steps = new ArrayList<>();

        steps.add(new StepDefinition(OperationName.REFUND_PAYMENT, 0, 0));
        steps.add(new StepDefinition(OperationName.REFUND_PERSONAL_CREDIT, 1, 0));
        steps.add(new StepDefinition(OperationName.REFUND_POINTS, 2, 1));

        return steps;
    }

    static ManualActionCause operationNameToManualActionCause(OperationName operationName) {
        return switch (operationName) {
            case REFUND_PAYMENT -> ManualActionCause.REFUND_PAYMENT_ERROR;
            case REFUND_PERSONAL_CREDIT -> ManualActionCause.REFUND_PERSONAL_CREDIT_ERROR;
            default -> throw new IllegalArgumentException("Invalid manual action cause name: " + operationName);
        };
    }
}
