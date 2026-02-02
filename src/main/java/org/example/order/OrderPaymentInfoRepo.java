package org.example.order;

import org.example.order.dto.OrderPaymentInfo;

import java.util.HashMap;
import java.util.Map;

public class OrderPaymentInfoRepo {

    private static final Map<String, OrderPaymentInfo> orderPaymentInfoStore = new HashMap<>();

    public static void save(String orderId, OrderPaymentInfo paymentInfo) {
        orderPaymentInfoStore.put(orderId, paymentInfo);
    }

    public static OrderPaymentInfo get(String orderId) {
        return orderPaymentInfoStore.get(orderId);
    }
}
