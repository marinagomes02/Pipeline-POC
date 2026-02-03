package org.example.pipeline.exceptions;

public class PipelineExecutionError extends RuntimeException {

    public record ErrorInfo(
            String message,
            String operationName,
            Integer stage,
            String pipelineId
    ) {}

    private final ErrorInfo errorInfo;

    public PipelineExecutionError(
            String message,
            String operationName,
            Integer stage,
            String pipelineId
    ) {
        this.errorInfo = new ErrorInfo(
                message,
                operationName,
                stage,
                pipelineId);
    }

    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }
}