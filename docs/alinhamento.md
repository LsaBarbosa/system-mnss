

# Backlog de Alinhamento — Sistema MNSS / Nova Aliança

## Objetivo

Corrigir as divergências encontradas entre documentação e código da branch `main`, sem mudar decisões agora oficializadas:

* manter Java 21;
* abandonar exigência de arquitetura hexagonal;
* manter o modelo evoluído de sincronização já presente no código;
* preservar o monólito modular local + online;
* manter operação local como prioridade do MVP;
* tornar documentação, OpenAPI, deploy e código coerentes entre si.

---

# 1. Épicos

| Épico | Nome                                   | Objetivo                                                                 | Prioridade |
| ----- | -------------------------------------- | ------------------------------------------------------------------------ | ---------- |
| EP-01 | Consolidação das decisões oficiais     | Atualizar docs para Java 21, arquitetura em camadas e sync evoluído      | P0         |
| EP-02 | Padronização estrutural do repositório | Resolver divergência `back-end/front-end` vs `back-end/front-end`          | P0         |
| EP-03 | Contratos OpenAPI                      | Corrigir contratos divergentes dos DTOs reais                            | P0         |
| EP-04 | Sincronização evoluída                 | Oficializar e completar o modelo atual de sync                           | P0         |
| EP-05 | Segurança e autorização                | Alinhar autenticação/autorização, especialmente KDS e endpoints críticos | P0         |
| EP-06 | Infra e deploy realista                | Completar Docker Compose local/online com API, Nginx e front-ends         | P0         |
| EP-07 | KDS e WebSocket                        | Padronizar rotas REST, eventos WebSocket e permissões                    | P0         |
| EP-08 | Qualidade, testes e CI                 | Aumentar confiabilidade dos testes, cobertura e build                    | P1         |
| EP-09 | Limpeza técnica                        | Remover duplicidades, defaults perigosos e inconsistências menores       | P1         |

---

# 2. Sprint 01 — Alinhamento documental e decisões oficiais

## Objetivo

Transformar as decisões atuais em documentação oficial para evitar que o projeto continue sendo avaliado contra regras antigas.

## Histórias

### S01-H01 — Atualizar stack oficial para Java 21

**Problema atual:** `AGENTS.md` cita Java 25, mas o build usa Java 21 no Gradle. O `build.gradle` já configura `JavaLanguageVersion.of(21)` .

**Tarefas:**

* Alterar `AGENTS.md`.
* Alterar `docs/arquitetura.md`.
* Alterar `docs/roadmap-sprints-sistema-mnss.md`.
* Alterar `docs/guia-codex-cli-implementacao-historias-sistema-mnss.md`.
* Alterar `docs/prompts-chatgpt-ui-codigo-copia-cola-sistema-mnss.md`.
* Garantir que toda referência a Java 25 vire Java 21.
* Garantir que exemplos de build usem Gradle e Java 21.

**Critérios de aceite:**

* Nenhum documento orientador cita Java 25.
* Documentação e `build.gradle` estão coerentes.
* A decisão oficial fica explícita:

```text
Back-end: Java 21 + Spring Boot 3.x
```

---

### S01-H02 — Substituir arquitetura hexagonal por arquitetura modular em camadas

**Problema atual:** a documentação exige arquitetura hexagonal, mas o código usa Spring/JPA em pacotes por domínio, como `localapp.domain.catalog.ProductEntity` com anotações JPA  e `PdvSaleService` com `@Service`, transações, repositories e integrações .

**Nova decisão oficial:**

```text
Monólito modular em camadas por domínio
```

**Estrutura recomendada:**

```text
back-end/local-app/src/main/java/.../localapp/
├── catalog/
│   ├── web/
│   ├── service/
│   ├── entity/
│   ├── repository/
│   └── dto/
├── pdv/
│   ├── web/
│   ├── service/
│   ├── dto/
│   └── mapper/
├── cash/
├── kds/
├── sync/
├── security/
└── shared/
```

**Tarefas:**

* Remover dos documentos a exigência:

```text
adapter -> application -> domain
```

* Remover a regra:

```text
domínio e aplicação não podem depender de Spring/JPA
```

* Substituir por:

```text
Controllers não devem conter regra de negócio.
Services concentram regra de aplicação.
Entities representam persistência e estado de domínio.
Repositories ficam isolados por módulo.
DTOs não devem expor entidades diretamente.
```

