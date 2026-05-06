# Roadmap de Desenvolvimento — Sistema MNSS / Nova Aliança

> Documento orientador para desenvolvimento incremental do sistema integrado da Padaria e Lanchonete Nova Aliança, com épicos, sprints, histórias pequenas, desenvolvimento back-end/front-end em paralelo, referências documentais e testes unitários por história.

---

## 1. Premissas usadas no roadmap

1. O sistema será **híbrido: local + online**.
2. A operação crítica da loja fica no ambiente local: **PDV, caixa, KDS, impressão, banco local e eventos pendentes**.
3. O ambiente online atende **site, cardápio, pedidos online, pagamentos, WhatsApp, chatbot, webhooks e sincronização**.
4. O PDV, o KDS e o caixa **não podem depender da internet**.
5. A sincronização será assíncrona, resiliente e idempotente.
6. O desenvolvimento deve ocorrer sempre com **back-end e front-end em paralelo** dentro da mesma sprint.
7. Cada sprint deve entregar uma fatia funcional validável.
8. O MVP deve priorizar primeiro a operação local e depois completar o fluxo online.
9. O back-end deve seguir **arquitetura modular em camadas** dentro do monólito modular: `web -> service -> entity/repository`.
10. O front-end deve seguir **arquitetura Angular por features**, com camadas `domain`, `application`, `data-access`, `ui` e `pages`.

---

## 2. Legenda dos documentos de referência

| Código | Documento | Uso no roadmap |
|---|---|---|
| ARQ | `arquitetura.md` | Decisões arquiteturais local + online, monólito modular, front-end por features, stack, componentes e comunicação. |
| DOM | `modelo-de-dominio.md` | Entidades, agregados, enums e regras de negócio. |
| BD | `banco-de-dados.md` | Schema inicial, Flyway, tabelas, UUID, índices e convenções. |
| SYNC | `sincronizacao.md` | Outbox, inbox, retry, HMAC, push/pull e painel de sincronização. |
| PDV | `pdv.md` | Fluxos do caixa, venda presencial, pagamento, impressão, gaveta, eventos e APIs. |
| KDS | `kds.md` | Tickets por setor, WebSocket, preparo, status e tela da cozinha. |
| FLUXOS | `fluxos-e-casos-de-uso.md` | Fluxos funcionais, casos de uso, matriz de módulos, critérios de aceite e prioridade MVP. |
| DL | `deploy-local.md` | Docker Compose local, Nginx, health checks, backup local e estrutura do servidor. |
| DO | `deploy-online.md` | VPS, Docker Compose online, Nginx, HTTPS, webhooks, CI/CD e backup online. |
| HW | `hardware.md` | Estrutura física, PDV, KDS, impressoras, rede e contingência. |

---

## 3. Épicos do projeto

| Épico | Nome | Objetivo | Prioridade |
|---|---|---|---|
| EP-01 | Fundação técnica | Criar repositório, estrutura modular, builds, padrões e ambiente de execução. | P0 |
| EP-02 | Infra local e online | Subir PostgreSQL, Redis, RabbitMQ, Nginx e apps em Docker Compose local/online. | P0 |
| EP-03 | Segurança e usuários | Autenticação, autorização, perfis, permissões e usuário administrador inicial. | P0 |
| EP-04 | Banco e domínio base | Migrations Flyway, entidades, enums, repositories e auditoria técnica base. | P0 |
| EP-05 | Catálogo e disponibilidade | Categorias, produtos, canais de venda, disponibilidade e administração local/online. | P0 |
| EP-06 | Caixa | Abertura, suprimento, sangria, fechamento e resumo financeiro. | P0 |
| EP-07 | PDV | Carrinho, venda presencial, busca de produtos, código de barras, desconto, pagamento e finalização. | P0 |
| EP-08 | Pagamentos presenciais | Dinheiro, Pix, débito, crédito, voucher e pagamento misto. | P0 |
| EP-09 | Impressão e gaveta | Comprovante, resumo de caixa, sangria, suprimento e acionamento de gaveta. | P0 |
| EP-10 | KDS | Geração de tickets, tela por setor, preparo, pronto e atualização em tempo real. | P0 |
| EP-11 | Sincronização | SyncEvent, outbox/inbox, push local → online, pull online → local, retry e idempotência. | P0 |
| EP-12 | Site e pedido online | Site institucional, cardápio público, carrinho e criação de pedido online. | P0 |
| EP-13 | Pagamento online e webhooks | Integração inicial com gateway, webhook idempotente e atualização de pedido. | P0 |
| EP-14 | Painel operacional | Painel de sincronização, health checks, monitoramento mínimo e logs. | P0 |
| EP-15 | Estoque operacional | Movimentações de estoque, baixa por venda e indisponibilidade automática. | P1 |
| EP-16 | WhatsApp e chatbot | Webhook WhatsApp, atendimento assistido e criação de pedido via conversa. | P1 |
| EP-17 | Hardening e go-live | Segurança final, backup, deploy, testes de operação e homologação da loja. | P0 |
| EP-18 | Evoluções futuras | NFC-e, pinpad, balança, fidelidade, métricas avançadas e multi-PDV. | P2 |

---

## 4. Sequência recomendada de sprints

| Sprint | Foco | Resultado esperado |
|---|---|---|
| Sprint 01 | Fundação técnica | Projeto executável, modular e com pipeline inicial. |
| Sprint 02 | Infra local/online mínima | Serviços base rodando localmente e estrutura da VPS preparada. |
| Sprint 03 | Segurança e usuários | Login, perfis, guards e cadastro básico de usuários. |
| Sprint 04 | Banco e domínio base | Flyway, entidades principais, repositories e seed inicial. |
| Sprint 05 | Catálogo | Categorias/produtos no back-end e telas administrativas no front-end. |
| Sprint 06 | Disponibilidade e cardápio interno | Controle de disponibilidade e listagem filtrada por canal. |
| Sprint 07 | Caixa | Abertura, sangria, suprimento, fechamento e telas correspondentes. |
| Sprint 08 | PDV — venda e carrinho | Criar venda, adicionar/remover itens, alterar quantidade e calcular total. |
| Sprint 09 | Pagamento presencial | Pagamento simples, misto e validações financeiras. |
| Sprint 10 | Finalização, impressão e cancelamento | Finalizar venda, imprimir, acionar gaveta e cancelar venda. |
| Sprint 11 | KDS — tickets e WebSocket | Criar tickets por setor e exibir em tempo real no KDS. |
| Sprint 12 | KDS — preparo e expedição | Iniciar preparo, marcar pronto e refletir no PDV/expedição. |
| Sprint 13 | Sincronização base | SyncEvent, outbox, envio local → online, RECEIVED_BY_STORE e idempotência básica. |
| Sprint 14 | Site e cardápio público | Site institucional, cardápio público e visualização de produtos. |
| Sprint 15 | Pedido online | Checkout, cliente, endereço, pedido online e status inicial. |
| Sprint 16 | Pagamento online e webhook | Criar cobrança, receber webhook, validar assinatura e atualizar pedido. |
| Sprint 17 | Sync online → local e painel | Pull de pedidos online, ACK (RECEIVED_BY_STORE), retry, painel de sync. |
| Sprint 18 | Observabilidade, deploy e homologação | Health checks, backup, deploy local/online e teste fim a fim. |
| Sprint 19 | Estoque básico | Baixa por venda, ajuste manual e disponibilidade automática. |
| Sprint 20 | WhatsApp inicial | Webhook, conversa assistida e pedido via WhatsApp. |

