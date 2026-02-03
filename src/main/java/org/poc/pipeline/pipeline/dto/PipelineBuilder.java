package org.poc.pipeline.pipeline.dto;

import org.poc.pipeline.pipeline.StepDefinition;
import org.poc.pipeline.pipeline.StepOperation;

import java.util.List;

public interface PipelineBuilder {
    StepOperation<?, ?> nameToStepOperation(String name);
    List<StepDefinition> createPipelineSteps();
}
