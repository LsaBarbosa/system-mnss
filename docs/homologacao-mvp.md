# Roteiro de Homologação MVP — Sistema Nova Aliança

Este documento descreve os passos para validar o funcionamento do MVP em ambiente de homologação.

## 1. Fluxo Local (Loja Física)

### Cenário 1.1: Abertura de Caixa e Venda PDV
1. Acessar o sistema administrativo local.
2. Abrir o caixa do dia com valor inicial.
3. Acessar o PDV.
4. Adicionar produtos ao carrinho.
5. Finalizar venda com pagamento em Dinheiro.
6. **Verificação**:
    - O saldo do caixa deve ser atualizado.
    - A venda deve aparecer no histórico de vendas.
    - Um evento de sincronização `SALE_FINISHED` deve ser criado no outbox.

### Cenário 1.2: Produção KDS
1. Realizar uma venda no PDV contendo itens que exigem preparo (ex: sanduíche).
2. Acessar a tela do KDS.
3. **Verificação**:
    - O ticket de preparo deve aparecer na coluna "AGUARDANDO".
    - Ao clicar em "INICIAR", o ticket deve mover para "EM PREPARO".
    - Ao finalizar, o pedido deve ser marcado como "PRONTO".

---

## 2. Fluxo Online (Site ⟷ Loja)

### Cenário 2.1: Pedido via Site
1. Acessar o site institucional (online).
2. Selecionar produtos e adicionar ao carrinho.
3. Informar dados do cliente e endereço de entrega.
4. Finalizar pedido com pagamento na entrega.
5. **Verificação**:
    - O pedido deve ser gravado no banco de dados online.
    - Um evento de sincronização `ORDER_CREATED` deve ser gerado no servidor online.

### Cenário 2.2: Sincronização e Recebimento na Loja
1. Aguardar o intervalo de pull do servidor local (ou forçar via dashboard).
2. **Verificação**:
    - O pedido online deve aparecer no PDV local com origem "SITE".
    - O pedido deve aparecer no KDS local para preparo.
    - O status do pedido no servidor online deve mudar para `RECEIVED_BY_STORE`.

---

## 3. Observabilidade e Resiliência

### Cenário 3.1: Monitoramento de Saúde
1. Acessar `/api/health` e `/api/sync/health`.
2. **Verificação**:
    - Todas as dependências (DB, Redis, Rabbit) devem estar `UP`.
    - O contador de eventos pendentes deve ser visível.

### Cenário 3.2: Operação Offline
1. Simular queda de internet no servidor local (desativar rede do container local-app).
2. Realizar uma venda no PDV.
3. **Verificação**:
    - A venda deve ser processada normalmente.
    - O evento de sync deve ficar com status `PENDING` ou `FAILED` (aguardando retry).
    - Reativar internet e verificar se o evento é sincronizado automaticamente.
