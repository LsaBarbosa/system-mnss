# KDS — Sistema Nova Aliança

## 1. Objetivo

Este documento especifica o módulo **KDS** do Sistema Nova Aliança.

KDS significa **Kitchen Display System**.  
É a tela usada pela produção/cozinha para acompanhar pedidos em tempo real.

Na Padaria e Lanchonete Nova Aliança, o KDS será usado por setores como:

- Chapa
- Bebidas
- Balcão
- Confeitaria
- Expedição
- Delivery

## 2. Decisão principal

O KDS será local.

> A cozinha não pode depender da internet para receber pedidos do caixa.

O KDS acessará a API local e receberá atualizações via WebSocket.

```text
PDV
↓
API Local
↓
WebSocket
↓
KDS
```

## 3. Responsabilidades do KDS

O KDS deverá:

- Exibir pedidos pendentes
- Separar pedidos por setor
- Mostrar tempo de espera
- Permitir iniciar preparo
- Permitir marcar item como pronto
- Permitir marcar pedido como pronto
- Alertar pedidos atrasados
- Atualizar tela em tempo real
- Funcionar na rede local
- Continuar recebendo pedidos do PDV sem internet

## 4. Fluxo principal

```text
Pedido criado no PDV ou sincronizado do online
↓
API local salva pedido
↓
Sistema identifica itens que exigem preparo
↓
Sistema cria KDS Ticket
↓
API local publica evento
↓
KDS recebe via WebSocket
↓
Produção visualiza pedido
↓
Produção inicia preparo
↓
Produção marca como pronto
↓
PDV/expedição recebe atualização
```

## 5. Setores

Setores previstos:

```text
BALCAO
CHAPA
BEBIDAS
CONFEITARIA
EXPEDICAO
DELIVERY
SEM_PREPARO
```

### 5.1 Chapa

Exemplos:

- Hambúrguer
- Misto quente
- Pão na chapa
- Lanches quentes

### 5.2 Bebidas

Exemplos:

- Suco
- Café
- Vitamina
- Chocolate quente

### 5.3 Confeitaria

Exemplos:

- Tortas
- Fatias
- Encomendas
- Bolos

### 5.4 Expedição

Responsável por conferir o pedido completo.

## 6. KDS Ticket

O KDS Ticket representa uma ordem de produção.

### Entidade

```text
KdsTicket
- id
- orderId
- ticketNumber
- sector
- status
- createdAt
- startedAt
- readyAt
- finishedAt
```

### Status

```java
public enum KdsTicketStatus {
    WAITING,
    IN_PREPARATION,
    READY,
    FINISHED,
    CANCELED
}
```

## 7. KDS Ticket Item

Representa cada item dentro de um ticket.

```text
KdsTicketItem
- id
- kdsTicketId
- orderItemId
- status
- createdAt
- updatedAt
```

## 8. Regra de criação de tickets

Quando um pedido é criado, o sistema deve verificar os itens.

### Exemplo

Pedido:

```text
1 X-Burger → CHAPA
1 Suco de laranja → BEBIDAS
1 Fatia de torta → CONFEITARIA
```

Resultado:

```text
Ticket 1: CHAPA
- X-Burger

Ticket 2: BEBIDAS
- Suco de laranja

Ticket 3: CONFEITARIA
- Fatia de torta
```

A expedição pode visualizar o pedido completo.

## 9. Estados do pedido no KDS

### Por item

```text
WAITING_PREPARATION
IN_PREPARATION
READY
CANCELED
```

### Por ticket

```text
WAITING
IN_PREPARATION
READY
FINISHED
CANCELED
```

### Por pedido

```text
ACCEPTED
IN_PREPARATION
READY
FINISHED
```

## 10. Regras de status

- Se pelo menos um item estiver em preparo, o pedido fica `IN_PREPARATION`.
- Se todos os itens de preparo estiverem prontos, o pedido pode ficar `READY`.
- Se um ticket for cancelado, deve registrar motivo.
- Pedido só fica pronto quando todos os tickets obrigatórios estiverem prontos.
- Itens sem preparo não precisam gerar ticket.

## 11. Tela do KDS

### Card do pedido

Cada pedido deve mostrar:

```text
Pedido #1521
Origem: PDV / Site / WhatsApp
Tipo: Balcão / Retirada / Entrega
Tempo: 08 min
Status: Aguardando preparo

Itens:
- 1 X-Burger
  Obs: sem cebola
- 1 Suco de laranja
```

### Botões