**Critérios de aceite:**

* Documentação não exige mais arquitetura hexagonal.
* O padrão oficial passa a ser arquitetura modular em camadas.
* O código atual deixa de ser considerado divergente por usar Spring/JPA em entidades.

---

### S01-H03 — Oficializar o modelo evoluído de sincronização

**Problema atual:** documentação antiga fala em `event_type`, `entity_type`, `entity_id`, `origin`, `destination`, `synced_at`. O código usa `idempotency_key`, `direction`, `source_environment`, `target_environment`, `aggregate_type`, `aggregate_id`, `next_retry_at`, `last_error`, `processed_at` .

**Tarefas:**

* Atualizar `docs/sincronizacao.md`.
* Atualizar `docs/banco-de-dados.md`.
* Atualizar roadmap.
* Atualizar OpenAPI de sync.
* Documentar o schema oficial:

```text
sync_events
- id
- idempotency_key
- direction
- source_environment
- target_environment
- aggregate_type
- aggregate_id
- event_type
- payload
- status
- retry_count
- next_retry_at
- last_error
- processed_at
- created_at
- updated_at
- version
```

**Critérios de aceite:**

* Documentação de sync passa a bater com `SyncEventEntity`.
* Documentação explica o motivo do modelo evoluído.
* Não há mais referência contraditória ao modelo antigo.

---

### S01-H04 — Definir padrão final de nomes de pastas

**Problema atual:** documentação espera `back-end/front-end`, mas o repositório usa `back-end/front-end`; o README atual já descreve `back-end` e `front-end` .

**Decisão recomendada:** manter o que já existe no repo:

```text
back-end/
front-end/
infra/
docs/
```

**Tarefas:**

* Atualizar todos os documentos que citam `back-end/`.
* Atualizar todos os documentos que citam `front-end/`.
* Atualizar prompts do Codex.
* Atualizar comandos de build/teste.
* Atualizar exemplos de caminhos.

**Critérios de aceite:**

* Nenhum documento usa caminho diferente do repositório real.
* `README.md`, `AGENTS.md` e roadmap usam a mesma estrutura.

---

# 3. Sprint 02 — Contratos OpenAPI e DTOs reais

## Objetivo

Fazer os contratos OpenAPI refletirem exatamente o que os controllers e DTOs retornam.

---

### S02-H01 — Corrigir contrato de `/api/health`

**Problema atual:** OpenAPI define `checkedAt`, mas o DTO real `TechnicalHealthResponse` retorna `timestamp` e `version` .

**Tarefas:**

* Escolher um único contrato.
* Recomendo manter o DTO atual e atualizar OpenAPI para:

```yaml
HealthResponse:
  required:
    - status
    - environment
    - offlineCriticalOperation
    - message
    - version
    - timestamp
    - components
```

* Atualizar `docs/contracts/local-api.openapi.yml`.
* Atualizar `docs/contracts/online-api.openapi.yml`, se existir.
* Atualizar testes de contrato.

**Critérios de aceite:**

* Payload real e OpenAPI iguais.
* Front-end consome o campo correto.
* Teste de contrato cobre `/api/health`.

---

### S02-H02 — Revisar contrato completo do PDV

**Tarefas:**

* Comparar `PdvSaleController` com OpenAPI.
* Validar endpoints reais:

```text
POST   /api/pdv/sales
GET    /api/pdv/sales
GET    /api/pdv/sales/{saleId}
POST   /api/pdv/sales/{saleId}/items
PATCH  /api/pdv/sales/{saleId}/items/{itemId}
DELETE /api/pdv/sales/{saleId}/items/{itemId}
POST   /api/pdv/sales/{saleId}/payment
POST   /api/pdv/sales/{saleId}/finish
POST   /api/pdv/sales/{saleId}/discount
POST   /api/pdv/sales/{saleId}/cancel
POST   /api/pdv/sales/{saleId}/print
```

O controller atual expõe esses endpoints em `/api/pdv/sales` .

**Critérios de aceite:**

* OpenAPI documenta todos os endpoints reais.
* Front-end usa somente endpoints documentados.
* Contrato diferencia pagamento por `/api/pdv/sales/{saleId}/payment` e `/api/orders/{orderId}/payments`, se ambos continuarem existindo.

---

### S02-H03 — Revisar contrato completo do KDS

**Tarefas:**

* Documentar endpoints reais do `KdsController`:

