package org.example.pipeline;

import org.example.pipeline.dto.StepEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class BuildPipelineOp {

    public Pipeline execute(
            String pipelineId,
            Function<String, StepOperation<?, ?>> stepOperationMapperFn
    ) {
        List<StepEntity> stepEntities = StepRepo.getByPipelineId(pipelineId)
                .sorted(Comparator.comparing(StepEntity::order))
                .toList();

        return doExecute(stepEntities, pipelineId, stepOperationMapperFn);
    }

    private Pipeline doExecute(
            List<StepEntity> stepEntities,
            String pipelineId,
            Function<String, StepOperation<?, ?>> stepOperationMapperFn
    ) {
        List<Step> steps = new ArrayList<>();

        for (StepEntity stepEntity : stepEntities) {
            StepOperation<?, ?> operation = stepOperationMapperFn.apply(stepEntity.operationName());
            Step step = new Step(stepEntity.order(), stepEntity.stage(), stepEntity.operationName(), operation);
            steps.add(step);
        }

        return new Pipeline(pipelineId, steps);
    }
}