---

# 5. Sprints detalhadas

---

## Sprint 01 — Fundação técnica

**Épicos:** EP-01, EP-04  
**Objetivo:** deixar a base do projeto preparada para evolução modular.

**Referências e motivo:**
- ARQ define monólito modular local + online, monólito modular, front-end por features e estrutura sugerida do repositório.
- README define módulos sugeridos: `local-api`, `online-api`, `sync` e `front-end` Angular unificado por features (`admin`, `site`, `pdv`, `kds`).
- FLUXOS recomenda iniciar por estrutura modular, segurança, Flyway e banco base.

| ID | História pequena | Back-end | Front-end | Testes unitários back-end | Testes unitários front-end | Docs |
|---|---|---|---|---|---|---|
| S01-H01 | Como dev, quero criar o monorepo para organizar back-end, front-end, infra e docs. | Criar estrutura `back-end/`, `infra/`, `docs/`. | Criar estrutura `front-end/` com app Angular único organizado por features `admin`, `site`, `pdv` e `kds`. | Verificar estrutura modular básica e pacotes. | Teste simples validando o shell Angular e as rotas principais por feature. | ARQ, README |
| S01-H02 | Como dev, quero criar o bootstrap da API local. | Criar `local-app` Spring Boot com `/actuator/health` e `/api/ping`. | Criar environment local apontando para API local. | Context load; `/api/ping` retorna 200; profile `local` carrega. | Service HTTP chama `/api/ping` e trata sucesso/erro. | ARQ, DL |
| S01-H03 | Como dev, quero criar o bootstrap da API online. | Criar `online-app` Spring Boot com profile `online`. | Criar environment online para `site-publico` e `admin`. | Context load; profile `online` exige variáveis obrigatórias. | Configuração de environment é carregada corretamente. | ARQ, DO |
| S01-H04 | Como dev, quero padronizar resposta de erro. | Criar `ApiError`, `BusinessException`, `GlobalExceptionHandler`. | Criar componente/shared service para exibir erro padronizado. | Validação retorna 400; regra de negócio retorna código esperado; erro inesperado não vaza stacktrace. | Interceptor converte erro HTTP em mensagem exibível. | FLUXOS |
| S01-H05 | Como dev, quero configurar qualidade mínima. | Adicionar JUnit 5, Mockito, Testcontainers, MapStruct, OpenAPI. | Adicionar ESLint, Prettier, testes Angular. | Build falha com teste quebrado; cobertura mínima configurada. | `ng test` executa; componente base renderiza sem erro. | ARQ |

**Critério de aceite da sprint:** back-end local e online sobem; front-end compila; CI executa testes; estrutura modular existe; novas histórias respeitam monólito modular e front-end por features/camadas.

---

## Sprint 02 — Infra local e online mínima

**Épicos:** EP-02, EP-14, EP-17  
**Objetivo:** preparar containers, variáveis e health checks dos ambientes.

**Referências e motivo:**
- DL define Docker Compose local com API, PostgreSQL, RabbitMQ, Redis e Nginx.
- DO define Docker Compose online, HTTPS, firewall, backups e CI/CD.
- ARQ define que PostgreSQL, RabbitMQ e Redis não devem ser expostos diretamente.

| ID | História pequena | Back-end | Front-end | Testes unitários back-end | Testes unitários front-end | Docs |
|---|---|---|---|---|---|---|
| S02-H01 | Como dev, quero subir infraestrutura local com Docker Compose. | Criar `infra/local/docker-compose.yml`. | Configurar front local para acessar Nginx/API local. | Validar propriedades de conexão por profile; falha rápida sem `DB_HOST`. | Environment local possui URL válida. | DL, ARQ |
| S02-H02 | Como dev, quero subir infraestrutura online base. | Criar `infra/online/docker-compose.yml`. | Configurar URLs online para site/admin. | Profile online exige `JWT_SECRET` e `SYNC_MASTER_SECRET`. | Environment produção não usa localhost. | DO, ARQ |
| S02-H03 | Como operador técnico, quero health check técnico. | Configurar Actuator health para DB, Redis e RabbitMQ. | Criar tela simples de status técnico no admin. | Health retorna `UP` com mocks; retorna `DOWN` quando dependência falha. | Componente mostra `Online`, `Instável`, `Offline`. | DL, DO, SYNC |
| S02-H04 | Como dev, quero Nginx local inicial. | Configurar proxy para API local. | Configurar build estático dos apps locais. | Validação de propriedades de CORS/proxy. | Testar carregamento de base href. | DL |
| S02-H05 | Como admin, quero scripts básicos de backup. | Criar scripts `backup-postgres.sh` para local e online. | Exibir status do último backup futuramente. | Service de metadados de backup calcula status válido/atrasado. | Pipe/formatação de data do último backup. | DL, DO |

**Critério de aceite da sprint:** ambiente local sobe com API, banco, RabbitMQ, Redis e Nginx; ambiente online possui compose base e variáveis.

---

## Sprint 03 — Segurança e usuários

**Épicos:** EP-03, EP-14  
**Objetivo:** permitir login e controle inicial de acesso por perfil.

**Referências e motivo:**
- DOM define `User`, `Role` e perfis `ADMIN`, `GERENTE`, `CAIXA`, `ATENDENTE`, `COZINHA`, `ENTREGADOR`, `CONSULTA`.
- FLUXOS define F03/F04 e UC-001/UC-002 para autenticação e gestão de usuários.
- ARQ exige login por operador, permissões por perfil e logs de auditoria.

| ID | História pequena | Back-end | Front-end | Testes unitários back-end | Testes unitários front-end | Docs |
|---|---|---|---|---|---|---|
| S03-H01 | Como usuário interno, quero fazer login. | Criar endpoint `POST /api/auth/login`. | Criar tela de login. | Login válido retorna token; senha inválida retorna 401; usuário inativo é bloqueado. | Form inválido bloqueia submit; sucesso armazena token; erro exibe mensagem. | DOM, FLUXOS |
| S03-H02 | Como sistema, quero carregar usuário autenticado. | Criar `GET /api/auth/me`. | Criar AuthService e estado do usuário. | Token válido retorna usuário/perfis; token expirado retorna 401. | Guard redireciona não autenticado para login. | FLUXOS |
| S03-H03 | Como admin, quero cadastrar usuário interno. | Criar CRUD básico de usuários. | Criar tela de listagem/criação de usuário. | Username duplicado falha; senha é hasheada; perfil obrigatório. | Form exige campos obrigatórios; lista renderiza usuários. | DOM, FLUXOS |
| S03-H04 | Como admin, quero atribuir perfis. | Criar vínculo `user_roles`. | Criar seleção de perfis no formulário. | Usuário sem perfil operacional é recusado; perfil inexistente falha. | Seleção múltipla mantém estado correto. | DOM, BD |
| S03-H05 | Como gerente, quero permissões para ações críticas. | Criar anotação/validador de permissão. | Criar diretiva/guard por perfil. | Usuário sem permissão recebe 403; admin acessa tudo. | Botão crítico oculta/desabilita sem perfil. | ARQ, FLUXOS |

