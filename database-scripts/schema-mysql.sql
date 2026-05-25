-- MySQL Database Schema for Genesys Integration
-- This script creates the necessary tables and indexes for the Genesys integration system

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS genesys_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE genesys_db;

-- Create customers table
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_contact_date TIMESTAMP NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    INDEX idx_customer_id (customer_id),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_last_contact (last_contact_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create interactions table
CREATE TABLE IF NOT EXISTS interactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    interaction_id VARCHAR(100) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    direction VARCHAR(10),
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    duration BIGINT,
    content TEXT,
    status ENUM('ACTIVE', 'COMPLETED', 'ABANDONED', 'TRANSFERRED') DEFAULT 'ACTIVE',
    agent_id VARCHAR(50),
    queue_name VARCHAR(100),
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    INDEX idx_interaction_id (interaction_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_agent_id (agent_id),
    INDEX idx_start_time (start_time),
    INDEX idx_status (status),
    INDEX idx_queue_name (queue_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create agent_sessions table
CREATE TABLE IF NOT EXISTS agent_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agent_id VARCHAR(50) NOT NULL,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP NULL,
    status ENUM('LOGGED_IN', 'LOGGED_OUT', 'BUSY', 'AWAY') DEFAULT 'LOGGED_IN',
    workstation_id VARCHAR(100),
    extension VARCHAR(20),
    INDEX idx_agent_id (agent_id),
    INDEX idx_login_time (login_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create call_records table
CREATE TABLE IF NOT EXISTS call_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_id VARCHAR(100) UNIQUE NOT NULL,
    customer_id BIGINT,
    agent_id VARCHAR(50),
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    duration BIGINT,
    call_type VARCHAR(20),
    direction ENUM('INBOUND', 'OUTBOUND'),
    recording_url TEXT,
    disposition VARCHAR(50),
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,
    INDEX idx_call_id (call_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_agent_id (agent_id),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create queue_statistics table
CREATE TABLE IF NOT EXISTS queue_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    queue_name VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active_calls INT DEFAULT 0,
    waiting_calls INT DEFAULT 0,
    avg_wait_time INT DEFAULT 0,
    longest_wait_time INT DEFAULT 0,
    available_agents INT DEFAULT 0,
    total_agents INT DEFAULT 0,
    INDEX idx_queue_name (queue_name),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample data
INSERT INTO customers (customer_id, name, email, phone, status) VALUES
('CUST001', 'John Doe', 'john.doe@example.com', '+1234567890', 'ACTIVE'),
('CUST002', 'Jane Smith', 'jane.smith@example.com', '+1234567891', 'ACTIVE'),
('CUST003', 'Bob Johnson', 'bob.johnson@example.com', '+1234567892', 'INACTIVE')
ON DUPLICATE KEY UPDATE name=VALUES(name), email=VALUES(email);

INSERT INTO interactions (interaction_id, customer_id, type, direction, agent_id, queue_name, status, duration) VALUES
('INT001', 1, 'VOICE', 'INBOUND', 'AGENT001', 'support', 'COMPLETED', 240),
('INT002', 2, 'CHAT', 'INBOUND', 'AGENT002', 'sales', 'ACTIVE', NULL),
('INT003', 1, 'EMAIL', 'OUTBOUND', 'AGENT001', 'support', 'COMPLETED', 120)
ON DUPLICATE KEY UPDATE type=VALUES(type), status=VALUES(status);

-- Create views for reporting
CREATE OR REPLACE VIEW customer_interaction_summary AS
SELECT
    c.customer_id,
    c.name,
    c.email,
    COUNT(i.id) as total_interactions,
    MAX(i.start_time) as last_interaction,
    AVG(i.duration) as avg_interaction_duration,
    c.status as customer_status
FROM customers c
LEFT JOIN interactions i ON c.id = i.customer_id
GROUP BY c.id, c.customer_id, c.name, c.email, c.status;

CREATE OR REPLACE VIEW daily_queue_metrics AS
SELECT
    DATE(qs.timestamp) as date,
    qs.queue_name,
    AVG(qs.avg_wait_time) as avg_daily_wait_time,
    MAX(qs.longest_wait_time) as max_daily_wait_time,
    AVG(qs.active_calls) as avg_active_calls,
    AVG(qs.waiting_calls) as avg_waiting_calls,
    AVG(qs.available_agents) as avg_available_agents,
    AVG(qs.total_agents) as avg_total_agents
FROM queue_statistics qs
GROUP BY DATE(qs.timestamp), qs.queue_name;

-- Create stored procedures for common operations
DELIMITER //

CREATE PROCEDURE GetCustomerInteractions(IN customerId VARCHAR(50))
BEGIN
    SELECT
        i.interaction_id,
        i.type,
        i.direction,
        i.start_time,
        i.duration,
        i.status,
        i.agent_id,
        i.queue_name
    FROM interactions i
    JOIN customers c ON i.customer_id = c.id
    WHERE c.customer_id = customerId
    ORDER BY i.start_time DESC;
END //

CREATE PROCEDURE GetAgentPerformance(IN agentId VARCHAR(50), IN startDate DATE, IN endDate DATE)
BEGIN
    SELECT
        COUNT(*) as total_interactions,
        AVG(duration) as avg_duration,
        COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_interactions,
        COUNT(CASE WHEN status = 'TRANSFERRED' THEN 1 END) as transferred_interactions
    FROM interactions
    WHERE agent_id = agentId
    AND DATE(start_time) BETWEEN startDate AND endDate;
END //

CREATE PROCEDURE UpdateQueueStatistics(
    IN queueName VARCHAR(100),
    IN activeCalls INT,
    IN waitingCalls INT,
    IN avgWaitTime INT,
    IN longestWaitTime INT,
    IN availableAgents INT,
    IN totalAgents INT
)
BEGIN
    INSERT INTO queue_statistics (
        queue_name, active_calls, waiting_calls, avg_wait_time,
        longest_wait_time, available_agents, total_agents
    ) VALUES (
        queueName, activeCalls, waitingCalls, avgWaitTime,
        longestWaitTime, availableAgents, totalAgents
    );
END //

DELIMITER ;

-- Grant permissions (adjust user as needed)
-- CREATE USER 'genesys_user'@'localhost' IDENTIFIED BY 'genesys_password';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON genesys_db.* TO 'genesys_user'@'localhost';
-- FLUSH PRIVILEGES;

-- Show table information
SHOW TABLES;

SELECT 'Database schema created successfully!' as message;