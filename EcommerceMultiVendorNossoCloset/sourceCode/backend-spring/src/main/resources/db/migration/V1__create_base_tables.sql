-- ===== MIGRATION V1: CRIAÇÃO DAS TABELAS BASE =====
-- Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
-- Sistema Nosso Closet - Pedidos Personalizados

-- Extensões necessárias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabela de usuários (refatorada)
CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    mobile VARCHAR(20) NOT NULL,
    whatsapp VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_CUSTOMER',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    credit_balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    registration_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_access TIMESTAMP,
    delivery_preference VARCHAR(30) NOT NULL DEFAULT 'NATAL_PICKUP',
    notes VARCHAR(200),
    last_order_date TIMESTAMP,
    total_orders INTEGER DEFAULT 0,
    total_spent DECIMAL(10,2) DEFAULT 0.00,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de fornecedores
CREATE TABLE IF NOT EXISTS suppliers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_name VARCHAR(100) NOT NULL,
    whatsapp VARCHAR(20) NOT NULL,
    website VARCHAR(200),
    email VARCHAR(100),
    minimum_order_value DECIMAL(10,2) NOT NULL,
    delivery_time_days INTEGER NOT NULL,
    admin_fee_percentage DECIMAL(5,2) NOT NULL,
    performance_rating INTEGER NOT NULL DEFAULT 5,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    notes VARCHAR(1000),
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_performance_rating CHECK (performance_rating >= 1 AND performance_rating <= 5),
    CONSTRAINT chk_admin_fee_percentage CHECK (admin_fee_percentage >= 0.00 AND admin_fee_percentage <= 100.00),
    CONSTRAINT chk_minimum_order_value CHECK (minimum_order_value > 0),
    CONSTRAINT chk_delivery_time_days CHECK (delivery_time_days > 0 AND delivery_time_days <= 365)
);

-- Tabela de categorias de fornecedores
CREATE TABLE IF NOT EXISTS supplier_categories (
    supplier_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    PRIMARY KEY (supplier_id, category),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE
);

-- Tabela de catálogos
CREATE TABLE IF NOT EXISTS catalogs (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    file_url VARCHAR(500),
    type VARCHAR(30) NOT NULL DEFAULT 'PDF',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP,
    season VARCHAR(50),
    view_count INTEGER DEFAULT 0,
    download_count INTEGER DEFAULT 0,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE
);

-- Tabela de imagens dos catálogos
CREATE TABLE IF NOT EXISTS catalog_images (
    catalog_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (catalog_id, image_url),
    FOREIGN KEY (catalog_id) REFERENCES catalogs(id) ON DELETE CASCADE
);

-- Tabela de tags dos catálogos
CREATE TABLE IF NOT EXISTS catalog_tags (
    catalog_id BIGINT NOT NULL,
    tag VARCHAR(30) NOT NULL,
    PRIMARY KEY (catalog_id, tag),
    FOREIGN KEY (catalog_id) REFERENCES catalogs(id) ON DELETE CASCADE
);

-- Tabela de pedidos coletivos
CREATE TABLE IF NOT EXISTS collective_orders (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    minimum_value DECIMAL(10,2) NOT NULL,
    current_value DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_deadline TIMESTAMP NOT NULL,
    supplier_payment_date TIMESTAMP,
    anticipated_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_received DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    profit_margin DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    observations VARCHAR(500),
    tracking_code VARCHAR(100),
    expected_delivery_date TIMESTAMP,
    actual_delivery_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP,
    minimum_reached_at TIMESTAMP,
    payment_window_opened_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    closed_by VARCHAR(50),
    
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE RESTRICT,
    CONSTRAINT chk_minimum_value CHECK (minimum_value > 0),
    CONSTRAINT chk_current_value CHECK (current_value >= 0),
    CONSTRAINT chk_anticipated_amount CHECK (anticipated_amount >= 0),
    CONSTRAINT chk_total_received CHECK (total_received >= 0)
);

-- Tabela de pedidos personalizados
CREATE TABLE IF NOT EXISTS custom_orders (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    supplier_id BIGINT,
    collective_order_id BIGINT,
    product_image_url VARCHAR(500) NOT NULL,
    reference_code VARCHAR(50),
    description VARCHAR(1000) NOT NULL,
    preferred_color VARCHAR(50) NOT NULL,
    size VARCHAR(30) NOT NULL,
    category VARCHAR(50) NOT NULL,
    observations VARCHAR(500),
    estimated_price DECIMAL(10,2),
    final_price DECIMAL(10,2),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_ANALYSIS',
    urgency VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    quantity INTEGER NOT NULL DEFAULT 1,
    admin_notes VARCHAR(500),
    analyzed_by VARCHAR(50),
    cancellation_reason VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    analyzed_at TIMESTAMP,
    confirmed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    
    FOREIGN KEY (client_id) REFERENCES app_users(id) ON DELETE RESTRICT,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL,
    FOREIGN KEY (collective_order_id) REFERENCES collective_orders(id) ON DELETE SET NULL,
    CONSTRAINT chk_quantity CHECK (quantity > 0 AND quantity <= 10),
    CONSTRAINT chk_estimated_price CHECK (estimated_price IS NULL OR estimated_price > 0),
    CONSTRAINT chk_final_price CHECK (final_price IS NULL OR final_price > 0)
);

