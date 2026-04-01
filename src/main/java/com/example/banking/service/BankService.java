package com.example.banking.service;

import com.example.banking.dto.AccountView;
import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.dto.CreateExternalAccountRequest;
import com.example.banking.dto.CreateMerchantRequest;
import com.example.banking.dto.DashboardResponse;
import com.example.banking.dto.ExternalAccountView;
import com.example.banking.dto.MerchantView;
import com.example.banking.entity.AccountEntity;
import com.example.banking.entity.CheckingAccountEntity;
import com.example.banking.entity.ExternalBankAccountEntity;
import com.example.banking.entity.MerchantEntity;
import com.example.banking.entity.SavingsAccountEntity;
import com.example.banking.entity.SessionEntity;
import com.example.banking.entity.TransactionResultEntity;
import com.example.banking.entity.TransactionStatus;
import com.example.banking.exception.InsufficientFundsException;
import com.example.banking.exception.InvalidTransactionException;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.ExternalBankAccountRepository;
import com.example.banking.repository.MerchantRepository;
import com.example.banking.repository.SessionRepository;
import com.example.banking.repository.TransactionResultRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Transactional
public class BankService {
    private final AccountRepository accountRepository;
    private final ExternalBankAccountRepository externalBankAccountRepository;
    private final MerchantRepository merchantRepository;
    private final SessionRepository sessionRepository;
    private final TransactionResultRepository transactionResultRepository;
    private final TransactionProcessor transactionProcessor;
    private final EntityManager entityManager;
    private final ConcurrentHashMap<String, ReentrantLock> accountLocks = new ConcurrentHashMap<>();

