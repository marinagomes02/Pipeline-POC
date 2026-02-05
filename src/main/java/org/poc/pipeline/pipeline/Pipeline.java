package org.poc.pipeline.pipeline;

import org.poc.pipeline.pipeline.dto.Step;
import org.poc.pipeline.pipeline.dto.StepOperation;
import org.poc.pipeline.pipeline.exceptions.PipelineExecutionError;
import org.poc.pipeline.pipeline.exceptions.StepOperationExecutionError;

import java.util.ArrayList;
import java.util.List;

public class Pipeline<I, O> {

    record RollbackStep<I, O>(StepOperation<I, O> operation, O output) {
        void rollback() {
            operation.rollback(output);
        }
    }

    private final Integer DEFAULT_INITIAL_STAGE = 0;

    private final String pipelineId;
    private final List<Step<?, ?>> steps;
    private Integer startStage;
    private final List<RollbackStep<?, ?>> executedSteps = new ArrayList<>();

    public Pipeline(String pipelineId, List<Step<?, ?>> steps) {
        this.pipelineId = pipelineId;
        this.steps = steps;
        this.startStage = DEFAULT_INITIAL_STAGE;
    }

    public static <I, O> Builder<I, O> builder(Step<I, O> firstStep) {
        return new Builder<>(firstStep);
    }

    public O execute(I input) {
        Object currentInput;
        try {
            currentInput = doExecute(input);
        } catch (PipelineExecutionError e) {
            rollback();
            throw e;
        } finally {
            executedSteps.clear();
        }
        return (O) currentInput;
    }

    private O doExecute(I input) {
        Object currentInput = input;
        for (Step<?, ?> step : steps) {
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
                Object output = operation.execute(currentInput);
                executedSteps.add(new RollbackStep<>(operation, output));
                currentInput = output;
            } catch (StepOperationExecutionError e) {
                System.out.println("Step operation execution error in step: " + step.operationName() + " - " + e.getMessage());
                throw new PipelineExecutionError(e.getMessage(), step.operationName(), step.stage(), pipelineId);
            }
        }
        return (O) currentInput;
    }

    private void rollback() {
            for (int i = executedSteps.size() - 1; i >= 0; i--) {
            try {
                executedSteps.get(i).rollback();
            } catch (Exception e) {
                System.out.println("Error during rollback for step: " + executedSteps.get(i).operation().getClass().getName() + " - " + e.getMessage());
            }
        }
    }

    public static class Builder<I, O> {
        private final List<Step<?, ?>> steps;

        private Builder(Step<I, O> firstStep) {
            this.steps = new ArrayList<>();
            steps.add(firstStep);
        }

        private Builder(List<Step<?, ?>> steps) {
            this.steps = new ArrayList<>(steps);
        }

        public <NextO> Builder<I, NextO> pipe(Step<? super O, NextO> nextStep) {
            steps.add(nextStep);
            return new Builder<>(steps);
        }

        public Pipeline<I, O> build(String pipelineId) {
            return new Pipeline<>(pipelineId, steps);
        }
    }

    public String pipelineId() {
        return pipelineId;
    }

    public void setStartStage(Integer startStage) {
        this.startStage = startStage;
    }
}