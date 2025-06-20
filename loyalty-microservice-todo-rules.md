# LOYALTY MICROSERVICE - CQRS + AXON + KAFKA TODO RULES

## 📋 TỔNG QUAN KIẾN TRÚC
Microservice Loyalty sử dụng **CQRS + Event Sourcing** với **Axon Framework** và **Kafka** cho event streaming

### 🏗️ Architecture Components:
- **Commands** - Write operations
- **Events** - Domain events  
- **Aggregates** - Business logic containers
- **Projections** - Read models
- **Sagas** - Cross-aggregate workflows
- **Kafka** - Event streaming & messaging

---

## 🎯 1. MODULE MEMBER (THÀNH VIÊN) - CQRS

### 🔧 1.1 Member Aggregate & Commands
- [ ] **Member Aggregate:**
  - `@Aggregate` class MemberAggregate
  - `@AggregateIdentifier` memberId (UUID)
  - State: email, phone, name, status, tier, etc.
  - Business logic validation

- [ ] **Member Commands:**
  - `CreateMemberCommand(memberId, email, firstName, lastName, phone)`
  - `UpdateMemberProfileCommand(memberId, profileData)`  
  - `VerifyMemberEmailCommand(memberId, verificationCode)`
  - `VerifyMemberPhoneCommand(memberId, otpCode)`
  - `DeactivateMemberCommand(memberId, reason)`
  - `AssignMemberToTierCommand(memberId, tierId)`
  - `AddCustomAttributeCommand(memberId, key, value)`

- [ ] **Member Command Handlers:**
  - `@CommandHandler` methods trong MemberAggregate
  - Validation logic trước khi emit events
  - Business rules enforcement

### 🔧 1.2 Member Events
- [ ] **Member Domain Events:**
  - `MemberCreatedEvent(memberId, email, firstName, lastName, phone, timestamp)`
  - `MemberProfileUpdatedEvent(memberId, oldProfile, newProfile, timestamp)`
  - `MemberEmailVerifiedEvent(memberId, email, timestamp)`
  - `MemberPhoneVerifiedEvent(memberId, phone, timestamp)`
  - `MemberDeactivatedEvent(memberId, reason, timestamp)`
  - `MemberTierAssignedEvent(memberId, oldTierId, newTierId, timestamp)`
  - `CustomAttributeAddedEvent(memberId, key, value, timestamp)`

- [ ] **Event Handlers:**
  - `@EventHandler` methods để update projections
  - Kafka event publishing
  - External service notifications

### 🔧 1.3 Member Query Models & Projections
- [ ] **Member Read Models:**
  - `MemberProjection` - Complete member view
  - `MemberSummaryProjection` - List view data
  - `MemberAnalyticsProjection` - Analytics data
  - `MemberSegmentProjection` - Segmentation data

- [ ] **Member Query Handlers:**
  - `@QueryHandler` FindMemberByIdQuery
  - `@QueryHandler` FindMemberByEmailQuery  
  - `@QueryHandler` FindMembersBySegmentQuery
  - `@QueryHandler` GetMemberAnalyticsQuery
  - `@QueryHandler` GetMemberTimelineQuery

- [ ] **Member Projection Handlers:**
  - `@EventHandler` methods trong projection classes
  - Update read models khi receive events
  - Denormalized data optimization

### 🔧 1.4 Member APIs (CQRS Separation)
- [ ] **Command APIs (Write):**
  - `POST /api/members/commands/create`
  - `PUT /api/members/commands/{id}/update-profile`
  - `POST /api/members/commands/{id}/verify-email`
  - `POST /api/members/commands/{id}/verify-phone`
  - `DELETE /api/members/commands/{id}/deactivate`

- [ ] **Query APIs (Read):**
  - `GET /api/members/queries/{id}`
  - `GET /api/members/queries/by-email/{email}`
  - `GET /api/members/queries/search?filters=...`
  - `GET /api/members/queries/{id}/timeline`
  - `GET /api/members/queries/{id}/analytics`

---

## 🎯 2. MODULE TIER (PHÂN CẤP) - CQRS

### 🔧 2.1 Tier Aggregate & Commands
- [ ] **Tier Aggregate:**
  - `@Aggregate` class TierAggregate  
  - State: name, level, conditions, benefits
  - Tier calculation logic

- [ ] **Tier Commands:**
  - `CreateTierCommand(tierId, name, level, conditions, benefits)`
  - `UpdateTierCommand(tierId, updateData)`
  - `ActivateTierCommand(tierId)`
  - `DeactivateTierCommand(tierId)`
  - `CalculateMemberTierCommand(memberId, criteria)`

