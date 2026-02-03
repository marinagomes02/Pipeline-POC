package org.example.manualaction;

import org.example.manualaction.dto.ManualActionCause;
import org.example.manualaction.dto.ManualActionEntity;
import org.example.manualaction.dto.ManualActionId;
import org.example.manualaction.dto.ManualActionRepo;
import org.example.order.OrderLineStatusRepo;
import org.example.order.dto.OrderLineStatus;
import org.example.order.dto.Status;

import java.util.Optional;
import java.util.UUID;

public class RegisterManualActionOp {

    public void execute(
            String orderId,
            String pipelineId,
            Integer stage,
            Integer nextStage,
            String errorMessage,
            ManualActionCause cause
    ) {
        ManualActionId manualActionId = new ManualActionId(UUID.randomUUID().toString());
        ManualActionEntity manualAction = new ManualActionEntity(
                manualActionId,
                orderId,
                pipelineId,
                stage,
                nextStage,
                errorMessage,
                cause);
        ManualActionRepo.save(manualAction);
        OrderLineStatusRepo.save(orderId, new OrderLineStatus(Status.FAILED, Optional.of(manualActionId)));
        System.out.println("Registered manual action: " + manualAction);
    }
}
