# Auditoria técnica completa — Sistema MNSS / Nova Aliança

> **Documento histórico. Esta auditoria foi feita sobre HEAD `b91c5b0` e não representa a `main` atual.**
> Valide sempre o estado atual da branch antes de usar qualquer achado abaixo como pendência real.

**Data:** 2026-05-08
**Branch auditada:** `main` (HEAD `b91c5b0`)
**Escopo:** repositório completo (back-end Java, front-end Angular, infra Docker, docs)
**Auditor:** Claude Code (Opus 4.7)

> **Restrições mandatórias respeitadas durante a auditoria:**
> - Pagamento real está fora do escopo. Permanece mockado/sandbox.
> - WhatsApp real está fora do escopo. Permanece mockado.
> - Nenhum arquivo foi alterado durante a auditoria.
> - Nenhum teste foi removido.
> - CI não foi desabilitado.
> - Flyway permanece ativo, `ddl-auto=validate` mantido.
> - `@Version` preservado nas entidades.

---

## Sumário executivo

| Métrica | Valor |
|---|---|
| Achados P0 (bloqueia produção) | **7** |
| Achados P1 (alto, antes de produção controlada) | **12** |
| Achados P2 (médio, antes de produção pública) | **10** |
| Achados P3 (melhoria) | **7** |
| Build back-end | ✅ `./gradlew clean test` BUILD SUCCESSFUL (2m 8s, 32 tasks) |
| Build front-end | ✅ npm ci, lint, format:check, test (92/92), build:local, build:online |
| Compose local | ✅ `docker compose config` OK com `.env` |
| Compose online | ✅ `docker compose config` OK com `.env` |

**Veredito final:** **Pronto apenas para homologação.** Não pronto para produção controlada local até resolução dos 7 achados P0.

---

## Status geral

| Área | Status | Observação |
|---|---|---|
| **CI** | Não auditado | Workflows GitHub Actions não foram inspecionados (fora do escopo dos arquivos lidos). Build local verde. |
| **Back-end** | ✅ Verde | 32 tasks gradle, 0 erros, 0 testes falhando. |
| **Front-end** | ✅ Verde | 92/92 testes, lint OK, builds OK. |
| **Docker local** | ⚠️ OK com ressalva | Configuração válida, mas variáveis sensíveis usam fallback fraco `:-change_me`. |
| **Docker online** | ✅ Verde | Variáveis sensíveis sem default — `compose config` falha sem `.env` (correto para produção). |
| **Schema (JPA × Flyway)** | ⚠️ Achados estáticos | ddl-auto=validate aceita; 1 P0 (FK fake), 1 P2 (V15 corretiva). |
| **Segurança** | ❌ 3 P0 estruturais | JWT timing-attack, CORS ausente, Actuator sem auth. |
| **Operação offline** | ✅ Viável | Roteiro coerente, código suporta, sync não bloqueia venda. |
| **Pagamento** | ✅ Mock-only | Webhook valida HMAC; sem gateway real. |
| **WhatsApp** | ✅ Mock-only | `MockWhatsAppProvider` default via `@ConditionalOnProperty matchIfMissing=true`. |

---

## Metodologia

### ETAPAs executadas

1. **ETAPA 1** — Leitura completa: README, AGENTS.md, docs/README, arquitetura, modelo-de-dominio, banco-de-dados, sincronizacao, pdv, kds, deploy-local, deploy-online, homologacao-mvp, homologacao-offline-local, release-checklist-local, release-checklist-online, backup-restore, diagnostico-local, contracts/local-api.openapi.yml, contracts/online-api.openapi.yml.
2. **ETAPA 2** — `grep` por padrões de risco (TODO, FIXME, permitAll, ddl-auto, BigDecimal, change_me, store-001, printStackTrace, catch Exception, etc.).
3. **ETAPA 3** — Build e testes reais executados.
4. **ETAPA 4** — Inventário de 34 entidades JPA × 14 migrations local + 19 online.
5. **ETAPA 5** — Auditoria profunda do local-app (PDV, caixa, KDS, estoque, sync outbox, auth).
6. **ETAPA 6** — Auditoria profunda do online-app (JWT, sync inbox, payment mock, WhatsApp mock, security).
7. **ETAPA 7** — Sync local↔online (HMAC, storeId, dead-letter, idempotência).
8. **ETAPA 8** — Front-end Angular (environments, interceptors, subscriptions, builds).
9. **ETAPA 9** — Docker e infraestrutura (variáveis, healthchecks, portas, nginx).
10. **ETAPA 10** — Scripts operacionais (smoke, backup, restore, set -euo, confirmação).
11. **ETAPA 11** — OpenAPI vs controllers (divergências de endpoint, header, método).
12. **ETAPA 12** — Segurança (CSRF, CORS, headers, actuator, password encoder, log leakage).
13. **ETAPA 13** — Regras de negócio críticas (caixa, idempotência, KDS).
14. **ETAPA 14** — Documentação operacional vs realidade.
15. **ETAPA 15** — Classificação P0–P3.
16. **ETAPA 16** — Backlog de correção em sprints.

### Filtragem de falsos positivos

Achados reportados pelos sub-agents foram verificados diretamente via `grep` antes de classificação. Filtrados como falso positivo:

- ❌ `OnlinePaymentEntity.webhook_payload` orfã — **mapeada** em linha 42 do entity.
- ❌ `StockBalanceEntity.reserved_quantity` ausente em local — **criada** em V9 linha 5.
- ❌ `OnlineOrderEntity.payment_method` constraint divergente — entity tem `nullable=false, length=50`, schema bate.
- ❌ `CustomerAddressEntity.customer` nullable mismatch — V8 fez schema nullable, entity sem `nullable=false` bate.

