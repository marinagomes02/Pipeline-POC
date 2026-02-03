package org.poc.pipeline.refund.dto;

public enum RefundOperationName {
    REFUND_PAYMENT("REFUND_PAYMENT"),
    REFUND_PERSONAL_CREDIT("REFUND_PERSONAL_CREDIT"),
    REFUND_POINTS("REFUND_POINTS");

    private final String value;

    RefundOperationName(String value) {
        this.value = value;
    }

    public static RefundOperationName from(String name) {
        for (RefundOperationName operationName : RefundOperationName.values()) {
            if (operationName.name().equals(name)) {
                return operationName;
            }
        }
        throw new IllegalArgumentException("No enum constant with name " + name);
    }

    public String value() {
        return value;
    }
}