**Critério de aceite da sprint:** login funcional, usuário admin inicial, guards no front-end e autorização no back-end.

---

## Sprint 04 — Banco e domínio base

**Épicos:** EP-04  
**Objetivo:** criar o schema inicial e as entidades centrais.

**Referências e motivo:**
- BD define PostgreSQL, Flyway, UUID, `NUMERIC(12,2)`, timestamps e tabelas iniciais.
- DOM define agregados: Product, Order, Cash, Customer e Sync.
- FLUXOS reforça regras transversais: UUID, BigDecimal, auditoria e não confiar no front-end.

| ID | História pequena | Back-end | Front-end | Testes unitários back-end | Testes unitários front-end | Docs |
|---|---|---|---|---|---|---|
| S04-H01 | Como dev, quero migrations Flyway iniciais. | Criar migrations `roles`, `users`, `categories`, `products`, `orders`, `payments`, `cash`, `kds`, `sync`, `audit`. | Nenhuma tela nova; apenas preparar mocks de contratos. | Migration executa em banco vazio; migration duplicada falha; tabelas esperadas existem. | Testes de contratos mockados validam modelos TypeScript. | BD |
| S04-H02 | Como dev, quero entidades JPA base. | Criar entidades com UUID, `createdAt`, `updatedAt`, enums e BigDecimal. | Criar interfaces TypeScript correspondentes. | Entidade preenche timestamps; BigDecimal não aceita valor negativo onde proibido. | Modelos TypeScript compilam com campos obrigatórios. | DOM, BD |
| S04-H03 | Como dev, quero repositories base. | Criar repositories principais. | Criar services Angular com métodos vazios/mocks. | Repository persiste e busca entidade com Testcontainers. | Service usa URL correta e método HTTP esperado. | BD |
| S04-H04 | Como sistema, quero seed de roles. | Criar seed de perfis iniciais via Flyway/data initializer. | Exibir perfis disponíveis no cadastro de usuário. | Roles são criados uma única vez; execução repetida não duplica. | Select de roles carrega opções mockadas. | DOM, BD |
| S04-H05 | Como sistema, quero auditoria técnica base. | Criar `AuditLog` e serviço `AuditService`. | Preparar componente futuro de auditoria. | Audit log grava ação, usuário, entidade e timestamp; não aceita ação vazia. | Componente de tabela renderiza logs mockados. | DOM, FLUXOS |

**Critério de aceite da sprint:** banco versionado, entidades principais criadas, repositories testados com banco real de teste.

---

## Sprint 05 — Catálogo: categorias e produtos

**Épicos:** EP-05  
**Objetivo:** cadastrar e consultar categorias/produtos usados por PDV, site, WhatsApp e admin.

**Referências e motivo:**
- DOM define `Product`, `Category`, `UnitType`, `PreparationSector` e regras de canal.
- BD define tabelas `categories`, `products` e índices.
- FLUXOS define F05/F06 e UC-003/UC-004.

| ID | História pequena | Back-end | Front-end | Testes unitários back-end | Testes unitários front-end | Docs |
|---|---|---|---|---|---|---|
| S05-H01 | Como gerente, quero cadastrar categoria. | `POST /api/categories`. | Tela de criação de categoria. | Nome obrigatório; ordem default; categoria ativa por padrão. | Form valida nome; submit chama service; erro aparece. | DOM, BD, FLUXOS |
| S05-H02 | Como gerente, quero editar categoria. | `PATCH /api/categories/{id}`. | Tela/lista com ação editar. | Não edita categoria inexistente; atualiza `updatedAt`; preserva ID. | Modal/form edição popula valores. | DOM, FLUXOS |
| S05-H03 | Como gerente, quero definir visibilidade da categoria por canal. | Campos `showOnline`, `showOnPdv`, `showOnWhatsapp`. | Checkboxes de canal. | Categoria oculta no PDV não aparece em consulta PDV. | Checkboxes refletem estado e atualizam payload. | DOM, FLUXOS |
| S05-H04 | Como gerente, quero cadastrar produto. | `POST /api/products`. | Tela de criação de produto. | Preço obrigatório; categoria deve existir; unidade obrigatória; setor obrigatório. | Form valida preço, categoria, unidade e setor. | DOM, BD, FLUXOS |
| S05-H05 | Como gerente, quero editar produto. | `PATCH /api/products/{id}`. | Tela/lista com filtros e edição. | Alterar preço gera evento `PRODUCT_PRICE_CHANGED`; produto inexistente retorna erro. | Filtro por nome/categoria funciona com dados mockados. | DOM, SYNC |
| S05-H06 | Como operador, quero buscar produto por código de barras. | `GET /api/products/barcode/{barcode}`. | Campo de leitura/código no PDV. | Produto inativo não retorna como vendável; barcode inexistente retorna 404. | Campo captura Enter e chama service. | PDV, DOM |

**Critério de aceite da sprint:** admin cria e edita categorias/produtos; PDV já consegue consultar produtos locais.

---

## Sprint 06 — Disponibilidade e cardápio interno

**Épicos:** EP-05, EP-11  
**Objetivo:** controlar disponibilidade real dos produtos e refletir por canal.

**Referências e motivo:**
- DOM define `ProductAvailability`, `AvailabilityStatus` e `SalesChannel`.
- SYNC define que local manda na disponibilidade real.
- FLUXOS define F07/UC-005.

| ID | História pequena | Back-end | Front-end | Testes unitários back-end | Testes unitários front-end | Docs |
|---|---|---|---|---|---|---|
| S06-H01 | Como atendente, quero marcar produto indisponível. | `PATCH /api/products/{id}/availability`. | Tela de disponibilidade por produto. | Produto indisponível exige motivo conforme status; salva usuário responsável. | Toggle altera estado visual e envia motivo. | DOM, FLUXOS |
| S06-H02 | Como sistema, quero bloquear produto indisponível no online. | Regra de consulta pública filtra indisponíveis. | Preview de produto indisponível no admin. | Produto `UNAVAILABLE` não aparece em `sellOnline`. | Badge “Indisponível” aparece corretamente. | DOM, SYNC |
| S06-H03 | Como operador, quero listar apenas produtos vendáveis no PDV. | `GET /api/pdv/products`. | Grid/lista de produtos no PDV. | Produto inativo é removido; produto sem `sellOnPdv` não aparece. | Lista renderiza produtos agrupados por categoria. | PDV, DOM |
| S06-H04 | Como sistema, quero criar evento de sync ao mudar disponibilidade. | Criar `SyncEvent PRODUCT_UNAVAILABLE/AVAILABLE`. | Exibir indicador “pendente de sincronização”. | Alteração cria evento PENDING; falha não desfaz alteração local. | Indicador renderiza pendente/sincronizado. | SYNC, FLUXOS |
| S06-H05 | Como gerente, quero auditar alteração de disponibilidade. | Criar `AuditLog` na alteração. | Futuro painel mostra histórico. | Audit contém usuário, oldValue, newValue e entityId. | Tabela de histórico renderiza mocks. | DOM, FLUXOS |

