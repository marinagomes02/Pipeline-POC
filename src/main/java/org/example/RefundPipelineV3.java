package org.example;

import java.util.List;
import java.util.Optional;

import org.example.order.OrderPaymentInfoRepo;
import org.example.order.dto.OrderAssociatedIdsAndCreationDate;
import org.example.order.dto.OrderLineDetails;
import org.example.order.dto.OrderLineStatus;
import org.example.order.OrderLineStatusRepo;
import org.example.order.dto.OrderPaymentInfo;
import org.example.order.dto.PaymentMethod;
import org.example.order.dto.Status;
import org.example.pipeline.exceptions.PipelineExecutionError;
import org.example.refund.Refund2Op;
import org.example.refund.RefundOp;
import org.example.refund.dto.RefundPointsStepResponse;
import org.example.refund.dto.RefundTransactionPaymentStepRequest;

public class RefundPipelineV3 {

    public static void main(String[] args) {

        tryRefundAndSecondStepFailsMustCreateManualActionAndRollbackFirstStepUseCase();

        tryNewRefundWithSameStepsShouldReusePipelineUseCase();

        tryNewRefundWithDifferentStepsShouldCreateNewPipelineUseCase();
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

    private static void tryNewRefundWithDifferentStepsShouldCreateNewPipelineUseCase() {
        // Simulate changes to the refundOp -> it will create a new pipeline
        System.out.println("=== Simulate pipeline change and execute ===");
        String newOrderId = "ORDER-67890";
        OrderLineStatusRepo.save(newOrderId, new OrderLineStatus(Status.PENDING, Optional.empty()));
        OrderPaymentInfoRepo.save(newOrderId, new OrderPaymentInfo(PaymentMethod.PERSONAL_CREDIT));


        RefundTransactionPaymentStepRequest changedInitialInput = new RefundTransactionPaymentStepRequest(
                new OrderAssociatedIdsAndCreationDate(newOrderId),
                List.of(new OrderLineDetails("LINE-3")),
                Optional.empty());

        try {
            RefundPointsStepResponse result4 = new Refund2Op().processRefund(changedInitialInput);
        } catch (PipelineExecutionError e) {
            System.out.println("Use case with changed pipeline finished");
        }
    }
}
