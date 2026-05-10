BEGIN;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =========================================================
-- ROLES / USERS ONLINE
-- =========================================================
INSERT INTO roles (id, name, description, created_at, updated_at, version)
VALUES
('11000000-0000-0000-0000-000000000001', 'ADMIN', 'Administrador online', now(), now(), 0),
('11000000-0000-0000-0000-000000000002', 'ATTENDANT', 'Atendimento online e WhatsApp', now(), now(), 0),
('11000000-0000-0000-0000-000000000003', 'REPORTS', 'Acesso a relatórios remotos', now(), now(), 0)
ON CONFLICT (name) DO UPDATE SET
description = EXCLUDED.description,
updated_at = now();

-- Senha online: Online@2026!
INSERT INTO users (id, name, email, username, password_hash, active, created_at, updated_at, version)
VALUES
('21000000-0000-0000-0000-000000000001', 'Administrador Online', 'admin.online@novaalianca.com.br', 'admin.online', '$2a$10$6SQPJZUIjF3Kse4p10Wrgu71HrjbP8foCqSfXIQEKrXPMFNjdNmaa', true, now(), now(), 0),
('21000000-0000-0000-0000-000000000002', 'Atendimento Online', 'atendimento@novaalianca.com.br', 'atendimento.online', '$2a$10$6SQPJZUIjF3Kse4p10Wrgu71HrjbP8foCqSfXIQEKrXPMFNjdNmaa', true, now(), now(), 0),
('21000000-0000-0000-0000-000000000003', 'Relatórios Online', 'relatorios@novaalianca.com.br', 'relatorios.online', '$2a$10$6SQPJZUIjF3Kse4p10Wrgu71HrjbP8foCqSfXIQEKrXPMFNjdNmaa', true, now(), now(), 0)
ON CONFLICT (username) DO UPDATE SET
name = EXCLUDED.name,
email = EXCLUDED.email,
password_hash = EXCLUDED.password_hash,
active = true,
updated_at = now();

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'ADMIN'
WHERE u.username = 'admin.online'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'ATTENDANT'
WHERE u.username = 'atendimento.online'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'REPORTS'
WHERE u.username = 'relatorios.online'
ON CONFLICT DO NOTHING;

-- =========================================================
-- CATEGORIES / PRODUCTS ONLINE
-- =========================================================
INSERT INTO categories (id, name, description, display_order, image_url, active, show_online, show_on_pdv, show_on_whatsapp, created_at, updated_at, version)
VALUES
('31000000-0000-0000-0000-000000000001', 'Pães', 'Pães disponíveis para encomenda e retirada.', 1, '/assets/menu/paes.jpg', true, true, true, true, now(), now(), 0),
('31000000-0000-0000-0000-000000000002', 'Salgados', 'Salgados para retirada e delivery.', 2, '/assets/menu/salgados.jpg', true, true, true, true, now(), now(), 0),
('31000000-0000-0000-0000-000000000003', 'Bolos e Tortas', 'Sobremesas, tortas fatiadas e bolos.', 3, '/assets/menu/bolos.jpg', true, true, true, true, now(), now(), 0),
('31000000-0000-0000-0000-000000000004', 'Bebidas', 'Bebidas geladas e café.', 4, '/assets/menu/bebidas.jpg', true, true, true, true, now(), now(), 0)
ON CONFLICT (id) DO UPDATE SET
name = EXCLUDED.name,
description = EXCLUDED.description,
active = true,
updated_at = now();

