-- Limpeza do banco (truncando tabelas na ordem correta para respeitar FKs)
TRUNCATE TABLE audit_logs CASCADE;
TRUNCATE TABLE sync_events CASCADE;
TRUNCATE TABLE stock_movements CASCADE;
TRUNCATE TABLE kds_ticket_items CASCADE;
TRUNCATE TABLE kds_tickets CASCADE;
TRUNCATE TABLE cash_movements CASCADE;
TRUNCATE TABLE cash_registers CASCADE;
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

-- Seed de Usuários
-- Admin: admin / admin123
INSERT INTO users (id, name, email, username, password_hash, active) VALUES
('a1000000-0000-0000-0000-000000000001', 'Administrador', 'admin@novaalianca.local', 'admin', 'pbkdf2$210000$sodvHGjtOaEkW1SKSL7e3Q$-hF7_U3D84JMx56TrnYESryWF-8GpDpWOLDK0NMT4JQ', true),
('a1000000-0000-0000-0000-000000000002', 'Gerente', 'gerente@novaalianca.local', 'gerente', 'pbkdf2$210000$2-YX3-jHb6XHFrdM_UlX8g$tChX2471XwCCx158FUNbUctgwPaOIfYgC9wUR02N8yw', true),
('a1000000-0000-0000-0000-000000000003', 'Caixa', 'caixa@novaalianca.local', 'caixa', 'pbkdf2$210000$Otwf1e3B88UCtgo_lOwiQw$DoxkLFLYv8RY5Ov-nsKglNken-OIQ-9xLkAPn4X9vWM', true),
('a1000000-0000-0000-0000-000000000004', 'Cozinha', 'cozinha@novaalianca.local', 'cozinha', 'pbkdf2$210000$8JBfYQOdYNiEMqyDl01ehA$uOBARB3DtbKH8y1rNJI9at2BXqt3jiHC6VkaTJdNPwc', true),
('a1000000-0000-0000-0000-000000000005', 'Entrega', 'entrega@novaalianca.local', 'entrega', 'pbkdf2$210000$Iy_8WFpKmCehfqInoxR2fw$xc_NRk5pX22KyWXM-93J7uVCw1Re8oYfHRmMX7Su4Is', true);

-- Associar Roles
INSERT INTO user_roles (user_id, role_id) VALUES
('a1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000001'), -- Admin
('a1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000002'), -- Gerente
('a1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000003'), -- Caixa
('a1000000-0000-0000-0000-000000000004', 'b1000000-0000-0000-0000-000000000005'), -- Cozinha
('a1000000-0000-0000-0000-000000000005', 'b1000000-0000-0000-0000-000000000006'); -- Entrega

-- Seed de Categorias
INSERT INTO categories (id, name, description, display_order, active) VALUES
('c0000000-0000-0000-0000-000000000001', 'Pães', 'Pães frescos produzidos diariamente', 1, true),
('c0000000-0000-0000-0000-000000000002', 'Bolos', 'Bolos caseiros e recheados', 2, true),
('c0000000-0000-0000-0000-000000000003', 'Bebidas', 'Refrigerantes, sucos e cafés', 3, true),
('c0000000-0000-0000-0000-000000000004', 'Salgados', 'Coxinhas, quibes e empadas', 4, true),
('c0000000-0000-0000-0000-000000000005', 'Lanches', 'Sanduíches naturais e artesanais', 5, true);

