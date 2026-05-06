-- ============================================================================
-- Sistema Nova Alianca / MNSS
-- Seed unico de populacao produtiva inicial (Adaptado para o schema real)
-- ============================================================================

BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1. Identificacao de usuarios existentes para referenciar nas tabelas
DROP TABLE IF EXISTS seed_user_refs;
CREATE TEMP TABLE seed_user_refs ON COMMIT DROP AS
WITH ordered_users AS (
    SELECT
        id,
        username,
        ROW_NUMBER() OVER (ORDER BY active DESC, created_at ASC, id ASC) AS rn
    FROM users
)
SELECT
    (SELECT id FROM ordered_users WHERE rn = 1) AS admin_id,
    COALESCE(
        (SELECT id FROM ordered_users WHERE username ILIKE '%gerente%' LIMIT 1),
        (SELECT id FROM ordered_users WHERE rn = 2),
        (SELECT id FROM ordered_users WHERE rn = 1)
    ) AS gerente_id,
    COALESCE(
        (SELECT id FROM ordered_users WHERE username ILIKE '%caixa%' LIMIT 1),
        (SELECT id FROM ordered_users WHERE rn = 3),
        (SELECT id FROM ordered_users WHERE rn = 1)
    ) AS caixa_id,
    COALESCE(
        (SELECT id FROM ordered_users WHERE username ILIKE '%cozinha%' LIMIT 1),
        (SELECT id FROM ordered_users WHERE rn = 4),
        (SELECT id FROM ordered_users WHERE rn = 1)
    ) AS cozinha_id,
    COALESCE(
        (SELECT id FROM ordered_users WHERE username ILIKE '%entreg%' LIMIT 1),
        (SELECT id FROM ordered_users WHERE rn = 5),
        (SELECT id FROM ordered_users WHERE rn = 1)
    ) AS entregador_id;

