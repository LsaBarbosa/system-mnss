INSERT INTO roles (name, description)
VALUES
    ('ADMIN', 'Acesso administrativo completo.'),
    ('GERENTE', 'Acesso gerencial e acoes criticas.'),
    ('CAIXA', 'Operacao de caixa e PDV.'),
    ('ATENDENTE', 'Atendimento e pedidos.'),
    ('COZINHA', 'Operacao do KDS e preparo.'),
    ('ENTREGADOR', 'Operacao de entregas.'),
    ('CONSULTA', 'Acesso somente leitura.')
ON CONFLICT (name) DO NOTHING;
