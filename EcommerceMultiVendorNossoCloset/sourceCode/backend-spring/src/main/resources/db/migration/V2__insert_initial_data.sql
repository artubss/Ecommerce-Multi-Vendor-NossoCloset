-- ===== MIGRATION V2: DADOS INICIAIS =====
-- Fase 1 - Semana 2: Refatoração da Arquitetura de Dados
-- Sistema Nosso Closet - Dados de exemplo e configuração inicial

-- Inserir usuário administrador padrão
INSERT INTO app_users (
    password, email, full_name, mobile, whatsapp, role, status, 
    credit_balance, delivery_preference, created_by
) VALUES (
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', -- password (caso use BCrypt)
    'admin@nossocloset.com.br',
    'Administrador Sistema',
    '+5584999999999',
    '+5584999999999',
    'ROLE_ADMIN',
    'ACTIVE',
    0.00,
    'NATAL_PICKUP',
    'system'
) ON CONFLICT (email) DO NOTHING;

-- Inserir alguns fornecedores de exemplo
INSERT INTO suppliers (
    name, contact_name, whatsapp, website, email,
    minimum_order_value, delivery_time_days, admin_fee_percentage,
    performance_rating, status, notes, created_by
) VALUES 
(
    'Fornecedor Premium Moda',
    'Maria Silva',
    '+5584988887777',
    'https://premiummoda.com.br',
    'contato@premiummoda.com.br',
    500.00,
    15,
    8.50,
    5,
    'ACTIVE',
    'Fornecedor especializado em roupas femininas de alta qualidade',
    'admin@nossocloset.com.br'
),
(
    'Estilo Urbano Fornecimentos',
    'João Santos',
    '+5584977776666',
    'https://estilourbano.com.br',
    'joao@estilourbano.com.br',
    300.00,
    10,
    7.00,
    4,
    'ACTIVE',
    'Especializado em roupas casuais e streetwear',
    'admin@nossocloset.com.br'
),
(
    'Elegância Feminina Ltda',
    'Ana Costa',
    '+5584966665555',
    'https://elegancia.com.br',
    'ana@elegancia.com.br',
    800.00,
    20,
    10.00,
    5,
    'ACTIVE',
    'Fornecedor premium de vestidos e roupas sociais femininas',
    'admin@nossocloset.com.br'
),
(
    'Moda Jovem Brasil',
    'Carlos Oliveira',
    '+5584955554444',
    'https://modajovem.com.br',
    'carlos@modajovem.com.br',
    250.00,
    7,
    6.50,
    4,
    'ACTIVE',
    'Especializado em moda jovem e adolescente',
    'admin@nossocloset.com.br'
);

-- Inserir categorias para os fornecedores
INSERT INTO supplier_categories (supplier_id, category) VALUES
-- Fornecedor Premium Moda
(1, 'Vestidos'),
(1, 'Blusas'),
(1, 'Saias'),
(1, 'Calças Femininas'),
-- Estilo Urbano
(2, 'Camisetas'),
(2, 'Jeans'),
(2, 'Tênis'),
(2, 'Acessórios'),
-- Elegância Feminina
(3, 'Vestidos de Festa'),
(3, 'Roupas Sociais'),
(3, 'Blazers'),
(3, 'Sapatos Sociais'),
-- Moda Jovem
(4, 'Roupas Casuais'),
(4, 'Moletons'),
(4, 'Shorts'),
(4, 'Tênis Jovem');

-- Inserir alguns catálogos de exemplo
INSERT INTO catalogs (
    supplier_id, name, description, type, status,
    valid_from, valid_until, season, created_by
) VALUES
(
    1,
    'Coleção Verão 2024 - Premium',
    'Catálogo completo da coleção verão 2024 com vestidos, blusas e acessórios premium',
    'PDF',
    'ACTIVE',
    '2024-01-01 00:00:00',
    '2024-06-30 23:59:59',
    'Verão 2024',
    'admin@nossocloset.com.br'
),
(
    2,
    'Streetwear Urbano 2024',
    'Catálogo de roupas casuais e streetwear para o público jovem urbano',
    'IMAGE_COLLECTION',
    'ACTIVE',
    '2024-01-01 00:00:00',
    '2024-12-31 23:59:59',
    'Ano Todo 2024',
    'admin@nossocloset.com.br'
),
(
    3,
    'Elegância Social 2024',
    'Coleção exclusiva de vestidos de festa e roupas sociais femininas',
    'PDF',
    'ACTIVE',
    '2024-01-01 00:00:00',
    '2024-12-31 23:59:59',
    'Coleção 2024',
    'admin@nossocloset.com.br'
),
(
    4,
    'Moda Jovem Inverno 2024',
    'Catálogo de roupas para o público jovem - coleção inverno',
    'MIXED',
    'ACTIVE',
    '2024-05-01 00:00:00',
    '2024-09-30 23:59:59',
    'Inverno 2024',
    'admin@nossocloset.com.br'
);