**Critério de aceite da sprint:** disponibilidade local funciona, produtos são filtrados por canal e eventos de sync são gerados.

---

## Sprint 07 — Caixa

**Épicos:** EP-06  
**Objetivo:** controlar abertura, movimentações e fechamento do caixa.

**Referências e motivo:**
- PDV define abertura, fechamento, formas de pagamento e APIs de caixa.
- DOM define `CashRegister`, `CashMovement` e regras de caixa.
- FLUXOS define F08, F15, UC-006, UC-014, UC-023 e UC-024.

| ID | História pequena | Back-end | Front-end | Testes unitários back-end | Testes unitários front-end | Docs |
|---|---|---|---|---|---|---|
| S07-H01 | Como operador, quero abrir caixa. | `POST /api/cash-register/open`. | Tela/modal de abertura no PDV. | Valor inicial obrigatório; usuário sem permissão falha; caixa aberto duplicado é bloqueado. | Form exige valor inicial; sucesso libera PDV. | PDV, DOM, FLUXOS |
| S07-H02 | Como operador, quero consultar caixa atual. | `GET /api/cash-register/current`. | Header do PDV mostra caixa aberto/fechado. | Retorna caixa aberto do operador; sem caixa retorna estado vazio. | Header renderiza status corretamente. | PDV |
| S07-H03 | Como gerente, quero registrar sangria. | `POST /api/cash-register/{id}/movement` com `CASH_OUT`. | Botão/tela de sangria. | Motivo obrigatório; valor positivo; caixa fechado bloqueia. | Form valida valor e motivo. | PDV, FLUXOS |
| S07-H04 | Como gerente, quero registrar suprimento. | Movimento `CASH_IN`. | Botão/tela de suprimento. | Valor positivo; usuário autorizado; movimento vinculado ao caixa. | Submit chama endpoint correto. | PDV, FLUXOS |
| S07-H05 | Como operador, quero fechar caixa. | `POST /api/cash-register/{id}/close`. | Tela de fechamento com totais por forma. | Calcula esperado; diferença registrada; caixa fechado não recebe movimento. | Tela calcula diferença visualmente; exige justificativa se divergente. | PDV, FLUXOS |
| S07-H06 | Como operador, quero ver resumo do caixa. | `GET /api/cash-register/{id}/summary`. | Tela/impressão de resumo. | Agrupa por método; inclui sangria/suprimento/diferença. | Tabela de resumo renderiza totais. | PDV |

**Critério de aceite da sprint:** PDV bloqueia venda sem caixa aberto e permite abertura/fechamento com resumo.

---

## Sprint 08 — PDV: venda e carrinho

**Épicos:** EP-07  
**Objetivo:** criar venda presencial e manipular itens no carrinho.

**Referências e motivo:**
- PDV define fluxo principal de venda, APIs `/api/pdv/sales` e regras de produto/carrinho.
- FLUXOS define F09 e UC-007/UC-008/UC-009.
- DOM define `Order`, `OrderItem`, snapshots e status.

| ID | História pequena | Back-end | Front-end | Testes unitários back-end | Testes unitários front-end | Docs |
|---|---|---|---|---|---|---|
| S08-H01 | Como operador, quero iniciar venda. | `POST /api/pdv/sales`. | Botão “Nova venda”. | Sem caixa aberto falha; cria Order origem PDV; status CREATED. | Botão cria venda e inicializa carrinho. | PDV, FLUXOS |
| S08-H02 | Como operador, quero adicionar produto ao carrinho. | `POST /api/pdv/sales/{saleId}/items`. | Click no produto adiciona item. | Produto inativo bloqueia; salva snapshot de nome/preço; total calculado. | Carrinho adiciona item e atualiza subtotal. | DOM, PDV |
| S08-H03 | Como operador, quero alterar quantidade. | `PATCH /api/pdv/sales/{saleId}/items/{itemId}`. | Botões `+`, `-` e campo quantidade. | Quantidade zero/negativa falha; total recalcula; venda finalizada bloqueia. | Quantidade altera total visual. | PDV, DOM |
| S08-H04 | Como operador, quero remover item. | `DELETE /api/pdv/sales/{saleId}/items/{itemId}`. | Botão remover item. | Item inexistente retorna erro; total recalcula; venda finalizada bloqueia. | Item some do carrinho. | FLUXOS |
| S08-H05 | Como operador, quero buscar produto por nome/categoria/código. | Endpoints de busca PDV. | Busca lateral, categorias e input código de barras. | Busca ignora produtos não vendáveis; barcode inexistente retorna aviso. | Busca filtra lista; scanner por Enter adiciona item. | PDV |
| S08-H06 | Como operador, quero ver totais em tempo real. | Retornar subtotal, desconto e total no DTO da venda. | Painel direito do PDV com totais. | Total = soma itens - desconto + taxas; arredondamento monetário correto. | Componente totaliza payload recebido. | DOM, PDV |

**Critério de aceite da sprint:** operador cria venda, adiciona produtos, altera quantidades, remove itens e vê total.

---

## Sprint 09 — Pagamento presencial

**Épicos:** EP-08  
**Objetivo:** registrar pagamento simples e misto com validação de total.

**Referências e motivo:**
- PDV define formas de pagamento e regras de pagamento misto.
- DOM define `PaymentMethod`, `PaymentStatus` e `Payment`.
- FLUXOS define F10, F11 e UC-011.

| ID | História pequena | Back-end | Front-end | Testes unitários back-end | Testes unitários front-end | Docs |
|---|---|---|---|---|---|---|
| S09-H01 | Como operador, quero registrar pagamento em dinheiro. | `POST /api/pdv/sales/{saleId}/payment`. | Tela de pagamento dinheiro e troco. | Valor menor que total falha; troco calculado; Payment `PAID`. | Troco é exibido; valor inválido bloqueia botão. | PDV, DOM |
| S09-H02 | Como operador, quero registrar Pix presencial. | Método `PIX`. | Tela Pix presencial com confirmação manual. | Valor deve bater com total; cria CashMovement. | Confirmação atualiza estado de pagamento. | PDV |
| S09-H03 | Como operador, quero registrar débito/crédito/voucher. | Métodos `DEBIT_CARD`, `CREDIT_CARD`, `MEAL_VOUCHER`. | Seleção de forma de pagamento. | Método inválido falha; valor positivo; status pago. | Select de método monta payload correto. | PDV, DOM |
| S09-H04 | Como operador, quero pagamento misto. | Criar múltiplos `Payment` para uma venda. | Tela com múltiplas formas e saldo restante. | Soma menor/maior que total falha; cada pagamento gera CashMovement. | Saldo restante recalcula; não permite finalizar com saldo aberto. | PDV, FLUXOS |
| S09-H05 | Como sistema, quero movimentação de caixa por pagamento. | Criar `CashMovement SALE`. | Exibir pagamentos associados à venda. | Cada Payment cria uma movimentação; caixa fechado bloqueia. | Lista de pagamentos renderiza por método. | DOM, PDV |

**Critério de aceite da sprint:** venda pode receber pagamento simples ou misto, com total validado e movimentação de caixa.

---

## Sprint 10 — Finalização, impressão e cancelamento

**Épicos:** EP-07, EP-09, EP-14  
**Objetivo:** finalizar venda, imprimir comprovante, abrir gaveta e cancelar com auditoria.