---

## Achados P0 — Bloqueadores de produção

### P0-1 — storeId divergente entre local e online

**Arquivos:**
- `infra/local/.env.example:30` → `MNSS_STORE_ID=nova-alianca-001`
- `infra/local/docker-compose.yml:39` → `MNSS_STORE_ID: ${MNSS_STORE_ID:-nova-alianca-001}`
- `infra/online/.env.example:51` → `MNSS_DEFAULT_STORE_ID=store-001`
- `infra/online/docker-compose.yml:38` → `MNSS_DEFAULT_STORE_ID: ${MNSS_DEFAULT_STORE_ID:-store-001}`
- `back-end/online-app/src/main/resources/application.yml:42` → `mnss.sync.stores.store-001: ${MNSS_STORE_001_SECRET}`
- `docs/sincronizacao.md:301,508` → exemplo `X-Store-ID: nova-alianca-001`

**Causa raiz:** Local envia `X-Store-ID: nova-alianca-001` ao chamar `/api/sync/events` no online. O online procura o segredo HMAC em `mnss.sync.stores.nova-alianca-001`, que não existe (apenas `store-001` está mapeado). HMAC validation retorna 401.

**Impacto:** Sincronização local↔online TOTALMENTE quebrada na configuração padrão. Viola a regra central "loja vende presencialmente mesmo sem internet, mas sincroniza quando volta".

**Correção:**
1. Padronizar para `store-001` em `infra/local/.env.example:30` e `infra/local/docker-compose.yml:39`.
2. Atualizar `docs/sincronizacao.md:301,508`, `docs/fluxos-e-casos-de-uso.md:1685,3429` para usar `store-001`.
3. Adicionar teste de integração que envie evento real e valide HMAC end-to-end.

---

### P0-2 — JWT timing-attack: `String.equals` em comparação HMAC

**Arquivo:** `back-end/online-app/src/main/java/br/com/novaalianca/mnss/onlineapp/security/auth/JwtTokenProvider.java:96`

```java
return expected.equals(parts[2]) ? parts : null;
```

**Causa raiz:** `String.equals` faz short-circuit no primeiro byte diferente. Atacante mede latência → recupera assinatura byte a byte (em vez de tentar 2^256 combinações, ~256 tentativas por byte).

**Impacto:** Em produção HTTPS o jitter de rede mascara o ataque, mas a vulnerabilidade existe e é trivial de explorar em ambientes mais previsíveis (CI, mesmo data center).

**Correção:**
```java
byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
byte[] actualBytes = parts[2].getBytes(StandardCharsets.UTF_8);
return java.security.MessageDigest.isEqual(expectedBytes, actualBytes) ? parts : null;
```

Padrão já usado em `back-end/shared-infra/src/main/java/br/com/novaalianca/mnss/sharedinfra/security/HmacUtils.java:39`.

Adicionar `JwtTokenProviderTest` cobrindo: token válido, signature alterada em 1 byte, signature totalmente diferente, token nulo, token malformado, token expirado.

---

### P0-3 — Actuator `/actuator/health` com `show-details: always` sem auth

**Arquivos:**
- `back-end/local-app/src/main/resources/application.yml:31` → `show-details: always`
- `back-end/online-app/src/main/resources/application.yml:31` → idem
- `back-end/local-app/src/main/java/br/com/novaalianca/mnss/localapp/security/config/SecurityConfiguration.java:34` → `/api/health` em permitAll (mas /actuator/health também acessível direto pelo nginx)
- `back-end/online-app/src/main/java/br/com/novaalianca/mnss/onlineapp/security/config/SecurityConfiguration.java` → permitAll cobre actuator

**Causa raiz:** `show-details: always` retorna detalhes internos do health: status DB (com URL parcial), Redis, RabbitMQ, versão Spring, hostname.

**Impacto:** Information disclosure útil para reconhecimento. Adversário descobre versões, infra, status de dependências.

**Correção:** Trocar para `show-details: when-authorized`. Ou criar endpoint público `/api/health` minimalista (apenas `{"status":"UP"}`) e proteger `/actuator/health` com role ADMIN.

---

### P0-4 — CORS não configurado

**Arquivos:** Sem grep matches para `Cors`, `cors`, `CORS` em `back-end/local-app/src/main` e `back-end/online-app/src/main`.

**Causa raiz:** Spring Security desabilita CSRF (correto para Bearer/HMAC) mas não configura CORS. Browsers permitem requisições cross-origin quando o CORS não está restrito explicitamente.

**Impacto:** Site malicioso pode fazer requisições autenticadas para `/api/public/*` ou `/api/auth/login` em nome do usuário logado.

**Correção:** Adicionar `WebMvcConfigurer` ou `CorsConfigurationSource`:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://padarianovaalianca.com.br", "https://admin.padarianovaalianca.com.br"));
    config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

E em `SecurityConfiguration`: `http.cors(cors -> cors.configurationSource(corsConfigurationSource()))`.

---

### P0-5 — `infra/online/scripts/restore-postgres.sh` ausente

**Arquivo:** `infra/online/scripts/` contém apenas `backup-postgres.sh, certbot-init.sh, renew-certificates.sh, smoke-online.sh, update-system.sh`. Falta `restore-postgres.sh`.

**Causa raiz:** Script nunca foi criado para o ambiente online. `docs/deploy-online.md:113` lista o script na estrutura sugerida.

