package org.poc.pipeline;

import org.poc.pipeline.order.OrderLineStatusRepo;
import org.poc.pipeline.order.OrderPaymentInfoRepo;
import org.poc.pipeline.order.dto.OrderAssociatedIdsAndCreationDate;
import org.poc.pipeline.order.dto.OrderLineDetails;
import org.poc.pipeline.order.dto.OrderLineStatus;
import org.poc.pipeline.order.dto.OrderPaymentInfo;
import org.poc.pipeline.order.dto.PaymentMethod;
import org.poc.pipeline.order.dto.Status;
import org.poc.pipeline.pipeline.exceptions.PipelineExecutionError;
import org.poc.pipeline.refund.RefundAnotherOp;
import org.poc.pipeline.refund.RefundOp;
import org.poc.pipeline.refund.dto.RefundPaymentMethodStepResponse;
import org.poc.pipeline.refund.dto.RefundPointsStepResponse;
import org.poc.pipeline.refund.dto.RefundTransactionPaymentStepRequest;
import org.poc.pipeline.refund.dto.RefundTransactionPaymentStepResponse;

import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {

        tryRefundAndSecondStepFailsMustCreateManualActionAndRollbackFirstStepUseCase();

        tryNewRefundWithSameStepsShouldReusePipelineUseCase();

        tryRefundAndLastStepFailsMustCreateManualActionAndRollbackPreviousStepUseCase();
    }

    private static void tryRefundAndSecondStepFailsMustCreateManualActionAndRollbackFirstStepUseCase() {
        // Example: Process refund for Personal Credit payment method
        String orderId = "ORDER-12345";
        OrderLineStatusRepo.save(orderId, new OrderLineStatus(Status.PENDING, Optional.empty()));
        OrderPaymentInfoRepo.save(orderId, new OrderPaymentInfo(PaymentMethod.PERSONAL_CREDIT));

        RefundTransactionPaymentStepRequest initialInput = new RefundTransactionPaymentStepRequest(
                new OrderAssociatedIdsAndCreationDate(orderId),
                List.of(new OrderLineDetails("LINE-1")),
                Optional.empty());

        // Fail on refund payment
        System.out.println("=== First execution ===");
        try {
            RefundPointsStepResponse result = new RefundOp().processRefund(initialInput);
            System.out.println("Result: " + result);
        } catch (PipelineExecutionError e) {
            // DO nothing
        }
        System.out.println();

        // Simulate retry after failure (some steps already executed)
        System.out.println("=== Retry execution (resuming from last completed step) ===");
        RefundPointsStepResponse result2 = new RefundOp().processRefund(initialInput);
        System.out.println("Result: " + result2);
        System.out.println();
    }

    private static void tryNewRefundWithSameStepsShouldReusePipelineUseCase() {
        // Simulate another request that uses the same pipeline (a new pipeline will not be created)
        System.out.println("=== New request execution (should reuse existing pipeline) ===");
        String anotherOrderId = "ORDER-12346";
        OrderLineStatusRepo.save(anotherOrderId, new OrderLineStatus(Status.PENDING, Optional.empty()));
        OrderPaymentInfoRepo.save(anotherOrderId, new OrderPaymentInfo(PaymentMethod.PERSONAL_CREDIT));


        RefundTransactionPaymentStepRequest anotherInitialInput = new RefundTransactionPaymentStepRequest(
                new OrderAssociatedIdsAndCreationDate(anotherOrderId),
                List.of(new OrderLineDetails("LINE-2")),
                Optional.empty());

        try {
            RefundPointsStepResponse result3 = new RefundOp().processRefund(anotherInitialInput);
        } catch (PipelineExecutionError e) {
            System.out.println("Use case finished");
        }
        System.out.println();
    }

    private static void tryRefundAndLastStepFailsMustCreateManualActionAndRollbackPreviousStepUseCase() {
        // Simulate changes to the refundOp -> it will create a new pipeline
        System.out.println("=== Another pipeline and execute ===");
        String newOrderId = "ORDER-67890";
        OrderLineStatusRepo.save(newOrderId, new OrderLineStatus(Status.PENDING, Optional.empty()));
        OrderPaymentInfoRepo.save(newOrderId, new OrderPaymentInfo(PaymentMethod.PERSONAL_CREDIT));


        RefundTransactionPaymentStepRequest changedInitialInput = new RefundTransactionPaymentStepRequest(
                new OrderAssociatedIdsAndCreationDate(newOrderId),
                List.of(new OrderLineDetails("LINE-3")),
                Optional.empty());

        // Fail on refund payment
        System.out.println("=== First execution ===");
        try {
            RefundPaymentMethodStepResponse result = new RefundAnotherOp().processRefund(changedInitialInput);
        } catch (PipelineExecutionError e) {
                // DO nothing
        }

        System.out.println();

        // Simulate retry after failure (some steps already executed)
        System.out.println("=== Retry execution (resuming from last completed step) ===");
        RefundPaymentMethodStepResponse result2 = new RefundAnotherOp().processRefund(changedInitialInput);
        System.out.println("Result: " + result2);
        System.out.println();
    }
}