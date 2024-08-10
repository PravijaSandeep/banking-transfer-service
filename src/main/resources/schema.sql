-- Create table for Bank entity
CREATE TABLE BANK (
    code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- Create table for Account entity
CREATE TABLE ACCOUNT (
    acc_num VARCHAR(20) PRIMARY KEY,
    balance DECIMAL(15, 2) NOT NULL,
    holder_name VARCHAR(255) NOT NULL,
    bank_code VARCHAR(10),
    FOREIGN KEY (bank_code) REFERENCES BANK(code)
);

-- Create table for Payee entity
CREATE TABLE PAYEE (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nick_name VARCHAR(255),
    acc_num VARCHAR(20) NOT NULL,
    bank_code VARCHAR(10),
    account_id VARCHAR(20),
    FOREIGN KEY (bank_code) REFERENCES BANK(code),
    FOREIGN KEY (account_id) REFERENCES ACCOUNT(acc_num)
);