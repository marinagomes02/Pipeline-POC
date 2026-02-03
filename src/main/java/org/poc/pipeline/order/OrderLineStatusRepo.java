package org.poc.pipeline.order;

import org.poc.pipeline.order.dto.OrderLineStatus;

import java.util.HashMap;
import java.util.Map;

public class OrderLineStatusRepo {

    private static final Map<String, OrderLineStatus> orderLineStatusStore = new HashMap<>();

    public static void save(String orderLineId, OrderLineStatus status) {
        orderLineStatusStore.put(orderLineId, status);
    }

    public static OrderLineStatus get(String orderLineId) {
        return orderLineStatusStore.get(orderLineId);
    }
}
