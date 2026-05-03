# Fluxos da Aplicação e Mapeamento de Casos de Uso — Sistema Nova Aliança

## 1. Objetivo

Este documento consolida os **fluxos da aplicação** e o **mapeamento de casos de uso** do Sistema Nova Aliança, considerando a documentação técnica já definida para:

- Arquitetura local + online.
- Modelo de domínio.
- Banco de dados.
- Sincronização.
- PDV.
- KDS.
- Deploy local.
- Deploy online.
- Hardware.

O foco é transformar as decisões técnicas em fluxos funcionais claros para orientar:

- Desenvolvimento backend.
- Desenvolvimento frontend.
- Modelagem de APIs.
- Criação de testes.
- Planejamento de sprints.
- Validação do MVP.

---

## 2. Premissas principais do sistema

## 2.1 Sistema híbrido

O sistema será composto por dois ambientes:

```text
Ambiente local
+
Ambiente online
```

### Ambiente local

Responsável pela operação crítica da loja:

- PDV.
- Caixa.
- KDS.
- Impressão.
- Gaveta de dinheiro.
- Vendas presenciais.
- Banco local.
- RabbitMQ local.
- Redis local.
- Sync worker local.

### Ambiente online

Responsável pelos canais externos:

- Site institucional.
- Cardápio online.
- Pedidos online.
- WhatsApp.
- Chatbot.
- Pagamentos online.
- Webhooks.
- Relatórios remotos.
- Banco online.
- RabbitMQ online.
- Redis online.
- Sync worker online.

## 2.2 Regra central

```text
A loja deve continuar vendendo presencialmente mesmo sem internet.
```

Consequências práticas:

- O PDV não pode depender da nuvem.
- O KDS não pode depender da nuvem.
- O caixa não pode depender da nuvem.
- A impressão deve funcionar localmente.
- As vendas presenciais devem ser salvas no PostgreSQL local.
- A sincronização com a nuvem deve ocorrer de forma assíncrona.
- Pedidos online só chegam à loja quando houver internet.

## 2.3 Comunicação entre ambientes

A comunicação local ↔ online será feita por HTTPS.

Não devem ser expostos na internet:

- PostgreSQL.
- RabbitMQ.
- Redis.

Fluxo permitido:

```text
Local API / Sync Worker
↓ HTTPS
Online API / Sync Endpoint
```

---

## 3. Atores do sistema

| Ator | Descrição | Ambiente principal |
|---|---|---|
| Cliente presencial | Compra diretamente no balcão ou caixa | Local |
| Cliente online | Compra pelo site, WhatsApp ou chatbot | Online |
| Operador de caixa | Usa o PDV, registra vendas e pagamentos | Local |
| Atendente | Apoia pedidos, balcão, retirada e disponibilidade | Local |
| Produção / Cozinha | Usa o KDS para preparar pedidos | Local |
| Expedição | Confere pedido completo antes da entrega/retirada | Local |
| Entregador | Recebe pedidos para entrega | Local |
| Gerente | Autoriza desconto, cancelamento, sangria e ajustes | Local / Online |
| Administrador | Gerencia usuários, produtos, permissões e configurações | Local / Online |
| Sync Worker Local | Envia eventos locais e busca pendências online | Local |
| Sync Worker Online | Processa eventos online e integra webhooks | Online |
| Gateway de pagamento | Confirma ou recusa pagamentos online | Online |
| WhatsApp Provider | Envia mensagens e webhooks do WhatsApp | Online |

---

## 4. Módulos funcionais

| Módulo | Responsabilidade |
|---|---|
| Identity / Security | Login, autenticação, autorização, perfis e permissões |
| Catalog | Categorias, produtos, preços, imagens e visibilidade por canal |
| Availability | Disponibilidade de produtos por canal |
| Customer | Clientes e endereços |
| Order | Pedidos locais, pedidos online e ciclo de status |
| Payment | Pagamentos presenciais, online, mistos e webhooks |
| Cash | Abertura, movimentação e fechamento de caixa |
| PDV | Frente de caixa e vendas presenciais |
| KDS | Gestão de preparo por setor |
| Stock | Movimentações de estoque e indisponibilidade automática |
| Sync | Outbox, inbox, retry e sincronização local ↔ online |
| Audit | Auditoria de ações críticas |
| Notification | Mensagens para cliente, WhatsApp e alertas internos |
| Admin | Configuração operacional e gestão da loja |
| Observability | Health checks, métricas, logs e painel de operação |

---

## 5. Mapa geral dos fluxos

| ID | Fluxo | Categoria | Ambiente principal | Prioridade |
|---|---|---|---|---|
| F01 | Inicialização do ambiente local | Técnico | Local | MVP |
| F02 | Inicialização do ambiente online | Técnico | Online | MVP |
| F03 | Login e autorização | Segurança | Local / Online | MVP |
| F04 | Gestão de usuários e permissões | Segurança | Local / Online | MVP |
| F05 | Gestão de categorias | Catálogo | Local / Online | MVP |
| F06 | Gestão de produtos | Catálogo | Local / Online | MVP |
| F07 | Gestão de disponibilidade | Catálogo / Operação | Local | MVP |
| F08 | Abertura de caixa | Caixa | Local | MVP |
| F09 | Venda presencial no PDV | PDV | Local | MVP |
| F10 | Pagamento presencial simples | Pagamento | Local | MVP |
| F11 | Pagamento misto | Pagamento | Local | MVP |
| F12 | Desconto na venda | PDV / Caixa | Local | MVP |
| F13 | Cancelamento de venda | PDV / Auditoria | Local | MVP |
| F14 | Impressão e abertura de gaveta | PDV / Hardware | Local | MVP |
| F15 | Fechamento de caixa | Caixa | Local | MVP |
| F16 | Geração de tickets KDS | KDS | Local | MVP |
| F17 | Preparo de pedido no KDS | KDS | Local | MVP |
| F18 | Pedido pronto e expedição | KDS / PDV | Local | MVP |
| F19 | Consulta pública do cardápio | Site | Online | MVP |
| F20 | Pedido online pelo site | Site / Pedido | Online | MVP |
| F21 | Pagamento online | Pagamento | Online | MVP |
| F22 | Webhook de pagamento | Pagamento / Integração | Online | MVP |
| F23 | Pedido via WhatsApp / chatbot | WhatsApp | Online | Pós-MVP inicial |
| F24 | Sincronização local → online | Sync | Local / Online | MVP |
| F25 | Sincronização online → local | Sync | Online / Local | MVP |
| F26 | Tratamento de falha de sincronização | Sync | Local / Online | MVP |
| F27 | Movimentação de estoque | Estoque | Local | Pós-MVP inicial |
| F28 | Auditoria de ações críticas | Auditoria | Local / Online | MVP |
| F29 | Painel de sincronização | Operação | Local / Online | MVP |
| F30 | Health check e monitoramento | Operação | Local / Online | MVP |

---

# 6. Fluxos detalhados

---

## F01 — Inicialização do ambiente local

### Objetivo

Subir o ambiente local da padaria para permitir operação de PDV, caixa, KDS, impressão e sincronização.

### Atores

- Administrador técnico.
- Docker Compose.
- API Local.
- PostgreSQL Local.
- RabbitMQ Local.
- Redis Local.
- Nginx Local.
- Sync Worker Local.

### Pré-condições

- Servidor local instalado.
- Docker e Docker Compose configurados.
- Arquivo `.env` local preenchido.
- Banco local configurado.
- Rede local disponível.

### Fluxo principal

```text
Administrador executa docker compose up -d
↓
PostgreSQL Local inicializa
↓
RabbitMQ Local inicializa
↓
Redis Local inicializa
↓
API Local inicializa
↓
Flyway executa migrations pendentes
↓
Nginx Local expõe a API e frontends locais
↓
Sync Worker Local inicializa
↓
Health checks ficam disponíveis
↓
PDV e KDS conseguem acessar o servidor local
```

### Regras

- A API local deve falhar rápido se configurações críticas estiverem ausentes.
- O banco local não deve ser exposto fora da rede interna.
- O RabbitMQ local não deve ser público.
- O Redis local não deve ser público.
- O servidor local deve ter IP fixo.

### Saídas

- Ambiente local operacional.
- Health check disponível.
- Logs de inicialização.
- PDV e KDS aptos a operar.

---

## F02 — Inicialização do ambiente online

### Objetivo

Subir o ambiente online na VPS para site, pedidos online, pagamentos, WhatsApp e sincronização.

### Atores

- Administrador técnico.
- Docker Compose.
- API Online.
- PostgreSQL Online.
- RabbitMQ Online.
- Redis Online.
- Nginx.
- Certbot.
- Sync Worker Online.

### Pré-condições

- VPS criada.
- DNS apontado.
- Docker instalado.
- Firewall configurado.
- Certificados SSL emitidos ou preparados.
- Arquivo `.env` online preenchido.

### Fluxo principal

```text
Administrador executa docker compose up -d
↓
PostgreSQL Online inicializa
↓
RabbitMQ Online inicializa
↓
Redis Online inicializa
↓
API Online inicializa
↓
Flyway executa migrations pendentes
↓
Nginx expõe API/site/admin via HTTPS
↓
Sync Worker Online inicializa
↓
Endpoints públicos ficam disponíveis
↓
Webhooks podem ser recebidos
```

### Regras

- HTTPS deve ser obrigatório em produção.
- Banco, RabbitMQ e Redis não devem ter portas públicas.
- Webhooks devem validar assinatura.
- Segredos devem vir de variáveis de ambiente.

### Saídas

- Site disponível.
- API Online disponível.
- Endpoints de sync disponíveis.
- Webhooks aptos a receber eventos.

---

## F03 — Login e autorização

### Objetivo

