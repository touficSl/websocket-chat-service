#!/bin/bash
# Demo data - LOCAL DEMO ONLY.
# Runs after the schema scripts; uses the passwords from the container environment so none is hardcoded.
set -euo pipefail

mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" <<SQL

INSERT INTO common.customer (customer_id, customer_key, name, login, database_url, database_user, database_password, database_driver, database_catalog, timezone)
VALUES ('demo', 'demo-key', 'demohotel', 'demo',
        'jdbc:mysql://mysql:3306/demo?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8',
        '${MESSENGER_DB_USER}', '${MESSENGER_DB_PASSWORD}', 'com.mysql.cj.jdbc.Driver', 'demo', 'Asia/Dubai');

USE demo;

CREATE TABLE service (
  id INT NOT NULL AUTO_INCREMENT,
  service_name VARCHAR(100) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE hotel_service (
  id INT NOT NULL AUTO_INCREMENT,
  hotel_id VARCHAR(64) NOT NULL,
  service_id INT NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  creation_date DATETIME NULL,
  modification_date DATETIME NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO time_zone (id, hotel_timezone) VALUES ('1', 'Asia/Dubai');
INSERT INTO hotel (id, hotel_id, hotel_code, hotel_name, hotel_timezone_id, active) VALUES ('1', '1', 'DEMO', 'Demo Hotel', '1', 1);
INSERT INTO service (id, service_name) VALUES (1, 'messenger');
INSERT INTO hotel_service (hotel_id, service_id, enabled, creation_date) VALUES ('1', 1, 1, NOW());
INSERT INTO utilisateur (username, first_name, last_name, enabled) VALUES ('staff1', 'Front', 'Desk', 1), ('staff2', 'Guest', 'Relations', 1);
-- Least privilege account used by the messenger for both databases
CREATE USER IF NOT EXISTS '${MESSENGER_DB_USER}'@'%' IDENTIFIED BY '${MESSENGER_DB_PASSWORD}';
GRANT SELECT ON common.customer TO '${MESSENGER_DB_USER}'@'%';
GRANT SELECT ON demo.hotel TO '${MESSENGER_DB_USER}'@'%';
GRANT SELECT ON demo.hotel_service TO '${MESSENGER_DB_USER}'@'%';
GRANT SELECT ON demo.service TO '${MESSENGER_DB_USER}'@'%';
GRANT SELECT ON demo.time_zone TO '${MESSENGER_DB_USER}'@'%';
GRANT SELECT ON demo.utilisateur TO '${MESSENGER_DB_USER}'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON demo.messenger_guest_room TO '${MESSENGER_DB_USER}'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON demo.messenger_message TO '${MESSENGER_DB_USER}'@'%';
FLUSH PRIVILEGES;
SQL
