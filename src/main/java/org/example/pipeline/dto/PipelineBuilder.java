package org.example.pipeline.dto;

import org.example.pipeline.StepDefinition;
import org.example.pipeline.StepOperation;

import java.util.List;

public interface PipelineBuilder {
    StepOperation<?, ?> nameToStepOperation(String name);
    List<StepDefinition> createPipelineSteps();
}
