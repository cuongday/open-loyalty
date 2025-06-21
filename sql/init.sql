-- =====================================================
-- LOYALTY SERVICE DATABASE INITIALIZATION SCRIPT
-- =====================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- AXON EVENT STORE TABLES
-- =====================================================

-- Event Store table cho Event Sourcing
CREATE TABLE IF NOT EXISTS domain_event_entry (
    global_index BIGSERIAL PRIMARY KEY,
    aggregate_identifier VARCHAR(255) NOT NULL,
    sequence_number BIGINT NOT NULL,
    type VARCHAR(255) NOT NULL,
    event_identifier VARCHAR(255) NOT NULL UNIQUE,
    meta_data JSONB,
    payload JSONB NOT NULL,
    payload_revision VARCHAR(255),
    payload_type VARCHAR(255) NOT NULL,
    time_stamp VARCHAR(255) NOT NULL,
    UNIQUE (aggregate_identifier, sequence_number)
);

-- Snapshot table cho Aggregate snapshots
CREATE TABLE IF NOT EXISTS snapshot_event_entry (
    aggregate_identifier VARCHAR(255) PRIMARY KEY,
    sequence_number BIGINT NOT NULL,
    type VARCHAR(255) NOT NULL,
    event_identifier VARCHAR(255) NOT NULL UNIQUE,
    meta_data JSONB,
    payload JSONB NOT NULL,
    payload_revision VARCHAR(255),
    payload_type VARCHAR(255) NOT NULL,
    time_stamp VARCHAR(255) NOT NULL
);

-- Token Store table cho Event Tracking
CREATE TABLE IF NOT EXISTS token_entry (
    processor_name VARCHAR(255) PRIMARY KEY,
    segment INTEGER NOT NULL,
    token BYTEA,
    token_type VARCHAR(255),
    timestamp VARCHAR(255),
    owner VARCHAR(255)
);

-- Association Value Entry cho Saga tracking
CREATE TABLE IF NOT EXISTS association_value_entry (
    id BIGSERIAL PRIMARY KEY,
    association_key VARCHAR(255) NOT NULL,
    association_value VARCHAR(255) NOT NULL,
    saga_id VARCHAR(255) NOT NULL,
    saga_type VARCHAR(255) NOT NULL
);

-- Saga Entry table
CREATE TABLE IF NOT EXISTS saga_entry (
    saga_id VARCHAR(255) PRIMARY KEY,
    revision VARCHAR(255),
    saga_type VARCHAR(255) NOT NULL,
    serialized_saga BYTEA NOT NULL
);

-- =====================================================
-- MEMBER MODULE PROJECTIONS
-- =====================================================

-- Member Projection table
CREATE TABLE IF NOT EXISTS member_projection (
    member_id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    tier_id UUID,
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    date_of_birth DATE,
    gender VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Member Summary Projection cho list views
CREATE TABLE IF NOT EXISTS member_summary_projection (
    member_id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    status VARCHAR(50),
    tier_name VARCHAR(100),
    total_points BIGINT DEFAULT 0,
    total_transactions INTEGER DEFAULT 0,
    created_at TIMESTAMP,
    last_activity_at TIMESTAMP
);

-- Member Custom Attributes
CREATE TABLE IF NOT EXISTS member_custom_attributes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    member_id UUID NOT NULL REFERENCES member_projection(member_id) ON DELETE CASCADE,
    attribute_key VARCHAR(100) NOT NULL,
    attribute_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(member_id, attribute_key)
);

-- =====================================================
-- TIER MODULE PROJECTIONS
-- =====================================================

