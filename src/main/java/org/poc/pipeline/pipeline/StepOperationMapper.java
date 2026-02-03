package org.poc.pipeline.pipeline;

import java.util.function.Function;

@FunctionalInterface
public interface StepOperationMapper extends Function<String, StepOperation<?, ?>> {}