Permitir que usuários internos acessem módulos privados com permissões adequadas.

### Atores

- Administrador.
- Gerente.
- Operador de caixa.
- Atendente.
- Produção.
- Sistema de autenticação.

### Pré-condições

- Usuário cadastrado.
- Usuário ativo.
- Senha armazenada com hash.
- Perfil atribuído.

### Fluxo principal

```text
Usuário informa login e senha
↓
Sistema valida existência do usuário
↓
Sistema valida senha
↓
Sistema valida status ativo
↓
Sistema carrega perfis e permissões
↓
Sistema gera token JWT ou sessão local segura
↓
Frontend armazena credencial temporária
↓
Usuário acessa recursos conforme permissão
```

### Fluxos alternativos

| Situação | Resultado |
|---|---|
| Usuário inativo | Login negado |
| Senha inválida | Login negado |
| Perfil sem permissão | Retornar 403 |
| Token expirado | Exigir novo login |

### Regras

- Usuário inativo não autentica.
- Ações críticas exigem perfil adequado.
- Cancelamento, desconto acima do limite e sangria podem exigir senha de gerente.
- Toda falha de autenticação deve ser registrada tecnicamente, sem expor senha.

### Saídas

- Usuário autenticado.
- Contexto de permissões disponível.
- Auditoria de login quando aplicável.

---

## F04 — Gestão de usuários e permissões

### Objetivo

Permitir que o administrador gerencie os usuários internos e seus perfis.

### Atores

- Administrador.
- Gerente.
- Sistema de segurança.

### Pré-condições

- Usuário autenticado.
- Usuário com permissão administrativa.

### Fluxo principal

```text
Administrador acessa painel de usuários
↓
Sistema lista usuários cadastrados
↓
Administrador cria ou edita usuário
↓
Sistema valida campos obrigatórios
↓
Sistema vincula perfis
↓
Sistema salva alterações
↓
Sistema registra auditoria
```

### Perfis previstos

```text
ADMIN
GERENTE
CAIXA
ATENDENTE
COZINHA
ENTREGADOR
CONSULTA
```

### Regras

- Não permitir usuário sem perfil operacional.
- Não permitir username duplicado.
- Senha deve ser armazenada com hash.
- Inativação de usuário deve impedir novo login.
- Alteração de perfil deve gerar auditoria.

---

## F05 — Gestão de categorias

### Objetivo

Criar e organizar categorias usadas no PDV, site, WhatsApp e administração.

### Atores

- Administrador.
- Gerente.

### Pré-condições

- Usuário autenticado.
- Permissão para catálogo.

### Fluxo principal

```text
Usuário acessa gestão de categorias
↓
Sistema exibe categorias atuais
↓
Usuário cria ou edita categoria
↓
Sistema valida nome e ordenação
↓
Usuário define visibilidade por canal
↓
Sistema salva categoria
↓
Sistema cria evento de sincronização
```

### Regras

- Categoria inativa não aparece para venda.
- Categoria pode aparecer no PDV e não aparecer no site.
- Categoria pode aparecer no WhatsApp e não aparecer no PDV.
- Categoria deve ter ordem de exibição.

### Eventos gerados

```text
CATEGORY_CREATED
CATEGORY_UPDATED
CATEGORY_DISABLED
CATEGORY_ENABLED
```

---

## F06 — Gestão de produtos

### Objetivo

Cadastrar, editar e controlar produtos vendidos pela padaria.

### Atores

- Administrador.
- Gerente.

### Pré-condições

- Categoria existente.
- Usuário com permissão.

### Fluxo principal

```text
Usuário acessa cadastro de produtos
↓
Sistema exibe produtos existentes
↓
Usuário cria ou edita produto
↓
Sistema valida nome, preço, unidade e categoria
↓
Usuário define setor de preparo
↓
Usuário define canais de venda
↓
Sistema salva produto
↓
Sistema cria evento de sincronização
```

### Campos funcionais importantes

```text
name
description
categoryId
price
promotionalPrice
costPrice
sku
barcode
unitType
preparationSector
preparationTimeMinutes
active
available
sellOnPdv
sellOnline
sellOnWhatsapp
```

### Regras

- Produto inativo não aparece em nenhum canal.
- Produto indisponível não pode ser vendido online.
- Produto pode ser vendido no PDV e oculto no site.
- Produto pode ser vendido por unidade, peso, fatia, pacote ou encomenda.
- Produto com setor diferente de `SEM_PREPARO` pode gerar ticket no KDS.
- Alteração de preço não deve alterar pedidos antigos.

### Eventos gerados

```text
PRODUCT_CREATED
PRODUCT_UPDATED
PRODUCT_PRICE_CHANGED
PRODUCT_AVAILABLE
PRODUCT_UNAVAILABLE
```

---

## F07 — Gestão de disponibilidade

### Objetivo

Controlar se um produto pode ser vendido em cada canal.

### Atores

- Gerente.
- Atendente.
- Operador autorizado.
- Sistema de estoque.

### Pré-condições

- Produto cadastrado.
- Usuário autorizado.

### Fluxo principal

```text
Usuário acessa disponibilidade de produtos
↓
Sistema lista produtos ativos
↓
Usuário marca produto como disponível ou indisponível
↓
Sistema registra motivo quando necessário
↓
Sistema atualiza disponibilidade local
↓
Sistema cria evento de sincronização para online
↓
Site/WhatsApp refletem nova disponibilidade quando sincronizado
```

### Regras

- O ambiente local manda na disponibilidade real.
- Se o produto acabou na loja, deve ser bloqueado no online.
- Produto sob encomenda pode continuar visível mesmo sem estoque imediato.
- Mudança de disponibilidade deve gerar auditoria.

### Status previstos

```text
AVAILABLE
UNAVAILABLE
AVAILABLE_UNTIL_STOCK_ENDS
PRE_ORDER_ONLY
PICKUP_ONLY
DELIVERY_ONLY
COUNTER_ONLY
```

---

## F08 — Abertura de caixa

### Objetivo

Abrir o turno financeiro de um operador antes das vendas.

### Atores

- Operador de caixa.
- Gerente.
- Sistema de caixa.

### Pré-condições

- Usuário autenticado.
- Usuário com permissão de caixa.
- Não existir caixa aberto conflitante para operador/estação, conforme configuração.

### Fluxo principal

```text
Operador acessa PDV
↓
Sistema verifica caixa atual
↓
Sistema informa que não há caixa aberto
↓
Operador informa valor inicial
↓
Sistema valida permissão
↓
Sistema cria CashRegister com status OPEN
↓
Sistema registra CashMovement inicial, se aplicável
↓
PDV libera venda
```

### Regras

- Caixa fechado não permite venda.
- Caixa deve estar associado ao operador.
- Valor inicial deve ser informado.
- Abertura deve registrar data e hora.
- Abertura pode gerar evento de sincronização.

### Eventos gerados

```text
CASH_REGISTER_OPENED
CASH_MOVEMENT_CREATED
```

---

## F09 — Venda presencial no PDV

### Objetivo

Registrar uma venda presencial no caixa.

### Atores

- Operador de caixa.
- Cliente presencial.
- PDV.
- API Local.
- PostgreSQL Local.
- KDS, quando aplicável.

### Pré-condições

- PDV conectado à API Local.
- Caixa aberto.
- Produtos disponíveis no banco local.
- Operador autenticado.

### Fluxo principal

```text
Operador inicia nova venda
↓
Sistema cria pedido/venda com status CREATED
↓
Operador busca produto por nome, categoria, favorito ou código de barras
↓
Sistema valida produto ativo e permitido no PDV
↓
Operador adiciona produto ao carrinho
↓
Sistema calcula subtotal
↓
Operador ajusta quantidade, se necessário
↓
Sistema recalcula totais
↓
Operador seleciona pagamento
↓
Sistema registra pagamento
↓
Sistema finaliza venda
↓
Sistema gera movimentação de caixa
↓
Sistema imprime comprovante
↓
Sistema cria tickets KDS, se houver item de preparo
↓
Sistema cria evento de sincronização
```

### Regras

- Não finalizar venda sem itens.
- Não finalizar venda sem pagamento válido.
- Produto inativo não pode ser vendido.
- Produto indisponível deve ser bloqueado conforme configuração do canal.
- Item deve guardar snapshot de nome e preço.
- Venda finalizada deve gerar movimentação de caixa.
- Venda finalizada deve gerar evento de sincronização.

### Entidades envolvidas

- `Order`
- `OrderItem`
- `Payment`
- `CashRegister`
- `CashMovement`
- `Product`
- `ProductAvailability`
- `KdsTicket`
- `SyncEvent`
- `AuditLog`

### Eventos gerados

```text
SALE_CREATED
SALE_ITEM_ADDED
PAYMENT_REGISTERED
SALE_FINISHED
ORDER_SENT_TO_KDS
```

---

## F10 — Pagamento presencial simples

### Objetivo

Registrar pagamento de uma venda usando uma única forma de pagamento.

### Atores

- Operador de caixa.
- Cliente presencial.
- Sistema de pagamento local.

### Pré-condições

- Venda aberta.
- Total calculado.

### Fluxo principal

```text
Operador escolhe forma de pagamento
↓
Sistema exibe valor total
↓
Operador confirma pagamento
↓
Sistema valida valor recebido
↓
Sistema cria Payment
↓
Sistema atualiza paymentStatus do pedido
↓
Sistema cria CashMovement
↓
Sistema libera finalização da venda
```

### Formas previstas

```text
CASH
PIX
DEBIT_CARD
CREDIT_CARD
MEAL_VOUCHER
```

### Regras

- Pagamento presencial pode ser confirmado manualmente pelo operador.
- Dinheiro pode gerar troco.
- Pagamento em dinheiro pode acionar gaveta.
- Cada pagamento deve gerar movimentação de caixa.

