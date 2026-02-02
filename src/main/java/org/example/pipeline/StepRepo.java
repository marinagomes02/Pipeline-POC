package org.example.pipeline;

import org.example.pipeline.dto.StepEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class StepRepo {

    static Map<String, StepEntity> stepStore = new HashMap<>();

    public static void save(StepEntity stepEntity) {
        stepStore.put(stepEntity.stepId(), stepEntity);
    }

    public static Stream<StepEntity> getByPipelineId(String pipelineId) {
        return stepStore.values().stream()
                .filter(s -> s.pipelineId().equals(pipelineId));
    }

}
