package com.example.banking.repository;

import com.example.banking.entity.TransactionResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionResultRepository extends JpaRepository<TransactionResultEntity, Long> {
    List<TransactionResultEntity> findTop10ByOrderByProcessedAtDesc();
    List<TransactionResultEntity> findAllByOrderByProcessedAtDesc();
}