---

## F11 — Pagamento misto

### Objetivo

Permitir que uma venda seja paga com mais de uma forma de pagamento.

### Atores

- Operador de caixa.
- Cliente presencial.

### Pré-condições

- Venda aberta.
- Total calculado.

### Fluxo principal

```text
Operador seleciona pagamento misto
↓
Sistema exibe total da venda
↓
Operador informa primeira forma e valor
↓
Sistema calcula saldo restante
↓
Operador informa segunda forma e valor
↓
Sistema valida soma dos pagamentos
↓
Sistema cria um Payment para cada forma
↓
Sistema cria uma CashMovement para cada pagamento
↓
Sistema atualiza pedido como pago
```

### Exemplo

```text
Total: R$ 42,00
R$ 20,00 em dinheiro
R$ 22,00 em Pix
```

### Regras

- Soma dos pagamentos deve ser igual ao total.
- Troco só se aplica a dinheiro.
- Cada pagamento deve ser salvo separadamente.
- Cada pagamento deve gerar movimentação de caixa.

---

## F12 — Desconto na venda

### Objetivo

Aplicar desconto em item ou no total da venda.

### Atores

- Operador de caixa.
- Gerente, quando necessário.

### Pré-condições

- Venda aberta.
- Produto ou total calculado.

### Fluxo principal

```text
Operador solicita desconto
↓
Sistema identifica tipo de desconto
↓
Sistema calcula novo total
↓
Sistema verifica limite permitido para o operador
↓
Se dentro do limite, aplica desconto
↓
Se acima do limite, solicita autorização de gerente
↓
Sistema registra desconto
↓
Sistema registra auditoria
```

### Tipos de desconto

```text
Desconto por valor
Desconto por percentual
Desconto por item
Desconto no total
```

### Regras

- Desconto deve respeitar limite configurado.
- Desconto acima do limite exige gerente.
- Desconto deve aparecer no resumo da venda.
- Desconto deve gerar auditoria.

---

## F13 — Cancelamento de venda

### Objetivo

Cancelar uma venda antes ou depois da finalização.

### Atores

- Operador de caixa.
- Gerente.
- Sistema de auditoria.

### Pré-condições

- Venda existente.

### Fluxo A — Cancelamento antes de finalizar

```text
Operador solicita cancelamento
↓
Sistema confirma descarte
↓
Sistema altera status para CANCELED ou remove venda temporária conforme regra
↓
Sistema libera PDV para nova venda
```

### Fluxo B — Cancelamento depois de finalizar

```text
Operador solicita cancelamento
↓
Sistema exige permissão superior
↓
Gerente informa senha ou autoriza ação
↓
Sistema solicita motivo
↓
Sistema registra cancelamento
↓
Sistema gera ajuste financeiro, se necessário
↓
Sistema registra auditoria
↓
Sistema cria evento de sincronização
```

### Regras

- Venda finalizada não deve permitir alteração simples.
- Cancelamento depois de finalizar deve exigir motivo.
- Cancelamento deve registrar usuário responsável.
- Cancelamento pode gerar estorno ou ajuste de caixa.
- Cancelamento deve gerar auditoria.

### Eventos gerados

```text
SALE_CANCELED
PAYMENT_CANCELED
CASH_MOVEMENT_CREATED
AUDIT_LOG_CREATED
```

---

## F14 — Impressão e abertura de gaveta

### Objetivo

Imprimir comprovantes e acionar gaveta quando necessário.

### Atores

- Operador de caixa.
- API Local.
- Printer Service.
- Impressora térmica.
- Gaveta de dinheiro.

### Pré-condições

- Venda finalizada ou movimentação criada.
- Impressora configurada.

### Fluxo principal

```text
Venda é finalizada
↓
API Local gera comando de impressão
↓
Printer Service envia comando ESC/POS
↓
Impressora imprime comprovante
↓
Se pagamento em dinheiro, impressora aciona gaveta
```

### Tipos de impressão

```text
Cupom simples
Comprovante de venda
Resumo de caixa
Sangria
Suprimento
Pedido de cozinha, se necessário
```

### Regras

- Impressão deve ser local.
- Gaveta geralmente é acionada pela impressora.
- Falha de impressão não deve duplicar venda.
- Reimpressão deve ser controlada e registrada.

---

## F15 — Fechamento de caixa

### Objetivo

Encerrar o turno financeiro do operador e calcular diferenças.

### Atores

- Operador de caixa.
- Gerente.
- Sistema de caixa.

### Pré-condições

- Caixa aberto.
- Usuário autorizado.

### Fluxo principal

```text
Operador solicita fechamento
↓
Sistema calcula total esperado
↓
Sistema agrupa valores por forma de pagamento
↓
Operador informa valores contados
↓
Sistema calcula diferença
↓
Sistema solicita observação se houver divergência
↓
Sistema fecha CashRegister
↓
Sistema imprime resumo
↓
Sistema cria evento de sincronização
```

### Resumo deve exibir

```text
Operador
Data/hora de abertura
Data/hora de fechamento
Valor inicial
Total em dinheiro
Total em Pix
Total em débito
Total em crédito
Total em voucher
Sangrias
Suprimentos
Total esperado
Total informado
Diferença
```

### Regras

- Caixa fechado não recebe novas movimentações.
- Diferença deve ser registrada.
- Fechamento deve gerar resumo.
- Fechamento deve gerar auditoria.

### Eventos gerados

```text
CASH_REGISTER_CLOSED
CASH_MOVEMENT_CREATED
AUDIT_LOG_CREATED
```

---

## F16 — Geração de tickets KDS

### Objetivo

Criar tickets de produção para itens que exigem preparo.

### Atores

- API Local.
- PDV.
- KDS.
- Produção.

### Pré-condições

- Pedido criado no PDV ou sincronizado do online.
- Pedido possui itens com setor de preparo.

### Fluxo principal

```text
Pedido é finalizado ou aceito
↓
Sistema percorre itens do pedido
↓
Sistema identifica preparationSector de cada item
↓
Sistema ignora itens SEM_PREPARO
↓
Sistema agrupa itens por setor
↓
Sistema cria KdsTicket por setor
↓
Sistema cria KdsTicketItem para cada item
↓
Sistema publica evento WebSocket
↓
KDS exibe novos tickets
```

### Exemplo

```text
Pedido #1521
- 1 X-Burger → CHAPA
- 1 Suco → BEBIDAS
- 1 Fatia de torta → CONFEITARIA

Resultado:
- Ticket CHAPA
- Ticket BEBIDAS
- Ticket CONFEITARIA
```

### Regras

- Pedido com setores diferentes pode gerar tickets diferentes.
- Expedição pode visualizar o pedido completo.
- Itens sem preparo não precisam gerar ticket.
- Ticket deve iniciar com status WAITING.

### Eventos gerados

```text
KDS_TICKET_CREATED
ORDER_SENT_TO_KDS
```

---

## F17 — Preparo de pedido no KDS

### Objetivo

Permitir que a produção acompanhe e atualize o preparo dos pedidos.

### Atores

- Produção.
- KDS.
- API Local.
- PDV.

### Pré-condições

- Ticket KDS criado.
- KDS conectado à API Local.

### Fluxo principal

```text
Produção visualiza ticket aguardando
↓
Produção clica em iniciar preparo
↓
Sistema altera ticket para IN_PREPARATION
↓
Sistema atualiza itens relacionados
↓
Sistema notifica PDV/expedição
↓
Produção prepara itens
↓
Produção marca item ou ticket como pronto
↓
Sistema verifica se todos os tickets do pedido estão prontos
↓
Se sim, pedido fica READY
```

### Regras

- Se pelo menos um item estiver em preparo, pedido fica IN_PREPARATION.
- Se todos os tickets obrigatórios estiverem prontos, pedido pode ficar READY.
- Cancelamento de item no KDS deve exigir permissão.
- Pedido só fica pronto quando todos os tickets obrigatórios estiverem prontos.

### Eventos gerados

```text
KDS_TICKET_STARTED
KDS_ITEM_STARTED
KDS_ITEM_READY
KDS_TICKET_READY
ORDER_READY
```

---

## F18 — Pedido pronto e expedição

### Objetivo

Conferir e liberar o pedido após preparo.

### Atores

- Expedição.
- Produção.
- Atendente.
- Entregador.
- Cliente.

### Pré-condições

- Pedido com status READY.

### Fluxo para retirada/balcão

```text
Todos os tickets ficam prontos
↓
Sistema muda pedido para READY
↓
PDV/atendimento recebe atualização
↓
Atendente chama cliente ou separa retirada
↓
Pedido é entregue ao cliente
↓
Sistema marca pedido como FINISHED
```

### Fluxo para delivery

```text
Todos os tickets ficam prontos
↓
Expedição confere pedido completo
↓
Sistema muda pedido para READY
↓
Entregador recebe pedido
↓
Sistema muda pedido para OUT_FOR_DELIVERY
↓
Após entrega, sistema muda para DELIVERED ou FINISHED
```

### Regras

- Expedição deve visualizar pedido completo.
- Pedido delivery não deve sair sem conferência.
- Status final deve impedir edição simples.

---

## F19 — Consulta pública do cardápio

### Objetivo

Permitir que clientes consultem produtos disponíveis no site ou WhatsApp.

### Atores

- Cliente online.
- Site.
- Chatbot.
- API Online.

### Pré-condições

- Produto ativo.
- Produto visível no canal.
- Categoria ativa.
- Produto disponível ou sob encomenda, conforme regra.

### Fluxo principal

