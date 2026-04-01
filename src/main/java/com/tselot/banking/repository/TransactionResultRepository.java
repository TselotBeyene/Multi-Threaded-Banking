package com.tselot.banking.repository;

import com.tselot.banking.entity.TransactionResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionResultRepository extends JpaRepository<TransactionResultEntity, Long> {
    List<TransactionResultEntity> findTop10ByOrderByProcessedAtDesc();
    List<TransactionResultEntity> findAllByOrderByProcessedAtDesc();
}