```text
GET   /api/kds/tickets
PATCH /api/kds/tickets/{id}/start
PATCH /api/kds/tickets/{id}/ready
PATCH /api/kds/tickets/{id}/finish
PATCH /api/kds/items/{id}/ready
PATCH /api/kds/orders/{id}/finish
```

O controller atual usa exatamente essa base `/api/kds` .

**Critérios de aceite:**

* OpenAPI cobre KDS.
* Front-end KDS consome contrato oficial.
* Eventos WebSocket também aparecem na documentação.

---

### S02-H04 — Criar validação automática de OpenAPI no build

**Tarefas:**

* Adicionar teste que carrega arquivos OpenAPI.
* Validar sintaxe.
* Validar rotas essenciais.
* Opcional: usar `springdoc-openapi` para comparar endpoints gerados com contrato versionado.

**Critérios de aceite:**

* Build falha se OpenAPI estiver inválido.
* Rotas principais de PDV, KDS, cash, auth, sync e health são verificadas.

---

# 4. Sprint 03 — Segurança e autorização

## Objetivo

Corrigir gaps de autorização e consolidar padrão de segurança.

---

### S03-H01 — Definir oficialmente Spring Security ou interceptor customizado

**Situação atual:** o código usa interceptor customizado `AuthenticatedUserInterceptor`, aplicado em `/api/**` com exceções para login, health, ping e público .

**Recomendação:** como a documentação cita Spring Security como stack, implementar Spring Security gradualmente.

**Tarefas:**

* Adicionar `spring-boot-starter-security`.
* Criar `SecurityFilterChain`.
* Migrar autenticação bearer do interceptor para filtro.
* Manter `@RequiresRole` ou trocar por `@PreAuthorize`.
* Atualizar testes.

**Critérios de aceite:**

* Login continua funcionando.
* `/api/auth/login`, `/api/ping`, `/api/health`, `/api/public/**` continuam públicos.
* Demais endpoints exigem autenticação.
* Teste cobre 401 e 403.

---

### S03-H02 — Proteger KDS por perfil

**Problema atual:** `KdsController` não possui `@RequiresRole`. Portanto, qualquer usuário autenticado pode manipular tickets.

**Tarefas:**

* Aplicar autorização:

```java
@RequiresRole({RoleName.ADMIN, RoleName.GERENTE, RoleName.COZINHA})
```

* Validar se `ATENDENTE` pode apenas consultar.
* Separar permissões:

```text
GET /api/kds/tickets -> ADMIN, GERENTE, COZINHA, ATENDENTE
PATCH start/ready/finish -> ADMIN, GERENTE, COZINHA
PATCH orders/finish -> ADMIN, GERENTE, EXPEDICAO, COZINHA
```

**Critérios de aceite:**

* Usuário sem perfil correto recebe 403.
* Teste cobre acesso permitido e negado.

---

### S03-H03 — Revisar permissões de ações críticas

**Tarefas:**

* Cancelamento de venda.
* Desconto acima do limite.
* Sangria.
* Suprimento.
* Fechamento de caixa.
* Reimpressão.
* Ajuste de estoque.
* Alteração de disponibilidade.

**Critérios de aceite:**

* Cada ação crítica tem regra de perfil no back-end.
* Toda ação crítica gera auditoria quando aplicável.
* Testes negativos existem.

---

### S03-H04 — Remover defaults sensíveis de produção

**Problema atual:** `application.yml` possui defaults como `admin123`, `mnss`, `local-dev-sync-secret-change-me` .

**Tarefas:**

* Criar profile `dev`.
* Mover defaults frágeis para `application-dev.yml`.
* Em `application.yml`, exigir variáveis obrigatórias.
* Falhar rápido se secret estiver ausente em `local`, `online` ou `prod`.

**Critérios de aceite:**

* Produção não sobe com `admin123`.
* Produção não sobe com secret default.
* Ambiente dev continua simples.

---

# 5. Sprint 04 — Sincronização evoluída

## Objetivo

Completar a sincronização usando o modelo evoluído já presente no código.

---

### S04-H01 — Consolidar schema oficial de `sync_events`

**Tarefas:**

* Revisar migrations `V1`, `V2`, `V4`.
* Garantir que `sync_events` tenha:

```text
id
idempotency_key
direction
source_environment
target_environment
aggregate_type
aggregate_id
event_type
payload
status
retry_count
next_retry_at
last_error
processed_at
created_at
updated_at
version
```

