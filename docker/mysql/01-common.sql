-- Common (tenant registry) database - LOCAL DEMO ONLY
-- Structure follows the columns read by the CustomerIdentificator library.
CREATE DATABASE IF NOT EXISTS common;

CREATE TABLE common.customer (
  id INT NOT NULL AUTO_INCREMENT,
  customer_id VARCHAR(64) NOT NULL,
  customer_key VARCHAR(128) NULL,
  name VARCHAR(64) NOT NULL,
  login VARCHAR(64) NULL,
  password VARBINARY(255) NULL,
  database_url VARCHAR(500) NOT NULL,
  database_user VARCHAR(64) NOT NULL,
  database_password VARCHAR(255) NULL,
  database_driver VARCHAR(100) NULL,
  database_catalog VARCHAR(64) NULL,
  firebase_server_key VARCHAR(255) NULL,
  has_otp TINYINT(1) NOT NULL DEFAULT 0,
  is_apns TINYINT(1) NOT NULL DEFAULT 0,
  is_dailypoint_active TINYINT(1) NOT NULL DEFAULT 0,
  is_otp_reset_password_active TINYINT(1) NOT NULL DEFAULT 0,
  privacy_policy VARCHAR(255) NULL,
  terms_and_conditions VARCHAR(255) NULL,
  timezone VARCHAR(64) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_customer_id (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
