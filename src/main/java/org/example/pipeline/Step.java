package org.example.pipeline;

import org.example.pipeline.dto.OperationName;

public class Step {
        private final Integer stage;
        private final Integer order;
        private final OperationName operationName;
        private final StepOperation<?, ?> operation;

        public Step(Integer order, Integer stage, OperationName operationName, StepOperation<?, ?> operation) {
            this.order = order;
            this.stage = stage;
            this.operationName = operationName;
            this.operation = operation;
        }

        public Integer getStage() { return stage; }
        public OperationName getOperationName() { return operationName; }
        public StepOperation<?, ?> getOperation() { return operation; }
        public Integer getOrder() { return order; }
    }