CREATE DATABASE IF NOT EXISTS budgetbuddy;
USE budgetbuddy;

CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    account_type ENUM('CHECKING', 'SAVINGS', 'CREDIT_CARD') NOT NULL,
    balance DOUBLE DEFAULT 0.0,
    overdraft_limit DOUBLE DEFAULT 0.0,
    interest_rate DOUBLE DEFAULT 0.0,
    credit_limit DOUBLE DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    parent_category_id BIGINT NULL,
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(50),
    is_default BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_category_id) REFERENCES category(id) ON DELETE SET NULL
);

INSERT INTO category (user_id, parent_category_id, name, icon, is_default) VALUES
(NULL, NULL, 'Food', 'food', TRUE),
(NULL, NULL, 'Housing', 'home', TRUE),
(NULL, NULL, 'Transport', 'car', TRUE),
(NULL, NULL, 'Entertainment', 'film', TRUE),
(NULL, NULL, 'Utilities', 'bolt', TRUE),
(NULL, NULL, 'Healthcare', 'heart', TRUE),
(NULL, NULL, 'Salary', 'wallet', TRUE),
(NULL, NULL, 'Freelance', 'briefcase', TRUE),
(NULL, NULL, 'Other', 'dots', TRUE);

CREATE TABLE `transaction` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    type ENUM('INCOME', 'EXPENSE', 'TRANSFER') NOT NULL,
    amount DOUBLE NOT NULL,
    description VARCHAR(255),
    is_recurring BOOLEAN DEFAULT FALSE,
    tags VARCHAR(255),
    tax_rate DOUBLE DEFAULT 0.0,
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(id)
);

CREATE TABLE budget (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    limit_amount DOUBLE NOT NULL,
    spent_amount DOUBLE DEFAULT 0.0,
    status ENUM('UNDER_LIMIT', 'APPROACHING_LIMIT', 'EXCEEDED') DEFAULT 'UNDER_LIMIT',
    month VARCHAR(20) NOT NULL,
    year INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(id),
    UNIQUE KEY unique_budget (user_id, category_id, month, year)
);

CREATE TABLE report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    report_type ENUM('WEEKLY', 'MONTHLY', 'CATEGORY') NOT NULL,
    period VARCHAR(50) NOT NULL,
    format ENUM('PDF', 'CSV') NOT NULL,
    total_income DOUBLE DEFAULT 0.0,
    total_expense DOUBLE DEFAULT 0.0,
    net_savings DOUBLE DEFAULT 0.0,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE command_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    command_type ENUM('ADD', 'EDIT', 'DELETE') NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    previous_state TEXT,
    new_state TEXT,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    budget_id BIGINT NOT NULL,
    message VARCHAR(255) NOT NULL,
    alert_type ENUM('WARNING', 'EXCEEDED') NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (budget_id) REFERENCES budget(id) ON DELETE CASCADE
);