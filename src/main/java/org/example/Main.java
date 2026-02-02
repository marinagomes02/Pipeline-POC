package org.example;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(String[] args) throws ParseException {
        System.out.println(((Step<String, String>) bdToCode("FuckThePolice", 1).get()).execute("Jesus"));

        System.out.println();

        System.out.println(executePipeline(
                buildPipelineFromBD(List.of(
                        new StepEntity("Alleluia", 0),
                        new StepEntity("FuckThePolice", 1))),
                "Christ",
                String.class));
    }

    record Pipeline(
            List<Step<?, ?>> steps
    ) {}

    record StepEntity(
            String name,
            Integer stage
    ) {}

    interface StepOperation<I, O> {
        O execute(I input);
    }

    static class AlleluiaOp implements StepOperation<String, String> {

        @Override
        public String execute(String input) {
            return input + " alleiuia";
        }
    }

    static class FuckThePoliceOp implements StepOperation<String, String> {

        @Override
        public String execute(String input) {
            System.out.println("ICE ICE");
            return input + " BABY";
        }
    }

    record Step<I, O>(StepOperation<I, O> op, Integer stage) {
        public O execute(I input) {
            return this.op.execute(input);
        }
    }

    private static <I, O> O executePipeline(Pipeline pipeline, I input, Class<O> outputClass) {
        Object in = input;
        for (Step<?, ?> step: pipeline.steps()) {
            in = ((Step<Object, Object>) step).execute(in);
        }

        return outputClass.cast(in);
    }

    private static Pipeline buildPipelineFromBD(List<StepEntity> steps) {
        List<Step<?, ?>> pipe = new ArrayList<>();
        steps.forEach(se ->
            bdToCode(se.name(), se.stage()).ifPresent(pipe::add)
        );
        return new Pipeline(pipe);
    }

    private static Optional<Step<?, ?>> bdToCode(String name, Integer stage) {
        return stringToStepOperation(name).map(op -> new Step<>(op, stage));
    }

    private static Optional<StepOperation<?, ?>> stringToStepOperation(String name) {
        if (name == "Alleluia") {
            return Optional.of(new AlleluiaOp());
        } else if (name == "FuckThePolice") {
            return Optional.of(new FuckThePoliceOp());
        }

        return Optional.empty();
    }
}