**Referências e motivo:**
- PDV define finalização, impressão local, gaveta e cancelamento.
- FLUXOS define F12, F13, F14 e UC-010/UC-012/UC-013.
- HW recomenda impressora térmica e gaveta ligada à impressora.

| ID | História pequena | Back-end | Front-end | Testes unitários back-end | Testes unitários front-end | Docs |
|---|---|---|---|---|---|---|
| S10-H01 | Como operador, quero finalizar venda paga. | `POST /api/pdv/sales/{saleId}/finish`. | Botão finalizar venda. | Sem itens falha; sem pagamento falha; status final correto. | Botão desabilita sem itens/pagamento. | PDV, FLUXOS |
| S10-H02 | Como sistema, quero criar evento de sync ao finalizar venda. | Criar `SyncEvent` com `idempotencyKey`. | Mostrar status “pendente de sync”. | Status PENDING criado; processed_at nulo; idempotency_key persistida. | Badge de pendência exibido. | PDV, SYNC |
| S10-H03 | Como operador, quero imprimir comprovante. | Criar serviço de impressão. | Botão imprimir/reimprimir comprovante. | Geração de comando não duplica venda; falha de impressão retorna erro controlado. | Estado de impressão mostra sucesso/erro. | PDV, DL, HW |
| S10-H04 | Como operador, quero abrir gaveta em pagamento dinheiro. | Adapter envia comando de gaveta. | Indicação visual de gaveta acionada. | Só aciona gaveta para dinheiro; não aciona para Pix/cartão. | Componente mostra ação apenas quando aplicável. | PDV, HW |
| S10-H05 | Como operador, quero aplicar desconto. | `POST /api/pdv/sales/{saleId}/discount`. | Modal de desconto por valor/percentual. | Limite permitido aplica; acima exige gerente; desconto negativo falha. | Form alterna valor/percentual; calcula preview. | PDV, FLUXOS |
| S10-H06 | Como gerente, quero cancelar venda finalizada. | `POST /api/pdv/sales/{saleId}/cancel`. | Modal motivo + autorização. | Motivo obrigatório; sem permissão falha; gera AuditLog e ajuste financeiro. | Form exige motivo; usuário sem perfil não vê ação. | PDV, FLUXOS |

**Critério de aceite da sprint:** venda finaliza, comprovante é gerado, gaveta é acionada em dinheiro e cancelamento exige controle.

---

## Sprint 11 — KDS: tickets e tempo real

**Épicos:** EP-10  
**Objetivo:** gerar tickets por setor e exibir no KDS em tempo real.

**Referências e motivo:**
- KDS define que a cozinha não depende da internet e recebe atualizações via WebSocket.
- KDS define `KdsTicket`, `KdsTicketItem`, setores, eventos e APIs.
- PDV define que itens com preparo devem ser enviados ao KDS.

| ID | História pequena | Back-end | Front-end | Testes unitários back-end | Testes unitários front-end | Docs |
|---|---|---|---|---|---|---|
| S11-H01 | Como sistema, quero criar tickets KDS ao finalizar venda com preparo. | Serviço agrupa itens por `preparationSector`. | KDS mostra tickets recebidos. | Itens `SEM_PREPARO` não geram ticket; setores diferentes geram tickets diferentes. | Lista renderiza tickets por setor. | KDS, PDV, DOM |
| S11-H02 | Como cozinha, quero filtrar tickets por setor. | `GET /api/kds/tickets?sector=CHAPA`. | Filtro/setor atual no KDS. | Filtro retorna apenas setor; setor inválido falha. | Seleção de setor atualiza lista. | KDS |
| S11-H03 | Como KDS, quero receber ticket via WebSocket. | Publicar evento `KDS_TICKET_CREATED`. | Cliente WebSocket no Angular KDS. | Evento emitido com payload esperado; erro de WebSocket não quebra transação. | Ao receber evento, card é adicionado. | KDS, ARQ |
| S11-H04 | Como produção, quero ver tempo desde criação. | DTO traz `createdAt` e tempo calculável. | Card mostra minutos de espera. | Tempo calculado corretamente; timezone não altera ordem. | Timer atualiza visualmente sem recarregar. | KDS |
| S11-H05 | Como produção, quero layout em colunas. | Endpoint retorna status dos tickets. | Colunas Aguardando, Em preparo, Pronto. | Status mapeado corretamente no DTO. | Ticket aparece na coluna certa. | KDS |

**Critério de aceite da sprint:** vendas com preparo aparecem no KDS por setor e em tempo real.

---

## Sprint 12 — KDS: preparo, pronto e expedição

**Épicos:** EP-10  
**Objetivo:** atualizar status de tickets/itens e refletir pedido pronto no PDV.

**Referências e motivo:**
- KDS define status `WAITING`, `IN_PREPARATION`, `READY`, `FINISHED`, `CANCELED`.
- FLUXOS define F17/F18 e UC-016/UC-017.
- KDS define que pedido só fica pronto quando todos os tickets obrigatórios estiverem prontos.

| ID | História pequena | Back-end | Front-end | Testes unitários back-end | Testes unitários front-end | Docs |
|---|---|---|---|---|---|---|
| S12-H01 | Como produção, quero iniciar preparo do ticket. | `PATCH /api/kds/tickets/{id}/start`. | Botão “Iniciar preparo”. | WAITING → IN_PREPARATION permitido; READY → start bloqueado. | Card muda para coluna Em preparo. | KDS, FLUXOS |
| S12-H02 | Como produção, quero marcar item pronto. | `PATCH /api/kds/items/{id}/ready`. | Botão pronto por item. | Item em preparo vira READY; item cancelado não pode ficar pronto. | Item aparece concluído. | KDS |
| S12-H03 | Como produção, quero marcar ticket pronto. | `PATCH /api/kds/tickets/{id}/ready`. | Botão “Ticket pronto”. | Todos itens ficam READY; `readyAt` preenchido. | Ticket muda para coluna Pronto. | KDS |
| S12-H04 | Como sistema, quero pedido READY quando todos tickets estiverem prontos. | Serviço atualiza `OrderStatus.READY`. | PDV/expedição recebe atualização. | Com um ticket pendente, pedido não fica READY; todos prontos, fica READY. | Banner/alerta no PDV indica pedido pronto. | KDS, FLUXOS |
| S12-H05 | Como expedição, quero finalizar pedido pronto. | Endpoint `finish` para pedido/ticket. | Tela/ação de expedição. | READY → FINISHED permitido; CREATED → FINISHED bloqueado. | Ação some após finalização. | KDS, FLUXOS |

**Critério de aceite da sprint:** produção atualiza preparo, pedido fica pronto corretamente e PDV recebe atualização.

---

## /

## Sprint 14 — Site e cardápio público

**Épicos:** EP-12  
**Objetivo:** publicar site institucional e cardápio online filtrado por disponibilidade.

**Referências e motivo:**
- README/ARQ definem site institucional, cardápio online e ambiente online.
- FLUXOS define F19/UC-018.
- DOM define flags `sellOnline`, categoria ativa e disponibilidade.

