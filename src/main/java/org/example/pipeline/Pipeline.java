package org.example.pipeline;

import org.example.pipeline.exceptions.PipelineExecutionError;
import org.example.pipeline.exceptions.StepOperationExecutionError;

import java.util.List;

public class Pipeline {

        private final Integer DEFAULT_INITIAL_STAGE = 0;

        private final String pipelineId;
        private final List<Step> steps;
        private Integer startStage;

        public Pipeline(String pipelineId, List<Step> steps) {
            this.pipelineId = pipelineId;
            this.steps = steps;
            this.startStage = DEFAULT_INITIAL_STAGE;
        }

        public <I, O> O execute(I input) {
            Object currentInput = input;

            // Execute steps from startStage onwards
            for (Step step : steps) {
                // Skip steps before startStage
                if (step.stage() < startStage) {
                    System.out.println("Skipping step (before start stage): " + step.operationName() +
                            " (stage " + step.stage() + ")");
                    @SuppressWarnings("unchecked")
                    StepOperation<Object, Object> operation = (StepOperation<Object, Object>) step.operation();
                    currentInput = operation.transformForSkip(currentInput);
                    continue;
                }

                try {
                    System.out.println("Executing step: " + step.operationName() + " (stage " + step.stage() + ")");
                    @SuppressWarnings("unchecked")
                    StepOperation<Object, Object> operation = (StepOperation<Object, Object>) step.operation();
                    currentInput = operation.execute(currentInput);
                } catch (StepOperationExecutionError e) {
                    throw new PipelineExecutionError(e.getMessage(), step.operationName(), step.stage(), pipelineId);
                }
            }

            @SuppressWarnings("unchecked")
            O result = (O) currentInput;
            return result;
        }

        public String pipelineId() {
            return pipelineId;
        }
        public void setStartStage(Integer startStage) {
            this.startStage = startStage;
        }
    }