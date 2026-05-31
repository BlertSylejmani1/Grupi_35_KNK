CREATE DATABASE IF NOT EXISTS smart_inventory CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_inventory;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(60) NOT NULL UNIQUE,
    password_hash VARCHAR(120) NOT NULL,
    role ENUM('ADMIN', 'EMPLOYEE') NOT NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS suppliers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL,
    phone VARCHAR(40) NOT NULL,
    address VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    category VARCHAR(80) NOT NULL,
    quantity INT NOT NULL CHECK (quantity >= 0),
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    supplier_id INT NULL,
    supplier VARCHAR(120) NOT NULL,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT fk_products_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL
);

ALTER TABLE users ADD COLUMN IF NOT EXISTS failed_attempts INT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP NULL;
ALTER TABLE products ADD COLUMN IF NOT EXISTS supplier_id INT NULL;

CREATE TABLE IF NOT EXISTS audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(60) NOT NULL,
    action VARCHAR(40) NOT NULL,
    entity VARCHAR(40) NOT NULL,
    details VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(40) NOT NULL,
    message VARCHAR(255) NOT NULL,
    email_sent BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_stock_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    old_quantity INT NOT NULL,
    new_quantity INT NOT NULL,
    changed_by VARCHAR(60) NOT NULL,
    change_type ENUM('RESTOCK', 'SALE', 'UPDATE') NOT NULL,
    reason VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_history_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS purchase_orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_id INT NULL,
    status ENUM('PENDING', 'APPROVED', 'RECEIVED') NOT NULL DEFAULT 'PENDING',
    created_by VARCHAR(60) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_purchase_orders_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS purchase_order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL CHECK (unit_price >= 0),
    CONSTRAINT fk_po_items_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_po_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

DELIMITER $$
DROP TRIGGER IF EXISTS products_before_insert$$
DROP TRIGGER IF EXISTS products_before_update$$
DROP FUNCTION IF EXISTS inventory_status$$
CREATE FUNCTION inventory_status(stock_quantity INT)
RETURNS VARCHAR(30)
DETERMINISTIC
BEGIN
    IF stock_quantity = 0 THEN
        RETURN 'OUT_OF_STOCK';
    ELSEIF stock_quantity <= 5 THEN
        RETURN 'LOW_STOCK';
    END IF;
    RETURN 'IN_STOCK';
END$$

CREATE TRIGGER products_before_insert
BEFORE INSERT ON products
FOR EACH ROW
BEGIN
    SET NEW.status = inventory_status(NEW.quantity);
END$$

CREATE TRIGGER products_before_update
BEFORE UPDATE ON products
FOR EACH ROW
BEGIN
    SET NEW.status = inventory_status(NEW.quantity);
END$$
DELIMITER ;

INSERT INTO users (username, password_hash, role) VALUES
('admin', '00112233445566778899aabbccddeeff:0e14da9d8aadc6fc27decd1b05097cd4312903005cbece01043ee2d4316b2398', 'ADMIN'),
('employee', '00112233445566778899aabbccddeeff:0e14da9d8aadc6fc27decd1b05097cd4312903005cbece01043ee2d4316b2398', 'EMPLOYEE')
ON DUPLICATE KEY UPDATE username = VALUES(username);

INSERT INTO suppliers (name, email, phone, address) VALUES
('Tech Supply', 'sales@techsupply.example', '+383 44 100 200', 'Prishtina Industrial Zone'),
('Cable House', 'orders@cablehouse.example', '+383 45 220 330', 'Rruga B, Prishtina'),
('Paper Pro', 'contact@paperpro.example', '+383 49 500 600', 'Ferizaj Business Park'),
('LightCo', 'support@lightco.example', '+383 43 700 800', 'Peja Center')
ON DUPLICATE KEY UPDATE email = VALUES(email), phone = VALUES(phone), address = VALUES(address);

INSERT INTO products (name, category, quantity, price, supplier_id, supplier, status) VALUES
('Wireless Mouse', 'Electronics', 18, 14.99, (SELECT id FROM suppliers WHERE name = 'Tech Supply'), 'Tech Supply', 'IN_STOCK'),
('USB-C Cable', 'Electronics', 4, 7.50, (SELECT id FROM suppliers WHERE name = 'Cable House'), 'Cable House', 'LOW_STOCK'),
('Notebook A5', 'Office', 40, 2.20, (SELECT id FROM suppliers WHERE name = 'Paper Pro'), 'Paper Pro', 'IN_STOCK'),
('Desk Lamp', 'Office', 0, 24.99, (SELECT id FROM suppliers WHERE name = 'LightCo'), 'LightCo', 'OUT_OF_STOCK')
ON DUPLICATE KEY UPDATE name = VALUES(name);

UPDATE products SET status = inventory_status(quantity);