```text
Cliente acessa site ou cardápio online
↓
Frontend consulta API pública
↓
API Online busca categorias e produtos visíveis
↓
Sistema aplica filtro de disponibilidade
↓
Sistema retorna cardápio
↓
Cliente visualiza produtos
```

### Regras

- Produto inativo não aparece.
- Categoria inativa não aparece.
- Produto indisponível não pode ser comprado online.
- Produto sob encomenda pode aparecer com regra específica.
- Disponibilidade online deve refletir sincronização do ambiente local.

---

## F20 — Pedido online pelo site

### Objetivo

Permitir que cliente faça pedido pelo site.

### Atores

- Cliente online.
- Site.
- API Online.
- Gateway de pagamento, quando aplicável.
- Sync Worker Local.
- KDS, após sincronização.

### Pré-condições

- Site online.
- Produto disponível para venda online.
- Cliente informou dados mínimos.
- Para entrega, endereço informado.

### Fluxo principal

```text
Cliente acessa cardápio
↓
Cliente adiciona produtos ao carrinho
↓
Cliente escolhe retirada, entrega ou consumo local quando aplicável
↓
Sistema valida itens, disponibilidade e valores
↓
Cliente informa dados pessoais
↓
Cliente escolhe forma de pagamento
↓
Sistema cria pedido online
↓
Sistema cria pagamento, se online
↓
Pedido fica PAYMENT_PENDING ou PAID conforme forma
↓
Sistema cria sync_event ORDER_CREATED
↓
Pedido aguarda envio para loja
```

### Regras

- Pedido online deve ter cliente.
- Pedido de entrega deve ter endereço.
- Pedido deve salvar snapshot de preço e nome dos produtos.
- Pedido não pode ser criado com produto indisponível.
- Pagamento online depende de confirmação do gateway.

### Status esperados

```text
CREATED
PAYMENT_PENDING
PAID
SENT_TO_STORE
RECEIVED_BY_STORE
ACCEPTED
IN_PREPARATION
READY
OUT_FOR_DELIVERY
DELIVERED
FINISHED
CANCELED
```

---

## F21 — Pagamento online

### Objetivo

Criar e controlar pagamento online de pedido feito pelo site ou WhatsApp.

### Atores

- Cliente online.
- API Online.
- Gateway de pagamento.

### Pré-condições

- Pedido online criado.
- Método de pagamento online selecionado.

### Fluxo principal

```text
Cliente confirma pedido
↓
API Online cria Payment com status PENDING
↓
API Online solicita cobrança ao gateway
↓
Gateway retorna dados de pagamento
↓
Cliente realiza pagamento
↓
Gateway processa transação
↓
Gateway envia webhook
```

### Regras

- Pagamento online não deve ser confirmado pelo frontend.
- Confirmação depende de webhook ou consulta confiável ao gateway.
- Transação deve guardar transactionId e gateway.
- Pagamento recusado deve atualizar pedido.

---

## F22 — Webhook de pagamento

### Objetivo

Receber confirmação, recusa ou expiração de pagamento online.

### Atores

- Gateway de pagamento.
- API Online.
- RabbitMQ Online.
- Sync Worker Online.

### Pré-condições

- Endpoint de webhook disponível.
- Assinatura ou token configurado.
- Pedido e pagamento existentes ou identificáveis.

### Fluxo principal

```text
Gateway envia webhook
↓
API Online valida assinatura
↓
API Online registra payload bruto
↓
Sistema verifica idempotência
↓
Sistema localiza pagamento
↓
Sistema atualiza status do Payment
↓
Sistema atualiza paymentStatus do Order
↓
Sistema publica evento interno
↓
Sistema cria sync_event para loja, quando necessário
↓
API responde sucesso ao gateway
```

### Regras

- Nunca confiar apenas no payload sem validar assinatura.
- Webhook duplicado não pode confirmar pagamento duas vezes.
- Payload bruto deve ser registrado para auditoria.
- Processamento pesado deve ser assíncrono.
- Resposta ao gateway deve ser rápida.

### Eventos gerados

```text
PAYMENT_APPROVED
PAYMENT_REFUSED
PAYMENT_EXPIRED
ORDER_PAID
```

---

## F23 — Pedido via WhatsApp / chatbot

### Objetivo

Permitir que o cliente faça ou inicie pedido pelo WhatsApp.

### Atores

- Cliente online.
- WhatsApp Provider.
- Chatbot.
- API Online.
- Atendente, se houver intervenção manual.

### Pré-condições

- Webhook WhatsApp configurado.
- Cliente enviou mensagem.
- Produtos disponíveis no canal WhatsApp.

### Fluxo principal automatizado

```text
Cliente envia mensagem no WhatsApp
↓
Provider envia webhook para API Online
↓
API valida origem do webhook
↓
Chatbot interpreta intenção
↓
Sistema consulta produtos disponíveis para WhatsApp
↓
Cliente escolhe itens
↓
Sistema monta carrinho
↓
Cliente informa entrega ou retirada
↓
Sistema confirma dados
↓
Sistema cria pedido online
↓
Sistema segue fluxo de pagamento ou pagamento na retirada
```

### Fluxo com atendimento manual

```text
Cliente envia mensagem
↓
Chatbot não entende ou cliente pede atendente
↓
Sistema transfere conversa para atendimento
↓
Atendente monta pedido no admin/online
↓
Sistema cria pedido com origem WHATSAPP
```

### Regras

- Produto precisa estar habilitado para WhatsApp.
- Mensagens recebidas devem ser idempotentes.
- Webhook deve validar assinatura/token.
- Pedido criado via WhatsApp deve seguir as mesmas regras de pedido online.

---

## F24 — Sincronização local → online

### Objetivo

Enviar eventos locais para o ambiente online.

### Atores

- API Local.
- RabbitMQ Local.
- Sync Worker Local.
- API Online.
- PostgreSQL Online.

### Exemplos de dados enviados

- Vendas presenciais.
- Fechamentos de caixa.
- Movimentações de caixa.
- Atualização de disponibilidade.
- Estoque.
- Status de pedidos.
- Logs críticos.
- Pedidos finalizados.

### Fluxo principal

```text
Evento relevante ocorre no ambiente local
↓
API Local salva dados no PostgreSQL Local
↓
API Local cria SyncEvent com status PENDING
↓
API Local publica evento no RabbitMQ Local
↓
Sync Worker Local consome evento
↓
Sync Worker assina payload com HMAC
↓
Sync Worker envia evento para API Online via HTTPS
↓
API Online valida Store ID, assinatura e idempotência
↓
API Online processa payload
↓
API Online responde sucesso
↓
Sync Worker Local marca evento como SYNCED
```

### Regras

- Evento deve ser idempotente.
- Falha deve gerar retry.
- Online deve registrar eventos recebidos na inbox.
- Sync não pode bloquear venda local.
- Dados críticos devem preservar rastreabilidade.

### Headers sugeridos

```http
X-Store-Id: nova-alianca-001
X-Signature: hash_hmac_sha256(payload, secret)
X-Event-Id: uuid
X-Event-Timestamp: 2026-05-03T10:30:00Z
```

---

## F25 — Sincronização online → local

### Objetivo

Trazer pedidos online, pagamentos e alterações externas para a loja.

### Atores

- API Online.
- Sync Worker Local.
- API Local.
- PostgreSQL Local.
- KDS.
- PDV.

### Exemplos de dados recebidos

- Pedidos do site.
- Pedidos do WhatsApp.
- Pagamentos online aprovados.
- Pagamentos recusados.
- Cancelamentos online.
- Atualizações de cliente.
- Alterações de cardápio, quando permitidas.

### Fluxo principal

```text
Pedido online é criado no ambiente online
↓
API Online cria SyncEvent com status PENDING
↓
Sync Worker Local consulta API Online por pendências
↓
API Online retorna eventos pendentes da loja
↓
Sync Worker Local baixa payload
↓
API Local valida idempotência
↓
API Local grava pedido no PostgreSQL Local
↓
API Local cria tickets KDS, se necessário
↓
PDV/KDS recebem atualização
↓
Local envia ACK para API Online
↓
Online marca evento como recebido pela loja
```

### Regras

- Local faz pull porque a loja pode estar atrás de NAT.
- Pedido online pago e loja offline deve ficar aguardando confirmação da loja.
- Evento duplicado não pode criar pedido duplicado.
- Pedido recebido pela loja deve atualizar status online para RECEIVED_BY_STORE.

---

## F26 — Tratamento de falha de sincronização

### Objetivo

Reprocessar eventos com falha sem perda de dados.

### Atores

- Sync Worker.
- Gerente.
- Administrador.
- Painel de sincronização.

### Pré-condições

- SyncEvent com erro ou pendente.

### Fluxo automático

```text
Sync Worker tenta enviar/processar evento
↓
Ocorre erro temporário
↓
Sistema incrementa retry_count
↓
Sistema agenda nova tentativa
↓
Evento muda para RETRYING
↓
Após nova tentativa bem-sucedida, muda para SYNCED
```

### Estratégia inicial de retry

```text
Tentativa 1: imediata
Tentativa 2: após 1 minuto
Tentativa 3: após 5 minutos
Tentativa 4: após 15 minutos
Tentativa 5: após 1 hora
```

### Fluxo de falha definitiva

```text
Evento excede número máximo de tentativas
↓
Sistema marca como FAILED
↓
Painel de sincronização exibe erro
↓
Gerente/Admin pode reprocessar ou ignorar
↓
Ação manual gera auditoria
```

### Regras

- Evento FAILED não deve ser apagado.
- Reprocessamento manual exige permissão.
- Ignorar evento exige motivo.
- Toda falha deve guardar mensagem de erro.

---

## F27 — Movimentação de estoque

### Objetivo

Registrar entradas, saídas, perdas, ajustes e baixas por venda.

### Atores

