package org.poc.pipeline.pipeline;

import org.poc.pipeline.pipeline.dto.StepEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class BuildPipelineOp {

    public Pipeline execute(
            String pipelineId,
            StepOperationMapper mapper
    ) {
        List<StepEntity> stepEntities = StepRepo.getByPipelineId(pipelineId)
                .sorted(Comparator.comparing(StepEntity::order))
                .toList();

        return doExecute(stepEntities, pipelineId, mapper);
    }

    private Pipeline doExecute(
            List<StepEntity> stepEntities,
            String pipelineId,
            StepOperationMapper mapper
    ) {
        List<Step> steps = new ArrayList<>();

        for (StepEntity stepEntity : stepEntities) {
            StepOperation<?, ?> operation = mapper.apply(stepEntity.operationName());
            Step step = new Step(stepEntity.order(), stepEntity.stage(), stepEntity.operationName(), operation);
            steps.add(step);
        }

        return new Pipeline(pipelineId, steps);
    }
}
