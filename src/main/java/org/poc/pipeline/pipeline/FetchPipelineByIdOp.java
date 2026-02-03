package org.poc.pipeline.pipeline;

import org.poc.pipeline.pipeline.dto.PipelineEntity;
import java.util.Optional;
import java.util.function.Function;

public class FetchPipelineByIdOp {

    public Optional<Pipeline> execute(
            String pipelineId,
            StepOperationMapper mapper
    ) {
        Optional<PipelineEntity> pipelineEntity = PipelineRepo.get(pipelineId);
        BuildPipelineOp buildPipelineOp = new BuildPipelineOp();

        return pipelineEntity
                .map(PipelineEntity::pipelineId)
                .map(id -> buildPipelineOp.execute(id, mapper));
    }
}