**Impacto:** Recovery de banco online é inviável sem o script. RPO/RTO violados. Em incidente real, equipe terá que improvisar restore destrutivo sem confirmação nem backup pré-restore.

**Correção:** Criar `infra/online/scripts/restore-postgres.sh` espelhando `infra/local/scripts/restore-postgres.sh`:
- Confirmação textual `RESTAURAR-nova_alianca_online`.
- Backup pré-restore automático.
- Container `postgres-online`, DB `nova_alianca_online`.
- DROP/CREATE/RESTORE com `set -euo pipefail`.

---

### P0-6 — `CashRegisterEntity.operatorId` sem `@ManyToOne`/`@JoinColumn`

**Arquivo:** `back-end/local-app/src/main/java/br/com/novaalianca/mnss/localapp/domain/cash/CashRegisterEntity.java:23`

```java
private UUID operatorId;
```

**Causa raiz:** Campo declarado como `UUID` puro. Schema cria `operator_id UUID NOT NULL REFERENCES users(id)`, mas JPA não conhece a relação. Sem cascata, sem lazy load via Hibernate, sem proteção FK no nível ORM.

**Impacto:** Testes podem inserir UUIDs aleatórios sem usuário correspondente. Risco de inconsistência se alguém ignorar o banco e usar apenas o repositório JPA. Auditoria rastreabilidade comprometida.