| ID | História pequena | Back-end | Front-end | Testes unitários back-end | Testes unitários front-end | Docs |
|---|---|---|---|---|---|---|
| S14-H01 | Como cliente, quero acessar site institucional. | Endpoint/config para dados públicos básicos. | Criar home com informações da padaria. | Endpoint público não exige login; payload básico válido. | Home renderiza nome, seções e CTA. | README, ARQ |
| S14-H02 | Como cliente, quero ver categorias online. | `GET /api/public/menu`. | Tela de cardápio por categoria. | Só categorias ativas e `showOnline=true`. | Categorias aparecem ordenadas. | DOM, FLUXOS |
| S14-H03 | Como cliente, quero ver produtos disponíveis. | Filtro por `active`, `sellOnline`, disponibilidade. | Cards de produto com preço/imagem. | Produto indisponível não aparece; promocional usa preço correto. | Card mostra preço/promocional corretamente. | DOM, FLUXOS |
| S14-H04 | Como cliente, quero buscar produto no cardápio. | Endpoint com filtro ou front filtra lista. | Campo busca no site. | Busca ignora inativos; resultado vazio retorna lista vazia. | Busca filtra por nome. | FLUXOS |
| S14-H05 | Como admin, quero preview do cardápio online. | Reusar endpoint público/admin. | Admin mostra como produto aparece no site. | Preview usa mesmas regras do público. | Preview exibe status e canal. | DOM |

**Critério de aceite da sprint:** cliente visualiza site e cardápio com apenas produtos online disponíveis.

---

## Sprint 15 — Pedido online

**Épicos:** EP-12, EP-11  
**Objetivo:** permitir criação de pedido online para retirada/entrega.

**Referências e motivo:**
- FLUXOS define F20/UC-019.
- DOM define `Customer`, `CustomerAddress`, `Order`, `OrderItem`, `DeliveryType` e regras de pedido online.
- SYNC define que pedido online deve virar pendência para loja.

| ID | História pequena | Back-end | Front-end | Testes unitários back-end | Testes unitários front-end | Docs |
|---|---|---|---|---|---|---|
| S15-H01 | Como cliente, quero adicionar produto ao carrinho online. | Validação server-side no checkout. | Carrinho no site. | Produto indisponível é recusado; preço recalculado no servidor. | Carrinho adiciona/remove item e recalcula visual. | DOM, FLUXOS |
| S15-H02 | Como cliente, quero informar meus dados. | Criar/atualizar `Customer`. | Form de dados do cliente. | Nome e telefone obrigatórios; e-mail inválido falha se informado. | Form valida campos obrigatórios. | DOM |
| S15-H03 | Como cliente, quero informar endereço para entrega. | Criar `CustomerAddress` quando `DELIVERY`. | Form de endereço condicional. | Delivery sem endereço falha; pickup não exige endereço. | Campo endereço aparece apenas em entrega. | DOM, FLUXOS |
| S15-H04 | Como cliente, quero criar pedido online. | `POST /api/public/orders`. | Checkout confirma pedido. | Pedido sem itens falha; snapshot de nome/preço salvo; status inicial correto. | Confirmação mostra número/status. | FLUXOS, DOM |
| S15-H05 | Como sistema, quero criar sync event do pedido online. | Criar `SyncEvent ORDER_CREATED` online. | Mostrar status “aguardando loja”. | Evento PENDING criado; pedido fica SENT_TO_STORE/aguardando envio. | Página de status exibe “aguardando confirmação da loja”. | SYNC, FLUXOS |

**Critério de aceite da sprint:** cliente cria pedido online válido e ele fica aguardando sincronização com a loja.

---

## Sprint 16 — Pagamento online e webhook

**Épicos:** EP-13  
**Objetivo:** controlar pagamento online sem confiar no front-end.

**Referências e motivo:**
- FLUXOS define F21/F22/UC-020.
- DO define webhooks com assinatura, payload bruto e processamento assíncrono.
- DOM define que pagamento online depende de confirmação do gateway.

| ID | História pequena | Back-end | Front-end | Testes unitários back-end | Testes unitários front-end | Docs |
|---|---|---|---|---|---|---|
| S16-H01 | Como cliente, quero escolher pagamento online. | `POST /api/public/payments/online`. | Opção Pix/cartão online no checkout. | Cria Payment PENDING; pedido fica PAYMENT_PENDING. | Seleção de método altera etapa de pagamento. | DOM, FLUXOS |
| S16-H02 | Como sistema, quero integrar gateway de pagamento. | Interface `PaymentGatewayService`. | Tela mostra instruções/dados retornados. | Integração mock retorna cobrança; falha do gateway retorna erro controlado. | Componente renderiza QR/instruções mockadas. | FLUXOS |
| S16-H03 | Como gateway, quero enviar webhook. | `POST /api/public/payments/webhook`. | Página de status consulta pedido. | Assinatura inválida falha; payload bruto é registrado; webhook duplicado é idempotente. | Status muda conforme polling/mock. | DO, FLUXOS |
| S16-H04 | Como sistema, quero aprovar pagamento por webhook. | Atualizar `PaymentStatus.PAID` e `Order.PAID`. | Página de pedido mostra pago. | Front-end não consegue confirmar pagamento; só webhook altera para PAID. | Status “Pago” aparece após atualização. | DOM, FLUXOS |
| S16-H05 | Como sistema, quero tratar pagamento recusado/expirado. | Atualizar para REFUSED/EXPIRED. | Mostrar instrução para nova tentativa. | Status recusado não cria envio para loja; expirado bloqueia pagamento antigo. | Mensagem de recusado/expirado aparece. | DOM, FLUXOS |

**Critério de aceite da sprint:** pagamento online é criado, webhook atualiza status e duplicidade é bloqueada.

---

## Sprint 17 — Sync online → local e painel de sincronização

**Épicos:** EP-11, EP-14  
**Objetivo:** baixar pedidos online para a loja e monitorar falhas.

**Referências e motivo:**
- SYNC define pull online → local porque a loja pode estar atrás de NAT.
- SYNC define painel com eventos pendentes, erro, retry, reprocessar e ignorar.
- KDS define que pedidos online sincronizados devem gerar tickets locais.

| ID | História pequena | Back-end | Front-end | Testes unitários back-end | Testes unitários front-end | Docs |
|---|---|---|---|---|---|---|
| S17-H01 | Como worker local, quero buscar pendências online. | `GET /api/sync/pending?storeId=...`. | Painel mostra pedidos aguardando loja. | Retorna apenas eventos da loja; sem assinatura válida falha. | Lista de pendências renderiza. | SYNC |
| S17-H02 | Como API local, quero processar pedido online recebido. | Gravar Order/Items/Payment local. | PDV mostra pedido online recebido. | Evento duplicado não cria pedido duplicado; cliente/endereço persistem. | Pedido aparece na lista local. | SYNC, DOM |
| S17-H03 | Como sistema, quero confirmar recebimento. | `POST /api/sync/events/{id}/ack`. | Status muda para recebido pela loja. | ACK marca online como RECEIVED_BY_STORE; ACK duplicado é seguro. | Badge de status atualiza. | SYNC |
| S17-H04 | Como sistema, quero gerar KDS para pedido online. | Criar tickets locais se houver preparo. | KDS exibe pedido origem SITE/WHATSAPP. | Pedido online com setores gera tickets; sem preparo não gera. | Card mostra origem correta. | KDS, SYNC |
| S17-H05 | Como gerente, quero painel de sincronização. | `GET /api/sync/status` e listagem por status. | Tela com pendentes, erros e retries. | Filtra PENDING/FAILED; mensagem de erro retornada. | Filtros funcionam; cards mostram contadores. | SYNC, FLUXOS |
| S17-H06 | Como gerente, quero reprocessar ou ignorar evento. | Endpoints reprocessar/ignorar com auditoria. | Botões “Reprocessar” e “Ignorar”. | Sem permissão falha; ignorar exige motivo; reprocessar altera status. | Modal exige motivo para ignorar. | SYNC, FLUXOS |