INSERT INTO products (
id, category_id, name, description, price, promotional_price, cost_price, sku, barcode, image_url,
unit_type, preparation_sector, preparation_time_minutes, active, available,
sell_on_pdv, sell_online, sell_on_whatsapp, stock_controlled, created_at, updated_at, version
)
VALUES
('42000000-0000-0000-0000-000000000001', '31000000-0000-0000-0000-000000000001', 'Pão Francês', 'Pão francês tradicional.', 0.90, null, 0.32, 'ONLINE-PAO-FRANCES', '7891000000018', '/assets/products/pao-frances.jpg', 'UNIT', 'SEM_PREPARO', 0, true, true, true, true, true, true, now(), now(), 0),
('42000000-0000-0000-0000-000000000002', '31000000-0000-0000-0000-000000000002', 'Coxinha de Frango', 'Coxinha de frango para retirada ou delivery.', 7.50, null, 2.80, 'ONLINE-COXINHA', '7891000000025', '/assets/products/coxinha.jpg', 'UNIT', 'CHAPA', 12, true, true, true, true, true, true, now(), now(), 0),
('42000000-0000-0000-0000-000000000003', '31000000-0000-0000-0000-000000000003', 'Torta de Doce de Leite - Fatia', 'Fatia de torta de doce de leite.', 12.00, 10.90, 4.50, 'ONLINE-TORTA-DL-FATIA', '7891000000032', '/assets/products/torta-doce-leite.jpg', 'UNIT', 'SEM_PREPARO', 0, true, true, true, true, true, true, now(), now(), 0),
('42000000-0000-0000-0000-000000000004', '31000000-0000-0000-0000-000000000004', 'Suco Natural de Laranja 300ml', 'Suco natural preparado na hora.', 8.00, null, 3.00, 'ONLINE-SUCO-LARANJA', '7891000000049', '/assets/products/suco-laranja.jpg', 'UNIT', 'BEBIDAS', 5, true, true, true, true, true, false, now(), now(), 0)
ON CONFLICT (id) DO UPDATE SET
price = EXCLUDED.price,
available = EXCLUDED.available,
active = EXCLUDED.active,
updated_at = now();

INSERT INTO product_availability (id, product_id, status, available_quantity, channel, reason, updated_by, created_at, updated_at, version)
VALUES
('43000000-0000-0000-0000-000000000001', '42000000-0000-0000-0000-000000000001', 'AVAILABLE', 300.000, 'SITE', 'Sincronizado com estoque local', '21000000-0000-0000-0000-000000000001', now(), now(), 0),
('43000000-0000-0000-0000-000000000002', '42000000-0000-0000-0000-000000000002', 'AVAILABLE', 70.000, 'SITE', 'Disponível para delivery', '21000000-0000-0000-0000-000000000001', now(), now(), 0),
('43000000-0000-0000-0000-000000000003', '42000000-0000-0000-0000-000000000003', 'AVAILABLE', 28.000, 'WHATSAPP', 'Disponível para atendimento mock', '21000000-0000-0000-0000-000000000002', now(), now(), 0)
ON CONFLICT (id) DO UPDATE SET
status = EXCLUDED.status,
available_quantity = EXCLUDED.available_quantity,
updated_at = now();

-- =========================================================
-- CUSTOMERS / ADDRESSES
-- =========================================================
INSERT INTO customers (id, name, phone, email, document, birth_date, created_at, updated_at, version)
VALUES
('52000000-0000-0000-0000-000000000001', 'Juliana Martins', '21999990001', 'juliana.martins@email.com', '45678901234', '1992-06-10', now(), now(), 0),
('52000000-0000-0000-0000-000000000002', 'Roberto Almeida', '21999990002', 'roberto.almeida@email.com', '56789012345', '1981-11-03', now(), now(), 0),
('52000000-0000-0000-0000-000000000003', 'Paula Ribeiro', '21999990003', 'paula.ribeiro@email.com', '67890123456', '1997-03-17', now(), now(), 0)
ON CONFLICT (id) DO UPDATE SET
name = EXCLUDED.name,
phone = EXCLUDED.phone,
email = EXCLUDED.email,
updated_at = now();

INSERT INTO customer_addresses (id, customer_id, label, street, number, complement, neighborhood, city, state, zip_code, reference, latitude, longitude, default_address, created_at, updated_at, version)
VALUES
('53000000-0000-0000-0000-000000000001', '52000000-0000-0000-0000-000000000001', 'Casa', 'Rua Santa Clara', '45', 'Apto 201', 'Centro', 'Magé', 'RJ', '25900020', 'Prédio próximo à padaria', -22.6579000, -43.0315000, true, now(), now(), 0),
('53000000-0000-0000-0000-000000000002', '52000000-0000-0000-0000-000000000002', 'Casa', 'Rua do Comércio', '300', null, 'Centro', 'Magé', 'RJ', '25900050', 'Casa com portão preto', -22.6581000, -43.0320000, true, now(), now(), 0),
('53000000-0000-0000-0000-000000000003', '52000000-0000-0000-0000-000000000003', 'Trabalho', 'Avenida Principal', '900', 'Sala 4', 'Piabetá', 'Magé', 'RJ', '25931000', 'Em frente ao mercado', -22.6073000, -43.1831000, true, now(), now(), 0)
ON CONFLICT (id) DO UPDATE SET
street = EXCLUDED.street,
number = EXCLUDED.number,
reference = EXCLUDED.reference,
updated_at = now();