    public BankService(AccountRepository accountRepository,
                       ExternalBankAccountRepository externalBankAccountRepository,
                       MerchantRepository merchantRepository,
                       SessionRepository sessionRepository,
                       TransactionResultRepository transactionResultRepository,
                       TransactionProcessor transactionProcessor,
                       EntityManager entityManager) {
        this.accountRepository = accountRepository;
        this.externalBankAccountRepository = externalBankAccountRepository;
        this.merchantRepository = merchantRepository;
        this.sessionRepository = sessionRepository;
        this.transactionResultRepository = transactionResultRepository;
        this.transactionProcessor = transactionProcessor;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<AccountView> getAccounts() {
        return accountRepository.findAll().stream()
                .sorted(Comparator.comparing(AccountEntity::getAccountNumber))
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExternalAccountView> getExternalAccounts() {
        return externalBankAccountRepository.findAll().stream()
                .sorted(Comparator.comparing(ExternalBankAccountEntity::getId))
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MerchantView> getMerchants() {
        return merchantRepository.findAll().stream()
                .sorted(Comparator.comparing(MerchantEntity::getId))
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SessionEntity> getSessions() {
        return sessionRepository.findAll().stream()
                .sorted(Comparator.comparing(SessionEntity::getLoginTime).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResultEntity> getAuditTrail() {
        return transactionResultRepository.findAllByOrderByProcessedAtDesc();
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        return new DashboardResponse(
                getAccounts(),
                getSessions(),
                transactionResultRepository.findTop10ByOrderByProcessedAtDesc(),
                transactionResultRepository.count(),
                (int) sessionRepository.count(),
                transactionProcessor.pendingCount()
        );
    }

    public AccountView createAccount(CreateAccountRequest request) {
        String type = request.accountType().trim().toUpperCase();
        if (accountRepository.existsById(request.accountNumber())) {
            throw new InvalidTransactionException("Account already exists: " + request.accountNumber());
        }
        AccountEntity account = switch (type) {
            case "CHECKING" -> new CheckingAccountEntity(
                    request.accountNumber(),
                    request.ownerName(),
                    amount(request.openingBalance()),
                    amount(request.minimumBalance())
            );
            case "SAVINGS" -> new SavingsAccountEntity(
                    request.accountNumber(),
                    request.ownerName(),
                    amount(request.openingBalance()),
                    amount(request.minimumBalance()),
                    positiveRateOrZero(request.interestRate())
            );
            default -> throw new InvalidTransactionException("Unsupported accountType. Use CHECKING or SAVINGS");
        };
        return toView(accountRepository.save(account));
    }

    public ExternalAccountView createExternalAccount(CreateExternalAccountRequest request) {
        ExternalBankAccountEntity entity = new ExternalBankAccountEntity(
                request.bankName().trim(),
                request.routingCode().trim(),
                request.accountNumber().trim(),
                request.ownerName().trim()
        );
        return toView(externalBankAccountRepository.save(entity));
    }

    public MerchantView createMerchant(CreateMerchantRequest request) {
        MerchantEntity entity = new MerchantEntity(
                request.merchantCode().trim(),
                request.merchantName().trim(),
                blankOrDefault(request.settlementReference(), request.merchantCode().trim())
        );
        return toView(merchantRepository.save(entity));
    }

    public synchronized String login(String ownerName) {
        String sessionId = UUID.randomUUID().toString();
        sessionRepository.save(new SessionEntity(sessionId, ownerName, Instant.now()));
        return sessionId;
    }

    public void logout(String sessionId) {
        sessionRepository.deleteById(sessionId);
    }

    public TransactionResultEntity processDeposit(String accountNumber, BigDecimal amount, String description, Integer priority) {
        String transactionId = queuedTransactionId(priority);
        return executeDeposit(transactionId, accountNumber, amount(amount), blankOrDefault(description, "Deposit"));
    }

    public TransactionResultEntity processWithdraw(String accountNumber, BigDecimal amount, String description, Integer priority) {
        String transactionId = queuedTransactionId(priority);
        return executeWithdraw(transactionId, accountNumber, amount(amount), blankOrDefault(description, "Withdrawal"));
    }

    public TransactionResultEntity processTransfer(String source, String target, BigDecimal amount, String description, Integer priority) {
        String transactionId = queuedTransactionId(priority);
        return executeInternalTransfer(transactionId, source, target, amount(amount), blankOrDefault(description, "Internal transfer"));
    }

    public TransactionResultEntity processExternalTransfer(String source, Long externalAccountId, BigDecimal amount, String description, Integer priority) {
        String transactionId = queuedTransactionId(priority);
        return executeExternalTransfer(transactionId, source, externalAccountId, amount(amount), blankOrDefault(description, "External transfer"));
    }

    public TransactionResultEntity processPayment(String accountNumber, Long merchantId, BigDecimal amount, String description, Integer priority) {
        String transactionId = queuedTransactionId(priority);
        return executeMerchantPayment(transactionId, accountNumber, merchantId, amount(amount), blankOrDefault(description, "Merchant payment"));
    }

    private String queuedTransactionId(Integer priority) {
        String transactionId = UUID.randomUUID().toString();
        transactionProcessor.submit(transactionId, safePriority(priority));
        String next = transactionProcessor.next();
        if (next == null) {
            throw new InvalidTransactionException("No transaction available for processing");
        }
        return next;
    }

    public TransactionResultEntity executeDeposit(String transactionId, String accountNumber, BigDecimal amount, String description) {
        ReentrantLock lock = localLock(accountNumber);
        lock.lock();
        try {
            AccountEntity account = requireAccountForUpdate(accountNumber);
            account.setBalance(account.getBalance().add(amount));
            account.incrementTransactionCount();
            accountRepository.save(account);
            entityManager.flush();
            return saveSuccess(transactionId, description + " completed for " + accountNumber + " amount " + amount);
        } catch (RuntimeException ex) {
            return saveFailure(transactionId, ex.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public TransactionResultEntity executeWithdraw(String transactionId, String accountNumber, BigDecimal amount, String description) {
        ReentrantLock lock = localLock(accountNumber);
        lock.lock();
        try {
            AccountEntity account = requireAccountForUpdate(accountNumber);
            assertCanWithdraw(account, amount);
            account.setBalance(account.getBalance().subtract(amount));
            account.incrementTransactionCount();
            accountRepository.save(account);
            entityManager.flush();
            return saveSuccess(transactionId, description + " completed for " + accountNumber + " amount " + amount);
        } catch (RuntimeException ex) {
            return saveFailure(transactionId, ex.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public TransactionResultEntity executeInternalTransfer(String transactionId, String sourceAccount, String targetAccount, BigDecimal amount, String description) {
        if (sourceAccount.equals(targetAccount)) {
            return saveFailure(transactionId, "Cannot transfer to the same account");
        }

        List<String> ordered = new ArrayList<>(List.of(sourceAccount, targetAccount));
        ordered.sort(String::compareTo);
        ReentrantLock firstLock = localLock(ordered.get(0));
        ReentrantLock secondLock = localLock(ordered.get(1));

        firstLock.lock();
        secondLock.lock();
        try {
            List<AccountEntity> accounts = accountRepository.findAllByAccountNumbersForUpdate(ordered);
            if (accounts.size() != 2) {
                throw new InvalidTransactionException("One or both accounts were not found");
            }
            AccountEntity first = accounts.get(0);
            AccountEntity second = accounts.get(1);
            AccountEntity source = first.getAccountNumber().equals(sourceAccount) ? first : second;
            AccountEntity target = source == first ? second : first;

            assertCanWithdraw(source, amount);
            source.setBalance(source.getBalance().subtract(amount));
            target.setBalance(target.getBalance().add(amount));
            source.incrementTransactionCount();
            target.incrementTransactionCount();
            accountRepository.save(source);
            accountRepository.save(target);
            entityManager.flush();
            return saveSuccess(transactionId, description + " completed from " + sourceAccount + " to " + targetAccount + " amount " + amount);
        } catch (RuntimeException ex) {
            return saveFailure(transactionId, ex.getMessage());
        } finally {
            secondLock.unlock();
            firstLock.unlock();
        }
    }

    public TransactionResultEntity executeExternalTransfer(String transactionId, String sourceAccount, Long externalAccountId, BigDecimal amount, String description) {
        ReentrantLock lock = localLock(sourceAccount);
        lock.lock();
        try {
            AccountEntity source = requireAccountForUpdate(sourceAccount);
            ExternalBankAccountEntity beneficiary = externalBankAccountRepository.findById(externalAccountId)
                    .filter(ExternalBankAccountEntity::isActive)
                    .orElseThrow(() -> new InvalidTransactionException("External account not found or inactive: " + externalAccountId));
            assertCanWithdraw(source, amount);
            source.setBalance(source.getBalance().subtract(amount));
            source.incrementTransactionCount();
            accountRepository.save(source);
            entityManager.flush();
            return saveSuccess(transactionId,
                    description + " completed from " + sourceAccount + " to external " + beneficiary.getBankName() + "/" + beneficiary.getAccountNumber() + " amount " + amount);
        } catch (RuntimeException ex) {
            return saveFailure(transactionId, ex.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public TransactionResultEntity executeMerchantPayment(String transactionId, String accountNumber, Long merchantId, BigDecimal amount, String description) {
        ReentrantLock lock = localLock(accountNumber);
        lock.lock();
        try {
            AccountEntity account = requireAccountForUpdate(accountNumber);
            MerchantEntity merchant = merchantRepository.findById(merchantId)
                    .filter(MerchantEntity::isActive)
                    .orElseThrow(() -> new InvalidTransactionException("Merchant not found or inactive: " + merchantId));
            assertCanWithdraw(account, amount);
            account.setBalance(account.getBalance().subtract(amount));
            account.incrementTransactionCount();
            accountRepository.save(account);
            entityManager.flush();
            return saveSuccess(transactionId,
                    description + " completed for merchant " + merchant.getMerchantName() + " amount " + amount + " settlement " + merchant.getSettlementReference());
        } catch (RuntimeException ex) {
            return saveFailure(transactionId, ex.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public String generateAccountReport(ReportingEngine<AccountView> engine) {
        return engine.generate(getAccounts(), view -> view.accountNumber() + " | " + view.ownerName() + " | " + view.accountType() + " | " + view.balance());
    }

    public String generateTransactionReport(ReportingEngine<TransactionResultEntity> engine) {
        return engine.generate(getAuditTrail(), result -> result.getTransactionId() + " | " + result.getStatus() + " | " + result.getMessage());
    }

    private AccountEntity requireAccountForUpdate(String accountNumber) {
        return accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new InvalidTransactionException("Account not found: " + accountNumber));
    }

    private void assertCanWithdraw(AccountEntity account, BigDecimal amount) {
        if (!account.canWithdraw(amount)) {
            throw new InsufficientFundsException("Insufficient funds in account " + account.getAccountNumber() + " for amount " + amount);
        }
    }

    private TransactionResultEntity saveSuccess(String transactionId, String message) {
        return transactionResultRepository.save(new TransactionResultEntity(transactionId, TransactionStatus.SUCCESS, message, Instant.now()));
    }

    private TransactionResultEntity saveFailure(String transactionId, String message) {
        return transactionResultRepository.save(new TransactionResultEntity(transactionId, TransactionStatus.FAILED, message, Instant.now()));
    }

    private AccountView toView(AccountEntity account) {
        return new AccountView(
                account.getAccountNumber(),
                account.getOwnerName(),
                account.getAccountType(),
                account.getBalance(),
                account.getMinimumBalance(),
                account.getTransactionCount()
        );
    }

    private ExternalAccountView toView(ExternalBankAccountEntity account) {
        return new ExternalAccountView(
                account.getId(),
                account.getBankName(),
                account.getRoutingCode(),
                account.getAccountNumber(),
                account.getOwnerName(),
                account.isActive()
        );
    }

    private MerchantView toView(MerchantEntity merchant) {
        return new MerchantView(
                merchant.getId(),
                merchant.getMerchantCode(),
                merchant.getMerchantName(),
                merchant.getSettlementReference(),
                merchant.isActive()
        );
    }

    private ReentrantLock localLock(String accountNumber) {
        return accountLocks.computeIfAbsent(accountNumber, ignored -> new ReentrantLock(true));
    }

    private int safePriority(Integer priority) {
        return priority == null ? 5 : Math.max(1, priority);
    }

    private String blankOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private BigDecimal amount(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_EVEN);
    }

    private BigDecimal positiveRateOrZero(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(5, RoundingMode.HALF_EVEN);
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTransactionException("interestRate cannot be negative");
        }
        return value.setScale(5, RoundingMode.HALF_EVEN);
    }
}
