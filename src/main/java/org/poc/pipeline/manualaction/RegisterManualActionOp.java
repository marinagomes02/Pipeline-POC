package org.poc.pipeline.manualaction;

import org.poc.pipeline.manualaction.dto.ManualActionCause;
import org.poc.pipeline.manualaction.dto.ManualActionEntity;
import org.poc.pipeline.manualaction.dto.ManualActionId;
import org.poc.pipeline.manualaction.dto.ManualActionRepo;
import org.poc.pipeline.order.OrderLineStatusRepo;
import org.poc.pipeline.order.dto.OrderLineStatus;
import org.poc.pipeline.order.dto.Status;

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
