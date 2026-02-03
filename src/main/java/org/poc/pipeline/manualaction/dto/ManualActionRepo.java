package org.poc.pipeline.manualaction.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ManualActionRepo {
    static Map<String, ManualActionEntity> manualActionStore = new HashMap<>();

    public static void save(ManualActionEntity manualActionEntity) {
        manualActionStore.put(manualActionEntity.manualActionId().id(), manualActionEntity);
    }

    public static Optional<ManualActionEntity> get(ManualActionId manualActionId) {
        return Optional.ofNullable(manualActionStore.get(manualActionId.id()));
    }
}