- Iniciar preparo
- Marcar item pronto
- Marcar ticket pronto
- Ver detalhes
- Cancelar item, se autorizado

## 12. Layout sugerido

```text
Topo:
- Setor atual
- Filtros
- Status de conexão
- Hora

Colunas:
- Aguardando
- Em preparo
- Pronto

Cards:
- Pedido
- Tempo
- Origem
- Tipo
- Itens
- Observações
```

## 13. Cores de atenção

O sistema pode usar indicadores visuais:

```text
Normal: pedido dentro do tempo
Atenção: pedido próximo do limite
Atrasado: pedido passou do tempo esperado
```

Não depender apenas de cor.  
Também exibir texto ou ícone.

## 14. Tempo de preparo

Cada produto pode ter tempo estimado.

Exemplo:

```text
X-Burger: 12 minutos
Suco: 5 minutos
Pão na chapa: 4 minutos
```

O KDS deve calcular:

- Tempo desde criação do pedido
- Tempo em preparo
- Tempo excedido
- Tempo médio por setor

## 15. Comunicação em tempo real

Usar WebSocket.

### Eventos previstos

```text
/kds/orders/new
/kds/orders/updated
/kds/items/ready
/kds/orders/canceled
/kds/tickets/updated
```

### Payload exemplo

```json
{
  "eventType": "KDS_TICKET_CREATED",
  "ticketId": "a77fdad4-9609-433d-89e4-f0208da178b1",
  "orderNumber": 1521,
  "sector": "CHAPA",
  "items": [
    {
      "name": "X-Burger",
      "quantity": 1,
      "observation": "Sem cebola"
    }
  ]
}
```

## 16. APIs do KDS

Endpoints sugeridos:

```text
GET    /api/kds/tickets
GET    /api/kds/tickets?sector=CHAPA
GET    /api/kds/tickets/{id}
PATCH  /api/kds/tickets/{id}/start
PATCH  /api/kds/tickets/{id}/ready
PATCH  /api/kds/tickets/{id}/finish
PATCH  /api/kds/items/{id}/start
PATCH  /api/kds/items/{id}/ready
PATCH  /api/kds/items/{id}/cancel
```

## 17. Eventos gerados

```text
KDS_TICKET_CREATED
KDS_TICKET_STARTED
KDS_TICKET_READY
KDS_TICKET_FINISHED
KDS_ITEM_STARTED
KDS_ITEM_READY
KDS_ITEM_CANCELED
ORDER_READY
```

## 18. Integração com PDV

Quando o KDS marca pedido como pronto:

```text
KDS marca ticket como pronto
↓
API local atualiza pedido
↓
PDV recebe evento
↓
Caixa/atendimento vê pedido pronto
```

## 19. Integração com pedidos online

Pedidos online chegam assim:

```text
Site/WhatsApp cria pedido
↓
Online salva pedido
↓
Local baixa pedido por sincronização
↓
Local cria KDS Ticket
↓
KDS exibe pedido
```

Se a loja estiver sem internet, pedidos online não chegam até a loja até a conexão voltar.

## 20. Modo offline

Sem internet, o KDS continua funcionando para pedidos locais.

Continua funcionando:

- Pedidos criados no PDV
- Atualização de status
- Tickets por setor
- Comunicação local via WebSocket
- Produção

Não funciona:

- Recebimento de novos pedidos online
- Atualização do cliente externo
- Sincronização com a nuvem

## 21. Hardware do KDS

### Opção econômica

```text
Tablet Android 10" ou 11"
Suporte de parede
Carregador fixo
Wi-Fi estável
```

### Opção robusta

```text
Mini PC
Monitor 24"
Suporte de parede
Mouse sem fio ou tela touch
Rede cabeada
```

### Cuidados

- Evitar calor direto.
- Evitar gordura/fritura.
- Proteger cabos.
- Usar suporte fixo.
- Manter fonte em local seguro.
- Preferir rede cabeada quando possível.

## 22. MVP do KDS

Primeira versão deve incluir:

1. Listar tickets pendentes.
2. Separar por setor.
3. Receber pedido via WebSocket.
4. Marcar ticket em preparo.
5. Marcar ticket pronto.
6. Atualizar pedido no PDV.
7. Mostrar tempo desde criação.
8. Funcionar localmente.

## 23. Evoluções futuras

- Sons de alerta.
- Impressão reserva na cozinha.
- Métricas por setor.
- Tempo médio de preparo.
- Tela de expedição.
- Painel de pedidos atrasados.
- Ranking de itens mais preparados.
- Integração com etiquetas.
