package com.example.banking.dto;

import com.example.banking.entity.SessionEntity;
import com.example.banking.entity.TransactionResultEntity;

import java.util.List;

public record DashboardResponse(
        List<AccountView> accounts,
        List<SessionEntity> sessions,
        List<TransactionResultEntity> auditTrail,
        long totalTransactions,
        int activeSessionCount,
        int pendingQueueCount
) {}
