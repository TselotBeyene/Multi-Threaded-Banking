package com.tselot.banking.repository;

import com.tselot.banking.entity.ExternalBankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalBankAccountRepository extends JpaRepository<ExternalBankAccountEntity, Long> {
}