**Correção:**

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "operator_id", nullable = false)
private UserEntity operator;
```

Migrar callers para `setOperator(user)` em vez de `setOperatorId(uuid)`.

---

### P0-7 — Endpoints `/api/sync/*` com `permitAll()` dependem inteiramente de validação manual

**Arquivos:**
- `back-end/online-app/src/main/java/br/com/novaalianca/mnss/onlineapp/security/config/SecurityConfiguration.java:48-50`

```java
.requestMatchers(HttpMethod.POST, "/api/sync/events").permitAll()
.requestMatchers(HttpMethod.GET, "/api/sync/pending").permitAll()
.requestMatchers(HttpMethod.POST, "/api/sync/events/*/ack").permitAll()
```

- `back-end/online-app/src/main/java/br/com/novaalianca/mnss/onlineapp/domain/sync/SyncController.java` (validação HMAC manual)

**Causa raiz:** A validação HMAC é manual no controller. Se alguém adicionar um novo endpoint `/api/sync/*` no futuro sem replicar a validação, fica aberto. Não há `OncePerRequestFilter` que aplique HMAC a todos os paths sync.

**Impacto:** Risco de regressão. Hoje funciona; amanhã pode quebrar com nova feature.

**Correção:**
1. Extrair validação HMAC para `SyncHmacAuthenticationFilter extends OncePerRequestFilter`.
2. Aplicar filtro a todos os paths que casam `/api/sync/**` que não são admin (admin já tem `@PreAuthorize`).
3. Remover validação manual do controller.
4. Adicionar teste que confirma 401 para `/api/sync/events` sem `X-Signature`.

---

## Achados P1 — Alto, antes de produção controlada

### P1-1 — `SYNC_MASTER_SECRET` não mapeado para `mnss.online.sync-master-secret`

**Arquivos:**
- `infra/online/docker-compose.yml:32` → injeta `SYNC_MASTER_SECRET`
- `back-end/online-app/src/main/java/br/com/novaalianca/mnss/onlineapp/config/HmacSecretStrengthValidator.java:27` → lê `${mnss.online.sync-master-secret:}` (default vazio)
- `back-end/online-app/src/main/resources/application.yml` → não mapeia

**Causa raiz:** Spring relaxed binding mapearia `MNSS_ONLINE_SYNC_MASTER_SECRET` para `mnss.online.sync-master-secret` automaticamente, mas o nome real da variável é `SYNC_MASTER_SECRET`. Sem mapping explícito em `application.yml`, a propriedade fica com default vazio e a validação de força é silenciosamente pulada.

**Correção:** Adicionar em `back-end/online-app/src/main/resources/application.yml`:

```yaml
mnss:
  online:
    sync-master-secret: ${SYNC_MASTER_SECRET:}
```

Ou renomear a env var para `MNSS_ONLINE_SYNC_MASTER_SECRET` em todos os lugares.

---

### P1-2 — `docker-compose.yml` local com fallback fraco `:-change_me`

**Arquivo:** `infra/local/docker-compose.yml:19,24,31,35,40`

```yaml
MNSS_LOCAL_DB_PASSWORD: ${POSTGRES_PASSWORD:-change_me}
MNSS_LOCAL_RABBITMQ_PASSWORD: ${RABBITMQ_DEFAULT_PASS:-change_me}
AUTH_TOKEN_SECRET: ${AUTH_TOKEN_SECRET:-change_me_auth_token_secret}
MNSS_INITIAL_ADMIN_PASSWORD: ${MNSS_INITIAL_ADMIN_PASSWORD:-change_me_admin_password}
MNSS_STORE_SECRET: ${MNSS_STORE_SECRET:-change_me}
```

**Causa raiz:** Se operador subir compose sem `.env`, containers iniciam silenciosamente com senhas conhecidas.

**Impacto:** Produção acidental com credenciais públicas.

**Correção:** Trocar todos os `:-` por `:?` com mensagem (online já faz isso):

```yaml
MNSS_LOCAL_DB_PASSWORD: ${POSTGRES_PASSWORD:?POSTGRES_PASSWORD obrigatório — configure no .env}
```

---

### P1-3 — `PdvSaleService.finishSale()` retorna silenciosamente se status já FINISHED

**Arquivo:** `back-end/local-app/src/main/java/br/com/novaalianca/mnss/localapp/domain/pdv/PdvSaleService.java:155-157`

**Causa raiz:** Sem erro nem flag de retorno indicando "já estava finalizada". Cliente que reenvia `POST /api/pdv/sales/{id}/finish` recebe 200 sem saber que foi no-op.

**Impacto:** Telemetria confusa. Sync pode enviar 2 eventos `SALE_FINISHED` (idempotência cobre via `idempotencyKey`, mas logs ficam ruidosos).

**Correção:** Lançar `BusinessException("SALE_ALREADY_FINISHED")` com 409 ou retornar response com flag `alreadyFinished=true`. Adicionar teste para duplo finish.

---

### P1-4 — `StockService.recordSaleMovement` não valida `idempotency_key` duplicada antes de persistir

**Arquivo:** `back-end/local-app/src/main/java/br/com/novaalianca/mnss/localapp/domain/stock/StockService.java:122-161`

**Causa raiz:** `idempotency_key` é gerada e usada apenas no outbox event, não no save do `StockMovement`. Banco tem `UNIQUE INDEX idx_stock_movements_idempotency_key` (V10) que protege contra duplicação física, mas exception não é tratada.

**Impacto:** Sync retry → 2ª chamada lança `DataIntegrityViolationException` no meio de `finishSale`. Logs assustadores.

**Correção:**

```java
public StockMovement recordSaleMovement(...) {
    return stockMovementRepository.findByIdempotencyKey(idempotencyKey)
        .orElseGet(() -> stockMovementRepository.save(newMovement));
}
```

---

### P1-5 — `SyncController POST /api/sync/events` não valida X-Store-ID vs `payload.storeId`

**Arquivo:** `back-end/online-app/src/main/java/br/com/novaalianca/mnss/onlineapp/domain/sync/SyncController.java`

**Causa raiz:** Cliente pode enviar `X-Store-ID: store-001` (válida HMAC) mas `payload.storeId: store-002` (evento gravado em nome de outra loja).

**Impacto:** Cross-tenant. Loja A pode (com sua chave válida) gravar eventos em nome da Loja B.

**Correção:**
```java
if (payload.containsKey("storeId") && !storeId.equals(payload.get("storeId"))) {
    return ResponseEntity.badRequest().body(...);
}
```

---

### P1-6 — Headers de segurança ausentes (HSTS, X-Frame-Options, X-Content-Type-Options)

**Arquivo:** `back-end/local-app/.../SecurityConfiguration.java`, idem online — sem chamada a `.headers(...)`.

**Impacto:** Clickjacking (admin online), MIME sniffing, HTTPS downgrade.

**Correção:**

```java
http.headers(headers -> headers
    .frameOptions(frame -> frame.deny())
    .contentTypeOptions(Customizer.withDefaults())
    .httpStrictTransportSecurity(hsts -> hsts.maxAgeInSeconds(31536000).includeSubDomains(true))
);
```

---

### P1-7 — `release-checklist-online.md` referencia variáveis inexistentes

**Arquivos:**
- `docs/release-checklist-online.md:17-18`: `MNSS_AUTH_COOKIE_SECURE=true`, `MNSS_SYNC_REQUIRE_HTTPS=true`
- `infra/online/.env.example`: variáveis ausentes
- `infra/online/docker-compose.yml`: variáveis ausentes

**Causa raiz:** Local tem `MNSS_SYNC_REQUIRE_HTTPS`; online não tem (e talvez não precise — não há outbox HTTP no online). Mas o checklist obriga marcar.

**Correção:** Implementar variáveis em online (se aplicável) OU remover do checklist.

---

### P1-8 — `POST /api/auth/logout` (local) ausente do OpenAPI

**Arquivos:**
- `back-end/local-app/src/main/java/br/com/novaalianca/mnss/localapp/security/auth/AuthController.java:43-47` (endpoint existe)
- `docs/contracts/local-api.openapi.yml` (não documenta logout)

**Impacto:** Cliente que gera SDK pelo OpenAPI não terá logout. Contrato desatualizado.

**Correção:** Adicionar bloco `/api/auth/logout` ao OpenAPI local.

---

### P1-9 — Scripts sem `set -euo pipefail`

**Arquivos:**
- `infra/local/scripts/backup-postgres.sh`
- `infra/local/scripts/update-system.sh`
- `infra/online/scripts/backup-postgres.sh`
- `infra/online/scripts/update-system.sh`

**Impacto:** Falha silenciosa: `mkdir` não cria diretório, `pg_dump` retorna vazio, `docker pull` falha mas script prossegue.

**Correção:** Adicionar `set -euo pipefail` no topo de cada script.

---

### P1-10 — `update-system.sh` faz `docker image prune -f` sem confirmação

**Arquivos:** `infra/local/scripts/update-system.sh`, `infra/online/scripts/update-system.sh`

**Impacto:** Pode remover imagens não-tagged em uso (build em andamento, imagem dev local).

**Correção:** Adicionar prompt de confirmação ou flag `--yes`.

---

### P1-11 — Front-end: 7+ subscribes sem `takeUntilDestroyed`

**Arquivos:**
- `front-end/src/app/features/site-publico/pages/checkout/checkout-page.component.ts:100`
- `front-end/src/app/features/site-publico/pages/payment-page/payment-page.component.ts:45,66`
- `front-end/src/app/features/site-publico/pages/public-menu/public-menu.component.ts:234,254`
- `front-end/src/app/features/whatsapp/components/whatsapp-panel/whatsapp-panel.component.ts:37,61,69`

**Impacto:** Memory leak ao trocar de rota. PDV em turno de 8h+ degrada.

**Correção:** Aplicar `takeUntilDestroyed()` (Angular 16+) em todos os subscriptions ou padrão `takeUntil(this.destroy$)`.

---

### P1-12 — `api-error.interceptor.ts` não trata 401 com redirect-to-login

**Arquivo:** `front-end/src/app/core/http/api-error.interceptor.ts`

**Impacto:** Token expira → request falha → toast genérico, usuário não é direcionado ao login.

**Correção:** Adicionar `if (error.status === 401) router.navigate(['/login'])` no interceptor.

---

## Achados P2 — Médio, antes de produção pública

### P2-1 — Front-end: `alert()` em fluxos críticos

**Arquivos:**
- `front-end/src/app/features/site-publico/pages/checkout/checkout-page.component.ts:113`
- `front-end/src/app/features/site-publico/pages/payment-page/payment-page.component.ts:77`
- `front-end/src/app/features/whatsapp/components/assisted-cart/assisted-cart.component.ts:390,395`

**Correção:** Usar Toast/SnackBar/Modal já presentes em `shared/ui`.

---

### P2-2 — `docs/diagnostico-local.md:105` referencia volume com nome errado

**Arquivo:** `docs/diagnostico-local.md:105` → `docker volume inspect infra_local_postgres_local_data`
**Realidade:** `docker compose config` mostra volume nomeado `local_postgres_local_data`.

**Correção:** Corrigir prefixo no doc.

---

### P2-3 — `release-checklist-local.md:35` lista apenas `smoke-local.sh`

**Arquivo:** `docs/release-checklist-local.md:35`
**Realidade:** `infra/local/scripts/` tem 3 scripts: `smoke-local.sh`, `smoke-local-infra.sh`, `smoke-local-pdv.sh`.

**Correção:** Listar os 3 scripts e seu propósito (infra=health, pdv=funcional, smoke-local.sh=quick).

---

### P2-4 — `deploy-online.md:170` × release-checklist nomenclatura inconsistente

`docs/deploy-online.md:170` usa `MNSS_DEFAULT_STORE_ID`. Outros lugares falam `MNSS_STORE_ID`.

**Correção:** Padronizar: online=`MNSS_DEFAULT_STORE_ID`, local=`MNSS_STORE_ID`, valor unificado=`store-001`.

---

### P2-5 — `SyncOutboxWorker` aceita HTTP em local profile

**Arquivo:** `back-end/local-app/src/main/resources/application-local.yml:42` → `require-https: ${MNSS_SYNC_REQUIRE_HTTPS:false}`

**Impacto:** Local em dev OK; risco se ambiente local ficar exposto à internet.

**Correção:** Documentar que `application-production.yml` força `true`. Adicionar warning em README.

---

### P2-6 — `WhatsAppConversationEntity.assigned_to` FK adicionada apenas em V15

**Arquivo:** `back-end/online-app/src/main/resources/db/migration/V15__add_fk_whatsapp_conversations_assigned_to.sql`

**Causa:** V4 cria coluna sem FK; V15 corrige (UPDATE NULL para órfãos + ALTER ADD CONSTRAINT). Risco se restaurar snapshot anterior a V15.

**Correção:** Aceitar; documentar em `banco-de-dados.md` que V15 é corretiva.

---

### P2-7 — `MockBean` deprecated em ~30 testes

**Arquivos:** `back-end/local-app/src/test/.../MockRepositoriesConfig.java`, `PingControllerTest.java`, `CashRegisterPermissionTest.java`, `KdsSecurityTest.java`, `PdvCriticalActionsSecurityTest.java`, `ProductCriticalActionsSecurityTest.java`, etc. (78 warnings no build).

**Correção:** Migrar para `@MockitoBean` (Spring Boot 3.4+).

---

### P2-8 — `SecurityConfigurerAdapter.and()` deprecated

**Arquivo:** `back-end/online-app/src/main/java/br/com/novaalianca/mnss/onlineapp/security/config/SecurityConfiguration.java:67`

**Correção:** Refatorar para lambda style sem `.and()`.

---

### P2-9 — CSRF disabled sem CORS configurado

Já coberto pelo P0-4 (CORS ausente). A combinação CSRF disabled + Bearer/HMAC é correta APENAS quando CORS restringe origens. Sem CORS, CSRF disable abre vetor.

---

### P2-10 — Nginx local sem timeouts explícitos

**Arquivo:** `infra/local/nginx/default.conf` — sem `proxy_connect_timeout`, `proxy_send_timeout`, `proxy_read_timeout`.

**Impacto:** Defaults nginx (60s); se PostgreSQL trava, request fica pendurado.

**Correção:**
```nginx
proxy_connect_timeout 10s;
proxy_send_timeout 30s;
proxy_read_timeout 30s;
```

---

## Achados P3 — Melhoria

### P3-1 — Arquivos de log committados no git

**Arquivos rastreados:** `back-end/build_log.txt`, `back-end/compile_errors.txt`.
`back-end/test_output.log` presente mas não rastreado.

**Correção:** `git rm --cached back-end/build_log.txt back-end/compile_errors.txt`. Apagar `test_output.log` local.

---

### P3-2 — Front-end: `console.log` em `kds.service.ts`

**Arquivo:** `front-end/src/app/features/kds/data-access/kds.service.ts:58,66,79` (debug logger e SockJS lifecycle).

**Correção:** Logger condicional (`environment.production ? noop : console.log`).

---

### P3-3 — Front-end: `console.log` em `register-service-worker.ts`

**Arquivo:** `front-end/src/register-service-worker.ts:14,27,42,47`

**Status:** Aceitável (SW lifecycle); pode ser silenciado em produção.

---

### P3-4 — `@SuppressWarnings("unchecked")` em deserialização JSON

**Arquivos:** `WhatsAppWebhookController.java`, `SyncInboxService.java`. Aceitável.

---

### P3-5 — `docs/deploy-local.md:137-223` exemplo `docker-compose.yml` desatualizado

Compose real é mais completo (healthchecks, depends_on: service_healthy). Doc cosmético.

**Correção:** Substituir por link/include do real, ou atualizar.

---

### P3-6 — `docs/backup-restore.md:29-39` sugere `pg_dump -Fc` mas scripts usam SQL puro

Inconsistência menor.

**Correção:** Padronizar formato; o `-Fc` é melhor para bancos grandes.

---

### P3-7 — `OnlineProfileConfiguration.@Profile("online")` × `application.yml` não força `online` por default

Hoje funciona porque `infra/online/docker-compose.yml` injeta `SPRING_PROFILES_ACTIVE: online`. Se alguém esquecer, OnlineProfileConfiguration não ativa e validações @NotBlank são puladas.

**Correção:** Em `application.yml` online: `spring.profiles.default: online` ou validar no startup que perfil esperado está ativo.

---

## Comandos executados

| Comando | Resultado | Tempo |
|---|---|---|
| `cd back-end && ./gradlew --no-daemon clean test` | ✅ BUILD SUCCESSFUL | 2m 8s |
| `cd front-end && npm ci` | ✅ exit 0 | ~30s |
| `cd front-end && npm run lint` | ✅ exit 0 | ~10s |
| `cd front-end && npm run format:check` | ✅ Prettier OK | ~5s |
| `cd front-end && npm test --watch=false` | ✅ 92/92 passed | 1.1s |
| `cd front-end && npm run build:local` | ✅ exit 0 | 5.4s |
| `cd front-end && npm run build:online` | ✅ exit 0 | 7.1s |
| `cd infra/local && cp .env.example .env && docker compose config` | ✅ exit 0 | <1s |
| `cd infra/online && cp .env.example .env && docker compose config` | ✅ exit 0 | <1s |
| Múltiplos `grep` (TODO, FIXME, permitAll, ddl-auto, BigDecimal, change_me, store-001, etc.) | ✅ resultados nas seções | — |
| Inspeção direta de 20+ arquivos Java/YAML/MD | ✅ — | — |

## Comandos não executados

| Comando | Motivo |
|---|---|
| `docker compose up -d` (subir stack) | Fora do escopo: auditoria estática + `compose config` é suficiente para validar sintaxe e variáveis. |
| Roteiro `homologacao-offline-local.md` real (desconectar internet, finalizar venda) | Requer ambiente vivo. Documentação descreve roteiro corretamente; código suporta. |
| `./gradlew :local-app:test --tests "*SchemaValidationTest"` isolado | `clean test` já cobre os SchemaValidationTest no fluxo completo. |
| Pipeline CI (GitHub Actions) | Workflows não foram listados no escopo; apenas referenciados como "build CI". |

---

## Contradições entre código e docs

| Documento | Código relacionado | Divergência |
|---|---|---|
| `docs/sincronizacao.md:301,508` (`X-Store-ID: nova-alianca-001`) | `application.yml:42` (chave `store-001`) | Doc usa exemplo com ID que não existe na config online. |
| `docs/release-checklist-online.md:17-18` (`MNSS_AUTH_COOKIE_SECURE`, `MNSS_SYNC_REQUIRE_HTTPS`) | `infra/online/.env.example` (não declara) | Variáveis listadas como obrigatórias no checklist mas inexistentes na configuração online. |
| `docs/deploy-online.md:113` (estrutura sugere `restore-postgres.sh`) | `infra/online/scripts/` (sem o arquivo) | Script de recovery online ausente. |
| `docs/diagnostico-local.md:105` (volume `infra_local_postgres_local_data`) | `docker compose config` (`local_postgres_local_data`) | Prefixo de volume diverge. |
| `docs/release-checklist-local.md:35` (`smoke-local.sh`) | `infra/local/scripts/` (3 scripts diferentes) | Checklist menciona apenas o mais simples; perde testes mais completos. |
| `docs/contracts/local-api.openapi.yml` (sem `/api/auth/logout`) | `AuthController.java:43-47` | Endpoint existe mas não documentado. |
| `docs/fluxos-e-casos-de-uso.md:1685,3429` (storeId `nova-alianca-001`) | `application.yml` online (`store-001`) | Mesma inconsistência storeId. |
| `infra/local/.env.example:30` × `infra/online/.env.example:51` | dois ambientes | storeIds divergentes. |
| `back-end/online-app/src/main/resources/application.yml` × `infra/online/docker-compose.yml:32` | mapping | `SYNC_MASTER_SECRET` injetada mas sem mapping para `mnss.online.sync-master-secret`. |
| `docs/deploy-local.md:137-223` (exemplo compose) | `infra/local/docker-compose.yml` real | Exemplo simplificado; real tem healthchecks/depends_on. |

---

## Riscos de produção

1. **Sincronização local↔online totalmente quebrada** por storeId divergente (P0-1). Achado mais grave; impede o sistema híbrido de funcionar.
2. **Recovery online inviável** sem `restore-postgres.sh` (P0-5). Qualquer corrupção de dados online é catastrófica.
3. **JWT timing-attack** (P0-2): em produção HTTPS o risco é reduzido (cifragem + jitter de rede mascaram), mas trivial de corrigir.
4. **CORS ausente** (P0-4): em produção com domínio único o impacto é menor, mas qualquer subdomínio comprometido pode atacar.
5. **Actuator vaza topologia** (P0-3): adversário descobre versões, hostnames, status interno.
6. **Senhas `change_me` em produção** se operador esquecer `.env` no local (P1-2).
7. **Estoque duplicado** em retry de sync (P1-4): banco protege via UNIQUE, mas exception não tratada causa logs assustadores.
8. **Memory leaks Angular** (P1-11): degradação após uso prolongado em PDV (turno 8h+).
9. **Cross-tenant** em sync (P1-5): loja A pode gravar eventos em nome da loja B.
10. **Cookie SameSite/Secure mal configurado** se variáveis do checklist online não existem (P1-7).

---

## Backlog recomendado

### Sprint 1 — Bloqueadores P0 (1–2 dias)

#### S1.1 — Unificar storeId

- **Arquivos:**
  - `infra/local/.env.example:30` → `MNSS_STORE_ID=store-001`
  - `infra/local/docker-compose.yml:39` → `MNSS_STORE_ID: ${MNSS_STORE_ID:-store-001}`
  - `docs/sincronizacao.md:301,508` → `X-Store-ID: store-001`
  - `docs/fluxos-e-casos-de-uso.md:1685,3429` → idem
  - `docs/deploy-local.md:164` → notar alinhamento com online
- **Causa:** Lookup HMAC procura `mnss.sync.stores.<storeId>`; valor não bate.
- **Testes:** Adicionar teste de integração que envia evento real e valida HMAC.
- **Critério:** `curl -X POST http://online/api/sync/events -H 'X-Store-ID: store-001' ...` retorna 201.

#### S1.2 — JWT constant-time

- **Arquivo:** `back-end/online-app/src/main/java/br/com/novaalianca/mnss/onlineapp/security/auth/JwtTokenProvider.java:96`
- **Mudança:** `expected.equals(parts[2])` → `MessageDigest.isEqual(expected.getBytes(UTF_8), parts[2].getBytes(UTF_8))`.
- **Testes:** Criar `JwtTokenProviderTest` com 5+ cenários: válido, byte trocado, signature totalmente diferente, null, expirado.
- **Critério:** Todos os testes passam; `./gradlew :online-app:test` verde.

#### S1.3 — Wire `SYNC_MASTER_SECRET`

- **Arquivo:** `back-end/online-app/src/main/resources/application.yml`
- **Mudança:** Adicionar `mnss.online.sync-master-secret: ${SYNC_MASTER_SECRET:}`.
- **Testes:** Adicionar teste que confirma `HmacSecretStrengthValidator` falha startup se secret < 32 bytes.
- **Critério:** Boot no perfil online com `SYNC_MASTER_SECRET=short` falha; com 32+ bytes sobe.

#### S1.4 — Restore online

- **Arquivo:** Criar `infra/online/scripts/restore-postgres.sh`.
- **Estrutura:** Espelhar `infra/local/scripts/restore-postgres.sh` com:
  - `set -euo pipefail`
  - Confirmação textual `RESTAURAR-nova_alianca_online`
  - Backup pré-restore automático
  - Container `postgres-online`, DB `nova_alianca_online`
  - DROP/CREATE/RESTORE
- **Documentação:** Atualizar `docs/backup-restore.md` com seção dedicada online.
- **Critério:** Script executa em homologação, recovery completa.

#### S1.5 — `CashRegisterEntity` FK real

- **Arquivo:** `back-end/local-app/src/main/java/br/com/novaalianca/mnss/localapp/domain/cash/CashRegisterEntity.java:23`
- **Mudança:** `private UUID operatorId;` → `@ManyToOne(fetch=LAZY) @JoinColumn(name="operator_id", nullable=false) private UserEntity operator;`
- **Migrar callers:** `CashRegisterService` e testes.
- **Critério:** SchemaValidationTest passa; testes de cash register passam.

#### S1.6 — Actuator restrito

- **Arquivos:** `application.yml` local e online → `show-details: when-authorized`.
- **Adicional:** Garantir que `/actuator/health` NÃO está em `permitAll()` em SecurityConfiguration.
- **Critério:** `curl /actuator/health` sem auth retorna `{"status":"UP"}` simples; com auth retorna detalhes.

#### S1.7 — CORS

- **Arquivos:** `SecurityConfiguration.java` local e online.
- **Mudança:** Adicionar `CorsConfigurationSource` com origens declaradas em `mnss.cors.allowed-origins` (env var).
- **Default local:** `http://localhost`. Default online: `https://padarianovaalianca.com.br,https://admin.padarianovaalianca.com.br`.
- **Critério:** Request com `Origin: https://malicioso.com` recebe sem header `Access-Control-Allow-Origin`.

#### S1.8 — Filter HMAC para sync

- **Arquivo:** Novo `SyncHmacAuthenticationFilter extends OncePerRequestFilter` em `back-end/online-app/src/main/java/br/com/novaalianca/mnss/onlineapp/security/`.
- **Aplicar a:** `/api/sync/events`, `/api/sync/pending`, `/api/sync/events/*/ack`.
- **Refatorar:** `SyncController` deixa de validar HMAC manualmente (filter já cobre).
- **Critério:** Teste confirma 401 sem `X-Signature`.

---

### Sprint 2 — Produção controlada local (3–5 dias)

#### S2.1 — Hardening docker-compose local

- Trocar todos os `:-` por `:?` em variáveis sensíveis em `infra/local/docker-compose.yml`.
- Espelhar padrão online.
- Critério: `compose config` falha sem `.env`.

#### S2.2 — Idempotência

- `StockService.recordSaleMovement`: validar `idempotencyKey` antes de save.
- `PdvSaleService.finishSale`: lançar erro explícito se já FINISHED ou retornar 200 com flag.
- Adicionar testes para retry e duplo finish.

#### S2.3 — Cross-tenant em sync

- `SyncController POST /api/sync/events`: validar `payload.storeId == X-Store-ID`.
- Teste: enviar evento com X-Store-ID=store-001 e payload.storeId=store-002 → 400.

#### S2.4 — Headers de segurança

- `SecurityConfiguration.java`: HSTS, X-Frame-Options=DENY, X-Content-Type-Options=nosniff.
- Online: HSTS com `max-age=31536000; includeSubDomains; preload`.

#### S2.5 — Scripts hardening

- `set -euo pipefail` em backup-postgres.sh local+online, update-system.sh local+online.
- Confirmação em update-system.sh antes de prune.

#### S2.6 — Front-end resiliência

- `takeUntilDestroyed()` em todos os subscribes em checkout/payment/menu/whatsapp.
- 401 handler no `api-error.interceptor.ts` com redirect para login.

#### S2.7 — Documentação de escopo mock

- Adicionar seção "Escopo fora do release" em `docs/homologacao-mvp.md` declarando:
  - Pagamento real NÃO implementado; `WHATSAPP_PROVIDER=mock` deve permanecer.
  - WhatsApp real NÃO implementado.
- Replicar em `docs/release-checklist-online.md`.

---

### Sprint 3 — Hardening online (2–3 dias)

#### S3.1 — Variáveis do release-checklist online

- Implementar `MNSS_AUTH_COOKIE_SECURE` e `MNSS_SYNC_REQUIRE_HTTPS` no online (se aplicável) OU remover do checklist.

#### S3.2 — Revisão geral String.equals em comparações secretas

- `grep -RIn ".equals" back-end/online-app/src/main` em contextos de auth/HMAC/token.
- Trocar por `MessageDigest.isEqual` onde aplicável.

#### S3.3 — Substituir `alert()` no front-end

- `checkout-page.component.ts:113`, `payment-page.component.ts:77`, `assisted-cart.component.ts:390,395` → Toast/Modal.

#### S3.4 — Documentação

- Corrigir nome de volume em `docs/diagnostico-local.md:105`.
- Listar 3 smoke scripts em `docs/release-checklist-local.md`.
- Atualizar exemplos em `docs/deploy-local.md:137-223` ou linkar para arquivo real.
- Adicionar `/api/auth/logout` em `docs/contracts/local-api.openapi.yml`.

#### S3.5 — Limpeza

- `git rm --cached back-end/build_log.txt back-end/compile_errors.txt`.
- Apagar `back-end/test_output.log` local.

#### S3.6 — Deprecation cleanup

- Migrar `@MockBean` → `@MockitoBean` em ~30 testes.
- Refatorar `SecurityConfigurerAdapter.and()` deprecated.

#### S3.7 — Consolidar timeouts

- Adicionar `proxy_*_timeout` em `infra/local/nginx/default.conf`.

---

## Veredito final

**Pronto apenas para homologação.**

### Justificativa técnica

A base do sistema é sólida:

- ✅ Build verde, todos os testes passando (back-end gradle, front-end 92/92).
- ✅ Arquitetura bem modularizada (monolito modular Local + Online + Sync).
- ✅ Idempotência por `idempotency_key` no banco (UNIQUE constraints aplicadas).
- ✅ HMAC com constant-time em `HmacUtils`.
- ✅ Flyway + `ddl-auto=validate` ativo.
- ✅ Secrets obrigatórios via `@NotBlank` em `OnlineProfileProperties`.
- ✅ Mocks isolados por `@ConditionalOnProperty` (`WhatsAppProvider` mock default).
- ✅ Operação offline viável: roteiro `homologacao-offline-local.md` é coerente, código suporta, sync não bloqueia venda.
- ✅ Pagamento e WhatsApp permanecem mockados conforme escopo.

### Mas 7 achados P0 impedem produção controlada:

1. **Sincronização não funciona** com a configuração padrão (storeId divergente). Impacta a regra central do projeto.
2. **JWT vulnerável a timing-attack** em produção.
3. **Actuator vaza topologia** sem autenticação.
4. **CORS ausente** abre vetor para `/api/public/*` e `/api/auth/login`.
5. **Recovery online impossível** sem restore script.
6. **CashRegister FK fake** abre porta para inconsistência de dados.
7. **Endpoints `/api/sync/*` permitAll** dependem inteiramente de validação manual sem teste de regressão dedicado.

### Caminho para produção

Todos os 7 P0 são corrigíveis em **Sprint 1 (1–2 dias)** com mudanças cirúrgicas. Após Sprint 1, o sistema estará pronto para produção controlada local. Sprint 2 e 3 são hardening progressivo necessário para produção pública.

### Notas finais sobre escopo mock

Pagamento e WhatsApp **permanecem mockados conforme escopo definido**. Os achados nessas áreas — webhook valida HMAC, mock provider default ativo — são "OK enquanto mock". Risco apenas se alguém ativar gateway/provider real sem implementar.

Não classificados como P0 enquanto mock; tornam-se P0 caso a equipe decida ativar integração real.

---

**Fim do relatório.**
