package org.poc.pipeline.pipeline;

import org.poc.pipeline.pipeline.dto.PipelineExecutionEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PipelineExecutionRepo {

    private static final Map<String, PipelineExecutionEntity> pipelineExecutions = new HashMap<>();

    public static PipelineExecutionEntity create(PipelineExecutionEntity pipelineExecutionEntity) {
        pipelineExecutions.put(pipelineExecutionEntity.executionId(), pipelineExecutionEntity);
        return pipelineExecutionEntity;
    }

    public static Optional<PipelineExecutionEntity> get(String executionId) {
        return Optional.ofNullable(pipelineExecutions.get(executionId));
    }
}