- Gerente.
- Atendente autorizado.
- Sistema de venda.
- Sistema de estoque.

### Pré-condições

- Produto cadastrado.
- Regra de controle de estoque ativa para o produto.

### Fluxo por venda

```text
Venda é finalizada
↓
Sistema identifica produtos com controle de estoque
↓
Sistema calcula quantidade de baixa
↓
Sistema cria StockMovement do tipo SALE
↓
Sistema atualiza disponibilidade, se necessário
↓
Sistema cria evento de sincronização
```

### Fluxo por ajuste manual

```text
Usuário acessa ajuste de estoque
↓
Seleciona produto
↓
Informa quantidade e motivo
↓
Sistema valida permissão
↓
Sistema cria StockMovement
↓
Sistema recalcula disponibilidade
↓
Sistema registra auditoria
```

### Regras

- Ajuste manual deve registrar motivo.
- Produto com estoque zerado pode ficar indisponível automaticamente.
- Estoque físico é fonte local.
- Estoque online deve ser reflexo sincronizado.

---

## F28 — Auditoria de ações críticas

### Objetivo

Registrar operações sensíveis para rastreabilidade.

### Atores

- Sistema de auditoria.
- Usuários internos.
- Administrador.

### Eventos auditáveis

```text
Login
Cancelamento de venda
Desconto
Sangria
Suprimento
Fechamento de caixa
Alteração de preço
Alteração de disponibilidade
Cancelamento de pedido
Estorno
Alteração de permissão
Reprocessamento de sync
Ignorar evento de sync
```

### Fluxo principal

```text
Usuário executa ação crítica
↓
Sistema captura usuário, ação e entidade
↓
Sistema registra valor anterior, quando aplicável
↓
Sistema registra novo valor, quando aplicável
↓
Sistema registra IP/origem, quando disponível
↓
Sistema persiste AuditLog
```

### Regras

- Auditoria não deve depender do frontend.
- Ação crítica não deve ocorrer sem usuário identificável, salvo processos técnicos identificados.
- Logs de auditoria não devem ser editáveis pelo usuário comum.

---

## F29 — Painel de sincronização

### Objetivo

Permitir acompanhamento operacional da comunicação local ↔ online.

### Atores

- Gerente.
- Administrador.
- Suporte técnico.

### Informações exibidas

```text
Última sincronização
Eventos pendentes
Eventos com erro
Pedidos online aguardando loja
Pedidos locais aguardando envio
Quantidade de retries
Mensagem de erro
Status dos workers
Status da conexão com a nuvem
```

### Ações disponíveis

```text
Reprocessar evento
Ignorar evento
Ver payload técnico
Ver erro
Filtrar por status
Filtrar por tipo de evento
```

### Regras

- Reprocessar evento exige permissão.
- Ignorar evento exige permissão e motivo.
- Payload pode conter dados sensíveis; acesso deve ser controlado.

---

## F30 — Health check e monitoramento

### Objetivo

Monitorar saúde técnica dos ambientes local e online.

### Atores

- Sistema.
- Administrador.
- Suporte técnico.

### Health checks locais

```text
PostgreSQL Local online
RabbitMQ Local online
Redis Local online
API Local online
Última sincronização com nuvem
Quantidade de eventos pendentes
Espaço em disco
Status do KDS
Status do PDV
```

### Health checks online

```text
PostgreSQL Online online
RabbitMQ Online online
Redis Online online
API Online online
Certificado HTTPS válido
Webhooks ativos
Eventos pendentes para lojas
Espaço em disco
Último backup
```

### Endpoints sugeridos

```text
GET /actuator/health
GET /actuator/metrics
GET /api/sync/status
GET /api/sync/health
```

---

# 7. Casos de uso

## 7.1 Convenção

Cada caso de uso será descrito no seguinte formato:

```text
ID
Nome
Ator principal
Atores secundários
Objetivo
Pré-condições
Fluxo principal
Fluxos alternativos
Pós-condições
Regras de negócio
Entidades envolvidas
Eventos gerados
Prioridade
```

---

## UC-001 — Autenticar usuário

| Campo | Descrição |
|---|---|
| Ator principal | Usuário interno |
| Atores secundários | Sistema de segurança |
| Objetivo | Permitir acesso aos módulos privados |
| Prioridade | MVP |

### Pré-condições

- Usuário cadastrado.
- Usuário ativo.
- Senha cadastrada com hash.

### Fluxo principal

```text
1. Usuário informa credenciais.
2. Sistema valida usuário.
3. Sistema valida senha.
4. Sistema valida status ativo.
5. Sistema carrega permissões.
6. Sistema retorna token/sessão.
```

### Fluxos alternativos

- Credenciais inválidas: negar acesso.
- Usuário inativo: negar acesso.
- Usuário sem perfil: negar acesso administrativo.

### Regras

- Senha nunca deve ser salva em texto puro.
- Token expirado deve exigir novo login.
- Perfil controla acesso às ações.

### Entidades

- `User`
- `Role`
- `AuditLog`

---

## UC-002 — Criar usuário interno

| Campo | Descrição |
|---|---|
| Ator principal | Administrador |
| Atores secundários | Sistema de segurança |
| Objetivo | Cadastrar operador, gerente, cozinha ou consulta |
| Prioridade | MVP |

### Fluxo principal

```text
1. Administrador acessa cadastro de usuário.
2. Informa nome, username, senha e perfil.
3. Sistema valida duplicidade.
4. Sistema gera hash da senha.
5. Sistema salva usuário.
6. Sistema registra auditoria.
```

### Regras

- Username deve ser único.
- Perfil é obrigatório.
- Usuário pode ser inativado, não removido fisicamente.

### Entidades

- `User`
- `Role`
- `AuditLog`

---

## UC-003 — Cadastrar categoria

| Campo | Descrição |
|---|---|
| Ator principal | Gerente / Administrador |
| Objetivo | Organizar produtos por grupo |
| Prioridade | MVP |

### Fluxo principal

```text
1. Usuário acessa categorias.
2. Informa nome, descrição e ordem.
3. Define canais de visibilidade.
4. Sistema valida campos.
5. Sistema salva categoria.
6. Sistema cria evento de sincronização.
```

### Regras

- Categoria inativa não aparece para venda.
- Categoria pode ser visível em canais diferentes.

### Entidades

- `Category`
- `SyncEvent`
- `AuditLog`

### Eventos

```text
CATEGORY_CREATED
CATEGORY_UPDATED
```

---

## UC-004 — Cadastrar produto

| Campo | Descrição |
|---|---|
| Ator principal | Gerente / Administrador |
| Objetivo | Criar produto vendável |
| Prioridade | MVP |

### Fluxo principal

```text
1. Usuário acessa produtos.
2. Informa nome, categoria, preço e unidade.
3. Define setor de preparo.
4. Define canais de venda.
5. Define disponibilidade inicial.
6. Sistema salva produto.
7. Sistema cria evento de sincronização.
```

### Regras

- Produto deve ter preço válido.
- Produto deve ter unidade.
- Produto inativo não aparece em nenhum canal.
- Produto com preparo deve ter setor definido.

### Entidades

- `Product`
- `Category`
- `ProductAvailability`
- `SyncEvent`
- `AuditLog`

---

## UC-005 — Alterar disponibilidade de produto

| Campo | Descrição |
|---|---|
| Ator principal | Gerente / Atendente autorizado |
| Objetivo | Bloquear ou liberar produto para venda |
| Prioridade | MVP |

### Fluxo principal

```text
1. Usuário localiza produto.
2. Altera status de disponibilidade.
3. Informa motivo, se indisponível.
4. Sistema salva alteração local.
5. Sistema registra auditoria.
6. Sistema cria evento para ambiente online.
```

### Regras

- Local manda na disponibilidade real.
- Indisponibilidade deve ser refletida online.
- Alteração deve registrar usuário.

### Entidades

- `Product`
- `ProductAvailability`
- `SyncEvent`
- `AuditLog`

---

## UC-006 — Abrir caixa

| Campo | Descrição |
|---|---|
| Ator principal | Operador de caixa |
| Atores secundários | Gerente |
| Objetivo | Iniciar turno financeiro |
| Prioridade | MVP |

### Fluxo principal

```text
1. Operador acessa PDV.
2. Sistema identifica ausência de caixa aberto.
3. Operador informa valor inicial.
4. Sistema valida permissão.
5. Sistema cria CashRegister OPEN.
6. PDV libera vendas.
```

### Regras

- Não vender sem caixa aberto, se regra ativa.
- Caixa aberto pertence ao operador/estação.
- Valor inicial é obrigatório.

### Entidades

- `CashRegister`
- `CashMovement`
- `User`

### Eventos

```text
CASH_REGISTER_OPENED
```

---

## UC-007 — Criar venda no PDV

| Campo | Descrição |
|---|---|
| Ator principal | Operador de caixa |
| Atores secundários | Cliente presencial |
| Objetivo | Iniciar venda presencial |
| Prioridade | MVP |

### Fluxo principal

```text
1. Operador inicia nova venda.
2. Sistema cria Order com origem PDV.
3. Sistema define status CREATED.
4. Operador adiciona itens.
```

### Regras

- Caixa deve estar aberto.
- Operador deve estar autenticado.

### Entidades

- `Order`
- `CashRegister`
- `User`

### Eventos

```text
SALE_CREATED
```

---

## UC-008 — Adicionar item à venda

| Campo | Descrição |
|---|---|
| Ator principal | Operador de caixa |
| Objetivo | Inserir produto no carrinho |
| Prioridade | MVP |

### Fluxo principal

```text
1. Operador busca produto.
2. Sistema valida produto ativo.
3. Sistema valida disponibilidade no PDV.
4. Sistema captura snapshot de nome e preço.
5. Sistema adiciona item.
6. Sistema recalcula total.
```