-- Tabela de cores alternativas dos pedidos personalizados
CREATE TABLE IF NOT EXISTS custom_order_alternative_colors (
    custom_order_id BIGINT NOT NULL,
    color VARCHAR(50) NOT NULL,
    PRIMARY KEY (custom_order_id, color),
    FOREIGN KEY (custom_order_id) REFERENCES custom_orders(id) ON DELETE CASCADE
);

-- Tabela de produtos dos catálogos
CREATE TABLE IF NOT EXISTS catalog_products (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    catalog_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    reference_code VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    subcategory VARCHAR(50) NOT NULL,
    material VARCHAR(100),
    brand VARCHAR(100),
    gender VARCHAR(50),
    age_group VARCHAR(50),
    supplier_price DECIMAL(10,2) NOT NULL,
    selling_price DECIMAL(10,2) NOT NULL,
    discount_percentage DECIMAL(5,2),
    promotional_price DECIMAL(10,2),
    promotion_start_date TIMESTAMP,
    promotion_end_date TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    view_count INTEGER DEFAULT 0,
    favorite_count INTEGER DEFAULT 0,
    order_count INTEGER DEFAULT 0,
    observations VARCHAR(500),
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE,
    FOREIGN KEY (catalog_id) REFERENCES catalogs(id) ON DELETE CASCADE,
    CONSTRAINT chk_supplier_price CHECK (supplier_price > 0),
    CONSTRAINT chk_selling_price CHECK (selling_price > 0),
    CONSTRAINT chk_discount_percentage CHECK (discount_percentage IS NULL OR (discount_percentage >= 0 AND discount_percentage <= 100)),
    CONSTRAINT chk_promotional_price CHECK (promotional_price IS NULL OR promotional_price > 0)
);

-- Tabela de imagens dos produtos
CREATE TABLE IF NOT EXISTS catalog_product_images (
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (product_id, image_url),
    FOREIGN KEY (product_id) REFERENCES catalog_products(id) ON DELETE CASCADE
);

-- Tabela de cores dos produtos
CREATE TABLE IF NOT EXISTS catalog_product_colors (
    product_id BIGINT NOT NULL,
    color VARCHAR(50) NOT NULL,
    PRIMARY KEY (product_id, color),
    FOREIGN KEY (product_id) REFERENCES catalog_products(id) ON DELETE CASCADE
);

-- Tabela de tamanhos dos produtos
CREATE TABLE IF NOT EXISTS catalog_product_sizes (
    product_id BIGINT NOT NULL,
    size VARCHAR(30) NOT NULL,
    PRIMARY KEY (product_id, size),
    FOREIGN KEY (product_id) REFERENCES catalog_products(id) ON DELETE CASCADE
);

-- Tabela de tags dos produtos
CREATE TABLE IF NOT EXISTS catalog_product_tags (
    product_id BIGINT NOT NULL,
    tag VARCHAR(30) NOT NULL,
    PRIMARY KEY (product_id, tag),
    FOREIGN KEY (product_id) REFERENCES catalog_products(id) ON DELETE CASCADE
);