-- Inserir tags para os catálogos
INSERT INTO catalog_tags (catalog_id, tag) VALUES
-- Coleção Verão Premium
(1, 'verão'),
(1, 'premium'),
(1, 'feminino'),
(1, 'elegante'),
-- Streetwear Urbano
(2, 'streetwear'),
(2, 'urbano'),
(2, 'jovem'),
(2, 'casual'),
-- Elegância Social
(3, 'social'),
(3, 'festa'),
(3, 'elegante'),
(3, 'premium'),
-- Moda Jovem Inverno
(4, 'jovem'),
(4, 'inverno'),
(4, 'casual'),
(4, 'confortável');

-- Inserir alguns produtos de exemplo nos catálogos
INSERT INTO catalog_products (
    supplier_id, catalog_id, title, description, reference_code,
    category, subcategory, material, brand, gender,
    supplier_price, selling_price, status, created_by
) VALUES
(
    1, 1,
    'Vestido Floral Verão Premium',
    'Lindo vestido floral em tecido leve e confortável, perfeito para o verão. Modelagem que valoriza a silhueta feminina.',
    'VFV-001-2024',
    'Vestidos',
    'Vestidos Casuais',
    'Viscose',
    'Premium Collection',
    'Feminino',
    45.00,
    75.00,
    'AVAILABLE',
    'admin@nossocloset.com.br'
),
(
    1, 1,
    'Blusa Cropped Básica',
    'Blusa cropped em algodão com modelagem moderna e confortável. Disponível em várias cores.',
    'BCB-002-2024',
    'Blusas',
    'Blusas Casuais',
    'Algodão',
    'Premium Collection',
    'Feminino',
    25.00,
    45.00,
    'AVAILABLE',
    'admin@nossocloset.com.br'
),
(
    2, 2,
    'Camiseta Streetwear Urbana',
    'Camiseta oversized com estampa urbana exclusiva. Tecido de alta qualidade com caimento perfeito.',
    'CSU-003-2024',
    'Camisetas',
    'Camisetas Oversized',
    'Algodão 100%',
    'Urban Style',
    'Unissex',
    30.00,
    55.00,
    'AVAILABLE',
    'admin@nossocloset.com.br'
),
(
    3, 3,
    'Vestido de Festa Longo',
    'Vestido longo para ocasiões especiais, com detalhes em renda e modelagem elegante.',
    'VFL-004-2024',
    'Vestidos',
    'Vestidos de Festa',
    'Crepe com Renda',
    'Elegância',
    'Feminino',
    120.00,
    200.00,
    'AVAILABLE',
    'admin@nossocloset.com.br'
),
(
    4, 4,
    'Moletom Jovem Inverno',
    'Moletom confortável com capuz, perfeito para os dias frios. Design moderno e jovial.',
    'MJI-005-2024',
    'Moletons',
    'Moletons com Capuz',
    'Moletom 70% Algodão',
    'Young Fashion',
    'Unissex',
    40.00,
    70.00,
    'AVAILABLE',
    'admin@nossocloset.com.br'
);

-- Inserir cores disponíveis para os produtos
INSERT INTO catalog_product_colors (product_id, color) VALUES
-- Vestido Floral
(1, 'rosa'),
(1, 'azul'),
(1, 'branco'),
-- Blusa Cropped
(2, 'branco'),
(2, 'preto'),
(2, 'rosa'),
(2, 'azul'),
-- Camiseta Streetwear
(3, 'preto'),
(3, 'cinza'),
(3, 'branco'),
-- Vestido de Festa
(4, 'azul marinho'),
(4, 'bordô'),
(4, 'preto'),
-- Moletom
(5, 'cinza'),
(5, 'preto'),
(5, 'azul');

-- Inserir tamanhos disponíveis para os produtos
INSERT INTO catalog_product_sizes (product_id, size) VALUES
-- Vestido Floral
(1, 'P'),
(1, 'M'),
(1, 'G'),
(1, 'GG'),
-- Blusa Cropped
(2, 'PP'),
(2, 'P'),
(2, 'M'),
(2, 'G'),
-- Camiseta Streetwear
(3, 'P'),
(3, 'M'),
(3, 'G'),
(3, 'GG'),
(3, 'XG'),
-- Vestido de Festa
(4, 'P'),
(4, 'M'),
(4, 'G'),
(4, 'GG'),
-- Moletom
(5, 'M'),
(5, 'G'),
(5, 'GG'),
(5, 'XG');

-- Inserir tags para os produtos
INSERT INTO catalog_product_tags (product_id, tag) VALUES
-- Vestido Floral
(1, 'verão'),
(1, 'floral'),
(1, 'casual'),
-- Blusa Cropped
(2, 'básico'),
(2, 'cropped'),
(2, 'algodão'),
-- Camiseta Streetwear
(3, 'streetwear'),
(3, 'oversized'),
(3, 'urbano'),
-- Vestido de Festa
(4, 'festa'),
(4, 'elegante'),
(4, 'longo'),
-- Moletom
(5, 'inverno'),
(5, 'confortável'),
(5, 'jovem');

