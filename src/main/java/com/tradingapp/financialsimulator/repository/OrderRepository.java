package com.tradingapp.financialsimulator.repository;

import com.tradingapp.financialsimulator.model.Order;
import com.tradingapp.financialsimulator.model.OrderStatus;
import com.tradingapp.financialsimulator.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository {
    List<Order> findByUser(User user);

    List<Order> findAllByStatus(OrderStatus status);

}
