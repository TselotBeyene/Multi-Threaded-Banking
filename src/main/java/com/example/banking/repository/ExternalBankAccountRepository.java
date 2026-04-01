package com.example.banking.repository;

import com.example.banking.entity.ExternalBankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalBankAccountRepository extends JpaRepository<ExternalBankAccountEntity, Long> {
}