-- Inserir cliente de exemplo
INSERT INTO app_users (
    password, email, full_name, mobile, whatsapp, role, status,
    credit_balance, delivery_preference, total_orders, total_spent, created_by
) VALUES (
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
    'cliente@exemplo.com',
    'Maria Exemplo',
    '+5584988776655',
    '+5584988776655',
    'ROLE_CUSTOMER',
    'ACTIVE',
    50.00,
    'NATAL_PICKUP',
    3,
    250.00,
    'system'
) ON CONFLICT (email) DO NOTHING;

-- Inserir um pedido coletivo de exemplo
INSERT INTO collective_orders (
    supplier_id, status, minimum_value, current_value, payment_deadline,
    observations, created_by
) VALUES (
    1,
    'OPEN',
    500.00,
    275.00,
    CURRENT_TIMESTAMP + INTERVAL '7 days',
    'Pedido coletivo da Premium Moda - Coleção Verão 2024',
    'admin@nossocloset.com.br'
);

-- Inserir alguns pedidos personalizados de exemplo
INSERT INTO custom_orders (
    client_id, supplier_id, collective_order_id, product_image_url,
    description, preferred_color, size, category, quantity,
    final_price, status, urgency, created_by
) VALUES 
(
    (SELECT id FROM app_users WHERE email = 'cliente@exemplo.com'),
    1,
    1,
    'https://exemplo.com/imagem-vestido.jpg',
    'Vestido floral longo para festa de casamento, similar ao do catálogo',
    'rosa',
    'M',
    'Vestidos',
    1,
    75.00,
    'IN_COLLECTIVE',
    'NORMAL',
    'cliente@exemplo.com'
),
(
    (SELECT id FROM app_users WHERE email = 'cliente@exemplo.com'),
    1,
    1,
    'https://exemplo.com/imagem-blusa.jpg',
    'Blusa cropped básica para uso casual, cor clara',
    'branco',
    'P',
    'Blusas',
    2,
    90.00,
    'IN_COLLECTIVE',
    'NORMAL',
    'cliente@exemplo.com'
),
(
    (SELECT id FROM app_users WHERE email = 'cliente@exemplo.com'),
    2,
    NULL,
    'https://exemplo.com/imagem-camiseta.jpg',
    'Camiseta oversized com estampa urbana moderna',
    'preto',
    'M',
    'Camisetas',
    1,
    55.00,
    'CONFIRMED',
    'LOW',
    'cliente@exemplo.com'
);

-- Inserir cores alternativas para os pedidos personalizados
INSERT INTO custom_order_alternative_colors (custom_order_id, color) VALUES
-- Vestido - cores alternativas
(1, 'azul'),
(1, 'branco'),
-- Blusa - cores alternativas
(2, 'rosa'),
(2, 'azul'),
-- Camiseta - cores alternativas
(3, 'cinza'),
(3, 'branco');

-- Inserir algumas transações de crédito de exemplo
INSERT INTO credit_transactions (
    client_id, transaction_type, amount, description,
    balance_after_transaction, status, created_by
) VALUES
(
    (SELECT id FROM app_users WHERE email = 'cliente@exemplo.com'),
    'PROMOTIONAL_CREDIT',
    50.00,
    'Crédito promocional de boas-vindas',
    50.00,
    'ACTIVE',
    'system'
),
(
    (SELECT id FROM app_users WHERE email = 'cliente@exemplo.com'),
    'REFERRAL_BONUS',
    25.00,
    'Bônus por indicação de novo cliente',
    75.00,
    'ACTIVE',
    'system'
),
(
    (SELECT id FROM app_users WHERE email = 'cliente@exemplo.com'),
    'USAGE_DEBIT',
    25.00,
    'Uso de crédito em pedido anterior',
    50.00,
    'USED',
    'sistema'
);

-- Comentários de finalização
COMMENT ON SCHEMA public IS 'Schema principal do sistema Nosso Closet com dados iniciais configurados';

-- Atualizar sequências para evitar conflitos
SELECT setval('app_users_id_seq', (SELECT MAX(id) FROM app_users));
SELECT setval('suppliers_id_seq', (SELECT MAX(id) FROM suppliers));
SELECT setval('catalogs_id_seq', (SELECT MAX(id) FROM catalogs));
SELECT setval('catalog_products_id_seq', (SELECT MAX(id) FROM catalog_products));
SELECT setval('collective_orders_id_seq', (SELECT MAX(id) FROM collective_orders));
SELECT setval('custom_orders_id_seq', (SELECT MAX(id) FROM custom_orders));
SELECT setval('credit_transactions_id_seq', (SELECT MAX(id) FROM credit_transactions));
