package com.tselot.banking.controller;

import com.tselot.banking.dto.AccountView;
import com.tselot.banking.dto.CreateAccountRequest;
import com.tselot.banking.dto.CreateExternalAccountRequest;
import com.tselot.banking.dto.CreateMerchantRequest;
import com.tselot.banking.dto.DashboardResponse;
import com.tselot.banking.dto.DepositRequest;
import com.tselot.banking.dto.ExternalAccountView;
import com.tselot.banking.dto.ExternalTransferRequest;
import com.tselot.banking.dto.LoginRequest;
import com.tselot.banking.dto.MerchantView;
import com.tselot.banking.dto.PaymentRequest;
import com.tselot.banking.dto.TransferRequest;
import com.tselot.banking.dto.WithdrawRequest;
import com.tselot.banking.entity.SessionEntity;
import com.tselot.banking.entity.TransactionResultEntity;
import com.tselot.banking.service.BankService;
import com.tselot.banking.service.ReportingEngine;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BankingController {
    private final BankService bankService;
    private final ReportingEngine<AccountView> accountReportingEngine;
    private final ReportingEngine<TransactionResultEntity> transactionReportingEngine;

    public BankingController(BankService bankService,
                             ReportingEngine<AccountView> accountReportingEngine,
                             ReportingEngine<TransactionResultEntity> transactionReportingEngine) {
        this.bankService = bankService;
        this.accountReportingEngine = accountReportingEngine;
        this.transactionReportingEngine = transactionReportingEngine;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return bankService.getDashboard();
    }

    @GetMapping("/accounts")
    public List<AccountView> accounts() {
        return bankService.getAccounts();
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountView> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bankService.createAccount(request));
    }

    @GetMapping("/external-accounts")
    public List<ExternalAccountView> externalAccounts() {
        return bankService.getExternalAccounts();
    }

    @PostMapping("/external-accounts")
    public ResponseEntity<ExternalAccountView> createExternalAccount(@Valid @RequestBody CreateExternalAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bankService.createExternalAccount(request));
    }

    @GetMapping("/merchants")
    public List<MerchantView> merchants() {
        return bankService.getMerchants();
    }

    @PostMapping("/merchants")
    public ResponseEntity<MerchantView> createMerchant(@Valid @RequestBody CreateMerchantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bankService.createMerchant(request));
    }

    @GetMapping("/sessions")
    public List<SessionEntity> sessions() {
        return bankService.getSessions();
    }

    @PostMapping("/sessions/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest request) {
        return Map.of("sessionId", bankService.login(request.ownerName()));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> logout(@PathVariable String sessionId) {
        bankService.logout(sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transactions/deposit")
    public TransactionResultEntity deposit(@Valid @RequestBody DepositRequest request) {
        return bankService.processDeposit(request.accountNumber(), request.amount(), request.description(), request.priority());
    }

    @PostMapping("/transactions/withdraw")
    public TransactionResultEntity withdraw(@Valid @RequestBody WithdrawRequest request) {
        return bankService.processWithdraw(request.accountNumber(), request.amount(), request.description(), request.priority());
    }

    @PostMapping("/transactions/transfer")
    public TransactionResultEntity transfer(@Valid @RequestBody TransferRequest request) {
        return bankService.processTransfer(request.sourceAccount(), request.targetAccount(), request.amount(), request.description(), request.priority());
    }

    @PostMapping("/transactions/external-transfer")
    public TransactionResultEntity externalTransfer(@Valid @RequestBody ExternalTransferRequest request) {
        return bankService.processExternalTransfer(request.sourceAccount(), request.externalAccountId(), request.amount(), request.description(), request.priority());
    }

    @PostMapping("/transactions/payment")
    public TransactionResultEntity payment(@Valid @RequestBody PaymentRequest request) {
        return bankService.processPayment(request.accountNumber(), request.merchantId(), request.amount(), request.description(), request.priority());
    }

    @GetMapping("/transactions/audit")
    public List<TransactionResultEntity> audit() {
        return bankService.getAuditTrail();
    }

    @GetMapping("/reports/accounts")
    public Map<String, String> accountReport() {
        return Map.of("report", bankService.generateAccountReport(accountReportingEngine));
    }

    @GetMapping("/reports/transactions")
    public Map<String, String> transactionReport() {
        return Map.of("report", bankService.generateTransactionReport(transactionReportingEngine));
    }
}
