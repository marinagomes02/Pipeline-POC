package org.example.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.pipeline.dto.PipelineEntity;
import org.example.pipeline.dto.StepEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FetchOrCreatePipelineOp {

    public Pipeline execute(List<StepDefinition> stepDefinitions, Function<String, StepOperation<?, ?>> stepOperationMapperFn) {
        String pipelineHash = computePipelineHash(stepDefinitions); // hash do record

        PipelineEntity pipelineEntity = getExistingPipeline(pipelineHash)
                .orElseGet(() -> createNewPipeline(stepDefinitions, pipelineHash));

        return new BuildPipelineOp().execute(pipelineEntity.pipelineId(), stepOperationMapperFn);
    }

    private Optional<PipelineEntity> getExistingPipeline(String pipelineHash) {
        return PipelineRepo.getByHash(pipelineHash);
    }

    private PipelineEntity createNewPipeline(List<StepDefinition> stepDefinitions, String pipelineHash) {
        PipelineEntity pipelineEntity = savePipeline(pipelineHash);

        stepDefinitions.forEach(stepDef ->
                saveStep(stepDef, pipelineEntity.pipelineId())
        );

        System.out.println("Created new pipeline with hash: " + pipelineHash + " and " + stepDefinitions.size() + " steps");

        return pipelineEntity;
    }

    private PipelineEntity savePipeline(String pipelineHash) {
        String pipelineId = UUID.randomUUID().toString();
        PipelineEntity pipelineEntity = new PipelineEntity(
                pipelineId,
                pipelineHash);
        PipelineRepo.save(pipelineEntity);
        return pipelineEntity;
    }

    private void saveStep(StepDefinition stepDefinition, String pipelineId) {
        String stepId = UUID.randomUUID().toString();
        StepEntity stepEntity = new StepEntity(
                stepId,
                pipelineId,
                stepDefinition.operationName(),
                stepDefinition.order(),
                stepDefinition.stage());
        StepRepo.save(stepEntity);
    }

    private String computePipelineHash(List<StepDefinition> steps) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Integer> stepNamesAndStages = steps.stream()
                    .map(Record::hashCode)
                    .toList();
            String json = objectMapper.writeValueAsString(stepNamesAndStages);
            return DigestUtils.sha256Hex(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error computing pipeline hash", e);
        }
    }
}