**Critério de aceite da sprint:** pedidos online chegam ao local por pull, são idempotentes, geram KDS e aparecem no painel.

---

## Sprint 18 — Observabilidade, deploy e homologação MVP

**Épicos:** EP-14, EP-17  
**Objetivo:** preparar operação real com health checks, backup, deploy e validação fim a fim.

**Referências e motivo:**
- DL e DO definem health checks, backup, logs, Docker Compose, Nginx e checklist.
- FLUXOS define F30 e critérios gerais de aceite.
- ARQ define segurança local/online e comunicação segura.

| ID | História pequena | Back-end | Front-end | Testes unitários back-end | Testes unitários front-end | Docs |
|---|---|---|---|---|---|---|
| S18-H01 | Como admin, quero health check operacional local. | `GET /api/sync/health` com DB/Rabbit/Redis/sync. | Painel operacional local. | Health reflete dependência falha; pendências são contadas. | Painel renderiza status e alertas. | DL, SYNC |
| S18-H02 | Como admin, quero health check online. | Health online com certificado/webhooks/eventos. | Painel online simples. | Webhook indisponível afeta status; DB down retorna DOWN. | Tela exibe status online. | DO, FLUXOS |
| S18-H03 | Como dev, quero pipeline de build e deploy. | Build, test, Docker image e push. | Build Angular e publicação estática. | Pipeline falha com testes quebrados; imagem recebe tag. | Build front falha com lint/test quebrado. | DO |
| S18-H04 | Como operador, quero validar fluxo fim a fim local. | Script/cenário: login → caixa → venda → pagamento → impressão → KDS → sync pending. | Roteiro de homologação no front. | Testes de serviços cobrem cada etapa principal. | Testes de componentes cobrem fluxo simulado. | PDV, KDS, FLUXOS |
| S18-H05 | Como admin, quero validar fluxo online fim a fim. | Site → pedido → pagamento → webhook → sync → KDS. | Roteiro de homologação site/admin. | Webhook + sync idempotente em sequência. | Checkout e status renderizam etapas. | FLUXOS, SYNC, DO |
| S18-H06 | Como suporte, quero documentação de implantação. | Atualizar README e scripts. | Tela/links de status e versão. | Endpoint `/version` retorna versão/commit. | Footer mostra versão. | DL, DO |

**Critério de aceite da sprint:** MVP homologável com fluxo local e online, deploy documentado e health checks funcionando.

---

## Sprint 19 — Estoque básico

**Épicos:** EP-15  
**Objetivo:** registrar movimentações de estoque e refletir disponibilidade.

**Referências e motivo:**
- DOM define `StockMovement` e regra de estoque físico local.
- FLUXOS define F27 e prioriza estoque como pós-MVP inicial.
- SYNC define que local manda no estoque físico e online recebe reflexo.

| ID | História pequena | Back-end | Front-end | Testes unitários back-end | Testes unitários front-end | Docs |
|---|---|---|---|---|---|---|
| S19-H01 | Como gerente, quero registrar entrada de estoque. | Criar `StockMovement IN`. | Tela de ajuste de estoque. | Quantidade positiva; produto obrigatório; usuário obrigatório. | Form valida produto/quantidade. | DOM, FLUXOS |
| S19-H02 | Como gerente, quero registrar perda. | Criar `StockMovement LOSS`. | Ação “perda”. | Motivo obrigatório; movimento audita usuário. | Modal exige motivo. | DOM |
| S19-H03 | Como sistema, quero baixar estoque por venda. | Ao finalizar venda, gerar `SALE`. | Mostrar estoque estimado no admin. | Venda baixa quantidade correta; venda cancelada ajusta conforme regra. | Coluna estoque renderiza quantidade. | DOM, FLUXOS |
| S19-H04 | Como sistema, quero indisponibilizar produto sem estoque. | Regra automática atualiza availability. | Badge “acabou”. | Estoque zero cria `PRODUCT_UNAVAILABLE`; sob encomenda não bloqueia. | Badge atualiza no catálogo. | DOM, SYNC |
| S19-H05 | Como sistema, quero sincronizar estoque/disponibilidade. | Criar `STOCK_MOVED` e `PRODUCT_UNAVAILABLE`. | Painel mostra pendente de sync. | Evento criado e idempotente. | Status de sync aparece. | SYNC |

**Critério de aceite da sprint:** estoque local registra movimentos e pode bloquear disponibilidade online via sincronização.

---

## Sprint 20 — WhatsApp inicial e atendimento assistido

**Épicos:** EP-16  
**Objetivo:** iniciar pedidos via WhatsApp com atendimento assistido antes de chatbot completo.

**Referências e motivo:**
- FLUXOS define F23 como pós-MVP inicial.
- DO define webhooks externos e validação de assinatura.
- DOM define origem `WHATSAPP` e canal `sellOnWhatsapp`.

| ID | História pequena | Back-end | Front-end | Testes unitários back-end | Testes unitários front-end | Docs |
|---|---|---|---|---|---|---|
| S20-H01 | Como provider, quero enviar webhook WhatsApp. | `POST /api/whatsapp/webhook`. | Painel lista conversas recebidas. | Assinatura inválida falha; mensagem duplicada é ignorada. | Lista renderiza conversa e última mensagem. | DO, FLUXOS |
| S20-H02 | Como atendente, quero ver produtos habilitados para WhatsApp. | Endpoint filtra `sellOnWhatsapp`. | Busca produto dentro da conversa. | Produto sem canal WhatsApp não aparece. | Busca filtra catálogo WhatsApp. | DOM, FLUXOS |
| S20-H03 | Como atendente, quero montar pedido assistido. | Criar pedido origem `WHATSAPP`. | Carrinho assistido no admin/WhatsApp. | Pedido exige cliente/telefone; snapshot salvo. | Carrinho assistido adiciona itens. | DOM, FLUXOS |
| S20-H04 | Como cliente, quero receber confirmação. | Adapter de envio de mensagem. | Botão enviar resumo. | Falha do provider não cancela pedido; erro é registrado. | Preview do resumo formatado. | FLUXOS |
| S20-H05 | Como sistema, quero sincronizar pedido WhatsApp para loja. | Criar SyncEvent ORDER_CREATED. | Status “aguardando loja”. | Evento idempotente; ACK atualiza status. | Status renderiza corretamente. | SYNC, FLUXOS |

**Critério de aceite da sprint:** mensagens chegam, atendente monta pedido e o pedido segue o mesmo fluxo de sync/KDS.

---

# 6. Matriz resumida por épico, sprint e documentos

