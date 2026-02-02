package org.example;

import java.util.List;
import java.util.Optional;
import org.example.order.dto.OrderAssociatedIdsAndCreationDate;
import org.example.order.dto.OrderLineDetails;
import org.example.order.dto.OrderLineStatus;
import org.example.order.OrderLineStatusRepo;
import org.example.order.dto.Status;
import org.example.pipeline.exceptions.PipelineExecutionError;
import org.example.refund.RefundOp;
import org.example.refund.dto.RefundPointsStepResponse;
import org.example.refund.dto.RefundTransactionPaymentStepRequest;

public class RefundPipelineV3 {

    public static void main(String[] args) {
        // Example: Process refund for Personal Credit payment method
        String orderId = "ORDER-12345";
        OrderLineStatusRepo.save(orderId, new OrderLineStatus(Status.PENDING, Optional.empty()));

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
}
