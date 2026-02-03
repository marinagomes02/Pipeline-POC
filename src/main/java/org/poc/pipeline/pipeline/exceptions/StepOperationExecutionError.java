package org.poc.pipeline.pipeline.exceptions;

public class StepOperationExecutionError extends RuntimeException {
        public StepOperationExecutionError(String message) {
            super(message);
        }
    }