- [ ] **Tier Command Handlers:**
  - Validation logic cho tier rules
  - Business logic cho tier calculations

### 🔧 2.2 Tier Events
- [ ] **Tier Domain Events:**
  - `TierCreatedEvent(tierId, name, level, conditions, benefits)`
  - `TierUpdatedEvent(tierId, oldData, newData)`
  - `TierActivatedEvent(tierId)`
  - `TierDeactivatedEvent(tierId)`
  - `MemberTierCalculatedEvent(memberId, oldTierId, newTierId, criteria)`
  - `TierUpgradeTriggeredEvent(memberId, fromTier, toTier, reason)`
  - `TierDowngradeTriggeredEvent(memberId, fromTier, toTier, reason)`

### 🔧 2.3 Tier Projections & Queries
- [ ] **Tier Read Models:**
  - `TierConfigurationProjection` - Tier settings
  - `MemberTierProjection` - Member-tier relationships
  - `TierAnalyticsProjection` - Tier performance metrics

- [ ] **Tier Query Handlers:**
  - `@QueryHandler` GetAllTiersQuery
  - `@QueryHandler` GetTierByIdQuery
  - `@QueryHandler` GetMemberTierQuery
  - `@QueryHandler` GetTierDistributionQuery

### 🔧 2.4 Tier Saga (Cross-Aggregate Workflow)
- [ ] **TierUpgradeSaga:**
  - `@Saga` class để orchestrate tier upgrades
  - Listen to MemberTierCalculatedEvent
  - Trigger benefit granting
  - Send notifications
  - Handle rollback scenarios

---

## 🎯 3. MODULE WALLET (VÍ ĐIỆN TỬ) - CQRS

### 🔧 3.1 Wallet Aggregate & Commands
- [ ] **Wallet Aggregate:**
  - `@Aggregate` class WalletAggregate
  - State: balance, pending, limits, currency type
  - Transaction validation logic

- [ ] **Wallet Commands:**
  - `CreateWalletCommand(walletId, memberId, type, currency)`
  - `AddUnitsCommand(walletId, amount, description, reference)`
  - `DeductUnitsCommand(walletId, amount, description, reference)`
  - `TransferUnitsCommand(fromWalletId, toWalletId, amount, description)`
  - `ExpireUnitsCommand(walletId, amount, reason)`
  - `ActivatePendingUnitsCommand(walletId, amount)`

### 🔧 3.2 Wallet Events
- [ ] **Wallet Domain Events:**
  - `WalletCreatedEvent(walletId, memberId, type, currency)`
  - `UnitsAddedEvent(walletId, amount, balanceBefore, balanceAfter, description, reference)`
  - `UnitsDeductedEvent(walletId, amount, balanceBefore, balanceAfter, description, reference)`
  - `UnitsTransferredEvent(fromWalletId, toWalletId, amount, description)`
  - `UnitsExpiredEvent(walletId, amount, reason, balanceAfter)`
  - `PendingUnitsActivatedEvent(walletId, amount, balanceAfter)`
  - `WalletBalanceLimitExceededEvent(walletId, attemptedAmount, limit)`

### 🔧 3.3 Wallet Projections & Queries
- [ ] **Wallet Read Models:**
  - `WalletBalanceProjection` - Current balances
  - `UnitTransferProjection` - Transaction history
  - `WalletAnalyticsProjection` - Usage analytics
  - `ExpirationProjection` - Expiring units tracking

- [ ] **Wallet Query Handlers:**
  - `@QueryHandler` GetWalletBalanceQuery
  - `@QueryHandler` GetMemberWalletsQuery
  - `@QueryHandler` GetTransferHistoryQuery
  - `@QueryHandler` GetExpiringUnitsQuery

### 🔧 3.4 Wallet Sagas
- [ ] **UnitExpirationSaga:**
  - Schedule expiration events
  - Handle FIFO expiration logic
  - Send expiration notifications

- [ ] **TransferProcessingSaga:**
  - Orchestrate multi-wallet transfers
  - Handle failure scenarios
  - Ensure transactional consistency

---

## 🎯 4. MODULE TRANSACTION (GIAO DỊCH) - CQRS

### 🔧 4.1 Transaction Aggregate & Commands
- [ ] **Transaction Aggregate:**
  - `@Aggregate` class TransactionAggregate
  - State: items, total, member, status
  - Point calculation logic