-- Tabela de transações de crédito
CREATE TABLE IF NOT EXISTS credit_transactions (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    description VARCHAR(500) NOT NULL,
    reference_order_id BIGINT,
    reference_custom_order_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    used_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    balance_after_transaction DECIMAL(10,2) NOT NULL,
    reference_code VARCHAR(100),
    observations VARCHAR(200),
    bonus_percentage DECIMAL(5,2),
    transfer_from_client_id BIGINT,
    transfer_to_client_id BIGINT,
    created_by VARCHAR(50),
    processed_by VARCHAR(50),
    
    FOREIGN KEY (client_id) REFERENCES app_users(id) ON DELETE RESTRICT,
    FOREIGN KEY (reference_order_id) REFERENCES collective_orders(id) ON DELETE SET NULL,
    FOREIGN KEY (reference_custom_order_id) REFERENCES custom_orders(id) ON DELETE SET NULL,
    FOREIGN KEY (transfer_from_client_id) REFERENCES app_users(id) ON DELETE SET NULL,
    FOREIGN KEY (transfer_to_client_id) REFERENCES app_users(id) ON DELETE SET NULL,
    CONSTRAINT chk_amount CHECK (amount > 0),
    CONSTRAINT chk_balance_after_transaction CHECK (balance_after_transaction >= 0),
    CONSTRAINT chk_bonus_percentage CHECK (bonus_percentage IS NULL OR (bonus_percentage >= 0 AND bonus_percentage <= 100))
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_app_users_email ON app_users(email);
CREATE INDEX IF NOT EXISTS idx_app_users_status ON app_users(status);
CREATE INDEX IF NOT EXISTS idx_app_users_role ON app_users(role);
CREATE INDEX IF NOT EXISTS idx_app_users_registration_date ON app_users(registration_date);

CREATE INDEX IF NOT EXISTS idx_suppliers_status ON suppliers(status);
CREATE INDEX IF NOT EXISTS idx_suppliers_performance_rating ON suppliers(performance_rating);
CREATE INDEX IF NOT EXISTS idx_suppliers_minimum_order_value ON suppliers(minimum_order_value);

CREATE INDEX IF NOT EXISTS idx_catalogs_supplier_id ON catalogs(supplier_id);
CREATE INDEX IF NOT EXISTS idx_catalogs_status ON catalogs(status);
CREATE INDEX IF NOT EXISTS idx_catalogs_valid_from ON catalogs(valid_from);
CREATE INDEX IF NOT EXISTS idx_catalogs_valid_until ON catalogs(valid_until);

CREATE INDEX IF NOT EXISTS idx_collective_orders_supplier_id ON collective_orders(supplier_id);
CREATE INDEX IF NOT EXISTS idx_collective_orders_status ON collective_orders(status);
CREATE INDEX IF NOT EXISTS idx_collective_orders_payment_deadline ON collective_orders(payment_deadline);
CREATE INDEX IF NOT EXISTS idx_collective_orders_created_at ON collective_orders(created_at);

CREATE INDEX IF NOT EXISTS idx_custom_orders_client_id ON custom_orders(client_id);
CREATE INDEX IF NOT EXISTS idx_custom_orders_supplier_id ON custom_orders(supplier_id);
CREATE INDEX IF NOT EXISTS idx_custom_orders_collective_order_id ON custom_orders(collective_order_id);
CREATE INDEX IF NOT EXISTS idx_custom_orders_status ON custom_orders(status);
CREATE INDEX IF NOT EXISTS idx_custom_orders_urgency ON custom_orders(urgency);
CREATE INDEX IF NOT EXISTS idx_custom_orders_category ON custom_orders(category);
CREATE INDEX IF NOT EXISTS idx_custom_orders_created_at ON custom_orders(created_at);

CREATE INDEX IF NOT EXISTS idx_catalog_products_supplier_id ON catalog_products(supplier_id);
CREATE INDEX IF NOT EXISTS idx_catalog_products_catalog_id ON catalog_products(catalog_id);
CREATE INDEX IF NOT EXISTS idx_catalog_products_status ON catalog_products(status);
CREATE INDEX IF NOT EXISTS idx_catalog_products_category ON catalog_products(category);
CREATE INDEX IF NOT EXISTS idx_catalog_products_subcategory ON catalog_products(subcategory);
CREATE INDEX IF NOT EXISTS idx_catalog_products_selling_price ON catalog_products(selling_price);
CREATE INDEX IF NOT EXISTS idx_catalog_products_reference_code ON catalog_products(reference_code);

CREATE INDEX IF NOT EXISTS idx_credit_transactions_client_id ON credit_transactions(client_id);
CREATE INDEX IF NOT EXISTS idx_credit_transactions_transaction_type ON credit_transactions(transaction_type);
CREATE INDEX IF NOT EXISTS idx_credit_transactions_status ON credit_transactions(status);
CREATE INDEX IF NOT EXISTS idx_credit_transactions_created_at ON credit_transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_credit_transactions_expires_at ON credit_transactions(expires_at);

-- Comentários nas tabelas
COMMENT ON TABLE app_users IS 'Usuários do sistema (clientes e administradores)';
COMMENT ON TABLE suppliers IS 'Fornecedores de produtos';
COMMENT ON TABLE catalogs IS 'Catálogos de produtos dos fornecedores';
COMMENT ON TABLE collective_orders IS 'Pedidos coletivos agrupados por fornecedor';
COMMENT ON TABLE custom_orders IS 'Pedidos personalizados dos clientes';
COMMENT ON TABLE catalog_products IS 'Produtos disponíveis nos catálogos';
COMMENT ON TABLE credit_transactions IS 'Transações de crédito dos clientes';

-- Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Aplicar trigger nas tabelas com updated_at
CREATE TRIGGER update_app_users_updated_at BEFORE UPDATE ON app_users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_suppliers_updated_at BEFORE UPDATE ON suppliers FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_catalogs_updated_at BEFORE UPDATE ON catalogs FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_collective_orders_updated_at BEFORE UPDATE ON collective_orders FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_custom_orders_updated_at BEFORE UPDATE ON custom_orders FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_catalog_products_updated_at BEFORE UPDATE ON catalog_products FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
