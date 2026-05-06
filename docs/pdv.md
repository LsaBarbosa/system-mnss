# PDV — Sistema Nova Aliança

## 1. Objetivo

Este documento descreve o módulo de **PDV** do Sistema Nova Aliança.

O PDV será usado na frente de caixa da Padaria e Lanchonete Nova Aliança para registrar vendas presenciais, controlar pagamentos, imprimir comprovantes e integrar pedidos com o KDS.

## 2. Decisão principal

O PDV deve funcionar localmente.

> O caixa não pode depender da internet para vender.

Portanto, o PDV acessará a API local dentro da rede da padaria.

```text
PDV
↓
Rede local
↓
API Local
↓
PostgreSQL Local
```

## 3. Responsabilidades do PDV

O PDV deverá permitir:

- Abrir venda
- Buscar produto
- Ler código de barras
- Adicionar produto
- Remover produto
- Alterar quantidade
- Aplicar desconto
- Selecionar forma de pagamento
- Registrar pagamento misto
- Finalizar venda
- Cancelar venda
- Imprimir comprovante
- Abrir gaveta
- Enviar itens de preparo para KDS
- Funcionar sem internet

## 4. Fluxo principal de venda

```text
Operador abre o PDV
↓
Sistema verifica caixa aberto
↓
Operador adiciona produtos
↓
Sistema calcula total
↓
Operador seleciona pagamento
↓
Sistema registra pagamento
↓
Sistema finaliza venda
↓
Sistema imprime comprovante
↓
Sistema envia itens para KDS, se necessário
↓
Sistema cria evento de sincronização
```

## 5. Abertura de caixa

Antes de vender, o operador poderá precisar abrir o caixa.

### Fluxo

```text
Operador informa valor inicial
↓
Sistema cria CashRegister
↓
Status = OPEN
↓
PDV libera vendas
```

### Regras

- Caixa fechado não permite venda.
- Caixa aberto deve estar associado a operador.
- Caixa deve registrar data/hora de abertura.
- Valor inicial deve ser informado.
- Apenas perfil autorizado pode abrir caixa.

## 6. Fechamento de caixa

### Fluxo

```text
Operador solicita fechamento
↓
Sistema calcula total esperado
↓
Operador informa valores contados
↓
Sistema calcula diferença
↓
Sistema fecha caixa
↓
Sistema imprime resumo
↓
Sistema cria evento de sincronização
```

### Resumo de fechamento

Deve exibir:

- Operador
- Data/hora de abertura
- Data/hora de fechamento
- Valor inicial
- Total em dinheiro
- Total em Pix
- Total em débito
- Total em crédito
- Total em voucher
- Sangrias
- Suprimentos
- Total esperado
- Total informado
- Diferença

## 7. Formas de pagamento

Formas previstas:

```text
Dinheiro
Pix presencial
Cartão de débito
Cartão de crédito
Vale/refeição
Pagamento online
Pagamento misto
```

### Enum sugerido

```java
public enum PaymentMethod {
    CASH,
    PIX,
    DEBIT_CARD,
    CREDIT_CARD,
    MEAL_VOUCHER,
    ONLINE_PIX,
    ONLINE_CREDIT_CARD,
    MIXED
}
```

## 8. Pagamento misto

O sistema deve permitir mais de uma forma de pagamento na mesma venda.

Exemplo:

```text
Total: R$ 42,00

R$ 20,00 em dinheiro
R$ 22,00 no Pix
```

### Regras

- Soma dos pagamentos deve ser igual ao total.
- Troco só se aplica a dinheiro.
- Cada pagamento deve ser salvo separadamente.
- Cada pagamento deve gerar movimentação de caixa.

## 9. Desconto

Tipos possíveis:

- Desconto por valor
- Desconto por percentual
- Desconto por item
- Desconto no total

### Regras

- Desconto deve respeitar limite configurado.
- Desconto acima do limite exige gerente.
- Desconto deve gerar auditoria.
- Desconto deve aparecer no resumo da venda.

## 10. Cancelamento

### Cancelamento antes de finalizar

Venda pode ser descartada.

### Cancelamento depois de finalizar

Deve exigir permissão.

Regras:

- Registrar motivo.
- Registrar usuário.
- Gerar auditoria.
- Gerar ajuste financeiro, se necessário.
- Gerar evento de sincronização.

## 11. Integração com KDS

Itens que exigem preparo devem ser enviados para o KDS.

Exemplos:

- Lanches
- Pão na chapa
- Bebidas preparadas
- Sucos
- Cafés
- Pedidos da confeitaria

Fluxo:

```text
Venda contém item de preparo
↓
PDV finaliza pedido
↓
API local cria KDS Ticket
↓
KDS recebe evento via WebSocket
↓
Produção inicia preparo
```

## 12. Integração com impressora

A impressão será local.

### Tipos de impressão

- Cupom simples
- Comprovante de venda
- Resumo de caixa
- Sangria
- Suprimento
- Pedido para cozinha, se necessário

### Fluxo