A migration inicial já cria grande parte desses campos  e a `V4` adiciona `version` e índice de outbox .

**Critérios de aceite:**

* Entidade e banco têm os mesmos campos.
* Hibernate `ddl-auto=validate` passa.
* Índices existem para worker de outbox.

---

### S04-H02 — Padronizar enum de status de sync

**Tarefas:**

* Revisar status atuais.
* Definir lista oficial:

```text
PENDING
PROCESSING
SYNCED
FAILED
RETRYING
IGNORED
DEAD_LETTER
RECEIVED_BY_STORE
```

**Critérios de aceite:**

* Documentação e enum batem.
* Front-end conhece todos os status.
* Painel de sync exibe status corretamente.

---

### S04-H03 — Garantir idempotência no recebimento online

**Tarefas:**

* Validar `idempotency_key`.
* Criar regra: evento repetido retorna sucesso sem reprocessar.
* Registrar evento recebido antes do processamento crítico.
* Cobrir com teste.

**Critérios de aceite:**

* Mesmo evento não duplica pedido, pagamento ou venda.
* Teste simula reenvio do mesmo evento.

---

### S04-H04 — Completar retry e dead letter

**Tarefas:**

* Usar `next_retry_at`.
* Incrementar `retry_count`.
* Marcar `DEAD_LETTER` após limite.
* Expor falhas no endpoint/painel de sync.

**Critérios de aceite:**

* Worker processa eventos pendentes.
* Worker reprocessa falhas no horário correto.
* Falhas permanentes aparecem para operação.

---

### S04-H05 — Atualizar OpenAPI de sync

**Tarefas:**

* Documentar headers:

```http
X-Store-Id
X-Signature
X-Event-Id
X-Event-Timestamp
```

* Documentar payload evoluído.
* Documentar respostas idempotentes.

**Critérios de aceite:**

* Local → Online documentado.
* Online → Local documentado.
* Front/back seguem o mesmo contrato.

---

# 6. Sprint 05 — Deploy local e online

## Objetivo

Fazer o deploy refletir o ambiente real descrito pelo projeto.

---

### S05-H01 — Completar Docker Compose local

**Problema atual:** `infra/local/docker-compose.yml` tem apenas PostgreSQL, RabbitMQ e Redis .

**Tarefas:**

Adicionar:

```text
nova-alianca-local-api
nginx-local
pdv
kds
admin-local
sync-worker-local, se separado
```

**Critérios de aceite:**

* `docker compose up -d` sobe ambiente local completo.
* PDV acessa API local.
* KDS acessa API local.
* Banco/Rabbit/Redis não ficam expostos publicamente.
* API local funciona sem internet.

---

### S05-H02 — Completar Docker Compose online

**Problema atual:** `infra/online/docker-compose.yml` tem apenas PostgreSQL, RabbitMQ e Redis .

**Tarefas:**

Adicionar:

```text
nova-alianca-online-api
site-publico
admin-online
nginx
certbot
sync-worker-online, se separado
```

**Critérios de aceite:**

* Ambiente online sobe via Docker Compose.
* Nginx faz proxy para API/site/admin.
* Banco/Rabbit/Redis não ficam públicos.
* HTTPS documentado para produção.

---

### S05-H03 — Criar `.env.example` local e online

**Tarefas:**

* Criar `infra/local/.env.example`.
* Criar `infra/online/.env.example`.
* Usar apenas placeholders:

```env
POSTGRES_PASSWORD=change_me
JWT_SECRET=change_me
STORE_SECRET=change_me
```

**Critérios de aceite:**

* Nenhum secret real versionado.
* README explica como copiar `.env.example` para `.env`.

---

### S05-H04 — Health check real no Docker Compose

**Tarefas:**

* Adicionar `healthcheck` em PostgreSQL.
* Adicionar `healthcheck` em RabbitMQ.
* Adicionar `healthcheck` em Redis.
* Adicionar `depends_on.condition: service_healthy`, se aplicável.

**Critérios de aceite:**

* API só sobe após dependências saudáveis.
* `/api/health` reflete DB, Redis e Rabbit.

---

# 7. Sprint 06 — KDS e WebSocket

## Objetivo

Padronizar o KDS entre documentação, código, front-end e operação real.

---

### S06-H01 — Oficializar tópicos WebSocket do KDS