### Fluxos alternativos

- Produto não encontrado: exibir aviso.
- Produto inativo: bloquear.
- Produto indisponível: bloquear ou solicitar autorização conforme regra.

### Regras

- Snapshot de preço é obrigatório.
- Alteração futura de preço não muda venda já criada.

### Entidades

- `Order`
- `OrderItem`
- `Product`

### Eventos

```text
SALE_ITEM_ADDED
```

---

## UC-009 — Remover item da venda

| Campo | Descrição |
|---|---|
| Ator principal | Operador de caixa |
| Objetivo | Remover item antes da finalização |
| Prioridade | MVP |

### Fluxo principal

```text
1. Operador seleciona item.
2. Solicita remoção.
3. Sistema remove ou cancela item.
4. Sistema recalcula total.
```

### Regras

- Item de venda finalizada não deve ser removido sem processo de cancelamento/estorno.
- Remoção pode gerar auditoria conforme configuração.

### Entidades

- `OrderItem`
- `Order`

### Eventos

```text
SALE_ITEM_REMOVED
```

---

## UC-010 — Aplicar desconto

| Campo | Descrição |
|---|---|
| Ator principal | Operador de caixa |
| Atores secundários | Gerente |
| Objetivo | Aplicar desconto controlado |
| Prioridade | MVP |

### Fluxo principal

```text
1. Operador informa desconto.
2. Sistema calcula novo total.
3. Sistema valida limite do operador.
4. Se permitido, aplica desconto.
5. Sistema registra auditoria.
```

### Fluxo alternativo

```text
1. Desconto excede limite.
2. Sistema solicita autorização de gerente.
3. Gerente autoriza.
4. Sistema aplica desconto.
5. Sistema registra auditoria com operador e autorizador.
```

### Regras

- Desconto acima do limite exige gerente.
- Desconto deve aparecer no resumo da venda.

### Entidades

- `Order`
- `OrderItem`
- `AuditLog`

### Eventos

```text
SALE_DISCOUNT_APPLIED
```

---

## UC-011 — Registrar pagamento

| Campo | Descrição |
|---|---|
| Ator principal | Operador de caixa |
| Objetivo | Registrar recebimento presencial |
| Prioridade | MVP |

### Fluxo principal

```text
1. Operador escolhe forma de pagamento.
2. Sistema recebe valor.
3. Sistema valida valor contra total.
4. Sistema cria Payment.
5. Sistema cria CashMovement.
6. Sistema atualiza paymentStatus.
```

### Regras

- Pagamento deve bater com total.
- Pagamento em dinheiro pode gerar troco.
- Cada pagamento gera movimentação de caixa.

### Entidades

- `Payment`
- `CashMovement`
- `Order`

### Eventos

```text
PAYMENT_REGISTERED
```

---

## UC-012 — Finalizar venda

| Campo | Descrição |
|---|---|
| Ator principal | Operador de caixa |
| Objetivo | Encerrar venda presencial |
| Prioridade | MVP |

### Fluxo principal

```text
1. Operador confirma finalização.
2. Sistema valida itens.
3. Sistema valida pagamento.
4. Sistema altera pedido para FINISHED ou ACCEPTED conforme tipo.
5. Sistema imprime comprovante.
6. Sistema aciona gaveta, se dinheiro.
7. Sistema gera tickets KDS, se necessário.
8. Sistema cria evento de sincronização.
```

### Regras

- Não finalizar sem itens.
- Não finalizar sem pagamento.
- Itens com preparo devem ir para KDS.
- Venda finalizada não permite edição simples.

### Entidades

- `Order`
- `OrderItem`
- `Payment`
- `CashMovement`
- `KdsTicket`
- `SyncEvent`

### Eventos

```text
SALE_FINISHED
ORDER_SENT_TO_KDS
```

---

## UC-013 — Cancelar venda finalizada

| Campo | Descrição |
|---|---|
| Ator principal | Gerente |
| Atores secundários | Operador de caixa |
| Objetivo | Cancelar venda já concluída |
| Prioridade | MVP |

### Fluxo principal

```text
1. Operador solicita cancelamento.
2. Sistema exige autorização.
3. Gerente autoriza.
4. Sistema solicita motivo.
5. Sistema cancela venda.
6. Sistema cria ajuste financeiro.
7. Sistema registra auditoria.
8. Sistema cria evento de sincronização.
```

### Regras

- Motivo é obrigatório.
- Usuário responsável deve ser registrado.
- Cancelamento deve gerar auditoria.
- Pode gerar estorno ou movimento de ajuste.

### Entidades

- `Order`
- `Payment`
- `CashMovement`
- `AuditLog`
- `SyncEvent`

### Eventos

```text
SALE_CANCELED
```

---

## UC-014 — Fechar caixa

| Campo | Descrição |
|---|---|
| Ator principal | Operador de caixa |
| Atores secundários | Gerente |
| Objetivo | Encerrar turno e apurar valores |
| Prioridade | MVP |

### Fluxo principal

```text
1. Operador solicita fechamento.
2. Sistema calcula valores esperados.
3. Operador informa valores contados.
4. Sistema calcula diferença.
5. Sistema solicita justificativa se houver divergência.
6. Sistema fecha caixa.
7. Sistema imprime resumo.
8. Sistema cria evento de sincronização.
```

### Regras

- Caixa fechado não recebe movimentações.
- Diferença deve ser registrada.
- Fechamento deve gerar resumo e auditoria.

### Entidades

- `CashRegister`
- `CashMovement`
- `AuditLog`
- `SyncEvent`

### Eventos

```text
CASH_REGISTER_CLOSED
```

---

## UC-015 — Criar tickets KDS

| Campo | Descrição |
|---|---|
| Ator principal | Sistema |
| Atores secundários | Produção |
| Objetivo | Separar itens de preparo por setor |
| Prioridade | MVP |

### Fluxo principal

```text
1. Pedido é criado/finalizado.
2. Sistema verifica setores dos itens.
3. Sistema agrupa por setor.
4. Sistema cria tickets.
5. Sistema envia evento WebSocket.
6. KDS exibe tickets.
```

### Regras

- Um pedido pode gerar múltiplos tickets.
- Itens SEM_PREPARO não geram ticket.
- Expedição pode ver pedido completo.

### Entidades

- `Order`
- `OrderItem`
- `KdsTicket`
- `KdsTicketItem`

### Eventos

```text
KDS_TICKET_CREATED
```

---

## UC-016 — Iniciar preparo no KDS

| Campo | Descrição |
|---|---|
| Ator principal | Produção |
| Objetivo | Indicar que o preparo começou |
| Prioridade | MVP |

### Fluxo principal

```text
1. Produção visualiza ticket WAITING.
2. Clica em iniciar preparo.
3. Sistema atualiza ticket para IN_PREPARATION.
4. Sistema atualiza pedido se necessário.
5. Sistema notifica PDV/expedição.
```

### Regras

- Status deve obedecer transição válida.
- Pedido fica IN_PREPARATION se algum ticket estiver em preparo.

### Entidades

- `KdsTicket`
- `KdsTicketItem`
- `Order`

### Eventos

```text
KDS_TICKET_STARTED
```

---

## UC-017 — Marcar ticket/item como pronto

| Campo | Descrição |
|---|---|
| Ator principal | Produção |
| Objetivo | Informar conclusão de preparo |
| Prioridade | MVP |

### Fluxo principal

```text
1. Produção marca item ou ticket como pronto.
2. Sistema atualiza status.
3. Sistema verifica demais tickets do pedido.
4. Se todos estiverem prontos, pedido vira READY.
5. Sistema notifica PDV/expedição.
```

### Regras

- Pedido só fica READY quando todos os tickets obrigatórios estiverem prontos.
- Data/hora de pronto deve ser registrada.

### Entidades

- `KdsTicket`
- `KdsTicketItem`
- `Order`

### Eventos

```text
KDS_ITEM_READY
KDS_TICKET_READY
ORDER_READY
```

---

## UC-018 — Consultar cardápio público

| Campo | Descrição |
|---|---|
| Ator principal | Cliente online |
| Objetivo | Visualizar produtos disponíveis |
| Prioridade | MVP |

### Fluxo principal

```text
1. Cliente acessa site.
2. Site solicita cardápio público.
3. API Online retorna categorias e produtos visíveis.
4. Cliente navega no cardápio.
```

### Regras

- Apenas produtos ativos e disponíveis devem aparecer.
- Respeitar flags `sellOnline` e `showOnline`.

### Entidades

- `Category`
- `Product`
- `ProductAvailability`

---

## UC-019 — Criar pedido online

| Campo | Descrição |
|---|---|
| Ator principal | Cliente online |
| Objetivo | Comprar pelo site |
| Prioridade | MVP |

### Fluxo principal

```text
1. Cliente monta carrinho.
2. Informa dados pessoais.
3. Escolhe entrega ou retirada.
4. Sistema valida pedido.
5. Sistema cria cliente/endereço, se necessário.
6. Sistema cria pedido.
7. Sistema cria pagamento ou registra pagamento na retirada.
8. Sistema cria evento para sincronização.
```

### Regras

- Pedido online deve ter cliente.
- Entrega exige endereço.
- Produto deve estar disponível online.
- Valores devem ser congelados no pedido.

### Entidades

- `Customer`
- `CustomerAddress`
- `Order`
- `OrderItem`
- `Payment`
- `SyncEvent`

### Eventos

```text
ORDER_CREATED
PAYMENT_CREATED
```

---

## UC-020 — Processar pagamento online

| Campo | Descrição |
|---|---|
| Ator principal | Gateway de pagamento |
| Atores secundários | Cliente online, API Online |
| Objetivo | Confirmar ou recusar pagamento |
| Prioridade | MVP |

