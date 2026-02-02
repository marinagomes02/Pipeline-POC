package org.example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestV4 {

    // In-memory stores (replace with actual DB repositories)
    static Map<String, PipelineEntity> pipelineStore = new HashMap<>();
    static Map<String, StepEntity> stepStore = new HashMap<>();
    static Map<String, ManualActionEntity> manualActionStore = new HashMap<>();
    static Map<String, OrderLineStatus> orderLineStatusStore = new HashMap<>();

    public static void main(String[] args) {
        // Example: Process refund for Personal Credit payment method
        String orderId = "ORDER-12345";
        orderLineStatusStore.put(orderId, new OrderLineStatus(Status.PENDING, Optional.empty()));
        RefundRequest initialInput = new RefundRequest(
                new OrderAssociatedIdsAndCreationDate(orderId),
                List.of(new OrderLineDetails("LINE-1"))
        );

        // Fail on refund payment
        System.out.println("=== First execution ===");
        try {
            RefundPointsStepResponse result = processRefund(initialInput);
            System.out.println("Result: " + result);
        } catch (PipelineExecutionError e) {
            System.out.println("Pipeline execution failed: " + e.getErrorInfo().message());
        }
        System.out.println();

        // Simulate retry after failure (some steps already executed)
        System.out.println("=== Retry execution (resuming from last completed step) ===");
        RefundPointsStepResponse result2 = processRefund(initialInput);
        System.out.println("Result: " + result2);
        System.out.println();
    }


    /**
     * Main entry point for processing a refund
     */
    public static RefundPointsStepResponse processRefund(RefundRequest initialInput) {
        // Fetch or create pipeline (lookup by hash - always the same since all steps are included)
        PipelineEntity pipelineEntity;

        OrderLineStatus orderLineStatus = orderLineStatusStore.get(initialInput.order().orderId());
        Optional<Integer> startStage = Optional.empty();

        if (orderLineStatus.manualActionId().isPresent()) {
            ManualActionEntity manualAction = manualActionStore.get(orderLineStatus.manualActionId().get().id());
            pipelineEntity = fetchPipelineById(manualAction.pipelineId());
            startStage = Optional.of(manualAction.stage()+1);
        } else {
            List<StepDefinition> stepDefinitions = createPipelineSteps();
            pipelineEntity = fetchOrCreatePipeline(stepDefinitions);
        }

        // Fetch all steps for this pipeline
        List<StepEntity> stepEntities = fetchStepsForPipeline(pipelineEntity.pipelineId());

        // Sort steps by stage
        stepEntities.sort(Comparator.comparing(StepEntity::order));

        // Build pipeline with step operations
        Pipeline pipeline = buildPipeline(stepEntities, pipelineEntity.pipelineId(), startStage);

        // Execute pipeline, passing payment method for step operations that need it
        try {
            return pipeline.execute(initialInput);
        } catch (PipelineExecutionError e) {
            if (e.getErrorInfo().operationName() == OperationName.REFUND_PAYMENT ||
                    e.getErrorInfo().operationName() == OperationName.REFUND_PERSONAL_CREDIT) {
                registerManualAction(
                        initialInput.order().orderId(),
                        pipelineEntity.pipelineId(),
                        e.getErrorInfo().stage(),
                        e.getErrorInfo().message(),
                        operationNameToManualActionCause(e.getErrorInfo().operationName()));
            }
            throw e;
        }
    }

    // ==================== Domain Models ====================

    enum PaymentMethod {
        PERSONAL_CREDIT,
        CREDIT_CARD,
        POINTS
    }

    enum OperationName {
        REFUND_PAYMENT,
        REFUND_PERSONAL_CREDIT,
        REFUND_POINTS
    }

    enum ManualActionCause {
        REFUND_PAYMENT_ERROR,
        REFUND_PERSONAL_CREDIT_ERROR
    }

    record OrderLineStatus(
            Status status,
            Optional<ManualActionId> manualActionId
    ) {}

    record ManualActionId(String id) {}

    enum Status {
        PENDING,
        COMPLETED,
        FAILED
    }

    /**
     * Pipeline entity stored in DB
     */
    record PipelineEntity(
            String pipelineId,
            String pipelineHash  // Hash of step sequence - used for lookup
    ) {}

    /**
     * Step entity stored in DB
     */
    record StepEntity(
            String stepId,
            String pipelineId,
            OperationName operationName,
            Integer order,
            Integer stage
    ) {}

    /**
     * Manual action entity stored in DB (tracks steps that need manual intervention)
     */
    record ManualActionEntity(
            String manualActionId,
            String orderId,
            String pipelineId,
            Integer stage,
            String errorMessage,
            ManualActionCause cause
    ) {}

    // ==================== DTOs ====================

    // Placeholder types - replace with your actual types
    record OrderAssociatedIdsAndCreationDate(String orderId) {}
    record OrderLineDetails(String lineId) {}
    record ReturnPublicId(String id) {}
    record OrderPaymentInfo(String paymentId) {}

    public record RefundRequest(
            OrderAssociatedIdsAndCreationDate order,
            List<OrderLineDetails> orderLines
    ) {}

    public record RefundTransactionPaymentStepRequest(
            OrderAssociatedIdsAndCreationDate order,
            List<OrderLineDetails> orderLines,
            Optional<ReturnPublicId> returnPublicId
    ) {}

    public record RefundTransactionPaymentStepResponse(
            OrderAssociatedIdsAndCreationDate order,
            List<OrderLineDetails> orderLines,
            OrderPaymentInfo orderPaymentInfo,
            Optional<ReturnPublicId> returnPublicId
    ) {}

    public record RefundPaymentMethodStepResponse(
            OrderAssociatedIdsAndCreationDate order,
            List<OrderLineDetails> orderLines,
            Optional<ReturnPublicId> returnPublicId
    ) {}

    public record RefundPointsStepResponse(
            Optional<ReturnPublicId> returnPublicId,
            List<OrderLineDetails> orderLines,
            OrderAssociatedIdsAndCreationDate order
    ) {}

    public static class StepOperationExecutionError extends RuntimeException {
        public StepOperationExecutionError(String message) {
            super(message);
        }
    }

    public static class PipelineExecutionError extends RuntimeException {
        private final ErrorInfo errorInfo;
        public record ErrorInfo(
                String message,
                OperationName operationName,
                Integer stage,
                String pipelineId
        ) {}
        public PipelineExecutionError(String message, OperationName operationName, Integer stage, String pipelineId) {
            this.errorInfo = new ErrorInfo(message, operationName, stage, pipelineId);
        }
        public ErrorInfo getErrorInfo() {
            return errorInfo;
        }
    }

    // ==================== Pipeline Execution ====================

    static class Pipeline {

        private final Integer DEFAULT_INITIAL_STAGE = 0;

        private final String pipelineId;
        private final List<Step> steps;
        private final Integer startStage;

        public Pipeline(String pipelineId, List<Step> steps, Optional<Integer> startStage) {
            this.pipelineId = pipelineId;
            this.steps = steps;
            this.startStage = startStage.orElse(DEFAULT_INITIAL_STAGE);
        }

        public <I, O> O execute(I input) {
            Object currentInput = input;

            // Transform through steps before startStage (skipped steps)
            for (Step step : steps) {
                if (step.getStage() < startStage) {
                    System.out.println("Skipping step (before start stage): " + step.getOperationName() +
                            " (stage " + step.getStage() + ")");
                    @SuppressWarnings("unchecked")
                    StepOperation<Object, Object> operation = (StepOperation<Object, Object>) step.getOperation();
                    currentInput = operation.transformForSkip(currentInput);
                }
            }

            // Execute steps from startStage onwards
            for (Step step : steps) {
                if (step.getStage() < startStage) {
                    continue; // Skip steps before startStage
                }

                try {
                    System.out.println("Executing step: " + step.getOperationName() + " (stage " + step.getStage() + ")");
                    @SuppressWarnings("unchecked")
                    StepOperation<Object, Object> operation = (StepOperation<Object, Object>) step.getOperation();
                    currentInput = operation.execute(currentInput);
                } catch (StepOperationExecutionError e) {
                    throw new PipelineExecutionError(e.getMessage(), step.getOperationName(), step.getStage(), pipelineId);
                }
            }

            @SuppressWarnings("unchecked")
            O result = (O) currentInput;
            return result;
        }
    }


    static class Step {
        private final Integer stage;
        private final Integer order;
        private final OperationName operationName;
        private final StepOperation<?, ?> operation;

        public Step(Integer order, Integer stage, OperationName operationName, StepOperation<?, ?> operation) {
            this.order = order;
            this.stage = stage;
            this.operationName = operationName;
            this.operation = operation;
        }

        public Integer getStage() { return stage; }
        public OperationName getOperationName() { return operationName; }
        public StepOperation<?, ?> getOperation() { return operation; }
        public Integer getOrder() { return order; }
    }

    interface StepOperation<I, O> {
        /**
         * Executes the step operation
         */
        O execute(I input);

        /**
         * Transforms input to output DTO when step is skipped (already executed).
         * This allows the pipeline to continue with the correct DTO type even when
         * skipping completed steps.
         */
        O transformForSkip(I input);

    }
    // ==================== Step Operations ====================

    static class RefundPaymentStepOp implements StepOperation<RefundRequest, RefundTransactionPaymentStepResponse> {
        @Override
        public RefundTransactionPaymentStepResponse execute(RefundRequest input) {
            try {
                // Convert RefundRequest to RefundTransactionPaymentStepRequest
                RefundTransactionPaymentStepRequest stepRequest = new RefundTransactionPaymentStepRequest(
                        input.order(),
                        input.orderLines(),
                        Optional.empty()  // returnPublicId will be created during execution
                );

                // Perform actual refund payment logic here
                // In real scenario, this would call the payment service and get orderPaymentInfo
                OrderPaymentInfo orderPaymentInfo = new OrderPaymentInfo("PAY-INFO-" + UUID.randomUUID().toString().substring(0, 8));
                Optional<ReturnPublicId> returnPublicId = Optional.of(new ReturnPublicId("RET-" + UUID.randomUUID().toString().substring(0, 8)));
                throw new StepOperationExecutionError("Simulated refund payment error");
            /*return new RefundTransactionPaymentStepResponse(
                stepRequest.order(),
                stepRequest.orderLines(),
                orderPaymentInfo,
                returnPublicId
            );*/
            } catch (Exception e) {
                throw new StepOperationExecutionError(e.getMessage());
            }
        }

        @Override
        public RefundTransactionPaymentStepResponse transformForSkip(RefundRequest input) {
            // When skipping, transform input to expected output DTO
            // In a real scenario, you would fetch orderPaymentInfo and returnPublicId from DB
            RefundTransactionPaymentStepRequest stepRequest = new RefundTransactionPaymentStepRequest(
                    input.order(),
                    input.orderLines(),
                    Optional.empty()
            );

            // Fetch from DB or reconstruct - placeholder for now
            OrderPaymentInfo orderPaymentInfo = new OrderPaymentInfo("PAY-INFO-SKIPPED");
            Optional<ReturnPublicId> returnPublicId = Optional.of(new ReturnPublicId("RET-SKIPPED"));

            return new RefundTransactionPaymentStepResponse(
                    stepRequest.order(),
                    stepRequest.orderLines(),
                    orderPaymentInfo,
                    returnPublicId
            );
        }
    }

    static class RefundPersonalCreditStepOp implements StepOperation<RefundTransactionPaymentStepResponse, RefundPaymentMethodStepResponse> {
        @Override
        public RefundPaymentMethodStepResponse execute(RefundTransactionPaymentStepResponse input) {
            // This method is called when payment method is not available
            // Default behavior: just transform (for backward compatibility)
            return transformForSkip(input);
        }

        /**
         * Executes with payment method check - if not personal credit, just returns transformed output
         */
        public RefundPaymentMethodStepResponse executeWithPaymentMethod(
                RefundTransactionPaymentStepResponse input, PaymentMethod paymentMethod) {

            // If payment method is not personal credit, just return transformed output without processing
            if (paymentMethod != PaymentMethod.PERSONAL_CREDIT) {
                System.out.println("Skipping personal credit refund - payment method is: " + paymentMethod);
                return transformForSkip(input);
            }

            // Perform actual personal credit refund logic here
            // In real scenario, this would process the personal credit refund
            // and potentially update returnPublicId

            return new RefundPaymentMethodStepResponse(
                    input.order(),
                    input.orderLines(),
                    input.returnPublicId()  // May be updated during execution
            );
        }

        @Override
        public RefundPaymentMethodStepResponse transformForSkip(RefundTransactionPaymentStepResponse input) {
            // When skipping, transform input to expected output DTO
            // Remove orderPaymentInfo and keep the rest
            return new RefundPaymentMethodStepResponse(
                    input.order(),
                    input.orderLines(),
                    input.returnPublicId()
            );
        }
    }

    static class RefundPointsStepOp implements StepOperation<Object, RefundPointsStepResponse> {
        @Override
        public RefundPointsStepResponse execute(Object input) {

            // Handle both RefundPaymentMethodStepResponse and RefundTransactionPaymentStepResponse as input
            OrderAssociatedIdsAndCreationDate order;
            List<OrderLineDetails> orderLines;
            Optional<ReturnPublicId> returnPublicId;

            if (input instanceof RefundPaymentMethodStepResponse rpm) {
                order = rpm.order();
                orderLines = rpm.orderLines();
                returnPublicId = rpm.returnPublicId();
            } else if (input instanceof RefundTransactionPaymentStepResponse rtp) {
                order = rtp.order();
                orderLines = rtp.orderLines();
                returnPublicId = rtp.returnPublicId();
            } else {
                throw new IllegalArgumentException("Unexpected input type: " + input.getClass());
            }

            // Perform actual points refund logic here
            // In real scenario, this would process the points refund
            // and potentially update returnPublicId

            return new RefundPointsStepResponse(
                    returnPublicId,
                    orderLines,
                    order
            );
        }

        @Override
        public RefundPointsStepResponse transformForSkip(Object input) {
            // When skipping, transform input to expected output DTO
            OrderAssociatedIdsAndCreationDate order;
            List<OrderLineDetails> orderLines;
            Optional<ReturnPublicId> returnPublicId;

            if (input instanceof RefundPaymentMethodStepResponse rpm) {
                order = rpm.order();
                orderLines = rpm.orderLines();
                returnPublicId = rpm.returnPublicId();
            } else if (input instanceof RefundTransactionPaymentStepResponse rtp) {
                order = rtp.order();
                orderLines = rtp.orderLines();
                returnPublicId = rtp.returnPublicId();
            } else {
                throw new IllegalArgumentException("Unexpected input type: " + input.getClass());
            }

            return new RefundPointsStepResponse(
                    returnPublicId,
                    orderLines,
                    order
            );
        }
    }

    // ==================== Pipeline Factory ====================

    /**
     * Creates pipeline steps - always includes all steps regardless of payment method
     */
    static List<StepDefinition> createPipelineSteps() {
        List<StepDefinition> steps = new ArrayList<>();

        // Always execute all steps: refundPayment, refundPersonalCredit, refundPoints
        steps.add(new StepDefinition(OperationName.REFUND_PAYMENT, 0, 0));
        steps.add(new StepDefinition(OperationName.REFUND_PERSONAL_CREDIT, 1, 0));
        steps.add(new StepDefinition(OperationName.REFUND_POINTS, 2, 1));

        return steps;
    }

    record StepDefinition(OperationName operationName, Integer order, Integer stage) {}

    // ==================== Database Operations ====================

    private static PipelineEntity fetchPipelineById(String pipelineId) {
        return Optional.ofNullable(pipelineStore.get(pipelineId)).orElseThrow(() -> new RuntimeException("Pipeline not found"));
    }

    /**
     * Fetches existing pipeline by hash or creates a new one
     */
    static PipelineEntity fetchOrCreatePipeline(List<StepDefinition> stepDefinitions) {
        String pipelineHash = computePipelineHash(stepDefinitions);

        // Try to find existing pipeline by hash
        Optional<PipelineEntity> existing = pipelineStore.values().stream()
                .filter(p -> p.pipelineHash().equals(pipelineHash))
                .findFirst();

        if (existing.isPresent()) {
            System.out.println("Found existing pipeline with hash: " + pipelineHash);
            return existing.get();
        }

        // Create new pipeline
        String pipelineId = UUID.randomUUID().toString();

        PipelineEntity pipelineEntity = new PipelineEntity(
                pipelineId,
                pipelineHash
        );

        pipelineStore.put(pipelineId, pipelineEntity);

        // Create step entities
        for (StepDefinition stepDef : stepDefinitions) {
            String stepId = UUID.randomUUID().toString();
            StepEntity stepEntity = new StepEntity(
                    stepId,
                    pipelineId,
                    stepDef.operationName(),
                    stepDef.order(),
                    stepDef.stage()
            );
            stepStore.put(stepId, stepEntity);
        }

        System.out.println("Created new pipeline with hash: " + pipelineHash +
                " and " + stepDefinitions.size() + " steps");

        return pipelineEntity;
    }

    /**
     * Fetches all steps for a pipeline
     */
    static List<StepEntity> fetchStepsForPipeline(String pipelineId) {
        return stepStore.values().stream()
                .filter(step -> step.pipelineId().equals(pipelineId))
                .collect(Collectors.toList());
    }

    /**
     * Builds executable pipeline from step entities
     */
    private static Pipeline buildPipeline(List<StepEntity> stepEntities, String pipelineId, Optional<Integer> startStage) {
        List<Step> steps = new ArrayList<>();

        for (StepEntity stepEntity : stepEntities) {
            StepOperation<?, ?> operation = nameToStepOperation(stepEntity.operationName());
            Step step = new Step(stepEntity.order(), stepEntity.stage(), stepEntity.operationName(), operation);
            steps.add(step);
        }

        return new Pipeline(pipelineId, steps, startStage);
    }

    /**
     * Maps operation name to step operation instance
     */
    static StepOperation<?, ?> nameToStepOperation(OperationName name) {
        return switch (name) {
            case REFUND_PAYMENT -> new RefundPaymentStepOp();
            case REFUND_PERSONAL_CREDIT -> new RefundPersonalCreditStepOp();
            case REFUND_POINTS -> new RefundPointsStepOp();
        };
    }

    static ManualActionCause operationNameToManualActionCause(OperationName operationName) {
        return switch (operationName) {
            case REFUND_PAYMENT -> ManualActionCause.REFUND_PAYMENT_ERROR;
            case REFUND_PERSONAL_CREDIT -> ManualActionCause.REFUND_PERSONAL_CREDIT_ERROR;
            default -> throw new IllegalArgumentException("Invalid manual action cause name: " + operationName);
        };
    }

    /**
     * Registers a manual action when a step fails
     */
    static void registerManualAction(String orderId, String pipelineId, Integer stage,
                                     String errorMessage, ManualActionCause cause) {
        String manualActionId = UUID.randomUUID().toString();
        ManualActionEntity manualAction = new ManualActionEntity(
                manualActionId,
                orderId,
                pipelineId,
                stage,
                errorMessage,
                cause
        );
        manualActionStore.put(manualActionId, manualAction);
        orderLineStatusStore.put(orderId, new OrderLineStatus(Status.FAILED, Optional.of(new ManualActionId(manualActionId))));
    }

    /**
     * Computes hash of pipeline step sequence
     */
    private static String computePipelineHash(List<StepDefinition> steps) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> stepNamesAndStages = steps.stream()
                    .map(step -> step.operationName() + "-" + step.stage())
                    .collect(Collectors.toList());
            String json = objectMapper.writeValueAsString(stepNamesAndStages);
            return DigestUtils.sha256Hex(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error computing pipeline hash", e);
        }
    }
}
