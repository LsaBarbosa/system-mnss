BEGIN;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =========================================================
-- ROLES
-- =========================================================
INSERT INTO roles (id, name, description, created_at, updated_at, version)
VALUES
('10000000-0000-0000-0000-000000000001', 'ADMIN', 'Administrador geral do sistema', now(), now(), 0),
('10000000-0000-0000-0000-000000000002', 'MANAGER', 'Gerente da padaria', now(), now(), 0),
('10000000-0000-0000-0000-000000000003', 'CASHIER', 'Operador de caixa e PDV', now(), now(), 0),
('10000000-0000-0000-0000-000000000004', 'KITCHEN', 'Operador KDS / cozinha', now(), now(), 0),
('10000000-0000-0000-0000-000000000005', 'STOCK', 'Operador de estoque', now(), now(), 0)
ON CONFLICT (name) DO UPDATE SET
description = EXCLUDED.description,
updated_at = now();

-- =========================================================
-- USERS
-- Senhas:
-- admin       / Admin@2026!
-- gerente     / Gerente@2026!
-- caixa01     / Caixa@2026!
-- cozinha01   / Cozinha@2026!
-- estoque01   / Estoque@2026!
-- =========================================================
INSERT INTO users (id, name, email, username, password_hash, active, created_at, updated_at, version)
VALUES
('20000000-0000-0000-0000-000000000001', 'Administrador Local', 'admin.local@novaalianca.local', 'admin', '$2a$10$8x2oBi8V07utnKnimIis.eCkDQADSH9dZE5.ECJGB9iGvKCLm4ytu', true, now(), now(), 0),
('20000000-0000-0000-0000-000000000002', 'Gerente da Loja', 'gerente@novaalianca.local', 'gerente', '$2a$10$vRfjjbaXEiWB5hBP95AWB.UPXyRaNN6..0eySV5zsDTFWTnobeFve', true, now(), now(), 0),
('20000000-0000-0000-0000-000000000003', 'Operador de Caixa 01', 'caixa01@novaalianca.local', 'caixa01', '$2a$10$kwgusHfvsimSL8LNHcC9XOjlxuWoz9laP8KdhKJo/BUnzAVS3nFbG', true, now(), now(), 0),
('20000000-0000-0000-0000-000000000004', 'Operador Cozinha 01', 'cozinha01@novaalianca.local', 'cozinha01', '$2a$10$HDuvyjeR3XeEtISCgGyw..0D1c8psr2HWYlAEmIlNWYU693.B.BVq', true, now(), now(), 0),
('20000000-0000-0000-0000-000000000005', 'Operador Estoque 01', 'estoque01@novaalianca.local', 'estoque01', '$2a$10$X0CEN4S.eVzKF/9RTUU8g.Ev6F4n0zNcT0IzX0qRLYy4WKrkK0kJi', true, now(), now(), 0)
ON CONFLICT (username) DO UPDATE SET
name = EXCLUDED.name,
email = EXCLUDED.email,
password_hash = EXCLUDED.password_hash,
active = true,
updated_at = now();

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ADMIN'
WHERE u.username = 'admin'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'MANAGER'
WHERE u.username = 'gerente'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'CASHIER'
WHERE u.username = 'caixa01'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'KITCHEN'
WHERE u.username = 'cozinha01'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'STOCK'
WHERE u.username = 'estoque01'
ON CONFLICT DO NOTHING;

