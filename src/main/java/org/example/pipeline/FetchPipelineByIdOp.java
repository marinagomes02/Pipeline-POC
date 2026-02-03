package org.example.pipeline;

import org.example.pipeline.dto.PipelineEntity;
import java.util.Optional;
import java.util.function.Function;

public class FetchPipelineByIdOp {

    public Optional<Pipeline> execute(
            String pipelineId,
            Function<String, StepOperation<?, ?>> stepOperationMapperFn
    ) {
        Optional<PipelineEntity> pipelineEntity = PipelineRepo.get(pipelineId);
        BuildPipelineOp buildPipelineOp = new BuildPipelineOp();

        return pipelineEntity
                .map(PipelineEntity::pipelineId)
                .map(id -> buildPipelineOp.execute(id, stepOperationMapperFn));
    }
}
