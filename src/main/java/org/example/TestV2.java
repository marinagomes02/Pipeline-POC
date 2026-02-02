package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestV2 {

    static Map<String, PipelineEntity> pipelineStore = new HashMap<>();
    static Map<String, StepEntity> stepStore = new HashMap<>();

    public static void main(String[] args) {
        String pipelineId = UUID.randomUUID().toString();

        Pipeline pipeline = buildPipelineFromBD(List.of(
                new StepEntity(UUID.randomUUID().toString(), OperationName.REFUND_TRANSACTION, 0, pipelineId),
                new StepEntity(UUID.randomUUID().toString(), OperationName.REFUND_PERSONAL_CREDIT, 1, pipelineId)));
        String result = pipeline.execute("Bola");
        System.out.println(result);

        System.out.println(new RefundPersonalCreditPipeline().execute("Christ"));
        System.out.println(" " + pipelineStore.size() + " " + pipelineStore.size());

        System.out.println(new RefundPersonalCreditPipeline().execute("Jesus"));
        System.out.println(" " + pipelineStore.size() + " " + pipelineStore.size());
    }

    static class Pipeline {
        private List<Step> steps;

        public Pipeline(List<Step> steps) {
            this.steps = steps;
        }

        public <I, O> O execute(I input) {
            Object in = input;
            for (Step step: steps) {
                StepOperation<Object, Object> currentStepOperation = (StepOperation<Object, Object>) nameToStepOperation(step.name());
                in = currentStepOperation.execute(in);
            }
            return (O) in;
        }

        private StepOperation<?, ?> nameToStepOperation(OperationName name) {
            if (name.equals(OperationName.REFUND_TRANSACTION)) {
                return new RefundTransactionStepOp();
            } else if (name.equals(OperationName.REFUND_PERSONAL_CREDIT)) {
                return new RefundPersonalCreditStepOp();
            }

            throw new RuntimeException("Unknown operation name: " + name);
        }
    }

    record PipelineEntity(
            String pipelineId,
            String pipelineManagedId
    ) {}

    record StepEntity(
            String id,
            OperationName name,
            Integer stage,
            String pipelineId
    ) {}

    interface StepOperation<I, O> {
        O execute(I input);
    }

    enum OperationName {
        REFUND_TRANSACTION,
        REFUND_PERSONAL_CREDIT
    }

    record Step(OperationName name, Integer stage) {
    }

    static class RefundTransactionStepOp implements StepOperation<String, String> {

        @Override
        public String execute(String input) {
            return input + " refund transaction -";
        }
    }

    static class RefundPersonalCreditStepOp implements StepOperation<String, String> {

        @Override
        public String execute(String input) {
            System.out.println("ICE ICE");
            return input + " refund personal credit -";
        }
    }

    static class RefundPersonalCreditPipeline {
        public String execute(String input) {
            Pipeline pipeline = fetchOrCreatePipeline(List.of(
                    new Step(OperationName.REFUND_TRANSACTION, 0),
                    new Step(OperationName.REFUND_PERSONAL_CREDIT, 1)
            ));
            return pipeline.execute(input);
        }
    }

    // GET PIPELINE FROM BD AND RETURN

    private static Pipeline buildPipelineFromBD(List<StepEntity> steps) {
        List<Step> pipe = new ArrayList<>();
        steps.forEach(se ->
            pipe.add(bdToCode(se.name(), se.stage()))
        );
        return new Pipeline(pipe);
    }

    private static Step bdToCode(OperationName name, Integer stage) {
        return new Step(name, stage);
    }

    private static Optional<OperationName> stepOperationToName(StepOperation<?, ?> op) {
        if (op instanceof RefundTransactionStepOp) {
            return Optional.of(OperationName.REFUND_TRANSACTION);
        } else if (op instanceof RefundPersonalCreditStepOp) {
            return Optional.of(OperationName.REFUND_PERSONAL_CREDIT);
        }

        return Optional.empty();
    }

    // SAVE PIPELINE TO BD AND RETURN

    private static Pipeline fetchOrCreatePipeline(List<Step> steps) {
        String pipelineId = computePipelineManagedId(steps);

        return Optional.ofNullable(pipelineStore.get(pipelineId))
                .map(entity -> new Pipeline(steps))
                .orElseGet(() -> createPipeline(pipelineId, steps));
    }

    private static String computePipelineManagedId(List<Step> steps) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> stepNamesAndStages = new ArrayList<>();
            steps.forEach(step ->
                    stepNamesAndStages.add(step.name() + "-" + step.stage()));
            var json = objectMapper.writeValueAsString(stepNamesAndStages);
            return DigestUtils.sha256Hex(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error computing pipeline ID", e);
        }
    }

    private static Pipeline createPipeline(String pipelineManagedId, List<Step> steps) {
        String pipelineId = UUID.randomUUID().toString();
        PipelineEntity pipelineEntity = new PipelineEntity(pipelineId, pipelineManagedId);
        pipelineStore.put(pipelineManagedId, pipelineEntity);

        steps.forEach(step -> createStep(step, pipelineId));
        return new Pipeline(steps);
    }

    private static void createStep(Step step, String pipelineId) {
        StepEntity stepEntity = new StepEntity(
                UUID.randomUUID().toString(),
                step.name(),
                step.stage(),
                pipelineId);
        stepStore.put(stepEntity.id(), stepEntity);
    }
}