**Problema atual:** código envia eventos em `/topic/kds/tickets`, `/topic/kds/tickets/{sector}` e `/topic/orders/ready` , mas a documentação antiga previa nomes diferentes.

**Decisão recomendada:** manter tópicos atuais e documentar:

```text
/topic/kds/tickets
/topic/kds/tickets/{sector}
/topic/orders/ready
```

**Critérios de aceite:**

* KDS docs atualizados.
* OpenAPI/contrato assíncrono documentado.
* Front-end assina os tópicos oficiais.

---

### S06-H02 — Garantir que falha no WebSocket não quebra venda

O código já tenta capturar exceções de WebSocket no KDS .

**Tarefas:**

* Criar teste garantindo que exceção no `SimpMessagingTemplate` não quebra transação principal.
* Registrar log warning em vez de engolir silenciosamente.
* Documentar comportamento.

**Critérios de aceite:**

* Falha de WebSocket não impede venda.
* Log registra falha.
* Teste automatizado cobre o caso.

---

### S06-H03 — Alinhar status KDS com documentação

**Tarefas:**

* Definir status oficiais:

```text
WAITING
IN_PREPARATION
READY
FINISHED
CANCELED
```

* Atualizar docs.
* Atualizar front-end.
* Atualizar filtros de tela.

**Critérios de aceite:**

* Nenhum status divergente entre Java, TypeScript e docs.
* KDS exibe colunas corretas.

---

# 8. Sprint 07 — Organização modular em camadas

## Objetivo

Melhorar organização sem migrar para hexagonal.

---

### S07-H01 — Padronizar pacotes do back-end local

**Situação atual:** muitas classes estão em `localapp.domain.*`, incluindo controllers, services, entities e repositories.

**Novo padrão:**

```text
localapp/catalog/entity
localapp/catalog/repository
localapp/catalog/service
localapp/catalog/web
localapp/catalog/dto
```

**Tarefas:**

* Reorganizar módulo `catalog`.
* Reorganizar módulo `pdv`.
* Reorganizar módulo `cash`.
* Reorganizar módulo `kds`.
* Reorganizar módulo `sync`.
* Reorganizar módulo `security`.

**Critérios de aceite:**

* Controller não fica em pacote `domain`.
* Repository não fica misturado com DTO.
* Service concentra regra de aplicação.
* Build e testes passam.

---

### S07-H02 — Padronizar pacotes do back-end online

**Tarefas:**

* Aplicar a mesma organização:

```text
onlineapp/order
onlineapp/payment
onlineapp/sync
onlineapp/publicmenu
onlineapp/whatsapp
```

**Critérios de aceite:**

* Local e online seguem padrão parecido.
* Documentação de arquitetura mostra a estrutura real.

---

### S07-H03 — Criar teste arquitetural simples

Como a arquitetura não será hexagonal, o teste deve validar apenas camadas básicas.

**Regras sugeridas:**

```text
- Controller não acessa Repository diretamente.
- Controller chama Service.
- Service pode acessar Repository.
- DTO não depende de Entity.
- Entity não depende de Controller.
```

**Critérios de aceite:**

* Teste falha se controller acessar repository direto.
* Teste falha se DTO depender de entidade JPA.

---

# 9. Sprint 08 — Qualidade, build e limpeza técnica

## Objetivo

Reduzir risco de regressão.

---

### S08-H01 — Aumentar cobertura mínima

**Problema atual:** `minimumInstructionCoverage = 0.01`, praticamente simbólico .

**Tarefas:**

* Subir para `0.30` inicialmente.
* Depois criar meta futura para `0.60`.
* Excluir classes puramente DTO/config, se necessário.

**Critérios de aceite:**

* Build falha abaixo da cobertura mínima.
* Cobertura mínima realista para MVP.

---

### S08-H02 — Corrigir imports duplicados e organização de código

**Exemplo:** `PdvSaleController` tem imports duplicados de `AuthenticatedUser`, `RoleName` e `List` .

**Tarefas:**

* Rodar formatter.
* Remover imports duplicados.
* Aplicar Checkstyle ou Spotless.
* Configurar verificação no build.

**Critérios de aceite:**

* Build falha com código fora do padrão.
* Imports duplicados removidos.

---

### S08-H03 — Testes de regressão para fluxos críticos

**Fluxos mínimos:**

