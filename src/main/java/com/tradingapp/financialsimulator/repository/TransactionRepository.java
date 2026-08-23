package com.tradingapp.financialsimulator.repository;

import com.tradingapp.financialsimulator.model.Transaction;
import com.tradingapp.financialsimulator.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    Page<Transaction> findByUserOrderByTimestampDesc(User user, Pageable pageable);


}