```text
Venda finalizada
↓
API local gera comando de impressão
↓
Printer service envia ESC/POS
↓
Impressora imprime
↓
Gaveta abre, se pagamento em dinheiro
```

## 13. Integração com gaveta

A gaveta geralmente é acionada pela impressora.

Fluxo:

```text
Pagamento em dinheiro
↓
PDV finaliza recebimento
↓
Sistema manda comando para impressora
↓
Impressora aciona gaveta
```

## 14. Leitor de código de barras

O PDV deve aceitar leitura por:

- Código de barras
- Digitação manual
- Busca por nome
- Favoritos
- Categoria

### Regras

- Se o produto for encontrado, adicionar item.
- Se não encontrado, mostrar aviso.
- Produto inativo não deve ser adicionado.
- Produto indisponível pode ser bloqueado conforme configuração.

## 15. Tela do PDV

Áreas principais:

```text
Topo:
- Operador
- Caixa aberto
- Hora
- Status de sincronização

Esquerda:
- Categorias
- Produtos favoritos
- Busca

Centro:
- Lista de produtos

Direita:
- Carrinho/venda atual
- Quantidades
- Descontos
- Total

Rodapé:
- Finalizar
- Cancelar
- Sangria
- Suprimento
- Fechar caixa
```

## 16. APIs do PDV

Endpoints do contexto de caixa (app PDV):

```text
POST   /api/pdv/sales
POST   /api/pdv/sales/{saleId}/items
PATCH  /api/pdv/sales/{saleId}/items/{itemId}
DELETE /api/pdv/sales/{saleId}/items/{itemId}
POST   /api/pdv/sales/{saleId}/discount
POST   /api/pdv/sales/{saleId}/payment
POST   /api/pdv/sales/{saleId}/finish
POST   /api/pdv/sales/{saleId}/cancel
GET    /api/pdv/products
GET    /api/pdv/products/barcode/{barcode}
```

> **Dois endpoints de pagamento coexistem por design:**
> - `POST /api/pdv/sales/{saleId}/payment` — usado exclusivamente pelo app PDV (caixa). `saleId` é o ID da venda em andamento.
> - `POST /api/orders/{orderId}/payments` — usado pela API genérica de ordens (admin, integrações). `orderId` é o mesmo ID, mas o contexto é diferente.
> Ambos delegam ao `PaymentService.payOrder()`. O app PDV deve usar sempre o endpoint `/pdv/sales/`; integradores externos devem usar `/orders/`.

## 17. APIs de caixa

```text
POST /api/cash-register/open
GET  /api/cash-register/current
POST /api/cash-register/{id}/movement
POST /api/cash-register/{id}/close
GET  /api/cash-register/{id}/summary
```

## 18. Entidades envolvidas

- Order
- OrderItem
- Payment
- CashRegister
- CashMovement
- Product
- ProductAvailability
- KdsTicket
- SyncEvent
- AuditLog

## 19. Regras de negócio

- Não vender sem caixa aberto, se configurado.
- Não finalizar venda sem pagamento.
- Não finalizar venda sem itens.
- Não permitir produto inativo.
- Produto indisponível deve ser bloqueado conforme canal.
- Pagamento deve bater com total.
- Venda finalizada gera movimentação de caixa.
- Venda com item de preparo gera ticket no KDS.
- Venda finalizada gera evento de sincronização.
- Cancelamento gera auditoria.

## 20. Eventos gerados pelo PDV

```text
SALE_CREATED
SALE_ITEM_ADDED
SALE_ITEM_REMOVED
SALE_DISCOUNT_APPLIED
PAYMENT_REGISTERED
SALE_FINISHED
SALE_CANCELED
CASH_REGISTER_OPENED
CASH_REGISTER_CLOSED
CASH_MOVEMENT_CREATED
ORDER_SENT_TO_KDS
```

## 21. Modo offline

O PDV deve funcionar sem internet.

Continua funcionando:

- Busca de produtos locais
- Venda
- Pagamento presencial
- Impressão
- Caixa
- KDS local
- Registro de eventos pendentes

Não funciona ou fica limitado:

- Pagamento online
- Sincronização
- Pedidos novos do site/WhatsApp
- Consulta remota

## 22. Status de sincronização na tela

O PDV deve mostrar indicador simples:

```text
Online sincronizado
Online com pendências
Offline
Erro de sincronização
```

## 23. Hardware do PDV

Kit recomendado:

```text
Computador ou mini PC
Monitor comum ou touchscreen
Impressora térmica 80mm
Gaveta de dinheiro
Leitor de código de barras 2D
Maquininha
Nobreak
Rede cabeada
```

## 24. Prioridade de implementação

### MVP do PDV

1. Abrir caixa
2. Listar produtos
3. Criar venda
4. Adicionar/remover itens
5. Registrar pagamento
6. Finalizar venda
7. Imprimir comprovante
8. Enviar item para KDS
9. Fechar caixa
10. Criar evento de sincronização

### Versões futuras

- Integração com pinpad
- NFC-e, se necessário
- Etiquetas
- Balança
- Promoções avançadas
- Cliente fidelidade