-- =========================================================
-- ONLINE ORDERS
-- Online possui orders.payment_method.
-- =========================================================
INSERT INTO orders (
id, customer_id, delivery_address_id, origin, status, payment_status, delivery_type, payment_method,
subtotal, discount_amount, delivery_fee, total_amount, notes, created_at, updated_at, version, finished_at, canceled_at
)
VALUES
('63000000-0000-0000-0000-000000000001', '52000000-0000-0000-0000-000000000001', '53000000-0000-0000-0000-000000000001', 'ONLINE', 'SENT_TO_STORE', 'PAID', 'DELIVERY', 'ONLINE_PIX', 35.40, 0.00, 6.00, 41.40, 'Pedido online pago em PIX mock.', now() - interval '1 hour', now() - interval '50 minutes', 0, null, null),
('63000000-0000-0000-0000-000000000002', '52000000-0000-0000-0000-000000000002', '53000000-0000-0000-0000-000000000002', 'ONLINE', 'PAYMENT_PENDING', 'PENDING', 'PICKUP', 'ONLINE_PIX', 22.90, 0.00, 0.00, 22.90, 'Pedido aguardando pagamento mock.', now() - interval '20 minutes', now() - interval '20 minutes', 0, null, null),
('63000000-0000-0000-0000-000000000003', '52000000-0000-0000-0000-000000000003', '53000000-0000-0000-0000-000000000003', 'WHATSAPP', 'ACCEPTED', 'PAID', 'DELIVERY', 'CASH', 31.50, 0.00, 6.00, 37.50, 'Pedido via WhatsApp mock.', now() - interval '35 minutes', now() - interval '25 minutes', 0, null, null)
ON CONFLICT (id) DO UPDATE SET
status = EXCLUDED.status,
payment_status = EXCLUDED.payment_status,
payment_method = EXCLUDED.payment_method,
updated_at = now();

INSERT INTO order_items (id, order_id, product_id, product_name_snapshot, quantity, unit_price, total_price, observation, status, preparation_sector, created_at, updated_at, version)
VALUES
('64000000-0000-0000-0000-000000000001', '63000000-0000-0000-0000-000000000001', '42000000-0000-0000-0000-000000000002', 'Coxinha de Frango', 2.000, 7.50, 15.00, 'Enviar quente', 'WAITING_PREPARATION', 'CHAPA', now() - interval '1 hour', now() - interval '50 minutes', 0),
('64000000-0000-0000-0000-000000000002', '63000000-0000-0000-0000-000000000001', '42000000-0000-0000-0000-000000000003', 'Torta de Doce de Leite - Fatia', 2.000, 10.90, 21.80, null, 'WAITING_PREPARATION', 'SEM_PREPARO', now() - interval '1 hour', now() - interval '50 minutes', 0),
('64000000-0000-0000-0000-000000000003', '63000000-0000-0000-0000-000000000002', '42000000-0000-0000-0000-000000000001', 'Pão Francês', 10.000, 0.90, 9.00, null, 'CREATED', 'SEM_PREPARO', now() - interval '20 minutes', now() - interval '20 minutes', 0),
('64000000-0000-0000-0000-000000000004', '63000000-0000-0000-0000-000000000002', '42000000-0000-0000-0000-000000000004', 'Suco Natural de Laranja 300ml', 1.000, 8.00, 8.00, 'Sem gelo', 'CREATED', 'BEBIDAS', now() - interval '20 minutes', now() - interval '20 minutes', 0),
('64000000-0000-0000-0000-000000000005', '63000000-0000-0000-0000-000000000003', '42000000-0000-0000-0000-000000000003', 'Torta de Doce de Leite - Fatia', 3.000, 10.50, 31.50, 'Enviar guardanapo', 'WAITING_PREPARATION', 'SEM_PREPARO', now() - interval '35 minutes', now() - interval '25 minutes', 0)
ON CONFLICT (id) DO UPDATE SET
status = EXCLUDED.status,
updated_at = now();

