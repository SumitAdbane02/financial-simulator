package com.tradingapp.financialsimulator.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a single, completed financial transaction (a buy or a sell).
 * This entity serves as an immutable audit log of all trading activity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Each transaction is associated with a user.
    // This allows for easy retrieval of a user's entire transaction history.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // The stock that was involved in the transaction.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    // The type of transaction (BUY or SELL), stored as a string for readability.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    // The number of shares traded in this transaction.
    // Using BigDecimal is essential for financial precision.
    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

    // The price per share at which the transaction was executed.
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    // The exact date and time when the transaction was created/executed.
    // @CreationTimestamp is a Hibernate-specific annotation that automatically
    // sets this field to the current timestamp when the entity is first persisted.
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}