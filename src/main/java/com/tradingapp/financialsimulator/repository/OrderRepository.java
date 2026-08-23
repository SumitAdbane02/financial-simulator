package com.tradingapp.financialsimulator.repository;

import com.tradingapp.financialsimulator.model.Order;
import com.tradingapp.financialsimulator.model.OrderStatus;
import com.tradingapp.financialsimulator.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    List<Order> findAllByStatus(OrderStatus status);
}