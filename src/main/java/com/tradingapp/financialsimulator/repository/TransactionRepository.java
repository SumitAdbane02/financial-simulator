package com.tradingapp.financialsimulator.repository;

import com.tradingapp.financialsimulator.model.Portfolio;
import com.tradingapp.financialsimulator.model.Transaction;
import com.tradingapp.financialsimulator.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    List<Transaction> findByPortfolioOrderByTimestampAsc(Portfolio portfolio);

    Page<Transaction> findByUserOrderByTimestampDesc(User user, Pageable pageable);


}
