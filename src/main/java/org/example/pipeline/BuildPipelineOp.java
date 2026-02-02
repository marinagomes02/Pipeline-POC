package org.example.pipeline;

import org.example.pipeline.dto.OperationName;
import org.example.pipeline.dto.StepEntity;
import org.example.refund.RefundPaymentStepOp;
import org.example.refund.RefundPersonalCreditStepOp;
import org.example.refund.RefundPointsStepOp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BuildPipelineOp {

    public Pipeline execute(String pipelineId) {
        List<StepEntity> stepEntities = StepRepo.getByPipelineId(pipelineId)
                .sorted(Comparator.comparing(StepEntity::order))
                .toList();

        return doExecute(stepEntities, pipelineId);
    }

    private Pipeline doExecute(List<StepEntity> stepEntities, String pipelineId) {
        List<Step> steps = new ArrayList<>();

        for (StepEntity stepEntity : stepEntities) {
            StepOperation<?, ?> operation = nameToStepOperation(stepEntity.operationName());
            Step step = new Step(stepEntity.order(), stepEntity.stage(), stepEntity.operationName(), operation);
            steps.add(step);
        }

        return new Pipeline(pipelineId, steps);
    }

    static StepOperation<?, ?> nameToStepOperation(OperationName name) {
        return switch (name) {
            case REFUND_PAYMENT -> new RefundPaymentStepOp();
            case REFUND_PERSONAL_CREDIT -> new RefundPersonalCreditStepOp();
            case REFUND_POINTS -> new RefundPointsStepOp();
        };
    }
}
