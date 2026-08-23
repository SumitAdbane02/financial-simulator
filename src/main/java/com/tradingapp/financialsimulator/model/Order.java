package com.tradingapp.financialsimulator.model;

import com.tradingapp.financialsimulator.model.enums.OrderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order {
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who placed the order.
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id",nullable = false)
     private User user;

    // The stock being ordered.
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "stock_id",nullable = false)
     private Stock stock;


    //The type of order
     @Enumerated(EnumType.STRING)
     @Column(nullable = false)
     private OrderType type;

    // The current status of the order
     @Enumerated(EnumType.STRING)
     @Column(nullable = false)
     private OrderStatus status;

    // The quantity of shares for the order.
     @Column(nullable = false,precision = 19,scale = 8)
     private BigDecimal quantity;

    // The timestamp when the order was created.
     @CreationTimestamp
     @Column(nullable = false,updatable = false)
     private LocalDateTime createdAt;

// The timestamp when the order was last updated
    @UpdateTimestamp
     @Column(nullable = false)
     private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    // The transaction generated after the order is executed.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;


}
