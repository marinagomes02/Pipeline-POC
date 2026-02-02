package org.example.pipeline.exceptions;

public class StepOperationExecutionError extends RuntimeException {
        public StepOperationExecutionError(String message) {
            super(message);
        }
    }