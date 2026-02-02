package org.example.pipeline;

public interface StepOperation<I, O> {
        O execute(I input);
        O transformForSkip(I input);
}