- [ ] **Transaction Commands:**
  - `CreateTransactionCommand(transactionId, documentNumber, items, total)`
  - `MatchTransactionWithMemberCommand(transactionId, memberId, matchCriteria)`
  - `ProcessTransactionCommand(transactionId)`
  - `CalculatePointsCommand(transactionId, earningRules)`
  - `CancelTransactionCommand(transactionId, reason)`
  - `RefundTransactionCommand(transactionId, refundAmount, items)`

### 🔧 4.2 Transaction Events
- [ ] **Transaction Domain Events:**
  - `TransactionCreatedEvent(transactionId, documentNumber, items, total, timestamp)`
  - `TransactionMatchedWithMemberEvent(transactionId, memberId, matchCriteria)`
  - `TransactionProcessedEvent(transactionId, memberId, pointsEarned)`
  - `PointsCalculatedEvent(transactionId, pointsBreakdown, totalPoints)`
  - `TransactionCancelledEvent(transactionId, reason, timestamp)`
  - `TransactionRefundedEvent(transactionId, refundAmount, refundedItems)`

### 🔧 4.3 Transaction Projections & Queries  
- [ ] **Transaction Read Models:**
  - `TransactionProjection` - Complete transaction view
  - `MemberTransactionProjection` - Member transaction history
  - `TransactionAnalyticsProjection` - Revenue & volume analytics
  - `UnmatchedTransactionProjection` - Transactions cần matching

- [ ] **Transaction Query Handlers:**
  - `@QueryHandler` GetTransactionByIdQuery
  - `@QueryHandler` GetMemberTransactionsQuery
  - `@QueryHandler` GetUnmatchedTransactionsQuery
  - `@QueryHandler` GetTransactionAnalyticsQuery

### 🔧 4.4 Transaction Sagas
- [ ] **TransactionProcessingSaga:**
  - Orchestrate transaction flow
  - Handle member matching
  - Calculate và award points
  - Update tier calculations
  - Send confirmation notifications

---

## 🎯 5. KAFKA INTEGRATION & EVENT STREAMING

### 🔧 5.1 Kafka Configuration
- [ ] **Kafka Topics Setup:**
  - `loyalty.member.events` - Member domain events
  - `loyalty.tier.events` - Tier domain events  
  - `loyalty.wallet.events` - Wallet domain events
  - `loyalty.transaction.events` - Transaction domain events
  - `loyalty.notifications` - Notification events
  - `loyalty.analytics` - Analytics events

- [ ] **Kafka Producers:**
  - Axon Kafka extension configuration
  - Event serialization setup
  - Error handling và retry logic
  - Dead letter queue configuration

- [ ] **Kafka Consumers:**
  - Event replay capability
  - Consumer group management
  - Offset management
  - Parallel processing setup

### 🔧 5.2 Event Publishing & Subscription
- [ ] **Event Publishing:**
  - Automatic publishing từ Axon Event Store
  - Event transformation for external services
  - Schema registry integration
  - Event versioning strategy

- [ ] **Event Subscription:**
  - Cross-service event consumption
  - Event deduplication
  - Idempotent event processing
  - Event ordering guarantees

### 🔧 5.3 External Service Integration
- [ ] **Kafka Event Bridges:**
  - Email service events
  - SMS service events  
  - Analytics platform events
  - CRM integration events
  - Push notification events

---

## 🎯 6. AXON FRAMEWORK CONFIGURATION

### 🔧 6.1 Axon Setup & Configuration
- [ ] **Core Axon Configuration:**
  - `@EnableAxon` configuration
  - Event Store configuration (JPA/MongoDB)
  - Command Bus configuration
  - Query Bus configuration
  - Event Processing Groups

- [ ] **Serialization Setup:**
  - Jackson serialization for events/commands
  - Event upcasting for versioning
  - Snapshot configuration
  - Event encryption setup

### 🔧 6.2 Axon Monitoring & Metrics
- [ ] **Axon Metrics:**
  - Command processing metrics
  - Event processing metrics
  - Query processing metrics
  - Aggregate loading metrics

- [ ] **Health Checks:**
  - Event Store health
  - Command Bus health
  - Query Bus health
  - Kafka connectivity health

### 🔧 6.3 Axon Testing Framework
- [ ] **Test Fixtures:**
  - `AggregateTestFixture` cho unit tests
  - `SagaTestFixture` cho saga tests
  - Integration test configuration
  - Test event store setup