### Fluxo principal

```text
1. API Online cria cobrança.
2. Cliente paga.
3. Gateway envia webhook.
4. API valida webhook.
5. Sistema atualiza Payment.
6. Sistema atualiza Order.
7. Sistema cria evento de sincronização.
```

### Regras

- Frontend não confirma pagamento.
- Webhook deve ser idempotente.
- Assinatura deve ser validada.

### Entidades

- `Payment`
- `Order`
- `SyncEvent`
- `AuditLog`

### Eventos

```text
PAYMENT_APPROVED
ORDER_PAID
```

---

## UC-021 — Baixar pedido online para loja

| Campo | Descrição |
|---|---|
| Ator principal | Sync Worker Local |
| Objetivo | Receber pedidos online no ambiente local |
| Prioridade | MVP |

### Fluxo principal

```text
1. Sync Worker Local consulta pendências online.
2. API Online retorna pedidos pendentes.
3. Local valida evento.
4. Local grava pedido.
5. Local cria tickets KDS.
6. Local confirma recebimento.
7. Online atualiza status para RECEIVED_BY_STORE.
```

### Regras

- Duplicidade deve ser bloqueada por idempotência.
- Loja offline mantém pedido aguardando no online.

### Entidades

- `SyncEvent`
- `Order`
- `OrderItem`
- `Payment`
- `KdsTicket`

### Eventos

```text
ORDER_RECEIVED_BY_STORE
ORDER_SENT_TO_KDS
```

---

## UC-022 — Sincronizar venda presencial para nuvem

| Campo | Descrição |
|---|---|
| Ator principal | Sync Worker Local |
| Objetivo | Enviar venda local para relatórios online |
| Prioridade | MVP |

### Fluxo principal

```text
1. Venda presencial é finalizada.
2. Sistema cria SyncEvent local.
3. Sync Worker envia evento por HTTPS.
4. API Online valida assinatura e idempotência.
5. API Online grava venda.
6. Local marca evento como SYNCED.
```

### Regras

- Falha de internet não bloqueia venda.
- Evento deve ser reenviado até sucesso ou falha definitiva.

### Entidades

- `Order`
- `OrderItem`
- `Payment`
- `CashMovement`
- `SyncEvent`

---

## UC-023 — Registrar sangria

| Campo | Descrição |
|---|---|
| Ator principal | Gerente / Operador autorizado |
| Objetivo | Registrar saída de dinheiro do caixa |
| Prioridade | MVP |

### Fluxo principal

```text
1. Usuário seleciona sangria.
2. Informa valor e motivo.
3. Sistema valida permissão.
4. Sistema cria CashMovement CASH_OUT.
5. Sistema imprime comprovante.
6. Sistema registra auditoria.
```

### Regras

- Motivo é obrigatório.
- Pode exigir gerente.
- Deve impactar fechamento de caixa.

### Entidades

- `CashRegister`
- `CashMovement`
- `AuditLog`

### Eventos

```text
CASH_MOVEMENT_CREATED
```

---

## UC-024 — Registrar suprimento

| Campo | Descrição |
|---|---|
| Ator principal | Gerente / Operador autorizado |
| Objetivo | Registrar entrada manual de dinheiro no caixa |
| Prioridade | MVP |

### Fluxo principal

```text
1. Usuário seleciona suprimento.
2. Informa valor e motivo.
3. Sistema valida permissão.
4. Sistema cria CashMovement CASH_IN.
5. Sistema imprime comprovante.
6. Sistema registra auditoria.
```

### Regras

- Motivo é obrigatório.
- Deve impactar fechamento de caixa.

### Entidades

- `CashRegister`
- `CashMovement`
- `AuditLog`

---

## UC-025 — Reprocessar evento de sincronização

| Campo | Descrição |
|---|---|
| Ator principal | Administrador / Gerente |
| Objetivo | Resolver falha de sync manualmente |
| Prioridade | MVP |

### Fluxo principal

```text
1. Usuário acessa painel de sincronização.
2. Filtra eventos FAILED.
3. Seleciona evento.
4. Clica em reprocessar.
5. Sistema valida permissão.
6. Sistema altera status para RETRYING ou PENDING.
7. Worker processa novamente.
8. Sistema registra auditoria.
```

### Regras

- Reprocessamento exige permissão.
- Evento não deve duplicar dado no destino.
- Auditoria é obrigatória.

### Entidades

- `SyncEvent`
- `AuditLog`

---

# 8. Matriz ator × casos de uso

| Caso de uso | Cliente presencial | Cliente online | Operador caixa | Produção | Gerente | Admin | Sistema/Worker |
|---|---:|---:|---:|---:|---:|---:|---:|
| UC-001 Autenticar usuário |  |  | X | X | X | X |  |
| UC-002 Criar usuário interno |  |  |  |  |  | X |  |
| UC-003 Cadastrar categoria |  |  |  |  | X | X |  |
| UC-004 Cadastrar produto |  |  |  |  | X | X |  |
| UC-005 Alterar disponibilidade |  |  | X |  | X | X |  |
| UC-006 Abrir caixa |  |  | X |  | X |  |  |
| UC-007 Criar venda PDV | X |  | X |  |  |  |  |
| UC-008 Adicionar item | X |  | X |  |  |  |  |
| UC-009 Remover item | X |  | X |  | X |  |  |
| UC-010 Aplicar desconto | X |  | X |  | X |  |  |
| UC-011 Registrar pagamento | X |  | X |  |  |  |  |
| UC-012 Finalizar venda | X |  | X |  |  |  | X |
| UC-013 Cancelar venda |  |  | X |  | X |  |  |
| UC-014 Fechar caixa |  |  | X |  | X |  |  |
| UC-015 Criar tickets KDS |  |  |  |  |  |  | X |
| UC-016 Iniciar preparo |  |  |  | X |  |  |  |
| UC-017 Marcar pronto |  |  |  | X |  |  | X |
| UC-018 Consultar cardápio |  | X |  |  |  |  |  |
| UC-019 Criar pedido online |  | X |  |  |  |  | X |
| UC-020 Processar pagamento online |  | X |  |  |  |  | X |
| UC-021 Baixar pedido online |  |  |  |  |  |  | X |
| UC-022 Sincronizar venda local |  |  |  |  |  |  | X |
| UC-023 Registrar sangria |  |  | X |  | X |  |  |
| UC-024 Registrar suprimento |  |  | X |  | X |  |  |
| UC-025 Reprocessar sync |  |  |  |  | X | X | X |

---

# 9. Matriz caso de uso × módulo

| Caso de uso | Security | Catalog | PDV | Cash | Payment | KDS | Sync | Audit | Stock | Online |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| UC-001 | X |  |  |  |  |  |  | X |  |  |
| UC-002 | X |  |  |  |  |  |  | X |  |  |
| UC-003 |  | X |  |  |  |  | X | X |  | X |
| UC-004 |  | X |  |  |  | X | X | X | X | X |
| UC-005 |  | X | X |  |  |  | X | X | X | X |
| UC-006 | X |  | X | X |  |  | X | X |  |  |
| UC-007 | X | X | X | X |  |  |  |  |  |  |
| UC-008 |  | X | X |  |  | X |  |  | X |  |
| UC-009 |  |  | X |  |  |  |  | X |  |  |
| UC-010 | X |  | X | X |  |  |  | X |  |  |
| UC-011 |  |  | X | X | X |  |  |  |  |  |
| UC-012 |  |  | X | X | X | X | X | X | X |  |
| UC-013 | X |  | X | X | X | X | X | X | X |  |
| UC-014 | X |  | X | X |  |  | X | X |  |  |
| UC-015 |  | X |  |  |  | X |  |  |  |  |
| UC-016 | X |  |  |  |  | X |  |  |  |  |
| UC-017 | X |  |  |  |  | X | X |  |  | X |
| UC-018 |  | X |  |  |  |  |  |  |  | X |
| UC-019 |  | X |  |  | X |  | X |  |  | X |
| UC-020 |  |  |  |  | X |  | X | X |  | X |
| UC-021 |  |  | X |  | X | X | X |  |  | X |
| UC-022 |  |  | X | X | X |  | X |  |  | X |
| UC-023 | X |  | X | X |  |  | X | X |  |  |
| UC-024 | X |  | X | X |  |  | X | X |  |  |
| UC-025 | X |  |  |  |  |  | X | X |  | X |

---

# 10. Máquinas de estado

## 10.1 Pedido online

```text
CREATED
↓
PAYMENT_PENDING
↓
PAID
↓
SENT_TO_STORE
↓
RECEIVED_BY_STORE
↓
ACCEPTED
↓
IN_PREPARATION
↓
READY
↓
OUT_FOR_DELIVERY
↓
DELIVERED
↓
FINISHED
```

Status alternativo em quase todas as etapas operacionais:

```text
CANCELED
```

## 10.2 Pedido local / PDV

```text
CREATED
↓
ACCEPTED
↓
IN_PREPARATION
↓
READY
↓
FINISHED
```

Para venda simples sem preparo:

```text
CREATED
↓
PAID
↓
FINISHED
```

## 10.3 Item do pedido

```text
CREATED
↓
WAITING_PREPARATION
↓
IN_PREPARATION
↓
READY
↓
DELIVERED
```

Alternativo:

```text
CANCELED
```

## 10.4 Ticket KDS

```text
WAITING
↓
IN_PREPARATION
↓
READY
↓
FINISHED
```

Alternativo:

```text
CANCELED
```

## 10.5 Pagamento

```text
PENDING
↓
AUTHORIZED
↓
PAID
```

Alternativos:

```text
REFUSED
CANCELED
REFUNDED
EXPIRED
```

## 10.6 Caixa

