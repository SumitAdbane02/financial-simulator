package com.tradingapp.financialsimulator.repository;

import com.tradingapp.financialsimulator.model.Portfolio;
import com.tradingapp.financialsimulator.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio,Long> {
    Optional<Portfolio> findByUser(User user);

    Optional<Portfolio> findByUserId(Long userId);
}