INSERT INTO payments (id, order_id, method, status, amount, transaction_id, gateway, webhook_payload, paid_at, canceled_at, created_at, updated_at, version)
VALUES
('65000000-0000-0000-0000-000000000001', '63000000-0000-0000-0000-000000000001', 'ONLINE_PIX', 'PAID', 41.40, 'MOCK-PIX-0001', 'MOCK_GATEWAY', '{"status":"PAID","provider":"mock"}', now() - interval '55 minutes', null, now() - interval '1 hour', now() - interval '55 minutes', 0),
('65000000-0000-0000-0000-000000000002', '63000000-0000-0000-0000-000000000002', 'ONLINE_PIX', 'PENDING', 22.90, 'MOCK-PIX-0002', 'MOCK_GATEWAY', '{"status":"PENDING","provider":"mock"}', null, null, now() - interval '20 minutes', now() - interval '20 minutes', 0),
('65000000-0000-0000-0000-000000000003', '63000000-0000-0000-0000-000000000003', 'CASH', 'PAID', 37.50, 'WHATSAPP-CASH-0003', 'MOCK_GATEWAY', '{"status":"PAID","provider":"mock"}', now() - interval '25 minutes', null, now() - interval '35 minutes', now() - interval '25 minutes', 0)
ON CONFLICT (id) DO UPDATE SET
status = EXCLUDED.status,
webhook_payload = EXCLUDED.webhook_payload,
updated_at = now();

-- =========================================================
-- STOCK ONLINE
-- =========================================================
INSERT INTO stock_balances (id, product_id, quantity, reserved_quantity, created_at, updated_at, version)
VALUES
('92000000-0000-0000-0000-000000000001', '42000000-0000-0000-0000-000000000001', 300.000, 0.000, now(), now(), 0),
('92000000-0000-0000-0000-000000000002', '42000000-0000-0000-0000-000000000002', 70.000, 2.000, now(), now(), 0),
('92000000-0000-0000-0000-000000000003', '42000000-0000-0000-0000-000000000003', 28.000, 1.000, now(), now(), 0),
('92000000-0000-0000-0000-000000000004', '42000000-0000-0000-0000-000000000004', 999.000, 0.000, now(), now(), 0)
ON CONFLICT (product_id) DO UPDATE SET
quantity = EXCLUDED.quantity,
reserved_quantity = EXCLUDED.reserved_quantity,
updated_at = now();

-- =========================================================
-- SYNC / LOCAL SALE SUMMARY
-- =========================================================
INSERT INTO sync_events (
id, idempotency_key, direction, source_environment, target_environment,
aggregate_type, aggregate_id, event_type, payload, status, retry_count,
next_retry_at, last_error, processed_at, created_at, updated_at, version
)
VALUES
('aa000000-0000-0000-0000-000000000001', 'ONLINE-SEED-PENDING-CATALOG-42000000-0000-0000-0000-000000000001', 'ONLINE_TO_LOCAL', 'ONLINE', 'LOCAL', 'PRODUCT', '42000000-0000-0000-0000-000000000001', 'PRODUCT_UPDATED', '{"storeId":"store-001","productId":"42000000-0000-0000-0000-000000000001","available":true}'::jsonb, 'PENDING', 0, now() + interval '10 minutes', null, null, now(), now(), 0),
('aa000000-0000-0000-0000-000000000002', 'ONLINE-SEED-RECEIVED-LOCAL-SALE-60000000-0000-0000-0000-000000000001', 'LOCAL_TO_ONLINE', 'LOCAL', 'ONLINE', 'ORDER', '60000000-0000-0000-0000-000000000001', 'SALE_FINISHED', '{"storeId":"store-001","localOrderId":"60000000-0000-0000-0000-000000000001","totalAmount":24.90}'::jsonb, 'SYNCED', 0, null, null, now() - interval '1 hour', now() - interval '1 hour', now(), 0)
ON CONFLICT (idempotency_key) DO UPDATE SET
payload = EXCLUDED.payload,
status = EXCLUDED.status,
updated_at = now();

