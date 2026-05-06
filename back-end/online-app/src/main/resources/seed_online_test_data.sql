-- Limpeza do banco Online (truncando tabelas na ordem correta)
TRUNCATE TABLE audit_logs CASCADE;
TRUNCATE TABLE sync_events CASCADE;
TRUNCATE TABLE whatsapp_messages CASCADE;
TRUNCATE TABLE whatsapp_conversations CASCADE;
TRUNCATE TABLE payments CASCADE;
TRUNCATE TABLE order_items CASCADE;
TRUNCATE TABLE orders CASCADE;
TRUNCATE TABLE product_availability CASCADE;
TRUNCATE TABLE products CASCADE;
TRUNCATE TABLE categories CASCADE;
TRUNCATE TABLE customer_addresses CASCADE;
TRUNCATE TABLE customers CASCADE;
TRUNCATE TABLE user_roles CASCADE;
TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE roles CASCADE;

-- Seed de Roles
INSERT INTO roles (id, name, description) VALUES
('b1000000-0000-0000-0000-000000000001', 'ADMIN', 'Acesso administrativo completo.'),
('b1000000-0000-0000-0000-000000000002', 'GERENTE', 'Acesso gerencial e acoes criticas.'),
('b1000000-0000-0000-0000-000000000003', 'CAIXA', 'Operacao de caixa e PDV.'),
('b1000000-0000-0000-0000-000000000004', 'ATENDENTE', 'Atendimento e pedidos.'),
('b1000000-0000-0000-0000-000000000005', 'COZINHA', 'Operacao do KDS e preparo.'),
('b1000000-0000-0000-0000-000000000006', 'ENTREGADOR', 'Operacao de entregas.'),
('b1000000-0000-0000-0000-000000000007', 'CONSULTA', 'Acesso somente leitura.');

-- Seed de Usuários (Mesmos hashes do local para consistência)
INSERT INTO users (id, name, email, username, password_hash, active) VALUES
('a1000000-0000-0000-0000-000000000001', 'Admin Online', 'admin-online@novaalianca.com', 'admin', 'pbkdf2$210000$sodvHGjtOaEkW1SKSL7e3Q$-hF7_U3D84JMx56TrnYESryWF-8GpDpWOLDK0NMT4JQ', true),
('a1000000-0000-0000-0000-000000000002', 'Gerente Online', 'gerente-online@novaalianca.com', 'gerente', 'pbkdf2$210000$2-YX3-jHb6XHFrdM_UlX8g$tChX2471XwCCx158FUNbUctgwPaOIfYgC9wUR02N8yw', true);

INSERT INTO user_roles (user_id, role_id) VALUES
('a1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000001'),
('a1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000002');

-- Seed de Categorias
INSERT INTO categories (id, name, description, display_order, active) VALUES
('c0000000-0000-0000-0000-000000000001', 'Pães', 'Pães frescos produzidos diariamente', 1, true),
('c0000000-0000-0000-0000-000000000002', 'Bolos', 'Bolos caseiros e recheados', 2, true),
('c0000000-0000-0000-0000-000000000003', 'Bebidas', 'Refrigerantes, sucos e cafés', 3, true);

-- Seed de Produtos
INSERT INTO products (id, category_id, name, description, price, unit_type, preparation_sector, active, available, stock_controlled) VALUES
('d0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'Pão Francês', 'Tradicional pãozinho de sal', 0.50, 'UNIT', 'PANIFICACAO', true, true, true),
('d0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000003', 'Coca-Cola 350ml', 'Lata gelada', 5.00, 'UNIT', 'SEM_PREPARO', true, true, true);

-- Seed de Clientes
INSERT INTO customers (id, name, phone, email, document) VALUES
('f0000000-0000-0000-0000-000000000001', 'João Silva', '11988887777', 'joao@email.com', '12345678901');

-- Seed de Pedidos Online
INSERT INTO orders (id, customer_id, origin, status, payment_status, delivery_type, subtotal, delivery_fee, total_amount, payment_method) VALUES
('e0000000-0000-0000-0000-000000000002', 'f0000000-0000-0000-0000-000000000001', 'ONLINE', 'PENDING', 'PENDING', 'DELIVERY', 5.50, 5.00, 10.50, 'CREDIT_CARD');

INSERT INTO order_items (id, order_id, product_id, product_name_snapshot, quantity, unit_price, total_price, status, preparation_sector) VALUES
('90000000-0000-0000-0000-000000000003', 'e0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001', 'Pão Francês', 1, 0.50, 0.50, 'PENDING', 'PANIFICACAO'),
('90000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000004', 'Coca-Cola 350ml', 1, 5.00, 5.00, 'PENDING', 'SEM_PREPARO');

-- Seed de WhatsApp
INSERT INTO whatsapp_conversations (id, customer_phone, customer_name, status, order_id) VALUES
('ff000000-0000-0000-0000-000000000001', '11988887777', 'João Silva', 'OPEN', 'e0000000-0000-0000-0000-000000000002');

INSERT INTO whatsapp_messages (conversation_id, direction, content, status) VALUES
('ff000000-0000-0000-0000-000000000001', 'INBOUND', 'Olá, gostaria de fazer um pedido.', 'READ'),
('ff000000-0000-0000-0000-000000000001', 'OUTBOUND', 'Olá João! Como posso ajudar?', 'DELIVERED'),
('ff000000-0000-0000-0000-000000000001', 'INBOUND', 'Quero 1 pão francês e uma coca.', 'RECEIVED');
