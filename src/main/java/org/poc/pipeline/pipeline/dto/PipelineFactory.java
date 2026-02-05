package org.poc.pipeline.pipeline.dto;

import org.poc.pipeline.pipeline.Pipeline;

public interface PipelineFactory<I, O> {
    Pipeline<I, O> create();
}
