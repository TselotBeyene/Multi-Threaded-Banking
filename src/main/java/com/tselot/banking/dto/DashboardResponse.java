package com.tselot.banking.dto;

import com.tselot.banking.entity.SessionEntity;
import com.tselot.banking.entity.TransactionResultEntity;

import java.util.List;

public record DashboardResponse(
        List<AccountView> accounts,
        List<SessionEntity> sessions,
        List<TransactionResultEntity> auditTrail,
        long totalTransactions,
        int activeSessionCount,
        int pendingQueueCount
) {}
