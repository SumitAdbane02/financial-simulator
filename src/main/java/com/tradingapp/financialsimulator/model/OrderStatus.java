package com.tradingapp.financialsimulator.model;
// Defines the possible states of a trading order.
public enum OrderStatus {
    // The order has been submitted but not yet executed. This applies to limit/stop orders.
    PENDING,
    // The order has been successfully executed.
    EXECUTED,
    // The order was cancelled by the user before execution.
    CANCELLED,
    // The order failed to execute due to an issue (e.g., insufficient funds, validation error).
    FAILED
}
