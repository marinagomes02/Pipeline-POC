package org.poc.pipeline.pipeline.exceptions;

public class PipelineExecutionError extends RuntimeException {

    public record ErrorInfo(
            String message,
            String operationName,
            Integer step,
            Integer stage,
            String pipelineId
    ) {}

    private final ErrorInfo errorInfo;

    public PipelineExecutionError(
            String message,
            String operationName,
            Integer step,
            Integer stage,
            String pipelineId
    ) {
        this.errorInfo = new ErrorInfo(
                message,
                operationName,
                step,
                stage,
                pipelineId);
    }

    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }
}