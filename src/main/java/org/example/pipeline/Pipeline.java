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

            // Transform through steps before startStage (skipped steps)
            for (Step step : steps) {
                if (step.getStage() < startStage) {
                    System.out.println("Skipping step (before start stage): " + step.getOperationName() +
                            " (stage " + step.getStage() + ")");
                    @SuppressWarnings("unchecked")
                    StepOperation<Object, Object> operation = (StepOperation<Object, Object>) step.getOperation();
                    currentInput = operation.transformForSkip(currentInput);
                }
            }

            // Execute steps from startStage onwards
            for (Step step : steps) {
                if (step.getStage() < startStage) {
                    continue; // Skip steps before startStage
                }

                try {
                    System.out.println("Executing step: " + step.getOperationName() + " (stage " + step.getStage() + ")");
                    @SuppressWarnings("unchecked")
                    StepOperation<Object, Object> operation = (StepOperation<Object, Object>) step.getOperation();
                    currentInput = operation.execute(currentInput);
                } catch (StepOperationExecutionError e) {
                    throw new PipelineExecutionError(e.getMessage(), step.getOperationName(), step.getStage(), pipelineId);
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