-- =========================================================
-- CATEGORIES
-- =========================================================
INSERT INTO categories (id, name, description, display_order, image_url, active, show_online, show_on_pdv, show_on_whatsapp, created_at, updated_at, version)
VALUES
('30000000-0000-0000-0000-000000000001', 'Pães', 'Pães franceses, doces, integrais e especiais.', 1, '/assets/menu/paes.jpg', true, true, true, true, now(), now(), 0),
('30000000-0000-0000-0000-000000000002', 'Salgados', 'Salgados assados e fritos para balcão e delivery.', 2, '/assets/menu/salgados.jpg', true, true, true, true, now(), now(), 0),
('30000000-0000-0000-0000-000000000003', 'Bolos e Tortas', 'Bolos caseiros, tortas fatiadas e sobremesas.', 3, '/assets/menu/bolos.jpg', true, true, true, true, now(), now(), 0),
('30000000-0000-0000-0000-000000000004', 'Bebidas', 'Cafés, sucos, refrigerantes e bebidas geladas.', 4, '/assets/menu/bebidas.jpg', true, true, true, true, now(), now(), 0),
('30000000-0000-0000-0000-000000000005', 'Mercearia', 'Itens de conveniência e produtos embalados.', 5, '/assets/menu/mercearia.jpg', true, false, true, false, now(), now(), 0)
ON CONFLICT (id) DO UPDATE SET
name = EXCLUDED.name,
description = EXCLUDED.description,
display_order = EXCLUDED.display_order,
active = true,
updated_at = now();

