package org.example.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.pipeline.dto.OperationName;
import org.example.pipeline.dto.PipelineEntity;
import org.example.pipeline.dto.StepEntity;
import org.example.refund.RefundPaymentStepOp;
import org.example.refund.RefundPersonalCreditStepOp;
import org.example.refund.RefundPointsStepOp;

import java.nio.channels.Pipe;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class FetchOrCreatePipelineOp {

    public Pipeline execute(List<StepDefinition> stepDefinitions) {
        String pipelineHash = computePipelineHash(stepDefinitions);

        PipelineEntity pipelineEntity = getExistingPipeline(pipelineHash)
                .orElseGet(() -> createNewPipeline(stepDefinitions, pipelineHash));

        return new BuildPipelineOp().execute(pipelineEntity.pipelineId());
    }

    private Optional<PipelineEntity> getExistingPipeline(String pipelineHash) {
        return PipelineRepo.getByHash(pipelineHash);
    }

    private PipelineEntity createNewPipeline(List<StepDefinition> stepDefinitions, String pipelineHash) {
        String pipelineId = UUID.randomUUID().toString();

        PipelineEntity pipelineEntity = new PipelineEntity(
                pipelineId,
                pipelineHash);

        PipelineRepo.save(pipelineEntity);

        for (StepDefinition stepDef : stepDefinitions) {
            String stepId = UUID.randomUUID().toString();
            StepEntity stepEntity = new StepEntity(
                    stepId,
                    pipelineId,
                    stepDef.operationName(),
                    stepDef.order(),
                    stepDef.stage());
            StepRepo.save(stepEntity);
        }

        System.out.println("Created new pipeline with hash: " + pipelineHash + " and " + stepDefinitions.size() + " steps");

        return pipelineEntity;
    }

    private String computePipelineHash(List<StepDefinition> steps) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> stepNamesAndStages = steps.stream()
                    .map(step -> step.operationName() + "-" + step.stage())
                    .collect(Collectors.toList());
            String json = objectMapper.writeValueAsString(stepNamesAndStages);
            return DigestUtils.sha256Hex(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error computing pipeline hash", e);
        }
    }
}
