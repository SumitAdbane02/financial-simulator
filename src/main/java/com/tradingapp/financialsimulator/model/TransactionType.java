package com.tradingapp.financialsimulator.model;

/**
 * Defines the type of a financial transaction.
 * Using an enum prevents errors from using raw strings and makes the code's intent clearer.
 */
public enum TransactionType {
    // Represents a purchase of a stock.
    BUY,

    // Represents a sale of a stock.
    SELL
}