| Épico | Sprints | Documentos base | Motivo |
|---|---|---|---|
| Fundação técnica | 01 | ARQ, README, FLUXOS | Base modular e ordem de implementação. |
| Infra local/online | 02, 18 | DL, DO, ARQ | Ambientes local e online em Docker/Nginx/health. |
| Segurança | 03 | DOM, FLUXOS, ARQ | Usuários, perfis, autorização e ações críticas. |
| Banco/domínio | 04 | BD, DOM | Schema, entidades, UUID, BigDecimal e Flyway. |
| Catálogo | 05, 06 | DOM, BD, FLUXOS | Categorias, produtos, canais e disponibilidade. |
| Caixa | 07 | PDV, DOM, FLUXOS | Abertura, movimentos, fechamento e resumo. |
| PDV | 08, 09, 10 | PDV, DOM, FLUXOS | Venda presencial, pagamento, impressão e cancelamento. |
| KDS | 11, 12 | KDS, PDV, FLUXOS | Tickets, setores, WebSocket, preparo e pronto. |
| Sincronização | 13, 17 | SYNC, ARQ, FLUXOS | Outbox, inbox, retry, HMAC, push/pull. |
| Site e pedidos online | 14, 15 | ARQ, DOM, FLUXOS | Cardápio, cliente, checkout e pedido online. |
| Pagamento online | 16 | DOM, DO, FLUXOS | Gateway, webhook, idempotência e status. |
| Estoque | 19 | DOM, SYNC, FLUXOS | Estoque físico local e disponibilidade online. |
| WhatsApp | 20 | DO, DOM, FLUXOS | Webhook, atendimento assistido e pedidos por canal. |

---

# 7. Critérios gerais de pronto por história

Uma história só deve ser considerada pronta quando cumprir todos os itens abaixo:

```text
[ ] Back-end implementado
[ ] Front-end correspondente implementado
[ ] Validações críticas no back-end
[ ] Testes unitários de regra de negócio
[ ] Testes unitários de componente/service no front-end
[ ] Contrato de API validado
[ ] Erros tratados com payload padronizado
[ ] Permissões aplicadas quando necessário
[ ] Auditoria criada quando a ação for crítica
[ ] Evento de sincronização criado quando aplicável
[ ] Documentação da API atualizada quando houver endpoint novo
```

---

# 8. Testes unitários mínimos por camada

## 8.1 Back-end

| Camada | Testar |
|---|---|
| Domain/UseCase | Regras de negócio, status, cálculos, transições e validações. |
| Service | Fluxos com mocks de repositories, workers, adapters e ports. |
| Controller | Status HTTP, payload de erro, validação de request e autorização. |
| Mapper | Conversão entity ↔ DTO sem perda de campos críticos. |
| Security | Token, perfil, usuário inativo, permissão ausente e ação crítica. |
| Sync | Idempotência, assinatura HMAC, retry, duplicidade e falha controlada. |
| Payment | Pagamento simples, misto, webhook duplicado, recusado e expirado. |
| Cash | Abertura duplicada, fechamento, diferença, caixa fechado e movimentos. |
| KDS | Agrupamento por setor, transição de status e pedido pronto. |

## 8.2 Front-end

| Camada | Testar |
|---|---|
| Component | Renderização, estado visual, botões, mensagens e validações. |
| Service | URL correta, método HTTP, payload e tratamento de erro. |
| Guard | Usuário autenticado, perfil permitido e redirecionamento. |
| Interceptor | Inclusão de token, tratamento de 401/403/500. |
| Form | Campos obrigatórios, máscaras, mensagens e bloqueio de submit inválido. |
| PDV UI | Carrinho, totalização, pagamento, desconto e finalização. |
| KDS UI | Colunas, WebSocket, movimentação de card e status. |
| Site UI | Cardápio, carrinho, checkout e status do pedido. |

---

# 9. Recomendações de execução

1. Trabalhar cada sprint com um contrato claro de API antes de codificar a tela.
2. Na mesma sprint, implementar primeiro o DTO/endpoint mínimo e depois a tela correspondente.
3. Não deixar front-end avançar com mock por muitas sprints sem back-end real.
4. Priorizar testes unitários de regras críticas: dinheiro, caixa, pagamento, cancelamento, sync e KDS.
5. Usar Testcontainers para validar migrations e repositories, mesmo que isso seja classificado como teste de integração.
6. Manter stories pequenas: cada história deve ser possível de desenvolver e testar isoladamente.
7. Nunca confiar apenas no front-end para regra de negócio crítica.
8. Toda ação crítica deve gerar auditoria.
9. Toda alteração que precisa viajar entre local e online deve gerar `SyncEvent`.
10. O sistema local deve continuar vendendo mesmo quando o online estiver indisponível.

---

# 10. Corte mínimo para MVP operacional

O MVP operacional pode ser considerado suficiente quando estes blocos estiverem prontos:

```text
[ ] Login e permissões básicas
[ ] Categorias e produtos
[ ] Disponibilidade local
[ ] Caixa: abrir, sangria, suprimento, fechar
[ ] PDV: criar venda, itens, pagamento, finalizar
[ ] Impressão de comprovante
[ ] KDS: tickets, preparo e pronto
[ ] SyncEvent pendente local
[ ] Envio local → online
[ ] Site/cardápio público
[ ] Pedido online
[ ] Pagamento online/webhook ou pagamento na retirada
[ ] Pull online → local
[ ] Painel de sincronização
[ ] Health checks e backup
```

---

# 11. Pendências que devem ser decididas antes das sprints finais

| Tema | Decisão necessária | Sprint limite sugerida |
|---|---|---|
| Gateway de pagamento | Mercado Pago, Asaas, Pagar.me, Stripe ou outro. | Sprint 15 |
| Impressão | API local direta ou serviço separado de impressão. | Sprint 09 |
| NFC-e | MVP ou fase futura. | Sprint 10 |
| Pinpad | Maquininha manual no MVP ou integração. | Sprint 09 |
| Estoque | Produto acabado, insumo ou ambos. | Sprint 18 |
| WhatsApp | Atendimento assistido primeiro ou chatbot completo. | Sprint 19 |
| Admin principal | Catálogo editado local, online ou ambos. | Sprint 06 |
| Multi-PDV | Preparar desde já ou deixar para evolução. | Sprint 10 |

---

# 12. Ordem prática de desenvolvimento dentro de cada sprint

Para cada sprint, seguir a sequência:

```text
1. Refinar contrato da API.
2. Criar/ajustar migration, se houver banco.
3. Criar DTOs e modelos TypeScript.
4. Implementar use case/service no back-end.
5. Criar controller/endpoints.
6. Criar service Angular.
7. Criar tela/componente correspondente.
8. Escrever testes unitários back-end.
9. Escrever testes unitários front-end.
10. Rodar fluxo manual mínimo.
11. Atualizar documentação se necessário.
```

---

# 13. Conclusão

Este roadmap prioriza primeiro o núcleo operacional local da loja: **PDV + caixa + KDS + impressão + banco local + sync pendente**. Em seguida, evolui para **site, pedido online, pagamento online, webhooks e sincronização completa**. Essa ordem reduz risco operacional porque a loja consegue vender presencialmente mesmo sem internet, enquanto os canais online evoluem sobre uma base local já estável.
