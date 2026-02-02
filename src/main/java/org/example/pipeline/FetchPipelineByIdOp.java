package org.example.pipeline;

import org.example.pipeline.dto.PipelineEntity;
import java.util.Optional;

public class FetchPipelineByIdOp {

    public Optional<Pipeline> execute(String pipelineId) {
        Optional<PipelineEntity> pipelineEntity = PipelineRepo.get(pipelineId);
        BuildPipelineOp buildPipelineOp = new BuildPipelineOp();

        return pipelineEntity
                .map(PipelineEntity::pipelineId)
                .map(buildPipelineOp::execute);
    }
}