```text
- login válido
- login inválido
- abrir caixa
- iniciar venda
- adicionar item
- pagamento parcial recusado
- pagamento total aceito
- finalizar venda
- gerar KDS quando item exige preparo
- não gerar KDS para SEM_PREPARO
- criar sync event ao finalizar venda
- cancelar venda com auditoria
```

**Critérios de aceite:**

* Cada fluxo tem teste automatizado.
* Testes negativos existem para regras críticas.

---

### S08-H04 — CI completo

**Tarefas:**

* Criar GitHub Actions:

```text
back-end-test
back-end-build
front-end-test
front-end-build
openapi-validate
docker-compose-validate
```

**Critérios de aceite:**

* Pull request falha se back-end quebrar.
* Pull request falha se front-end quebrar.
* Pull request falha se OpenAPI inválido.

---

# 10. Sprint 09 — Homologação final das divergências

## Objetivo

Validar que documentação, código, contratos e deploy estão coerentes.

---

### S09-H01 — Checklist automático de alinhamento

Criar um documento:

```text
docs/auditoria-alinhamento-main.md
```

Com matriz:

| Item                   | Documento | Código | Status   |
| ---------------------- | --------- | ------ | -------- |
| Java 21                | OK        | OK     | Alinhado |
| Arquitetura em camadas | OK        | OK     | Alinhado |
| Sync evoluído          | OK        | OK     | Alinhado |
| OpenAPI health         | OK        | OK     | Alinhado |
| KDS permissões         | OK        | OK     | Alinhado |
| Docker local           | OK        | OK     | Alinhado |

**Critérios de aceite:**

* Cada divergência anterior aparece como resolvida ou pendente justificada.
* Documento vira base de homologação técnica.

---

### S09-H02 — Roteiro manual de homologação técnica

Atualizar `docs/homologacao-mvp.md` com:

```text
- subir infra local
- testar /api/ping
- testar /api/health
- login
- abrir caixa
- venda PDV
- pagamento
- KDS
- sync pendente
- queda de internet
- retomada de sync
```

**Critérios de aceite:**

* Roteiro executável por alguém técnico.
* Cada passo tem resultado esperado.

---

### S09-H03 — Revisão final da documentação

**Tarefas:**

* Revisar `README.md`.
* Revisar `AGENTS.md`.
* Revisar `arquitetura.md`.
* Revisar `banco-de-dados.md`.
* Revisar `sincronizacao.md`.
* Revisar `pdv.md`.
* Revisar `kds.md`.
* Revisar `deploy-local.md`.
* Revisar `deploy-online.md`.
* Revisar contratos OpenAPI.

**Critérios de aceite:**

* Nenhum documento exige Java 25.
* Nenhum documento exige hexagonal.
* Nenhum documento usa modelo antigo de sync.
* Nenhum documento usa caminho errado.
* Deploy documentado bate com compose real.

---

# 11. Sequência recomendada

| Sprint    | Foco                                  | Resultado                                                            |
| --------- | ------------------------------------- | -------------------------------------------------------------------- |
| Sprint 01 | Decisões oficiais e documentação-base | Docs deixam de contradizer Java 21, arquitetura real e sync evoluído |
| Sprint 02 | OpenAPI e contratos                   | Front/back passam a ter contrato confiável                           |
| Sprint 03 | Segurança                             | KDS e ações críticas ficam protegidas                                |
| Sprint 04 | Sync evoluído                         | Modelo atual vira oficial e completo                                 |
| Sprint 05 | Deploy                                | Compose local/online representa o ambiente real                      |
| Sprint 06 | KDS/WebSocket                         | KDS fica coerente entre back, front e docs                           |
| Sprint 07 | Organização modular                   | Código fica limpo sem exigir hexagonal                               |
| Sprint 08 | Qualidade                             | Testes, cobertura e CI melhoram                                      |
| Sprint 09 | Homologação                           | Divergências anteriores são encerradas formalmente                   |

---

# 12. Ordem prática de execução no Codex

Use nesta ordem:

```text
1. S01-H01
2. S01-H02
3. S01-H03
4. S01-H04
5. S02-H01
6. S02-H02
7. S03-H02
8. S04-H01
9. S04-H02
10. S05-H01
11. S05-H02
12. S06-H01
13. S07-H01
14. S08-H03
15. S09-H01
```

Essa ordem resolve primeiro as contradições que mais confundem desenvolvimento e revisão. Depois ataca segurança, sync, deploy e qualidade.