---

## 🎯 7. SAGA ORCHESTRATION

### 🔧 7.1 Cross-Module Sagas
- [ ] **LoyaltyProgramSaga:**
  - Orchestrate member onboarding
  - Welcome campaign activation
  - Initial wallet creation
  - First tier assignment

- [ ] **PointEarningSaga:**
  - Listen to TransactionProcessedEvent
  - Calculate points based on tier
  - Update wallet balances
  - Trigger tier recalculation

- [ ] **TierUpgradeSaga:**
  - Monitor tier qualification events
  - Orchestrate tier upgrade process
  - Grant tier benefits
  - Send upgrade notifications

- [ ] **ExpirationManagementSaga:**
  - Schedule expiration events
  - Send expiration warnings
  - Process unit expirations
  - Handle grace periods

### 🔧 7.2 Saga Error Handling
- [ ] **Compensation Actions:**
  - Rollback commands cho failed sagas
  - Compensation event publishing
  - Error notification system
  - Manual intervention triggers

---

## 🎯 8. QUERY SIDE OPTIMIZATION

### 🔧 8.1 Projection Management
- [ ] **Projection Rebuilding:**
  - Event replay từ Event Store
  - Projection reset commands
  - Incremental rebuilding
  - Projection versioning

- [ ] **Read Model Optimization:**
  - Denormalized data structures
  - Caching strategies (Redis)
  - Index optimization
  - Query performance monitoring

### 🔧 8.2 CQRS APIs
- [ ] **Command Side APIs:**
  - Async command processing
  - Command validation
  - Response with command tracking IDs
  - Error handling standards

- [ ] **Query Side APIs:**
  - Fast read operations
  - Pagination support
  - Filtering và sorting
  - Real-time data synchronization

---

## 🎯 9. EVENT SOURCING BENEFITS

### 🔧 9.1 Audit & Compliance
- [ ] **Complete Audit Trail:**
  - Every state change recorded
  - Immutable event history
  - Compliance reporting
  - Data forensics capability

- [ ] **Event Replay:**
  - Bug reproduction
  - System recovery
  - Business intelligence
  - Projection rebuilding

### 🔧 9.2 Temporal Queries
- [ ] **Historical Data Access:**
  - Point-in-time queries
  - Member state at any date
  - Transaction history analysis
  - Tier progression tracking

---

## 🎯 10. DEPLOYMENT & INFRASTRUCTURE

### 🔧 10.1 Container Setup
- [ ] **Docker Configuration:**
  - Multi-stage builds
  - JVM optimization
  - Health check endpoints
  - Resource limits

- [ ] **Kubernetes Deployment:**
  - StatefulSets cho Event Store
  - Deployments cho application
  - Services và ingress
  - ConfigMaps và secrets

### 🔧 10.2 Event Store Setup
- [ ] **Event Store Configuration:**
  - PostgreSQL với JSONB
  - Event table partitioning
  - Snapshot table setup
  - Backup strategies

- [ ] **Performance Optimization:**
  - Connection pooling
  - Query optimization
  - Event batching
  - Compression strategies

---

## 🚀 IMPLEMENTATION PHASES

### Phase 1 (Foundation):
1. ✅ Axon Framework setup
2. ✅ Kafka cluster configuration  
3. ✅ Member aggregate với basic commands
4. ✅ Wallet aggregate với unit operations
5. ✅ Basic projections và queries

### Phase 2 (Core Features):
1. ✅ Transaction processing saga
2. ✅ Tier calculation engine
3. ✅ Complete CQRS APIs
4. ✅ Event streaming setup
5. ✅ Cross-module sagas

### Phase 3 (Advanced):
1. ✅ Analytics projections
2. ✅ External integrations
3. ✅ Advanced sagas
4. ✅ Performance optimization
5. ✅ Monitoring & alerts

---

## 📝 CQRS + AXON BEST PRACTICES

- **Event Design:** Small, focused events với clear business meaning
- **Aggregate Size:** Keep aggregates small và focused
- **Command Validation:** Validate trong aggregate, không trong handlers
- **Event Ordering:** Use aggregate sequences cho event ordering
- **Saga Design:** Stateless sagas với clear compensation logic
- **Projection Updates:** Idempotent event handlers
- **Error Handling:** Graceful degradation và circuit breakers
- **Testing:** Behavior-driven testing với event scenarios
- **Monitoring:** Track command/query/event processing metrics
- **Deployment:** Blue-green deployments với event replay capability