package org.poc.pipeline.pipeline;

import org.poc.pipeline.pipeline.dto.PipelineEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PipelineRepo {

    static Map<String, PipelineEntity> pipelineStore = new HashMap<>();

    public static void save(PipelineEntity pipelineEntity) {
        pipelineStore.put(pipelineEntity.pipelineId(), pipelineEntity);
    }

    public static Optional<PipelineEntity> get(String pipelineId) {
        return Optional.ofNullable(pipelineStore.get(pipelineId));
    }

    public static Optional<PipelineEntity> getByHash(String pipelineHash) {
        return pipelineStore.values().stream()
                .filter(p -> p.pipelineHash().equals(pipelineHash))
                .findFirst();
    }
}