-- Seed de Produtos
INSERT INTO products (id, category_id, name, description, price, unit_type, preparation_sector, active, available, stock_controlled) VALUES
('d0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'Pão Francês', 'Tradicional pãozinho de sal', 0.50, 'UNIT', 'PANIFICACAO', true, true, true),
('d0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000001', 'Pão de Queijo', 'Pão de queijo mineiro original', 2.50, 'UNIT', 'COZINHA', true, true, true),
('d0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000002', 'Bolo de Cenoura', 'Bolo com cobertura de chocolate', 15.00, 'UNIT', 'CONFEITARIA', true, true, false),
('d0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000003', 'Coca-Cola 350ml', 'Lata gelada', 5.00, 'UNIT', 'SEM_PREPARO', true, true, true),
('d0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000003', 'Café Espresso', 'Café moído na hora', 4.50, 'UNIT', 'COZINHA', true, true, false),
('d0000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000004', 'Coxinha de Frango', 'Coxinha grande com catupiry', 7.00, 'UNIT', 'COZINHA', true, true, true),
('d0000000-0000-0000-0000-000000000007', 'c0000000-0000-0000-0000-000000000005', 'X-Tudo', 'Hambúrguer completo artesanal', 22.00, 'UNIT', 'COZINHA', true, true, false);

-- Seed de Disponibilidade
INSERT INTO product_availability (product_id, status, available_quantity, channel) VALUES
('d0000000-0000-0000-0000-000000000001', 'AVAILABLE', 200, 'PDV'),
('d0000000-0000-0000-0000-000000000001', 'AVAILABLE', 50, 'ONLINE'),
('d0000000-0000-0000-0000-000000000004', 'AVAILABLE', 48, 'PDV'),
('d0000000-0000-0000-0000-000000000004', 'AVAILABLE', 24, 'ONLINE');

-- Seed de Clientes
INSERT INTO customers (id, name, phone, email, document) VALUES
('f0000000-0000-0000-0000-000000000001', 'João Silva', '11988887777', 'joao@email.com', '12345678901'),
('f0000000-0000-0000-0000-000000000002', 'Maria Souza', '11977776666', 'maria@email.com', '98765432100');

-- Seed de Endereços
INSERT INTO customer_addresses (customer_id, street, number, neighborhood, city, state, zip_code, default_address) VALUES
('f0000000-0000-0000-0000-000000000001', 'Rua das Flores', '123', 'Centro', 'São Paulo', 'SP', '01001000', true),
('f0000000-0000-0000-0000-000000000002', 'Av. Paulista', '1000', 'Bela Vista', 'São Paulo', 'SP', '01310100', true);

-- Seed de Pedidos
INSERT INTO orders (id, customer_id, origin, status, payment_status, delivery_type, subtotal, discount_amount, total_amount) VALUES
('e0000000-0000-0000-0000-000000000001', NULL, 'PDV', 'FINISHED', 'PAID', 'TAKE_AWAY', 10.00, 0, 10.00),
('e0000000-0000-0000-0000-000000000002', 'f0000000-0000-0000-0000-000000000001', 'ONLINE', 'PENDING', 'PENDING', 'DELIVERY', 22.00, 0, 27.00);

-- Seed de Itens de Pedido
INSERT INTO order_items (id, order_id, product_id, product_name_snapshot, quantity, unit_price, total_price, status, preparation_sector) VALUES
('90000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', 'Pão Francês', 10, 0.50, 5.00, 'COMPLETED', 'PANIFICACAO'),
('90000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000004', 'Coca-Cola 350ml', 1, 5.00, 5.00, 'COMPLETED', 'SEM_PREPARO'),
('90000000-0000-0000-0000-000000000003', 'e0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000007', 'X-Tudo', 1, 22.00, 22.00, 'PENDING', 'COZINHA');

-- Seed de Pagamentos
INSERT INTO payments (order_id, method, status, amount, paid_at) VALUES
('e0000000-0000-0000-0000-000000000001', 'CASH', 'PAID', 10.00, CURRENT_TIMESTAMP);

-- Seed de Caixa
INSERT INTO cash_registers (id, operator_id, status, opening_amount, opened_at) VALUES
('80000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000003', 'OPEN', 100.00, CURRENT_TIMESTAMP);

INSERT INTO cash_movements (cash_register_id, type, amount, description, order_id, created_by) VALUES
('80000000-0000-0000-0000-000000000001', 'IN', 10.00, 'Venda PDV e0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000003');

-- Seed de KDS
INSERT INTO kds_tickets (id, order_id, sector, status) VALUES
('70000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'PANIFICACAO', 'FINISHED');

INSERT INTO kds_ticket_items (kds_ticket_id, order_item_id, status) VALUES
('70000000-0000-0000-0000-000000000001', '90000000-0000-0000-0000-000000000001', 'READY');