```text
OPEN
↓
CLOSED
```

Alternativo:

```text
BLOCKED
```

## 10.7 Evento de sincronização

```text
PENDING
↓
PROCESSING
↓
SYNCED
```

Fluxo com falha:

```text
PENDING
↓
PROCESSING
↓
FAILED
↓
RETRYING
↓
PROCESSING
↓
SYNCED
```

Alternativo:

```text
IGNORED
```

---

# 11. Eventos de domínio e integração

## 11.1 PDV

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

## 11.2 KDS

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

## 11.3 Pedidos e pagamentos online

```text
ORDER_CREATED
ORDER_UPDATED
ORDER_CANCELED
ORDER_PAID
ORDER_SENT_TO_STORE
ORDER_RECEIVED_BY_STORE
PAYMENT_CREATED
PAYMENT_APPROVED
PAYMENT_REFUSED
PAYMENT_EXPIRED
```

## 11.4 Catálogo e disponibilidade

```text
PRODUCT_CREATED
PRODUCT_UPDATED
PRODUCT_PRICE_CHANGED
PRODUCT_UNAVAILABLE
PRODUCT_AVAILABLE
CATEGORY_CREATED
CATEGORY_UPDATED
```

## 11.5 Estoque

```text
STOCK_MOVED
STOCK_ADJUSTED
STOCK_LOSS_REGISTERED
```

---

# 12. APIs sugeridas por fluxo

## 12.1 Segurança

```text
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/me
GET  /api/users
POST /api/users
PATCH /api/users/{id}
PATCH /api/users/{id}/disable
```

## 12.2 Catálogo

```text
GET    /api/categories
POST   /api/categories
PATCH  /api/categories/{id}
DELETE /api/categories/{id}

GET    /api/products
POST   /api/products
PATCH  /api/products/{id}
PATCH  /api/products/{id}/availability
GET    /api/products/barcode/{barcode}
```

## 12.3 PDV

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

## 12.4 Caixa

```text
POST /api/cash-register/open
GET  /api/cash-register/current
POST /api/cash-register/{id}/movement
POST /api/cash-register/{id}/close
GET  /api/cash-register/{id}/summary
```

## 12.5 KDS

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

## 12.6 Site público e pedidos online

```text
GET  /api/public/menu
GET  /api/public/products
POST /api/orders
GET  /api/orders/{id}/status
POST /api/checkout/quote
```

## 12.7 Pagamentos e webhooks

```text
POST /api/payments/online
POST /api/payments/webhook
GET  /api/payments/{id}
```

## 12.8 WhatsApp

```text
POST /api/whatsapp/webhook
POST /api/whatsapp/messages/send
GET  /api/whatsapp/conversations/{id}
```

## 12.9 Sincronização

```text
POST /api/sync/events
POST /api/sync/events/batch
GET  /api/sync/events/{id}/status
GET  /api/sync/pending?storeId=nova-alianca-001
POST /api/sync/events/{id}/ack
POST /api/sync/events/{id}/fail
GET  /api/sync/status
GET  /api/sync/health
```

---

# 13. Regras transversais

## 13.1 Segurança

- Nunca confiar apenas no frontend.
- Usuário inativo não autentica.
- Ações críticas exigem permissão.
- Algumas ações podem exigir senha de gerente.
- Segredos devem ficar em variáveis de ambiente.
- Webhooks devem validar assinatura.

## 13.2 Dados

- IDs principais devem usar UUID.
- Valores monetários devem usar `BigDecimal` no Java e `NUMERIC(12,2)` no PostgreSQL.
- Tabelas principais devem ter `created_at` e `updated_at`.
- Alterações críticas devem gerar auditoria.
- Evitar exclusão física de registros críticos.

## 13.3 Pedidos

- Pedido não pode ser finalizado sem itens.
- Pedido online deve ter cliente.
- Pedido de entrega deve ter endereço.
- Pedido deve guardar snapshot de preço e nome do produto.
- Pedido finalizado não deve permitir alteração simples.

## 13.4 Pagamentos

- Pagamento presencial pode ser confirmado manualmente.
- Pagamento online depende de confirmação do gateway.
- Pagamento online não deve ser confirmado pelo frontend.
- Pagamento misto deve fechar exatamente o total da venda.
- Estorno/cancelamento deve gerar auditoria e ajuste financeiro.

## 13.5 KDS

- Item com preparo deve gerar ticket.
- Item sem preparo não precisa gerar ticket.
- Pedido só fica pronto quando todos os tickets obrigatórios estiverem prontos.
- KDS deve funcionar na rede local sem internet.

## 13.6 Sincronização

- Toda alteração relevante gera evento.
- Evento deve ser idempotente.
- Falha deve gerar retry.
- Evento sincronizado deve registrar data.
- Local manda na disponibilidade real.
- Local manda no estoque físico.
- Online manda em pedidos e pagamentos online até serem recebidos pela loja.

---

# 14. Prioridade para MVP

## 14.1 MVP operacional local

```text
1. Login e permissões básicas.
2. Cadastro de categorias.
3. Cadastro de produtos.
4. Disponibilidade de produto.
5. Abertura de caixa.
6. Venda PDV.
7. Pagamento presencial.
8. Pagamento misto.
9. Impressão de comprovante.
10. Gaveta de dinheiro.
11. Envio para KDS.
12. KDS por setor.
13. Pedido pronto.
14. Fechamento de caixa.
15. Eventos de sincronização pendentes.
```

## 14.2 MVP online

```text
1. Site institucional simples.
2. Cardápio público.
3. Pedido online.
4. Pagamento online ou pagamento na retirada.
5. Webhook de pagamento.
6. Painel online básico.
7. Sync online → local.
8. Sync local → online.
```

## 14.3 MVP técnico

```text
1. PostgreSQL local e online.
2. Flyway.
3. RabbitMQ local e online.
4. Redis local e online.
5. Docker Compose local.
6. Docker Compose online.
7. Nginx.
8. Health checks.
9. Logs estruturados.
10. Backup básico.
```

---

# 15. Pendências de decisão

| Tema | Decisão pendente | Impacto |
|---|---|---|
| NFC-e | Emitir fiscal no MVP ou deixar para fase futura | Alto |
| Pinpad | Integrar no início ou usar maquininha separada | Médio |
| Balança | Integrar produtos por peso no MVP ou fase futura | Médio |
| Estoque | Controlar estoque de produto acabado ou insumos | Alto |
| WhatsApp | Chatbot completo ou atendimento assistido primeiro | Médio |
| Pagamento online | Gateway inicial a escolher | Alto |
| Impressão | API Local direta ou serviço separado de impressão | Médio |
| Multi-loja | Preparar desde início ou focar unidade única | Médio |
| Admin principal | Administração de catálogo local, online ou ambos | Alto |

---

# 16. Recomendações de implementação

## 16.1 Ordem recomendada backend

```text
1. Estrutura modular do projeto.
2. Segurança e usuários.
3. Flyway e banco base.
4. Catálogo.
5. Disponibilidade.
6. Caixa.
7. PDV.
8. Pagamentos presenciais.
9. KDS.
10. Sync events.
11. Workers de sincronização.
12. Pedidos online.
13. Pagamentos online/webhook.
14. WhatsApp.
15. Estoque.
16. Observabilidade.
```

## 16.2 Ordem recomendada frontend

```text
1. Login.
2. Admin local básico.
3. Cadastro de categorias/produtos.
4. Tela PDV.
5. Tela abertura/fechamento de caixa.
6. Tela KDS.
7. Painel de sincronização.
8. Site público/cardápio.
9. Checkout online.
10. Admin online.
```

## 16.3 Ordem recomendada infraestrutura

```text
1. Docker Compose local.
2. PostgreSQL local.
3. RabbitMQ local.
4. Redis local.
5. API local.
6. Nginx local.
7. Backup local.
8. VPS online.
9. Docker Compose online.
10. Nginx + HTTPS.
11. API online.
12. Backups online.
13. Monitoramento.
```

---

# 17. Critérios de aceite gerais

## 17.1 PDV

- Vende sem internet.
- Bloqueia venda sem caixa aberto, se regra ativa.
- Finaliza venda com pagamento válido.
- Gera movimentação de caixa.
- Imprime comprovante.
- Gera ticket KDS quando necessário.
- Cria evento de sincronização.

## 17.2 KDS

- Recebe pedidos locais via WebSocket.
- Separa tickets por setor.
- Permite iniciar preparo.
- Permite marcar pronto.
- Atualiza pedido no PDV.
- Funciona sem internet.

## 17.3 Online

- Exibe apenas produtos disponíveis e habilitados.
- Cria pedido com cliente.
- Exige endereço para entrega.
- Processa pagamento via webhook.
- Deixa pedido aguardando loja se a loja estiver offline.

## 17.4 Sincronização

- Não bloqueia venda local.
- Envia vendas locais para online.
- Baixa pedidos online para local.
- Trata duplicidade.
- Faz retry em falha.
- Exibe pendências em painel.

## 17.5 Auditoria

- Registra ações críticas.
- Registra usuário responsável.
- Registra entidade afetada.
- Registra data/hora.
- Registra motivo quando aplicável.

---

# 18. Conclusão

Este documento transforma a arquitetura e os módulos já definidos em uma visão operacional de fluxos e casos de uso.

A primeira entrega deve priorizar a operação local:

```text
PDV + Caixa + KDS + Banco Local + Impressão + Sync pendente
```

Depois, evoluir para:

```text
Site + Pedidos Online + Pagamento Online + WhatsApp + Sync completo
```

A decisão mais importante continua sendo manter a loja operando presencialmente mesmo sem internet. Todo fluxo crítico de venda, caixa, KDS e impressão deve ser desenhado com essa regra como prioridade.
