# Multi-Threaded Banking & Transaction System

A full Java 21 Maven project that demonstrates:

- **OOP**: abstract `Account`, `CheckingAccount`, `SavingsAccount`
- **Interfaces**: `PaymentGateway`
- **Concurrency**: `synchronized`, `ReentrantLock`, `AtomicLong`, `AtomicInteger`
- **Collections**: `ConcurrentHashMap` and `PriorityQueue`
- **Exception Handling**: `InsufficientFundsException`, `InvalidTransactionException`
- **Generics**: reusable `ReportingEngine<T>`
- **Memory model notes**: visibility, atomicity, lock ordering, race-condition prevention

## Project Structure

- `account/` account hierarchy and locking rules
- `transaction/` transaction types and priority processing
- `service/` bank logic, transfer engine, reporting
- `concurrency/` concurrent simulation utilities
- `gateway/` payment gateway abstraction and mock implementation
- `session/` active user session model
- `model/` status/result/value objects
- `exception/` domain exceptions

## Run

```bash
mvn clean test
mvn exec:java
```

## What it demonstrates

### Java memory and simultaneous operations

- `synchronized` on login serializes session creation and creates a happens-before relationship.
- `ReentrantLock` protects each account's mutable balance and transaction count.
- `AtomicLong` and `AtomicInteger` provide lock-free atomic counters with visibility guarantees.
- `ConcurrentHashMap` allows concurrent account/session access without external locking.
- Transfer logic uses **deterministic lock ordering** to avoid deadlocks when two threads transfer in opposite directions.
- Priority transaction processing is modeled with `PriorityQueue` ordered by transaction priority and time.

### Included demo scenarios

- Sequential deposits, withdrawals, transfers, and gateway payments
- Multi-threaded concurrent transfers across several accounts
- Generic reporting over accounts and transaction results
- JUnit tests for data integrity under concurrency
