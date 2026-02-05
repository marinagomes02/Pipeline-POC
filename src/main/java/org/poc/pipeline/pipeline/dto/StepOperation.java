package org.poc.pipeline.pipeline.dto;

public interface StepOperation<I, O> {
        O execute(I input);
        O transformForSkip(I input);
        void rollback(O output);
}