-- Tier Configuration Projection
CREATE TABLE IF NOT EXISTS tier_configuration_projection (
    tier_id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    level INTEGER NOT NULL UNIQUE,
    min_points BIGINT DEFAULT 0,
    min_transactions INTEGER DEFAULT 0,
    min_spend_amount DECIMAL(12,2) DEFAULT 0,
    validity_months INTEGER DEFAULT 12,
    benefits JSONB,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Member Tier History
CREATE TABLE IF NOT EXISTS member_tier_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    member_id UUID NOT NULL,
    from_tier_id UUID,
    to_tier_id UUID NOT NULL,
    reason VARCHAR(255),
    effective_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- WALLET MODULE PROJECTIONS
-- =====================================================

-- Wallet Balance Projection
CREATE TABLE IF NOT EXISTS wallet_balance_projection (
    wallet_id UUID PRIMARY KEY,
    member_id UUID NOT NULL,
    wallet_type VARCHAR(50) NOT NULL,
    currency_code VARCHAR(10) DEFAULT 'POINTS',
    available_balance BIGINT DEFAULT 0,
    pending_balance BIGINT DEFAULT 0,
    total_earned BIGINT DEFAULT 0,
    total_spent BIGINT DEFAULT 0,
    total_expired BIGINT DEFAULT 0,
    last_transaction_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    UNIQUE(member_id, wallet_type, currency_code)
);

-- Unit Transfer Projection cho transaction history
CREATE TABLE IF NOT EXISTS unit_transfer_projection (
    transfer_id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL,
    member_id UUID NOT NULL,
    transfer_type VARCHAR(50) NOT NULL, -- EARN, SPEND, TRANSFER_IN, TRANSFER_OUT, EXPIRE
    amount BIGINT NOT NULL,
    balance_before BIGINT NOT NULL,
    balance_after BIGINT NOT NULL,
    description TEXT,
    reference_id VARCHAR(255),
    reference_type VARCHAR(100),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiry_date TIMESTAMP,
    status VARCHAR(50) DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Unit Expiration Tracking
CREATE TABLE IF NOT EXISTS unit_expiration_projection (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet_id UUID NOT NULL,
    member_id UUID NOT NULL,
    amount BIGINT NOT NULL,
    earned_date TIMESTAMP NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE', -- ACTIVE, EXPIRED, USED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- TRANSACTION MODULE PROJECTIONS
-- =====================================================

-- Transaction Projection
CREATE TABLE IF NOT EXISTS transaction_projection (
    transaction_id UUID PRIMARY KEY,
    document_number VARCHAR(255) NOT NULL UNIQUE,
    member_id UUID,
    transaction_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    total_amount DECIMAL(12,2) NOT NULL,
    points_earned BIGINT DEFAULT 0,
    points_spent BIGINT DEFAULT 0,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    store_id VARCHAR(100),
    channel VARCHAR(50),
    items JSONB,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Unmatched Transaction Projection
CREATE TABLE IF NOT EXISTS unmatched_transaction_projection (
    transaction_id UUID PRIMARY KEY,
    document_number VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(20),
    customer_email VARCHAR(255),
    total_amount DECIMAL(12,2) NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    store_id VARCHAR(100),
    match_attempts INTEGER DEFAULT 0,
    last_match_attempt TIMESTAMP,
    status VARCHAR(50) DEFAULT 'PENDING_MATCH',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- ANALYTICS PROJECTIONS
-- =====================================================

-- Member Analytics Projection
CREATE TABLE IF NOT EXISTS member_analytics_projection (
    member_id UUID PRIMARY KEY,
    total_points_earned BIGINT DEFAULT 0,
    total_points_spent BIGINT DEFAULT 0,
    total_points_expired BIGINT DEFAULT 0,
    current_balance BIGINT DEFAULT 0,
    total_transactions INTEGER DEFAULT 0,
    total_spend_amount DECIMAL(12,2) DEFAULT 0,
    avg_transaction_amount DECIMAL(12,2) DEFAULT 0,
    first_transaction_date TIMESTAMP,
    last_transaction_date TIMESTAMP,
    favorite_store_id VARCHAR(100),
    preferred_channel VARCHAR(50),
    engagement_score DECIMAL(5,2) DEFAULT 0,
    last_calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transaction Analytics Projection
CREATE TABLE IF NOT EXISTS transaction_analytics_projection (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    date_key DATE NOT NULL,
    store_id VARCHAR(100),
    channel VARCHAR(50),
    total_transactions INTEGER DEFAULT 0,
    total_amount DECIMAL(12,2) DEFAULT 0,
    total_points_awarded BIGINT DEFAULT 0,
    unique_members INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(date_key, store_id, channel)
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- Event Store indexes
CREATE INDEX IF NOT EXISTS idx_domain_event_aggregate ON domain_event_entry(aggregate_identifier);
CREATE INDEX IF NOT EXISTS idx_domain_event_timestamp ON domain_event_entry(time_stamp);

-- Member indexes
CREATE INDEX IF NOT EXISTS idx_member_email ON member_projection(email);
CREATE INDEX IF NOT EXISTS idx_member_phone ON member_projection(phone);
CREATE INDEX IF NOT EXISTS idx_member_status ON member_projection(status);
CREATE INDEX IF NOT EXISTS idx_member_tier ON member_projection(tier_id);

-- Wallet indexes
CREATE INDEX IF NOT EXISTS idx_wallet_member ON wallet_balance_projection(member_id);
CREATE INDEX IF NOT EXISTS idx_unit_transfer_wallet ON unit_transfer_projection(wallet_id);
CREATE INDEX IF NOT EXISTS idx_unit_transfer_member ON unit_transfer_projection(member_id);
CREATE INDEX IF NOT EXISTS idx_unit_transfer_date ON unit_transfer_projection(transaction_date);
CREATE INDEX IF NOT EXISTS idx_unit_expiration_date ON unit_expiration_projection(expiry_date);

-- Transaction indexes
CREATE INDEX IF NOT EXISTS idx_transaction_member ON transaction_projection(member_id);
CREATE INDEX IF NOT EXISTS idx_transaction_date ON transaction_projection(transaction_date);
CREATE INDEX IF NOT EXISTS idx_transaction_status ON transaction_projection(status);
CREATE INDEX IF NOT EXISTS idx_transaction_document ON transaction_projection(document_number);

-- =====================================================
-- INSERT DEFAULT DATA
-- =====================================================

-- Default Tier Configurations
INSERT INTO tier_configuration_projection (tier_id, name, level, min_points, min_transactions, min_spend_amount, benefits) VALUES
(uuid_generate_v4(), 'Bronze', 1, 0, 0, 0, '{"welcome_bonus": 100, "point_multiplier": 1.0, "special_offers": false}'),
(uuid_generate_v4(), 'Silver', 2, 1000, 5, 500, '{"welcome_bonus": 200, "point_multiplier": 1.2, "special_offers": true, "birthday_bonus": 100}'),
(uuid_generate_v4(), 'Gold', 3, 5000, 20, 2000, '{"welcome_bonus": 500, "point_multiplier": 1.5, "special_offers": true, "birthday_bonus": 200, "priority_support": true}'),
(uuid_generate_v4(), 'Platinum', 4, 15000, 50, 10000, '{"welcome_bonus": 1000, "point_multiplier": 2.0, "special_offers": true, "birthday_bonus": 500, "priority_support": true, "exclusive_events": true}')
ON CONFLICT (name) DO NOTHING;

-- Create user for application
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'loyalty_app') THEN
        CREATE USER loyalty_app WITH PASSWORD 'app_password';
    END IF;
END
$$;

-- Grant permissions
GRANT CONNECT ON DATABASE loyalty_db TO loyalty_app;
GRANT USAGE ON SCHEMA public TO loyalty_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO loyalty_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO loyalty_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO loyalty_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO loyalty_app;