-- 2. Roles (Garantir que as roles existem)
INSERT INTO roles (id, name, description, created_at, updated_at)
VALUES
    (md5('role:ADMIN')::uuid, 'ADMIN', 'Administrador geral do sistema.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (md5('role:GERENTE')::uuid, 'GERENTE', 'Gerente da loja: autoriza cancelamentos, descontos e sangrias.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (md5('role:CAIXA')::uuid, 'CAIXA', 'Operador de caixa e PDV.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (md5('role:ATENDENTE')::uuid, 'ATENDENTE', 'Atendimento de balcao, pedidos e disponibilidade.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (md5('role:COZINHA')::uuid, 'COZINHA', 'Equipe de preparo/KDS.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (md5('role:ENTREGADOR')::uuid, 'ENTREGADOR', 'Entrega e expedicao.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (md5('role:CONSULTA')::uuid, 'CONSULTA', 'Acesso de consulta operacional.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO UPDATE SET
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP;

-- 3. Atribuicoes de Roles
INSERT INTO user_roles (user_id, role_id)
SELECT x.user_id, r.id
FROM (
    SELECT admin_id AS user_id, 'ADMIN' AS role_name FROM seed_user_refs
    UNION ALL SELECT admin_id, 'GERENTE' FROM seed_user_refs
    UNION ALL SELECT admin_id, 'CONSULTA' FROM seed_user_refs
    UNION ALL SELECT gerente_id, 'GERENTE' FROM seed_user_refs
    UNION ALL SELECT gerente_id, 'CONSULTA' FROM seed_user_refs
    UNION ALL SELECT caixa_id, 'CAIXA' FROM seed_user_refs
    UNION ALL SELECT caixa_id, 'ATENDENTE' FROM seed_user_refs
    UNION ALL SELECT cozinha_id, 'COZINHA' FROM seed_user_refs
    UNION ALL SELECT entregador_id, 'ENTREGADOR' FROM seed_user_refs
) x
JOIN roles r ON r.name = x.role_name
WHERE x.user_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- 4. Categorias
INSERT INTO categories (
    id, name, description, display_order, image_url, active,
    show_online, show_on_pdv, show_on_whatsapp, created_at, updated_at
)
VALUES
    (md5('category:paes')::uuid, 'Pães', 'Pães frescos de fabricação própria.', 1, '/images/categories/paes.jpg', TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (md5('category:salgados')::uuid, 'Salgados', 'Salgados assados e fritos de vitrine.', 2, '/images/categories/salgados.jpg', TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (md5('category:lanches')::uuid, 'Lanches e chapa', 'Sanduíches e itens preparados na chapa.', 3, '/images/categories/lanches.jpg', TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (md5('category:bebidas')::uuid, 'Bebidas', 'Cafés, sucos, vitaminas, refrigerantes e água.', 4, '/images/categories/bebidas.jpg', TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (md5('category:confeitaria')::uuid, 'Confeitaria', 'Doces, fatias, pudins e itens de vitrine.', 5, '/images/categories/confeitaria.jpg', TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (md5('category:bolos-tortas')::uuid, 'Bolos e tortas', 'Bolos inteiros, tortas e encomendas.', 6, '/images/categories/bolos-tortas.jpg', TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (md5('category:mercearia')::uuid, 'Mercearia', 'Produtos complementares de conveniência.', 7, '/images/categories/mercearia.jpg', TRUE, FALSE, TRUE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (md5('category:encomendas')::uuid, 'Encomendas', 'Produtos sob encomenda e retirada programada.', 8, '/images/categories/encomendas.jpg', TRUE, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    display_order = EXCLUDED.display_order,
    image_url = EXCLUDED.image_url,
    active = EXCLUDED.active,
    show_online = EXCLUDED.show_online,
    show_on_pdv = EXCLUDED.show_on_pdv,
    show_on_whatsapp = EXCLUDED.show_on_whatsapp,
    updated_at = CURRENT_TIMESTAMP;

-- 5. Produtos
INSERT INTO products (
    id, category_id, name, description, price, promotional_price, cost_price,
    sku, barcode, image_url, unit_type, preparation_sector,
    preparation_time_minutes, active, available, sell_on_pdv, sell_online,
    sell_on_whatsapp, created_at, updated_at, stock_controlled
)
VALUES
    (md5('product:PAO-FRANCES-KG')::uuid, md5('category:paes')::uuid, 'Pão francês kg', 'Pão francês fresco vendido por peso.', 19.90, NULL, 8.30, 'PAO-FRANCES-KG', '7890001000010', '/images/products/pao-frances.jpg', 'KG', 'SEM_PREPARO', NULL, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    (md5('product:PAO-DOCE-UN')::uuid, md5('category:paes')::uuid, 'Pão doce', 'Pão doce tradicional.', 3.50, NULL, 1.35, 'PAO-DOCE-UN', '7890001000027', '/images/products/pao-doce.jpg', 'UNIT', 'SEM_PREPARO', NULL, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    (md5('product:PAO-QUEIJO-PORCAO')::uuid, md5('category:paes')::uuid, 'Pão de queijo porção', 'Porção individual com pães de queijo.', 8.00, NULL, 3.20, 'PAO-QUEIJO-PORCAO', '7890001000034', '/images/products/pao-queijo.jpg', 'UNIT', 'COZINHA', 5, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    (md5('product:PAO-ALHO-UN')::uuid, md5('category:paes')::uuid, 'Pão de alho', 'Pão de alho individual.', 7.00, NULL, 3.05, 'PAO-ALHO-UN', '7890001000065', '/images/products/pao-alho.jpg', 'UNIT', 'COZINHA', 4, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    (md5('product:SALG-COXINHA-FRANGO')::uuid, md5('category:salgados')::uuid, 'Coxinha de frango', 'Coxinha de frango cremosa.', 7.50, NULL, 3.10, 'SALG-COXINHA-FRANGO', '7890002000019', '/images/products/coxinha.jpg', 'UNIT', 'COZINHA', 3, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    (md5('product:SALG-JOELHO-PQ')::uuid, md5('category:salgados')::uuid, 'Joelho de presunto e queijo', 'Salgado assado recheado.', 8.00, NULL, 3.45, 'SALG-JOELHO-PQ', '7890002000026', '/images/products/joelho.jpg', 'UNIT', 'COZINHA', 3, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    (md5('product:SALG-EMPADA-FRANGO')::uuid, md5('category:salgados')::uuid, 'Empada de frango', 'Empada de frango individual.', 7.00, NULL, 2.90, 'SALG-EMPADA-FRANGO', '7890002000040', '/images/products/empada.jpg', 'UNIT', 'COZINHA', 3, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    (md5('product:LANCH-PAO-CHAPA')::uuid, md5('category:lanches')::uuid, 'Pão na chapa', 'Pão francês com manteiga preparado na chapa.', 5.00, NULL, 1.80, 'LANCH-PAO-CHAPA', '7890003000018', '/images/products/pao-na-chapa.jpg', 'UNIT', 'COZINHA', 4, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
    (md5('product:LANCH-MISTO-QUENTE')::uuid, md5('category:lanches')::uuid, 'Misto quente', 'Pão com presunto e queijo na chapa.', 12.00, NULL, 5.10, 'LANCH-MISTO-QUENTE', '7890003000025', '/images/products/misto-quente.jpg', 'UNIT', 'COZINHA', 8, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
    (md5('product:LANCH-XBURGER')::uuid, md5('category:lanches')::uuid, 'X-Burger', 'Hambúrguer com queijo.', 18.90, NULL, 8.60, 'LANCH-XBURGER', '7890003000032', '/images/products/xburger.jpg', 'UNIT', 'COZINHA', 12, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
    (md5('product:LANCH-XSALADA')::uuid, md5('category:lanches')::uuid, 'X-Salada', 'Hambúrguer com queijo, salada e molho.', 22.90, 20.90, 9.80, 'LANCH-XSALADA', '7890003000049', '/images/products/xsalada.jpg', 'UNIT', 'COZINHA', 14, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
    (md5('product:BEB-CAFE-COADO-P')::uuid, md5('category:bebidas')::uuid, 'Café coado pequeno', 'Café coado tradicional.', 3.50, NULL, 0.95, 'BEB-CAFE-COADO-P', '7890004000017', '/images/products/cafe-coado.jpg', 'UNIT', 'COZINHA', 2, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
    (md5('product:BEB-CAFE-LEITE')::uuid, md5('category:bebidas')::uuid, 'Café com leite', 'Café com leite preparado na hora.', 5.50, NULL, 1.80, 'BEB-CAFE-LEITE', '7890004000024', '/images/products/cafe-leite.jpg', 'UNIT', 'COZINHA', 3, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
    (md5('product:BEB-CAPPUCCINO')::uuid, md5('category:bebidas')::uuid, 'Cappuccino', 'Cappuccino cremoso.', 8.00, NULL, 2.60, 'BEB-CAPPUCCINO', '7890004000031', '/images/products/cappuccino.jpg', 'UNIT', 'COZINHA', 4, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
    (md5('product:BEB-SUCO-LARANJA')::uuid, md5('category:bebidas')::uuid, 'Suco de laranja', 'Suco natural de laranja 300 ml.', 9.00, NULL, 3.40, 'BEB-SUCO-LARANJA', '7890004000048', '/images/products/suco-laranja.jpg', 'UNIT', 'COZINHA', 5, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
    (md5('product:BEB-REFRI-LATA')::uuid, md5('category:bebidas')::uuid, 'Refrigerante lata', 'Refrigerante lata 350 ml.', 6.50, NULL, 3.30, 'BEB-REFRI-LATA', '7890004000062', '/images/products/refrigerante-lata.jpg', 'UNIT', 'SEM_PREPARO', NULL, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    (md5('product:BEB-AGUA-500')::uuid, md5('category:bebidas')::uuid, 'Água mineral', 'Água mineral 500 ml.', 3.50, NULL, 1.45, 'BEB-AGUA-500', '7890004000079', '/images/products/agua-mineral.jpg', 'UNIT', 'SEM_PREPARO', NULL, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    (md5('product:CONF-FATIA-BOLO-CHOC')::uuid, md5('category:confeitaria')::uuid, 'Fatia de bolo de chocolate', 'Fatia de bolo de chocolate com cobertura.', 8.50, NULL, 3.30, 'CONF-FATIA-BOLO-CHOC', '7890005000016', '/images/products/fatia-bolo-chocolate.jpg', 'UNIT', 'COZINHA', 2, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
    (md5('product:CONF-FATIA-TORTA-MORANGO')::uuid, md5('category:confeitaria')::uuid, 'Fatia de torta de morango', 'Fatia de torta de morango com chantilly.', 12.00, NULL, 5.20, 'CONF-FATIA-TORTA-MORANGO', '7890005000023', '/images/products/torta-morango.jpg', 'UNIT', 'COZINHA', 2, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
    (md5('product:CONF-BRIGADEIRO')::uuid, md5('category:confeitaria')::uuid, 'Brigadeiro', 'Brigadeiro tradicional.', 3.00, NULL, 1.10, 'CONF-BRIGADEIRO', '7890005000047', '/images/products/brigadeiro.jpg', 'UNIT', 'COZINHA', 1, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
    (md5('product:BOLO-SIMPLES-INTEIRO')::uuid, md5('category:bolos-tortas')::uuid, 'Bolo simples inteiro', 'Bolo simples inteiro para café da tarde.', 28.00, NULL, 12.50, 'BOLO-SIMPLES-INTEIRO', '7890006000015', '/images/products/bolo-simples.jpg', 'UNIT', 'COZINHA', 5, TRUE, TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
    (md5('product:ENC-TORTA-MEDIA')::uuid, md5('category:encomendas')::uuid, 'Torta média sob encomenda', 'Torta média para retirada programada.', 95.00, NULL, 42.00, 'ENC-TORTA-MEDIA', '7890007000014', '/images/products/torta-media.jpg', 'UNIT', 'COZINHA', 120, TRUE, TRUE, FALSE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
    (md5('product:MERC-MANTEIGA-200')::uuid, md5('category:mercearia')::uuid, 'Manteiga 200g', 'Manteiga de mesa 200g.', 13.90, NULL, 9.40, 'MERC-MANTEIGA-200', '7890008000013', '/images/products/manteiga.jpg', 'UNIT', 'SEM_PREPARO', NULL, TRUE, TRUE, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    (md5('product:MERC-LEITE-1L')::uuid, md5('category:mercearia')::uuid, 'Leite integral 1L', 'Leite integral embalagem 1 litro.', 6.90, NULL, 4.60, 'MERC-LEITE-1L', '7890008000020', '/images/products/leite.jpg', 'UNIT', 'SEM_PREPARO', NULL, TRUE, TRUE, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    (md5('product:MERC-REQUEIJAO-200')::uuid, md5('category:mercearia')::uuid, 'Requeijão 200g', 'Requeijão cremoso 200g.', 11.50, NULL, 7.50, 'MERC-REQUEIJAO-200', '7890008000037', '/images/products/requeijao.jpg', 'UNIT', 'SEM_PREPARO', NULL, TRUE, TRUE, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE)
ON CONFLICT (id) DO UPDATE SET
    category_id = EXCLUDED.category_id,
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    price = EXCLUDED.price,
    promotional_price = EXCLUDED.promotional_price,
    cost_price = EXCLUDED.cost_price,
    sku = EXCLUDED.sku,
    barcode = EXCLUDED.barcode,
    image_url = EXCLUDED.image_url,
    unit_type = EXCLUDED.unit_type,
    preparation_sector = EXCLUDED.preparation_sector,
    preparation_time_minutes = EXCLUDED.preparation_time_minutes,
    active = EXCLUDED.active,
    available = EXCLUDED.available,
    sell_on_pdv = EXCLUDED.sell_on_pdv,
    sell_online = EXCLUDED.sell_online,
    sell_on_whatsapp = EXCLUDED.sell_on_whatsapp,
    updated_at = CURRENT_TIMESTAMP,
    stock_controlled = EXCLUDED.stock_controlled;

-- 6. Disponibilidade de produtos
INSERT INTO product_availability (id, product_id, status, available_quantity, channel, reason, updated_by, updated_at)
SELECT
    md5('availability:' || v.sku || ':' || v.channel)::uuid,
    p.id,
    v.status,
    v.available_quantity,
    v.channel,
    v.reason,
    u.gerente_id,
    CURRENT_TIMESTAMP
FROM (VALUES
    ('PAO-FRANCES-KG', 'AVAILABLE', 42.500, 'ALL', 'Produção do dia disponível até acabar.'),
    ('PAO-DOCE-UN', 'AVAILABLE', 60.000, 'ALL', 'Produto disponível na vitrine.'),
    ('PAO-QUEIJO-PORCAO', 'AVAILABLE', 35.000, 'ALL', 'Produto disponível no balcão.'),
    ('PAO-ALHO-UN', 'AVAILABLE', 30.000, 'ALL', 'Produto disponível no balcão.'),
    ('SALG-COXINHA-FRANGO', 'AVAILABLE', 40.000, 'ALL', 'Quantidade estimada de vitrine.'),
    ('SALG-JOELHO-PQ', 'AVAILABLE', 28.000, 'ALL', 'Quantidade estimada de vitrine.'),
    ('SALG-EMPADA-FRANGO', 'AVAILABLE', 20.000, 'ALL', 'Quantidade estimada de vitrine.'),
    ('LANCH-PAO-CHAPA', 'AVAILABLE', NULL, 'ALL', 'Disponível conforme capacidade da chapa.'),
    ('LANCH-MISTO-QUENTE', 'AVAILABLE', NULL, 'ALL', 'Disponível conforme capacidade da chapa.'),
    ('LANCH-XBURGER', 'AVAILABLE', 25.000, 'ALL', 'Disponível conforme capacidade da chapa.'),
    ('LANCH-XSALADA', 'AVAILABLE', 20.000, 'ALL', 'Disponível conforme capacidade da chapa.'),
    ('BEB-CAFE-COADO-P', 'AVAILABLE', NULL, 'ALL', 'Disponível durante o expediente.'),
    ('BEB-CAFE-LEITE', 'AVAILABLE', NULL, 'ALL', 'Disponível durante o expediente.'),
    ('BEB-CAPPUCCINO', 'AVAILABLE', NULL, 'ALL', 'Disponível durante o expediente.'),
    ('BEB-SUCO-LARANJA', 'AVAILABLE', 35.000, 'ALL', 'Disponível enquanto houver laranja.'),
    ('BEB-REFRI-LATA', 'AVAILABLE', 48.000, 'ALL', 'Estoque refrigerado disponível.'),
    ('BEB-AGUA-500', 'AVAILABLE', 60.000, 'ALL', 'Estoque disponível.'),
    ('CONF-FATIA-BOLO-CHOC', 'AVAILABLE', 18.000, 'ALL', 'Produto disponível na vitrine.'),
    ('CONF-FATIA-TORTA-MORANGO', 'AVAILABLE', 12.000, 'ALL', 'Produto disponível na vitrine.'),
    ('CONF-BRIGADEIRO', 'AVAILABLE', 80.000, 'ALL', 'Produto disponível na vitrine.'),
    ('BOLO-SIMPLES-INTEIRO', 'AVAILABLE', 6.000, 'ALL', 'Bolos inteiros disponíveis até acabar.'),
    ('ENC-TORTA-MEDIA', 'AVAILABLE', 12.000, 'ALL', 'Produto vendido somente sob encomenda.'),
    ('MERC-MANTEIGA-200', 'AVAILABLE', NULL, 'ALL', 'Produto disponível apenas no balcão/PDV.'),
    ('MERC-LEITE-1L', 'AVAILABLE', NULL, 'ALL', 'Produto disponível apenas no balcão/PDV.'),
    ('MERC-REQUEIJAO-200', 'AVAILABLE', NULL, 'ALL', 'Produto disponível apenas no balcão/PDV.')
) AS v(sku, status, available_quantity, channel, reason)
JOIN products p ON p.sku = v.sku
CROSS JOIN seed_user_refs u
ON CONFLICT (id) DO UPDATE SET
    status = EXCLUDED.status,
    available_quantity = EXCLUDED.available_quantity,
    channel = EXCLUDED.channel,
    reason = EXCLUDED.reason,
    updated_by = EXCLUDED.updated_by,
    updated_at = CURRENT_TIMESTAMP;

-- 7. Clientes
INSERT INTO customers (id, name, phone, email, document, birth_date, created_at, updated_at)
VALUES
    (md5('customer:ana')::uuid, 'Ana Paula Martins', '+5521999011001', 'ana.martins@example.com', '00000000191', '1989-03-14', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (md5('customer:bruno')::uuid, 'Bruno Ferreira Lima', '+5521999011002', 'bruno.lima@example.com', NULL, '1984-07-22', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (md5('customer:carla')::uuid, 'Carla Souza Ribeiro', '+5521999011003', 'carla.ribeiro@example.com', NULL, '1992-01-18', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (md5('customer:daniel')::uuid, 'Daniel Alves Costa', '+5521999011004', 'daniel.costa@example.com', '00000000272', '1978-11-05', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    phone = EXCLUDED.phone,
    email = EXCLUDED.email,
    document = EXCLUDED.document,
    birth_date = EXCLUDED.birth_date,
    updated_at = CURRENT_TIMESTAMP;

-- 8. Enderecos
INSERT INTO customer_addresses (
    id, customer_id, label, street, number, complement, neighborhood,
    city, state, zip_code, reference, latitude, longitude, default_address,
    created_at, updated_at
)
VALUES
    (md5('address:ana:casa')::uuid, md5('customer:ana')::uuid, 'Casa', 'Rua das Flores', '120', 'Casa 2', 'Centro', 'Magé', 'RJ', '25900-000', 'Próximo à praça principal', -22.6521000, -43.0391000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (md5('address:bruno:casa')::uuid, md5('customer:bruno')::uuid, 'Casa', 'Rua Nova Esperança', '45', NULL, 'Piabetá', 'Magé', 'RJ', '25931-000', 'Portão azul', -22.6080000, -43.1820000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET
    customer_id = EXCLUDED.customer_id,
    label = EXCLUDED.label,
    street = EXCLUDED.street,
    number = EXCLUDED.number,
    complement = EXCLUDED.complement,
    neighborhood = EXCLUDED.neighborhood,
    city = EXCLUDED.city,
    state = EXCLUDED.state,
    zip_code = EXCLUDED.zip_code,
    reference = EXCLUDED.reference,
    latitude = EXCLUDED.latitude,
    longitude = EXCLUDED.longitude,
    default_address = EXCLUDED.default_address,
    updated_at = CURRENT_TIMESTAMP;

-- 9. Caixas
INSERT INTO cash_registers (
    id, operator_id, opened_at, closed_at, opening_amount, closing_amount,
    expected_amount, difference_amount, status, notes, created_at, updated_at
)
SELECT
    v.id,
    u.caixa_id,
    v.opened_at,
    v.closed_at,
    v.opening_amount,
    v.closing_amount,
    v.expected_amount,
    v.difference_amount,
    v.status,
    v.notes,
    v.opened_at,
    CURRENT_TIMESTAMP
FROM (VALUES
    (md5('cash-register:today')::uuid, CURRENT_DATE + TIME '05:30', NULL::TIMESTAMP, 250.00, NULL::NUMERIC, NULL::NUMERIC, NULL::NUMERIC, 'OPEN', 'Caixa principal aberto para a operação atual.')
) AS v(id, opened_at, closed_at, opening_amount, closing_amount, expected_amount, difference_amount, status, notes)
CROSS JOIN seed_user_refs u
ON CONFLICT (id) DO UPDATE SET
    operator_id = EXCLUDED.operator_id,
    opened_at = EXCLUDED.opened_at,
    closed_at = EXCLUDED.closed_at,
    opening_amount = EXCLUDED.opening_amount,
    closing_amount = EXCLUDED.closing_amount,
    expected_amount = EXCLUDED.expected_amount,
    difference_amount = EXCLUDED.difference_amount,
    status = EXCLUDED.status,
    notes = EXCLUDED.notes,
    updated_at = CURRENT_TIMESTAMP;

-- 10. Pedidos
INSERT INTO orders (
    id, customer_id, origin, status, payment_status, delivery_type,
    subtotal, discount_amount, delivery_fee, total_amount, notes,
    created_at, updated_at, finished_at, canceled_at
)
VALUES
    (md5('order:900005')::uuid, NULL, 'PDV', 'FINISHED', 'PAID', 'TAKE_AWAY', 13.50, 0.00, 0.00, 13.50, 'Café da manhã no balcão.', CURRENT_DATE + TIME '06:40', CURRENT_DATE + TIME '06:47', CURRENT_DATE + TIME '06:47', NULL),
    (md5('order:900007')::uuid, NULL, 'PDV', 'IN_PREPARATION', 'PAID', 'LOCAL_CONSUMPTION', 31.90, 0.00, 0.00, 31.90, 'Pedido em preparo na chapa e bebidas.', CURRENT_DATE + TIME '08:20', CURRENT_DATE + TIME '08:28', NULL, NULL),
    (md5('order:900009')::uuid, md5('customer:daniel')::uuid, 'ONLINE', 'PENDING', 'PENDING', 'DELIVERY', 28.00, 0.00, 5.00, 33.00, 'Aguardando confirmação do Pix online.', CURRENT_DATE + TIME '09:05', CURRENT_DATE + TIME '09:05', NULL, NULL)
ON CONFLICT (id) DO UPDATE SET
    customer_id = EXCLUDED.customer_id,
    origin = EXCLUDED.origin,
    status = EXCLUDED.status,
    payment_status = EXCLUDED.payment_status,
    delivery_type = EXCLUDED.delivery_type,
    subtotal = EXCLUDED.subtotal,
    discount_amount = EXCLUDED.discount_amount,
    delivery_fee = EXCLUDED.delivery_fee,
    total_amount = EXCLUDED.total_amount,
    notes = EXCLUDED.notes,
    updated_at = EXCLUDED.updated_at,
    finished_at = EXCLUDED.finished_at,
    canceled_at = EXCLUDED.canceled_at;

-- 11. Itens
INSERT INTO order_items (
    id, order_id, product_id, product_name_snapshot, quantity, unit_price,
    total_price, observation, status, preparation_sector, created_at, updated_at
)
VALUES
    (md5('order-item:900005:1')::uuid, md5('order:900005')::uuid, md5('product:LANCH-PAO-CHAPA')::uuid, 'Pão na chapa', 1.000, 5.00, 5.00, 'Bem passado.', 'COMPLETED', 'COZINHA', CURRENT_DATE + TIME '06:40', CURRENT_TIMESTAMP),
    (md5('order-item:900005:2')::uuid, md5('order:900005')::uuid, md5('product:BEB-CAFE-LEITE')::uuid, 'Café com leite', 1.000, 5.50, 5.50, NULL, 'COMPLETED', 'COZINHA', CURRENT_DATE + TIME '06:40', CURRENT_TIMESTAMP),
    (md5('order-item:900007:1')::uuid, md5('order:900007')::uuid, md5('product:LANCH-XSALADA')::uuid, 'X-Salada', 1.000, 22.90, 22.90, 'Sem tomate.', 'IN_PREPARATION', 'COZINHA', CURRENT_DATE + TIME '08:20', CURRENT_TIMESTAMP),
    (md5('order-item:900009:1')::uuid, md5('order:900009')::uuid, md5('product:BOLO-SIMPLES-INTEIRO')::uuid, 'Bolo simples inteiro', 1.000, 28.00, 28.00, NULL, 'PENDING', 'COZINHA', CURRENT_DATE + TIME '09:05', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET
    order_id = EXCLUDED.order_id,
    product_id = EXCLUDED.product_id,
    product_name_snapshot = EXCLUDED.product_name_snapshot,
    quantity = EXCLUDED.quantity,
    unit_price = EXCLUDED.unit_price,
    total_price = EXCLUDED.total_price,
    observation = EXCLUDED.observation,
    status = EXCLUDED.status,
    preparation_sector = EXCLUDED.preparation_sector,
    updated_at = CURRENT_TIMESTAMP;

-- 12. Pagamentos
INSERT INTO payments (
    id, order_id, method, status, amount, transaction_id, gateway,
    paid_at, canceled_at, created_at, updated_at
)
VALUES
    (md5('payment:900005:cash')::uuid, md5('order:900005')::uuid, 'CASH', 'PAID', 13.50, NULL, NULL, CURRENT_DATE + TIME '06:47', NULL, CURRENT_DATE + TIME '06:47', CURRENT_TIMESTAMP),
    (md5('payment:900007:pix')::uuid, md5('order:900007')::uuid, 'PIX', 'PAID', 31.90, 'PIX-PDV-900007', 'MANUAL', CURRENT_DATE + TIME '08:27', NULL, CURRENT_DATE + TIME '08:27', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET
    order_id = EXCLUDED.order_id,
    method = EXCLUDED.method,
    status = EXCLUDED.status,
    amount = EXCLUDED.amount,
    transaction_id = EXCLUDED.transaction_id,
    gateway = EXCLUDED.gateway,
    paid_at = EXCLUDED.paid_at,
    canceled_at = EXCLUDED.canceled_at,
    updated_at = CURRENT_TIMESTAMP;

-- 13. Movimentacoes de Caixa
INSERT INTO cash_movements (
    id, cash_register_id, type, payment_method, amount, description,
    order_id, created_by, created_at
)
SELECT
    v.id,
    v.cash_register_id,
    v.type,
    v.payment_method,
    v.amount,
    v.description,
    v.order_id,
    u.caixa_id,
    v.created_at
FROM (VALUES
    (md5('cash-movement:008')::uuid, md5('cash-register:today')::uuid, 'CASH_IN', 'CASH', 250.00, 'Abertura de caixa com fundo inicial.', NULL::uuid, CURRENT_DATE + TIME '05:30'),
    (md5('cash-movement:009')::uuid, md5('cash-register:today')::uuid, 'SALE', 'CASH', 13.50, 'Recebimento em dinheiro da venda PDV.', md5('order:900005')::uuid, CURRENT_DATE + TIME '06:47'),
    (md5('cash-movement:011')::uuid, md5('cash-register:today')::uuid, 'SALE', 'PIX', 31.90, 'Recebimento Pix presencial.', md5('order:900007')::uuid, CURRENT_DATE + TIME '08:27')
) AS v(id, cash_register_id, type, payment_method, amount, description, order_id, created_at)
CROSS JOIN seed_user_refs u
ON CONFLICT (id) DO UPDATE SET
    cash_register_id = EXCLUDED.cash_register_id,
    type = EXCLUDED.type,
    payment_method = EXCLUDED.payment_method,
    amount = EXCLUDED.amount,
    description = EXCLUDED.description,
    order_id = EXCLUDED.order_id,
    created_by = EXCLUDED.created_by;

-- 14. KDS
INSERT INTO kds_tickets (id, order_id, sector, status, created_at, started_at, ready_at, finished_at)
VALUES
    (md5('kds-ticket:900007:chapa')::uuid, md5('order:900007')::uuid, 'COZINHA', 'IN_PREPARATION', CURRENT_DATE + TIME '08:21', CURRENT_DATE + TIME '08:23', NULL::TIMESTAMP, NULL::TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET
    order_id = EXCLUDED.order_id,
    sector = EXCLUDED.sector,
    status = EXCLUDED.status,
    started_at = EXCLUDED.started_at,
    ready_at = EXCLUDED.ready_at,
    finished_at = EXCLUDED.finished_at;

INSERT INTO kds_ticket_items (id, kds_ticket_id, order_item_id, status, created_at, updated_at)
VALUES
    (md5('kds-ticket-item:900007:1')::uuid, md5('kds-ticket:900007:chapa')::uuid, md5('order-item:900007:1')::uuid, 'IN_PREPARATION', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET
    kds_ticket_id = EXCLUDED.kds_ticket_id,
    order_item_id = EXCLUDED.order_item_id,
    status = EXCLUDED.status,
    updated_at = CURRENT_TIMESTAMP;

-- 15. Estoque
INSERT INTO stock_movements (id, product_id, type, quantity, reason, order_id, created_by, created_at)
SELECT
    v.id,
    v.product_id,
    v.type,
    v.quantity,
    v.reason,
    v.order_id,
    u.cozinha_id,
    v.created_at
FROM (VALUES
    (md5('stock:001')::uuid, md5('product:PAO-FRANCES-KG')::uuid, 'IN', 120.000, 'Produção inicial do dia.', NULL::uuid, CURRENT_DATE + TIME '05:10'),
    (md5('stock:004')::uuid, md5('product:PAO-DOCE-UN')::uuid, 'IN', 90.000, 'Produção inicial do dia.', NULL::uuid, CURRENT_DATE + TIME '05:20')
) AS v(id, product_id, type, quantity, reason, order_id, created_at)
CROSS JOIN seed_user_refs u
ON CONFLICT (id) DO UPDATE SET
    product_id = EXCLUDED.product_id,
    type = EXCLUDED.type,
    quantity = EXCLUDED.quantity,
    reason = EXCLUDED.reason,
    order_id = EXCLUDED.order_id,
    created_by = EXCLUDED.created_by;

-- 16. Sincronizacao
INSERT INTO sync_events (
    id, idempotency_key, direction, source_environment, target_environment,
    aggregate_type, aggregate_id, event_type, payload, status,
    retry_count, last_error, created_at, processed_at
)
VALUES
    (md5('sync:001')::uuid, 'PAO-FRANCES-SYNC-1', 'LOCAL_TO_ONLINE', 'LOCAL', 'ONLINE', 'PRODUCT', md5('product:PAO-FRANCES-KG')::uuid, 'PRODUCT_UPDATED', '{"sku":"PAO-FRANCES-KG"}'::jsonb, 'PROCESSED', 0, NULL, CURRENT_DATE + TIME '06:05', CURRENT_DATE + TIME '06:06'),
    (md5('sync:008')::uuid, 'CASH-OPEN-SYNC-1', 'LOCAL_TO_ONLINE', 'LOCAL', 'ONLINE', 'CASH_REGISTER', md5('cash-register:today')::uuid, 'CASH_REGISTER_OPENED', '{"status":"OPEN"}'::jsonb, 'PENDING', 0, NULL, CURRENT_DATE + TIME '05:30', NULL)
ON CONFLICT (id) DO UPDATE SET
    event_type = EXCLUDED.event_type,
    aggregate_type = EXCLUDED.aggregate_type,
    aggregate_id = EXCLUDED.aggregate_id,
    payload = EXCLUDED.payload,
    source_environment = EXCLUDED.source_environment,
    target_environment = EXCLUDED.target_environment,
    status = EXCLUDED.status,
    retry_count = EXCLUDED.retry_count,
    last_error = EXCLUDED.last_error,
    updated_at = CURRENT_TIMESTAMP,
    processed_at = EXCLUDED.processed_at;

-- 17. Auditoria
INSERT INTO audit_logs (
    id, actor_user_id, action, entity_type, entity_id, details,
    ip_address, created_at
)
SELECT
    v.id,
    u.admin_id,
    v.action,
    v.entity_type,
    v.entity_id,
    v.details,
    v.ip_address,
    v.created_at
FROM (VALUES
    (md5('audit:001')::uuid, 'CATALOG_SEED_APPLIED', 'PRODUCT', NULL::uuid, '{"source":"seed-production"}'::jsonb, '127.0.0.1', CURRENT_DATE + TIME '06:00')
) AS v(id, action, entity_type, entity_id, details, ip_address, created_at)
CROSS JOIN seed_user_refs u
ON CONFLICT (id) DO UPDATE SET
    actor_user_id = EXCLUDED.actor_user_id,
    action = EXCLUDED.action,
    entity_type = EXCLUDED.entity_type,
    entity_id = EXCLUDED.entity_id,
    details = EXCLUDED.details,
    ip_address = EXCLUDED.ip_address;

COMMIT;