INSERT INTO online_local_sale_summaries (
id, store_id, local_order_id, order_number, total_amount, payment_status, finished_at, raw_payload, created_at, updated_at, version
)
VALUES
('ab000000-0000-0000-0000-000000000001', 'store-001', '60000000-0000-0000-0000-000000000001', 10001, 24.90, 'PAID', now() - interval '2 hours', '{"origin":"LOCAL","paymentMethod":"CASH","operator":"caixa01"}'::jsonb, now(), now(), 0),
('ab000000-0000-0000-0000-000000000002', 'store-001', '60000000-0000-0000-0000-000000000002', 10002, 35.00, 'PAID', now() - interval '55 minutes', '{"origin":"LOCAL","paymentMethod":"CARD","operator":"caixa01"}'::jsonb, now(), now(), 0)
ON CONFLICT (store_id, local_order_id) DO UPDATE SET
total_amount = EXCLUDED.total_amount,
payment_status = EXCLUDED.payment_status,
raw_payload = EXCLUDED.raw_payload,
updated_at = now();

-- =========================================================
-- WHATSAPP MOCK
-- =========================================================
INSERT INTO whatsapp_conversations (id, customer_phone, customer_name, status, assigned_to, order_id, created_at, updated_at, version)
VALUES
('ac000000-0000-0000-0000-000000000001', '21999990003', 'Paula Ribeiro', 'OPEN', '21000000-0000-0000-0000-000000000002', '63000000-0000-0000-0000-000000000003', now() - interval '40 minutes', now() - interval '20 minutes', 0),
('ac000000-0000-0000-0000-000000000002', '21999990004', 'Cliente Teste WhatsApp', 'OPEN', '21000000-0000-0000-0000-000000000002', null, now() - interval '10 minutes', now() - interval '5 minutes', 0)
ON CONFLICT (id) DO UPDATE SET
status = EXCLUDED.status,
assigned_to = EXCLUDED.assigned_to,
updated_at = now();

INSERT INTO whatsapp_messages (
id, conversation_id, external_message_id, direction, sender_phone, sender_name,
content, message_type, status, created_at, updated_at, version
)
VALUES
('ad000000-0000-0000-0000-000000000001', 'ac000000-0000-0000-0000-000000000001', 'mock-msg-0001', 'INBOUND', '21999990003', 'Paula Ribeiro', 'Boa tarde, quero pedir 3 fatias de torta.', 'TEXT', 'RECEIVED', now() - interval '38 minutes', now() - interval '38 minutes', 0),
('ad000000-0000-0000-0000-000000000002', 'ac000000-0000-0000-0000-000000000001', 'mock-msg-0002', 'OUTBOUND', null, 'Atendimento Nova Aliança', 'Pedido registrado. Total com entrega: R$ 37,50.', 'TEXT', 'SENT', now() - interval '35 minutes', now() - interval '35 minutes', 0),
('ad000000-0000-0000-0000-000000000003', 'ac000000-0000-0000-0000-000000000002', 'mock-msg-0003', 'INBOUND', '21999990004', 'Cliente Teste WhatsApp', 'Vocês têm pão doce disponível?', 'TEXT', 'RECEIVED', now() - interval '10 minutes', now() - interval '10 minutes', 0)
ON CONFLICT (id) DO UPDATE SET
content = EXCLUDED.content,
status = EXCLUDED.status,
updated_at = now();

-- =========================================================
-- AUDIT
-- =========================================================
INSERT INTO audit_logs (id, actor_user_id, action, entity_type, entity_id, details, ip_address, created_at, updated_at)
VALUES
('ae000000-0000-0000-0000-000000000001', '21000000-0000-0000-0000-000000000001', 'SEED_HOMOLOGATION_DATABASE', 'Database', null, '{"description":"Carga online para homologação próxima de produção"}'::jsonb, '127.0.0.1', now(), now()),
('ae000000-0000-0000-0000-000000000002', '21000000-0000-0000-0000-000000000002', 'WHATSAPP_CONVERSATION_HANDLED', 'WhatsAppConversation', 'ac000000-0000-0000-0000-000000000001', '{"provider":"mock","orderId":"63000000-0000-0000-0000-000000000003"}'::jsonb, '127.0.0.1', now(), now())
ON CONFLICT (id) DO UPDATE SET
details = EXCLUDED.details,
updated_at = now();

COMMIT;
