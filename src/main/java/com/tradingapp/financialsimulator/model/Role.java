package com.tradingapp.financialsimulator.model;

/**
 * Defines the roles a user can have within the application.
 * Using an enum for roles provides type safety and makes the code more readable
 * and maintainable compared to using plain strings.
 */
public enum Role {
    // A standard user role, allowed to perform trading activities.

    USER,
    // An administrator role, which might have additional privileges in the future (e.g., managing users).

    ADMIN
}