-- =========================================================
-- PRODUCTS
-- =========================================================
INSERT INTO products (
id, category_id, name, description, price, promotional_price, cost_price, sku, barcode, image_url,
unit_type, preparation_sector, preparation_time_minutes, active, available,
sell_on_pdv, sell_online, sell_on_whatsapp, stock_controlled, created_at, updated_at, version
)
VALUES
('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'Pão Francês', 'Pão francês tradicional, vendido por unidade.', 0.90, null, 0.32, 'PAO-FRANCES-UN', '7890000000011', '/assets/products/pao-frances.jpg', 'UNIT', 'SEM_PREPARO', 0, true, true, true, true, true, true, now(), now(), 0),
('40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', 'Pão Doce', 'Pão doce com creme e coco.', 4.50, null, 1.60, 'PAO-DOCE-UN', '7890000000028', '/assets/products/pao-doce.jpg', 'UNIT', 'SEM_PREPARO', 0, true, true, true, true, true, true, now(), now(), 0),
('40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000002', 'Coxinha de Frango', 'Coxinha de frango com massa cremosa.', 7.50, null, 2.80, 'COXINHA-FRANGO', '7890000000035', '/assets/products/coxinha.jpg', 'UNIT', 'CHAPA', 12, true, true, true, true, true, true, now(), now(), 0),
('40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000002', 'Pão de Queijo', 'Pão de queijo mineiro assado.', 3.50, null, 1.20, 'PAO-QUEIJO', '7890000000042', '/assets/products/pao-queijo.jpg', 'UNIT', 'CHAPA', 8, true, true, true, true, true, true, now(), now(), 0),
('40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000003', 'Fatia de Bolo de Cenoura', 'Fatia de bolo de cenoura com cobertura de chocolate.', 8.90, 7.90, 3.20, 'BOLO-CENOURA-FATIA', '7890000000059', '/assets/products/bolo-cenoura.jpg', 'UNIT', 'SEM_PREPARO', 0, true, true, true, true, true, true, now(), now(), 0),
('40000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-000000000004', 'Café Coado 200ml', 'Café coado tradicional.', 4.00, null, 1.00, 'CAFE-COADO-200', '7890000000066', '/assets/products/cafe.jpg', 'UNIT', 'BEBIDAS', 3, true, true, true, true, true, false, now(), now(), 0),
('40000000-0000-0000-0000-000000000007', '30000000-0000-0000-0000-000000000004', 'Suco Natural de Laranja 300ml', 'Suco natural de laranja.', 8.00, null, 3.00, 'SUCO-LARANJA-300', '7890000000073', '/assets/products/suco-laranja.jpg', 'UNIT', 'BEBIDAS', 5, true, true, true, true, true, false, now(), now(), 0),
('40000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000005', 'Leite Integral 1L', 'Leite integral longa vida 1 litro.', 6.90, null, 4.70, 'LEITE-1L', '7890000000080', '/assets/products/leite.jpg', 'UNIT', 'SEM_PREPARO', 0, true, true, true, false, false, true, now(), now(), 0)
ON CONFLICT (id) DO UPDATE SET
name = EXCLUDED.name,
description = EXCLUDED.description,
price = EXCLUDED.price,
promotional_price = EXCLUDED.promotional_price,
cost_price = EXCLUDED.cost_price,
available = EXCLUDED.available,
active = EXCLUDED.active,
updated_at = now();

-- =========================================================
-- PRODUCT AVAILABILITY
-- =========================================================
INSERT INTO product_availability (id, product_id, status, available_quantity, channel, reason, updated_by, created_at, updated_at, version)
VALUES
('41000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 'AVAILABLE', 350.000, 'PDV', 'Produção diária disponível', '20000000-0000-0000-0000-000000000005', now(), now(), 0),
('41000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000003', 'AVAILABLE', 80.000, 'PDV', 'Salgados prontos para venda', '20000000-0000-0000-0000-000000000005', now(), now(), 0),
('41000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000005', 'AVAILABLE', 42.000, 'SITE', 'Disponível para pedidos online', '20000000-0000-0000-0000-000000000002', now(), now(), 0),
('41000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000008', 'AVAILABLE', 60.000, 'PDV', 'Estoque refrigerado conferido', '20000000-0000-0000-0000-000000000005', now(), now(), 0)
ON CONFLICT (id) DO UPDATE SET
status = EXCLUDED.status,
available_quantity = EXCLUDED.available_quantity,
reason = EXCLUDED.reason,
updated_at = now();

-- =========================================================
-- CUSTOMERS / ADDRESSES
-- =========================================================
INSERT INTO customers (id, name, phone, email, document, birth_date, created_at, updated_at, version)
VALUES
('50000000-0000-0000-0000-000000000001', 'Mariana Oliveira', '21988880001', 'mariana.oliveira@email.com', '12345678901', '1990-04-12', now(), now(), 0),
('50000000-0000-0000-0000-000000000002', 'Carlos Henrique Souza', '21988880002', 'carlos.souza@email.com', '23456789012', '1984-09-22', now(), now(), 0),
('50000000-0000-0000-0000-000000000003', 'Fernanda Lima', '21988880003', 'fernanda.lima@email.com', '34567890123', '1995-01-30', now(), now(), 0)
ON CONFLICT (id) DO UPDATE SET
name = EXCLUDED.name,
phone = EXCLUDED.phone,
email = EXCLUDED.email,
updated_at = now();

INSERT INTO customer_addresses (id, customer_id, label, street, number, complement, neighborhood, city, state, zip_code, reference, latitude, longitude, default_address, created_at, updated_at, version)
VALUES
('51000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', 'Casa', 'Rua das Palmeiras', '120', 'Casa 2', 'Centro', 'Magé', 'RJ', '25900000', 'Próximo à praça principal', -22.6575000, -43.0312000, true, now(), now(), 0),
('51000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', 'Trabalho', 'Avenida Roberto Silveira', '455', 'Loja B', 'Centro', 'Magé', 'RJ', '25901000', 'Ao lado da farmácia', -22.6567000, -43.0305000, true, now(), now(), 0),
('51000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003', 'Casa', 'Rua Nova Esperança', '88', null, 'Suruí', 'Magé', 'RJ', '25925000', 'Portão azul', -22.6910000, -43.1071000, true, now(), now(), 0)
ON CONFLICT (id) DO UPDATE SET
street = EXCLUDED.street,
number = EXCLUDED.number,
reference = EXCLUDED.reference,
updated_at = now();

-- =========================================================
-- ORDERS LOCAL
-- Banco local NÃO possui orders.payment_method.
-- =========================================================
INSERT INTO orders (
id, customer_id, delivery_address_id, origin, status, payment_status, delivery_type,
subtotal, discount_amount, delivery_fee, total_amount, notes, created_at, updated_at, version, finished_at, canceled_at
)
VALUES
('60000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', null, 'PDV', 'FINISHED', 'PAID', 'LOCAL_CONSUMPTION', 24.90, 0.00, 0.00, 24.90, 'Venda balcão café da manhã.', now() - interval '2 hours', now() - interval '2 hours', 0, now() - interval '2 hours', null),
('60000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', '51000000-0000-0000-0000-000000000002', 'PDV', 'READY', 'PAID', 'PICKUP', 38.50, 3.50, 0.00, 35.00, 'Cliente retira no balcão.', now() - interval '1 hour', now() - interval '45 minutes', 0, null, null),
('60000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003', '51000000-0000-0000-0000-000000000003', 'WHATSAPP', 'IN_PREPARATION', 'PAID', 'DELIVERY', 52.40, 0.00, 6.00, 58.40, 'Pedido recebido via WhatsApp mock.', now() - interval '35 minutes', now() - interval '20 minutes', 0, null, null),
('60000000-0000-0000-0000-000000000004', null, null, 'PDV', 'CREATED', 'PENDING', 'LOCAL_CONSUMPTION', 0.00, 0.00, 0.00, 0.00, 'Venda aberta para teste de tela.', now(), now(), 0, null, null)
ON CONFLICT (id) DO UPDATE SET
status = EXCLUDED.status,
payment_status = EXCLUDED.payment_status,
total_amount = EXCLUDED.total_amount,
updated_at = now();

INSERT INTO order_items (id, order_id, product_id, product_name_snapshot, quantity, unit_price, total_price, observation, status, preparation_sector, created_at, updated_at, version)
VALUES
('61000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 'Pão Francês', 10.000, 0.90, 9.00, null, 'DELIVERED', 'SEM_PREPARO', now() - interval '2 hours', now() - interval '2 hours', 0),
('61000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000006', 'Café Coado 200ml', 2.000, 4.00, 8.00, 'Sem açúcar', 'DELIVERED', 'BEBIDAS', now() - interval '2 hours', now() - interval '2 hours', 0),
('61000000-0000-0000-0000-000000000003', '60000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000005', 'Fatia de Bolo de Cenoura', 1.000, 7.90, 7.90, null, 'DELIVERED', 'SEM_PREPARO', now() - interval '2 hours', now() - interval '2 hours', 0),
('61000000-0000-0000-0000-000000000004', '60000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000003', 'Coxinha de Frango', 3.000, 7.50, 22.50, 'Bem quente', 'READY', 'CHAPA', now() - interval '1 hour', now() - interval '40 minutes', 0),
('61000000-0000-0000-0000-000000000005', '60000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000004', 'Pão de Queijo', 4.000, 3.50, 14.00, null, 'READY', 'CHAPA', now() - interval '1 hour', now() - interval '40 minutes', 0),
('61000000-0000-0000-0000-000000000006', '60000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000005', 'Fatia de Bolo de Cenoura', 4.000, 7.90, 31.60, null, 'IN_PREPARATION', 'SEM_PREPARO', now() - interval '35 minutes', now() - interval '20 minutes', 0),
('61000000-0000-0000-0000-000000000007', '60000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000007', 'Suco Natural de Laranja 300ml', 2.000, 8.00, 16.00, 'Pouco gelo', 'IN_PREPARATION', 'BEBIDAS', now() - interval '35 minutes', now() - interval '20 minutes', 0)
ON CONFLICT (id) DO UPDATE SET
status = EXCLUDED.status,
updated_at = now();

INSERT INTO payments (id, order_id, method, status, amount, transaction_id, gateway, paid_at, canceled_at, created_at, updated_at, version)
VALUES
('62000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000001', 'CASH', 'PAID', 24.90, 'LOCAL-CASH-0001', 'LOCAL', now() - interval '2 hours', null, now() - interval '2 hours', now() - interval '2 hours', 0),
('62000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000002', 'CREDIT_CARD', 'PAID', 35.00, 'LOCAL-CARD-0002', 'LOCAL', now() - interval '55 minutes', null, now() - interval '55 minutes', now() - interval '55 minutes', 0),
('62000000-0000-0000-0000-000000000003', '60000000-0000-0000-0000-000000000003', 'PIX', 'PAID', 58.40, 'LOCAL-PIX-0003', 'LOCAL', now() - interval '30 minutes', null, now() - interval '30 minutes', now() - interval '30 minutes', 0)
ON CONFLICT (id) DO UPDATE SET
status = EXCLUDED.status,
amount = EXCLUDED.amount,
updated_at = now();

-- =========================================================
-- CASH REGISTER / MOVEMENTS
-- =========================================================
INSERT INTO cash_registers (id, operator_id, opened_at, closed_at, opening_amount, closing_amount, expected_amount, difference_amount, status, notes, created_at, updated_at, version)
VALUES
('70000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000003', current_date + time '05:30', null, 150.00, null, 209.90, null, 'OPEN', 'Caixa de homologação aberto para turno da manhã.', now(), now(), 0)
ON CONFLICT (id) DO UPDATE SET
status = EXCLUDED.status,
expected_amount = EXCLUDED.expected_amount,
updated_at = now();

INSERT INTO cash_movements (id, cash_register_id, type, payment_method, amount, description, order_id, created_by, created_at, updated_at, version)
VALUES
('71000000-0000-0000-0000-000000000001', '70000000-0000-0000-0000-000000000001', 'CASH_IN', null, 150.00, 'Abertura de caixa', null, '20000000-0000-0000-0000-000000000003', current_date + time '05:30', now(), 0),
('71000000-0000-0000-0000-000000000002', '70000000-0000-0000-0000-000000000001', 'SALE', 'CASH', 24.90, 'Venda PDV finalizada', '60000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000003', now() - interval '2 hours', now(), 0),
('71000000-0000-0000-0000-000000000003', '70000000-0000-0000-0000-000000000001', 'SALE', 'CREDIT_CARD', 35.00, 'Venda cartão finalizada', '60000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000003', now() - interval '55 minutes', now(), 0)
ON CONFLICT (id) DO UPDATE SET
amount = EXCLUDED.amount,
updated_at = now();

-- =========================================================
-- KDS
-- =========================================================
INSERT INTO kds_tickets (id, order_id, sector, status, created_at, updated_at, version, started_at, ready_at, finished_at)
VALUES
('80000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000002', 'CHAPA', 'READY', now() - interval '55 minutes', now() - interval '40 minutes', 0, now() - interval '52 minutes', now() - interval '42 minutes', null),
('80000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000003', 'BALCAO', 'IN_PREPARATION', now() - interval '30 minutes', now() - interval '20 minutes', 0, now() - interval '25 minutes', null, null)
ON CONFLICT (id) DO UPDATE SET
status = EXCLUDED.status,
updated_at = now();

INSERT INTO kds_ticket_items (id, kds_ticket_id, order_item_id, status, created_at, updated_at, version)
VALUES
('81000000-0000-0000-0000-000000000001', '80000000-0000-0000-0000-000000000001', '61000000-0000-0000-0000-000000000004', 'READY', now() - interval '55 minutes', now() - interval '40 minutes', 0),
('81000000-0000-0000-0000-000000000002', '80000000-0000-0000-0000-000000000001', '61000000-0000-0000-0000-000000000005', 'READY', now() - interval '55 minutes', now() - interval '40 minutes', 0),
('81000000-0000-0000-0000-000000000003', '80000000-0000-0000-0000-000000000002', '61000000-0000-0000-0000-000000000007', 'IN_PREPARATION', now() - interval '30 minutes', now() - interval '20 minutes', 0)
ON CONFLICT (id) DO UPDATE SET
status = EXCLUDED.status,
updated_at = now();

-- =========================================================
-- STOCK
-- =========================================================
INSERT INTO stock_balances (id, product_id, quantity, reserved_quantity, created_at, updated_at, version)
VALUES
('90000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 340.000, 0.000, now(), now(), 0),
('90000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000002', 120.000, 0.000, now(), now(), 0),
('90000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000003', 74.000, 3.000, now(), now(), 0),
('90000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000004', 96.000, 4.000, now(), now(), 0),
('90000000-0000-0000-0000-000000000005', '40000000-0000-0000-0000-000000000005', 37.000, 2.000, now(), now(), 0),
('90000000-0000-0000-0000-000000000006', '40000000-0000-0000-0000-000000000008', 60.000, 0.000, now(), now(), 0)
ON CONFLICT (product_id) DO UPDATE SET
quantity = EXCLUDED.quantity,
reserved_quantity = EXCLUDED.reserved_quantity,
updated_at = now();

INSERT INTO stock_movements (
id, product_id, type, quantity, previous_quantity, resulting_quantity, reason, order_id,
created_by, source, reference_type, reference_id, idempotency_key, created_at, updated_at, version
)
VALUES
('91000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 'IN', 350.000, 0.000, 350.000, 'Produção inicial do dia', null, '20000000-0000-0000-0000-000000000005', 'LOCAL', 'PRODUCTION', null, 'SEED:STOCK:PAO-FRANCES:IN', now() - interval '5 hours', now(), 0),
('91000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000001', 'SALE', 10.000, 350.000, 340.000, 'Venda finalizada', '60000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000003', 'LOCAL', 'ORDER', '60000000-0000-0000-0000-000000000001', 'SALE:60000000-0000-0000-0000-000000000001:40000000-0000-0000-0000-000000000001', now() - interval '2 hours', now(), 0),
('91000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000003', 'IN', 80.000, 0.000, 80.000, 'Produção de salgados', null, '20000000-0000-0000-0000-000000000005', 'LOCAL', 'PRODUCTION', null, 'SEED:STOCK:COXINHA:IN', now() - interval '4 hours', now(), 0),
('91000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000003', 'SALE', 6.000, 80.000, 74.000, 'Vendas do período', '60000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000003', 'LOCAL', 'ORDER', '60000000-0000-0000-0000-000000000002', 'SALE:60000000-0000-0000-0000-000000000002:40000000-0000-0000-0000-000000000003', now() - interval '55 minutes', now(), 0)
ON CONFLICT (id) DO UPDATE SET
quantity = EXCLUDED.quantity,
resulting_quantity = EXCLUDED.resulting_quantity,
updated_at = now();

-- =========================================================
-- SYNC EVENTS / AUDIT
-- =========================================================
INSERT INTO sync_events (
id, idempotency_key, direction, source_environment, target_environment,
aggregate_type, aggregate_id, event_type, payload, status, retry_count,
next_retry_at, last_error, processed_at, created_at, updated_at, version
)
VALUES
('a0000000-0000-0000-0000-000000000001', 'LOCAL-SEED-PRODUCT-40000000-0000-0000-0000-000000000001', 'LOCAL_TO_ONLINE', 'LOCAL', 'ONLINE', 'PRODUCT', '40000000-0000-0000-0000-000000000001', 'PRODUCT_UPDATED', '{"storeId":"store-001","productId":"40000000-0000-0000-0000-000000000001","name":"Pão Francês","available":true}'::jsonb, 'PENDING', 0, now() + interval '10 minutes', null, null, now(), now(), 0),
('a0000000-0000-0000-0000-000000000002', 'LOCAL-SEED-SALE-60000000-0000-0000-0000-000000000001', 'LOCAL_TO_ONLINE', 'LOCAL', 'ONLINE', 'ORDER', '60000000-0000-0000-0000-000000000001', 'SALE_FINISHED', '{"storeId":"store-001","localOrderId":"60000000-0000-0000-0000-000000000001","totalAmount":24.90,"paymentStatus":"PAID"}'::jsonb, 'PENDING', 0, now() + interval '10 minutes', null, null, now(), now(), 0)
ON CONFLICT (idempotency_key) DO UPDATE SET
payload = EXCLUDED.payload,
status = EXCLUDED.status,
updated_at = now();

INSERT INTO audit_logs (id, actor_user_id, action, entity_type, entity_id, details, ip_address, created_at, updated_at, version)
VALUES
('b0000000-0000-0000-0000-000000000001', (SELECT id FROM users WHERE username = 'admin'), 'SEED_HOMOLOGATION_DATABASE', 'Database', null, '{"description":"Carga de dados local para homologação próxima de produção"}'::jsonb, '127.0.0.1', now(), now(), 0),
('b0000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000003', 'PDV_SALE_FINISHED', 'Order', '60000000-0000-0000-0000-000000000001', '{"totalAmount":24.90,"paymentMethod":"CASH"}'::jsonb, '127.0.0.1', now() - interval '2 hours', now(), 0)
ON CONFLICT (id) DO UPDATE SET
details = EXCLUDED.details,
updated_at = now();

COMMIT;
