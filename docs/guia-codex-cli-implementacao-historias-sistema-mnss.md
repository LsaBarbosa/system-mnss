# Guia de Implementação por Histórias — Codex CLI — Sistema MNSS / Nova Aliança

> Documento operacional para executar o roadmap no **Codex CLI** dentro do repositório do projeto.

Este guia detalha as 106 histórias do roadmap e transforma cada uma em uma unidade de implementação executável.

---

## 1. Como usar no Codex CLI

Use **uma história por vez**. A execução recomendada é:

```bash
git checkout -b feature/SXX-HYY-descricao-curta
codex
```

Dentro do Codex CLI, cole o bloco da história correspondente e peça para ele implementar apenas aquele escopo.

### Regras globais para o Codex

1. O projeto deve seguir **monólito modular local + online**, sem microserviços no início.
2. O ambiente local é crítico: **PDV, caixa, KDS, impressão e banco local não podem depender da internet**.
3. PostgreSQL, RabbitMQ e Redis **não devem ser expostos publicamente**.
4. Toda regra crítica deve ficar no **back-end**.
5. Front-end pode validar e melhorar UX, mas não substitui validação server-side.
6. Valores monetários devem usar `BigDecimal` no Java e `NUMERIC(12,2)` no banco.
7. IDs principais devem ser UUID.
8. Eventos de sincronização, webhooks e integrações devem ser idempotentes.
9. Toda ação crítica deve gerar auditoria quando previsto.
10. Cada história deve terminar com testes unitários do back-end e do front-end quando aplicável.
11. Back-end deve seguir **arquitetura modular em camadas** (web, service, entity, repository, dto).
12. Controllers não devem conter regra de negócio; Services concentram a lógica de aplicação e negócio; Entities representam persistência; Repositories ficam isolados por módulo; DTOs não expõem entidades diretamente.
13. Front-end deve seguir **arquitetura Angular por features**: `domain`, `application`, `data-access`, `ui` e `pages`.
14. Componentes `ui` não acessam `HttpClient`; integrações HTTP ficam em `data-access`.

### Nota de estrutura do front-end

Quando este guia mencionar "admin", "site", "pdv" e "kds", tratar como **features dentro de um único app Angular** em `front-end/`, e não como múltiplos apps separados.

---

## 2. Documentos de referência usados

- **ARQ** — `arquitetura.md`: arquitetura híbrida local + online, monólito modular, back-end em camadas, front-end por features/camadas, comunicação HTTPS e separação entre operação local e canais online.
- **DOM** — `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- **BD** — `banco-de-dados.md`: PostgreSQL, Flyway, UUID, timestamps, NUMERIC para dinheiro, tabelas e índices iniciais.
- **SYNC** — `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.
- **PDV** — `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- **KDS** — `kds.md`: tickets por setor, WebSocket, status de preparo, tempo de espera e operação local da cozinha.
- **FLUXOS** — `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.
- **DL** — `deploy-local.md`: Docker Compose local, Nginx, health checks, backup, IP fixo e segurança local.
- **DO** — `deploy-online.md`: VPS Hostinger, Nginx, HTTPS, Docker Compose, webhooks, CI/CD, backup e segurança online.
- **HW** — `hardware.md`: servidor local, PDV, KDS, impressoras, gaveta, rede, nobreak e contingência.
- **README** — `README.md`: visão geral do projeto, módulos sugeridos e ordem de leitura da documentação.

---

## 3. Padrão de entrega por história

Para cada história, o Codex deve entregar:

- Código de produção.
- Testes unitários e/ou integração mínima quando houver persistência.
- Contratos DTO/API atualizados.
- Ajustes no front-end correspondente.
- Lista de arquivos alterados.
- Comandos de teste executados.
- Pendências explícitas, sem esconder limitações.

### Comandos recomendados

Ajuste conforme o projeto real:

```bash
# back-end
cd back-end
./gradlew test
./gradlew check

# front-end
cd front-end
npm install
npm test
npm run build
```

---

# 4. Histórias detalhadas para execução



---

## Sprint 01 — Fundação técnica

**Objetivo da sprint:** deixar a base do projeto preparada para evolução modular.


### S01-H01 — Como dev, quero criar o monorepo para organizar back-end, front-end, infra e docs.

**Domínio técnico:** Fundação técnica

**Documentos que justificam a implementação:**
- `arquitetura.md`: arquitetura híbrida local + online, monólito modular, back-end em camadas, front-end por features/camadas, comunicação HTTPS e separação entre operação local e canais online.
- `README.md`: visão geral do projeto, módulos sugeridos e ordem de leitura da documentação.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar estrutura `back-end/`, `infra/`, `docs/`.
- Front-end planejado no roadmap: Criar estrutura `front-end/` com apps `admin`, `pdv`, `kds`, `site-publico`.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: back-end/core-domain, back-end/local-app, back-end/online-app e back-end/shared-infra

Front-end:
- `front-end/` — app/feature alvo: front-end/admin, front-end/pdv, front-end/kds e front-end/site-publico

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar estrutura `back-end/`, `infra/`, `docs/`.
3. Implementar o front-end previsto: Criar estrutura `front-end/` com apps `admin`, `pdv`, `kds`, `site-publico`.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- manter monólito modular, sem criar microserviços.
- aplicar arquitetura modular em camadas no back-end: web -> service -> entity/repository.
- separar domínio, aplicação, portas e adapters de infraestrutura.
- aplicar arquitetura front-end por features com domain, application, data-access, ui e pages.
- deixar build e testes executáveis desde a primeira entrega.
- não implementar regra funcional de negócio antes da base estar compilando.

**Testes obrigatórios:**
- Back-end: Teste de arquitetura verificando pacotes obrigatórios com ArchUnit.
- Front-end: Teste simples validando que cada app Angular inicial renderiza o shell.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S01-H01: Como dev, quero criar o monorepo para organizar back-end, front-end, infra e docs.

Contexto obrigatório:
- Sprint: Sprint 01 — Fundação técnica
- Domínio: Fundação técnica
- Documentos de referência: ARQ, README
- Back-end esperado: Criar estrutura `back-end/`, `infra/`, `docs/`.
- Front-end esperado: Criar estrutura `front-end/` com apps `admin`, `pdv`, `kds`, `site-publico`.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: ARQ, README.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Teste de arquitetura verificando pacotes obrigatórios com ArchUnit..
5. Crie/ajuste testes unitários do front-end: Teste simples validando que cada app Angular inicial renderiza o shell..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S01-H02 — Como dev, quero criar o bootstrap da API local.

**Domínio técnico:** Fundação técnica

**Documentos que justificam a implementação:**
- `arquitetura.md`: arquitetura híbrida local + online, monólito modular, back-end em camadas, front-end por features/camadas, comunicação HTTPS e separação entre operação local e canais online.
- `deploy-local.md`: Docker Compose local, Nginx, health checks, backup, IP fixo e segurança local.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar `local-app` Spring Boot com `/actuator/health` e `/api/ping`.
- Front-end planejado no roadmap: Criar environment local apontando para API local.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: back-end/core-domain, back-end/local-app, back-end/online-app e back-end/shared-infra

Front-end:
- `front-end/` — app/feature alvo: front-end/admin, front-end/pdv, front-end/kds e front-end/site-publico

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar `local-app` Spring Boot com `/actuator/health` e `/api/ping`.
3. Implementar o front-end previsto: Criar environment local apontando para API local.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- manter monólito modular, sem criar microserviços.
- aplicar arquitetura modular em camadas no back-end: web -> service -> entity/repository.
- separar domínio, aplicação, portas e adapters de infraestrutura.
- aplicar arquitetura front-end por features com domain, application, data-access, ui e pages.
- deixar build e testes executáveis desde a primeira entrega.
- não implementar regra funcional de negócio antes da base estar compilando.

**Testes obrigatórios:**
- Back-end: Context load; `/api/ping` retorna 200; profile `local` carrega.
- Front-end: Service HTTP chama `/api/ping` e trata sucesso/erro.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S01-H02: Como dev, quero criar o bootstrap da API local.

Contexto obrigatório:
- Sprint: Sprint 01 — Fundação técnica
- Domínio: Fundação técnica
- Documentos de referência: ARQ, DL
- Back-end esperado: Criar `local-app` Spring Boot com `/actuator/health` e `/api/ping`.
- Front-end esperado: Criar environment local apontando para API local.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: ARQ, DL.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Context load; `/api/ping` retorna 200; profile `local` carrega..
5. Crie/ajuste testes unitários do front-end: Service HTTP chama `/api/ping` e trata sucesso/erro..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S01-H03 — Como dev, quero criar o bootstrap da API online.

**Domínio técnico:** Fundação técnica

**Documentos que justificam a implementação:**
- `arquitetura.md`: arquitetura híbrida local + online, monólito modular, back-end em camadas, front-end por features/camadas, comunicação HTTPS e separação entre operação local e canais online.
- `deploy-online.md`: VPS Hostinger, Nginx, HTTPS, Docker Compose, webhooks, CI/CD, backup e segurança online.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar `online-app` Spring Boot com profile `online`.
- Front-end planejado no roadmap: Criar environment online para `site-publico` e `admin`.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: back-end/core-domain, back-end/local-app, back-end/online-app e back-end/shared-infra

Front-end:
- `front-end/` — app/feature alvo: front-end/admin, front-end/pdv, front-end/kds e front-end/site-publico

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar `online-app` Spring Boot com profile `online`.
3. Implementar o front-end previsto: Criar environment online para `site-publico` e `admin`.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- manter monólito modular, sem criar microserviços.
- aplicar arquitetura modular em camadas no back-end: web -> service -> entity/repository.
- separar domínio, aplicação, portas e adapters de infraestrutura.
- aplicar arquitetura front-end por features com domain, application, data-access, ui e pages.
- deixar build e testes executáveis desde a primeira entrega.
- não implementar regra funcional de negócio antes da base estar compilando.

**Testes obrigatórios:**
- Back-end: Context load; profile `online` exige variáveis obrigatórias.
- Front-end: Configuração de environment é carregada corretamente.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S01-H03: Como dev, quero criar o bootstrap da API online.

Contexto obrigatório:
- Sprint: Sprint 01 — Fundação técnica
- Domínio: Fundação técnica
- Documentos de referência: ARQ, DO
- Back-end esperado: Criar `online-app` Spring Boot com profile `online`.
- Front-end esperado: Criar environment online para `site-publico` e `admin`.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: ARQ, DO.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Context load; profile `online` exige variáveis obrigatórias..
5. Crie/ajuste testes unitários do front-end: Configuração de environment é carregada corretamente..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S01-H04 — Como dev, quero padronizar resposta de erro.

**Domínio técnico:** Fundação técnica

**Documentos que justificam a implementação:**
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar `ApiError`, `BusinessException`, `GlobalExceptionHandler`.
- Front-end planejado no roadmap: Criar componente/shared service para exibir erro padronizado.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: back-end/core-domain, back-end/local-app, back-end/online-app e back-end/shared-infra

Front-end:
- `front-end/` — app/feature alvo: front-end/admin, front-end/pdv, front-end/kds e front-end/site-publico

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar `ApiError`, `BusinessException`, `GlobalExceptionHandler`.
3. Implementar o front-end previsto: Criar componente/shared service para exibir erro padronizado.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- manter monólito modular, sem criar microserviços.
- aplicar arquitetura modular em camadas no back-end: web -> service -> entity/repository.
- separar domínio, aplicação, portas e adapters de infraestrutura.
- aplicar arquitetura front-end por features com domain, application, data-access, ui e pages.
- deixar build e testes executáveis desde a primeira entrega.
- não implementar regra funcional de negócio antes da base estar compilando.

**Testes obrigatórios:**
- Back-end: Validação retorna 400; regra de negócio retorna código esperado; erro inesperado não vaza stacktrace.
- Front-end: Interceptor converte erro HTTP em mensagem exibível.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S01-H04: Como dev, quero padronizar resposta de erro.

Contexto obrigatório:
- Sprint: Sprint 01 — Fundação técnica
- Domínio: Fundação técnica
- Documentos de referência: FLUXOS
- Back-end esperado: Criar `ApiError`, `BusinessException`, `GlobalExceptionHandler`.
- Front-end esperado: Criar componente/shared service para exibir erro padronizado.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Validação retorna 400; regra de negócio retorna código esperado; erro inesperado não vaza stacktrace..
5. Crie/ajuste testes unitários do front-end: Interceptor converte erro HTTP em mensagem exibível..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S01-H05 — Como dev, quero configurar qualidade mínima.

**Domínio técnico:** Fundação técnica

**Documentos que justificam a implementação:**
- `arquitetura.md`: arquitetura híbrida local + online, monólito modular, back-end em camadas, front-end por features/camadas, comunicação HTTPS e separação entre operação local e canais online.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Adicionar JUnit 5, Mockito, Testcontainers, MapStruct, OpenAPI.
- Front-end planejado no roadmap: Adicionar ESLint, Prettier, testes Angular.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: back-end/core-domain, back-end/local-app, back-end/online-app e back-end/shared-infra

Front-end:
- `front-end/` — app/feature alvo: front-end/admin, front-end/pdv, front-end/kds e front-end/site-publico

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Adicionar JUnit 5, Mockito, Testcontainers, MapStruct, OpenAPI.
3. Implementar o front-end previsto: Adicionar ESLint, Prettier, testes Angular.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- manter monólito modular, sem criar microserviços.
- aplicar arquitetura modular em camadas no back-end: web -> service -> entity/repository.
- separar domínio, aplicação, portas e adapters de infraestrutura.
- aplicar arquitetura front-end por features com domain, application, data-access, ui e pages.
- deixar build e testes executáveis desde a primeira entrega.
- não implementar regra funcional de negócio antes da base estar compilando.

**Testes obrigatórios:**
- Back-end: Build falha com teste quebrado; cobertura mínima configurada.
- Front-end: `ng test` executa; componente base renderiza sem erro.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S01-H05: Como dev, quero configurar qualidade mínima.

Contexto obrigatório:
- Sprint: Sprint 01 — Fundação técnica
- Domínio: Fundação técnica
- Documentos de referência: ARQ
- Back-end esperado: Adicionar JUnit 5, Mockito, Testcontainers, MapStruct, OpenAPI.
- Front-end esperado: Adicionar ESLint, Prettier, testes Angular.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: ARQ.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Build falha com teste quebrado; cobertura mínima configurada..
5. Crie/ajuste testes unitários do front-end: `ng test` executa; componente base renderiza sem erro..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~

---

## Sprint 02 — Infra local e online mínima

**Objetivo da sprint:** preparar containers, variáveis e health checks dos ambientes.


### S02-H01 — Como dev, quero subir infraestrutura local com Docker Compose.

**Domínio técnico:** Infraestrutura local/online

**Documentos que justificam a implementação:**
- `deploy-local.md`: Docker Compose local, Nginx, health checks, backup, IP fixo e segurança local.
- `arquitetura.md`: arquitetura híbrida local + online, monólito modular, back-end em camadas, front-end por features/camadas, comunicação HTTPS e separação entre operação local e canais online.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar `infra/local/docker-compose.yml`.
- Front-end planejado no roadmap: Configurar front local para acessar Nginx/API local.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: infra/local, infra/online, Docker Compose, Nginx e configurações de profile

Front-end:
- `front-end/` — app/feature alvo: front-ends servidos por Nginx/containers e environments Angular

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar `infra/local/docker-compose.yml`.
3. Implementar o front-end previsto: Configurar front local para acessar Nginx/API local.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- não expor PostgreSQL, RabbitMQ ou Redis publicamente.
- usar variáveis de ambiente para segredos.
- health checks devem indicar estado real das dependências.
- ambiente local deve continuar operável sem internet.

**Testes obrigatórios:**
- Back-end: Validar propriedades de conexão por profile; falha rápida sem `DB_HOST`.
- Front-end: Environment local possui URL válida.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S02-H01: Como dev, quero subir infraestrutura local com Docker Compose.

Contexto obrigatório:
- Sprint: Sprint 02 — Infra local e online mínima
- Domínio: Infraestrutura local/online
- Documentos de referência: DL, ARQ
- Back-end esperado: Criar `infra/local/docker-compose.yml`.
- Front-end esperado: Configurar front local para acessar Nginx/API local.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DL, ARQ.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Validar propriedades de conexão por profile; falha rápida sem `DB_HOST`..
5. Crie/ajuste testes unitários do front-end: Environment local possui URL válida..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S02-H02 — Como dev, quero subir infraestrutura online base.

**Domínio técnico:** Infraestrutura local/online

**Documentos que justificam a implementação:**
- `deploy-online.md`: VPS Hostinger, Nginx, HTTPS, Docker Compose, webhooks, CI/CD, backup e segurança online.
- `arquitetura.md`: arquitetura híbrida local + online, monólito modular, back-end em camadas, front-end por features/camadas, comunicação HTTPS e separação entre operação local e canais online.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar `infra/online/docker-compose.yml`.
- Front-end planejado no roadmap: Configurar URLs online para site/admin.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: infra/local, infra/online, Docker Compose, Nginx e configurações de profile

Front-end:
- `front-end/` — app/feature alvo: front-ends servidos por Nginx/containers e environments Angular

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar `infra/online/docker-compose.yml`.
3. Implementar o front-end previsto: Configurar URLs online para site/admin.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- não expor PostgreSQL, RabbitMQ ou Redis publicamente.
- usar variáveis de ambiente para segredos.
- health checks devem indicar estado real das dependências.
- ambiente local deve continuar operável sem internet.

**Testes obrigatórios:**
- Back-end: Profile online exige `JWT_SECRET` e `SYNC_MASTER_SECRET`.
- Front-end: Environment produção não usa localhost.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S02-H02: Como dev, quero subir infraestrutura online base.

Contexto obrigatório:
- Sprint: Sprint 02 — Infra local e online mínima
- Domínio: Infraestrutura local/online
- Documentos de referência: DO, ARQ
- Back-end esperado: Criar `infra/online/docker-compose.yml`.
- Front-end esperado: Configurar URLs online para site/admin.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DO, ARQ.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Profile online exige `JWT_SECRET` e `SYNC_MASTER_SECRET`..
5. Crie/ajuste testes unitários do front-end: Environment produção não usa localhost..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S02-H03 — Como operador técnico, quero health check técnico.

**Domínio técnico:** Infraestrutura local/online

**Documentos que justificam a implementação:**
- `deploy-local.md`: Docker Compose local, Nginx, health checks, backup, IP fixo e segurança local.
- `deploy-online.md`: VPS Hostinger, Nginx, HTTPS, Docker Compose, webhooks, CI/CD, backup e segurança online.
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Configurar Actuator health para DB, Redis e RabbitMQ.
- Front-end planejado no roadmap: Criar tela simples de status técnico no admin.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: infra/local, infra/online, Docker Compose, Nginx e configurações de profile

Front-end:
- `front-end/` — app/feature alvo: front-ends servidos por Nginx/containers e environments Angular

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Configurar Actuator health para DB, Redis e RabbitMQ.
3. Implementar o front-end previsto: Criar tela simples de status técnico no admin.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- não expor PostgreSQL, RabbitMQ ou Redis publicamente.
- usar variáveis de ambiente para segredos.
- health checks devem indicar estado real das dependências.
- ambiente local deve continuar operável sem internet.

**Testes obrigatórios:**
- Back-end: Health retorna `UP` com mocks; retorna `DOWN` quando dependência falha.
- Front-end: Componente mostra `Online`, `Instável`, `Offline`.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S02-H03: Como operador técnico, quero health check técnico.

Contexto obrigatório:
- Sprint: Sprint 02 — Infra local e online mínima
- Domínio: Infraestrutura local/online
- Documentos de referência: DL, DO, SYNC
- Back-end esperado: Configurar Actuator health para DB, Redis e RabbitMQ.
- Front-end esperado: Criar tela simples de status técnico no admin.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DL, DO, SYNC.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Health retorna `UP` com mocks; retorna `DOWN` quando dependência falha..
5. Crie/ajuste testes unitários do front-end: Componente mostra `Online`, `Instável`, `Offline`..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S02-H04 — Como dev, quero Nginx local inicial.

**Domínio técnico:** Infraestrutura local/online

**Documentos que justificam a implementação:**
- `deploy-local.md`: Docker Compose local, Nginx, health checks, backup, IP fixo e segurança local.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Configurar proxy para API local.
- Front-end planejado no roadmap: Configurar build estático dos apps locais.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: infra/local, infra/online, Docker Compose, Nginx e configurações de profile

Front-end:
- `front-end/` — app/feature alvo: front-ends servidos por Nginx/containers e environments Angular

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Configurar proxy para API local.
3. Implementar o front-end previsto: Configurar build estático dos apps locais.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- não expor PostgreSQL, RabbitMQ ou Redis publicamente.
- usar variáveis de ambiente para segredos.
- health checks devem indicar estado real das dependências.
- ambiente local deve continuar operável sem internet.

**Testes obrigatórios:**
- Back-end: Validação de propriedades de CORS/proxy.
- Front-end: Testar carregamento de base href.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S02-H04: Como dev, quero Nginx local inicial.

Contexto obrigatório:
- Sprint: Sprint 02 — Infra local e online mínima
- Domínio: Infraestrutura local/online
- Documentos de referência: DL
- Back-end esperado: Configurar proxy para API local.
- Front-end esperado: Configurar build estático dos apps locais.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DL.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Validação de propriedades de CORS/proxy..
5. Crie/ajuste testes unitários do front-end: Testar carregamento de base href..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S02-H05 — Como admin, quero scripts básicos de backup.

**Domínio técnico:** Infraestrutura local/online

**Documentos que justificam a implementação:**
- `deploy-local.md`: Docker Compose local, Nginx, health checks, backup, IP fixo e segurança local.
- `deploy-online.md`: VPS Hostinger, Nginx, HTTPS, Docker Compose, webhooks, CI/CD, backup e segurança online.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar scripts `backup-postgres.sh` para local e online.
- Front-end planejado no roadmap: Exibir status do último backup futuramente.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: infra/local, infra/online, Docker Compose, Nginx e configurações de profile

Front-end:
- `front-end/` — app/feature alvo: front-ends servidos por Nginx/containers e environments Angular

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar scripts `backup-postgres.sh` para local e online.
3. Implementar o front-end previsto: Exibir status do último backup futuramente.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- não expor PostgreSQL, RabbitMQ ou Redis publicamente.
- usar variáveis de ambiente para segredos.
- health checks devem indicar estado real das dependências.
- ambiente local deve continuar operável sem internet.

**Testes obrigatórios:**
- Back-end: Service de metadados de backup calcula status válido/atrasado.
- Front-end: Pipe/formatação de data do último backup.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S02-H05: Como admin, quero scripts básicos de backup.

Contexto obrigatório:
- Sprint: Sprint 02 — Infra local e online mínima
- Domínio: Infraestrutura local/online
- Documentos de referência: DL, DO
- Back-end esperado: Criar scripts `backup-postgres.sh` para local e online.
- Front-end esperado: Exibir status do último backup futuramente.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DL, DO.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Service de metadados de backup calcula status válido/atrasado..
5. Crie/ajuste testes unitários do front-end: Pipe/formatação de data do último backup..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~

---

## Sprint 03 — Segurança e usuários

**Objetivo da sprint:** permitir login e controle inicial de acesso por perfil.


### S03-H01 — Como usuário interno, quero fazer login.

**Domínio técnico:** Segurança e usuários

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar endpoint `POST /api/auth/login`.
- Front-end planejado no roadmap: Criar tela de login.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: security, users, roles, auth e audit
- `AuthController`, `AuthService`, `UserService`, `RoleService`, `SecurityConfig` quando aplicável
- `User`, `Role`, `UserRepository`, `RoleRepository`, DTOs e mappers

Front-end:
- `front-end/` — app/feature alvo: admin/auth, guards, interceptors e tela de login
- `auth.service.ts`, `auth.interceptor.ts`, `auth.guard.ts`, tela de login e componentes de usuário

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar endpoint `POST /api/auth/login`.
3. Implementar o front-end previsto: Criar tela de login.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- senha nunca pode ser persistida em texto puro.
- usuário inativo não autentica.
- ações críticas devem validar permissão no back-end.
- front-end pode ocultar ação, mas nunca substituir validação server-side.

**Testes obrigatórios:**
- Back-end: Login válido retorna token; senha inválida retorna 401; usuário inativo é bloqueado.
- Front-end: Form inválido bloqueia submit; sucesso armazena token; erro exibe mensagem.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S03-H01: Como usuário interno, quero fazer login.

Contexto obrigatório:
- Sprint: Sprint 03 — Segurança e usuários
- Domínio: Segurança e usuários
- Documentos de referência: DOM, FLUXOS
- Back-end esperado: Criar endpoint `POST /api/auth/login`.
- Front-end esperado: Criar tela de login.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Login válido retorna token; senha inválida retorna 401; usuário inativo é bloqueado..
5. Crie/ajuste testes unitários do front-end: Form inválido bloqueia submit; sucesso armazena token; erro exibe mensagem..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S03-H02 — Como sistema, quero carregar usuário autenticado.

**Domínio técnico:** Segurança e usuários

**Documentos que justificam a implementação:**
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar `GET /api/auth/me`.
- Front-end planejado no roadmap: Criar AuthService e estado do usuário.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: security, users, roles, auth e audit
- `AuthController`, `AuthService`, `UserService`, `RoleService`, `SecurityConfig` quando aplicável
- `User`, `Role`, `UserRepository`, `RoleRepository`, DTOs e mappers

Front-end:
- `front-end/` — app/feature alvo: admin/auth, guards, interceptors e tela de login
- `auth.service.ts`, `auth.interceptor.ts`, `auth.guard.ts`, tela de login e componentes de usuário

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar `GET /api/auth/me`.
3. Implementar o front-end previsto: Criar AuthService e estado do usuário.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- senha nunca pode ser persistida em texto puro.
- usuário inativo não autentica.
- ações críticas devem validar permissão no back-end.
- front-end pode ocultar ação, mas nunca substituir validação server-side.

**Testes obrigatórios:**
- Back-end: Token válido retorna usuário/perfis; token expirado retorna 401.
- Front-end: Guard redireciona não autenticado para login.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S03-H02: Como sistema, quero carregar usuário autenticado.

Contexto obrigatório:
- Sprint: Sprint 03 — Segurança e usuários
- Domínio: Segurança e usuários
- Documentos de referência: FLUXOS
- Back-end esperado: Criar `GET /api/auth/me`.
- Front-end esperado: Criar AuthService e estado do usuário.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Token válido retorna usuário/perfis; token expirado retorna 401..
5. Crie/ajuste testes unitários do front-end: Guard redireciona não autenticado para login..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S03-H03 — Como admin, quero cadastrar usuário interno.

**Domínio técnico:** Segurança e usuários

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar CRUD básico de usuários.
- Front-end planejado no roadmap: Criar tela de listagem/criação de usuário.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: security, users, roles, auth e audit
- `AuthController`, `AuthService`, `UserService`, `RoleService`, `SecurityConfig` quando aplicável
- `User`, `Role`, `UserRepository`, `RoleRepository`, DTOs e mappers

Front-end:
- `front-end/` — app/feature alvo: admin/auth, guards, interceptors e tela de login
- `auth.service.ts`, `auth.interceptor.ts`, `auth.guard.ts`, tela de login e componentes de usuário

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar CRUD básico de usuários.
3. Implementar o front-end previsto: Criar tela de listagem/criação de usuário.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- senha nunca pode ser persistida em texto puro.
- usuário inativo não autentica.
- ações críticas devem validar permissão no back-end.
- front-end pode ocultar ação, mas nunca substituir validação server-side.

**Testes obrigatórios:**
- Back-end: Username duplicado falha; senha é hasheada; perfil obrigatório.
- Front-end: Form exige campos obrigatórios; lista renderiza usuários.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S03-H03: Como admin, quero cadastrar usuário interno.

Contexto obrigatório:
- Sprint: Sprint 03 — Segurança e usuários
- Domínio: Segurança e usuários
- Documentos de referência: DOM, FLUXOS
- Back-end esperado: Criar CRUD básico de usuários.
- Front-end esperado: Criar tela de listagem/criação de usuário.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Username duplicado falha; senha é hasheada; perfil obrigatório..
5. Crie/ajuste testes unitários do front-end: Form exige campos obrigatórios; lista renderiza usuários..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S03-H04 — Como admin, quero atribuir perfis.

**Domínio técnico:** Segurança e usuários

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `banco-de-dados.md`: PostgreSQL, Flyway, UUID, timestamps, NUMERIC para dinheiro, tabelas e índices iniciais.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar vínculo `user_roles`.
- Front-end planejado no roadmap: Criar seleção de perfis no formulário.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: security, users, roles, auth e audit
- `AuthController`, `AuthService`, `UserService`, `RoleService`, `SecurityConfig` quando aplicável
- `User`, `Role`, `UserRepository`, `RoleRepository`, DTOs e mappers

Front-end:
- `front-end/` — app/feature alvo: admin/auth, guards, interceptors e tela de login
- `auth.service.ts`, `auth.interceptor.ts`, `auth.guard.ts`, tela de login e componentes de usuário

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar vínculo `user_roles`.
3. Implementar o front-end previsto: Criar seleção de perfis no formulário.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- senha nunca pode ser persistida em texto puro.
- usuário inativo não autentica.
- ações críticas devem validar permissão no back-end.
- front-end pode ocultar ação, mas nunca substituir validação server-side.

**Testes obrigatórios:**
- Back-end: Usuário sem perfil operacional é recusado; perfil inexistente falha.
- Front-end: Seleção múltipla mantém estado correto.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S03-H04: Como admin, quero atribuir perfis.

Contexto obrigatório:
- Sprint: Sprint 03 — Segurança e usuários
- Domínio: Segurança e usuários
- Documentos de referência: DOM, BD
- Back-end esperado: Criar vínculo `user_roles`.
- Front-end esperado: Criar seleção de perfis no formulário.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, BD.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Usuário sem perfil operacional é recusado; perfil inexistente falha..
5. Crie/ajuste testes unitários do front-end: Seleção múltipla mantém estado correto..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S03-H05 — Como gerente, quero permissões para ações críticas.

**Domínio técnico:** Segurança e usuários

**Documentos que justificam a implementação:**
- `arquitetura.md`: arquitetura híbrida local + online, monólito modular, back-end em camadas, front-end por features/camadas, comunicação HTTPS e separação entre operação local e canais online.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar anotação/validador de permissão.
- Front-end planejado no roadmap: Criar diretiva/guard por perfil.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: security, users, roles, auth e audit
- `AuthController`, `AuthService`, `UserService`, `RoleService`, `SecurityConfig` quando aplicável
- `User`, `Role`, `UserRepository`, `RoleRepository`, DTOs e mappers

Front-end:
- `front-end/` — app/feature alvo: admin/auth, guards, interceptors e tela de login
- `auth.service.ts`, `auth.interceptor.ts`, `auth.guard.ts`, tela de login e componentes de usuário

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar anotação/validador de permissão.
3. Implementar o front-end previsto: Criar diretiva/guard por perfil.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- senha nunca pode ser persistida em texto puro.
- usuário inativo não autentica.
- ações críticas devem validar permissão no back-end.
- front-end pode ocultar ação, mas nunca substituir validação server-side.

**Testes obrigatórios:**
- Back-end: Usuário sem permissão recebe 403; admin acessa tudo.
- Front-end: Botão crítico oculta/desabilita sem perfil.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S03-H05: Como gerente, quero permissões para ações críticas.

Contexto obrigatório:
- Sprint: Sprint 03 — Segurança e usuários
- Domínio: Segurança e usuários
- Documentos de referência: ARQ, FLUXOS
- Back-end esperado: Criar anotação/validador de permissão.
- Front-end esperado: Criar diretiva/guard por perfil.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: ARQ, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Usuário sem permissão recebe 403; admin acessa tudo..
5. Crie/ajuste testes unitários do front-end: Botão crítico oculta/desabilita sem perfil..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~

---

## Sprint 04 — Banco e domínio base

**Objetivo da sprint:** criar o schema inicial e as entidades centrais.


### S04-H01 — Como dev, quero migrations Flyway iniciais.

**Domínio técnico:** Banco e domínio base

**Documentos que justificam a implementação:**
- `banco-de-dados.md`: PostgreSQL, Flyway, UUID, timestamps, NUMERIC para dinheiro, tabelas e índices iniciais.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar migrations `roles`, `users`, `categories`, `products`, `orders`, `payments`, `cash`, `kds`, `sync`, `audit`.
- Front-end planejado no roadmap: Nenhuma tela nova; apenas preparar mocks de contratos.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: core-domain, migrations Flyway, repositories e audit
- `resources/db/migration/Vxxx__*.sql`
- entidades JPA, enums, repositories e migrations Flyway

Front-end:
- `front-end/` — app/feature alvo: models TypeScript, services base e mocks de contrato
- modelos TypeScript em `shared/models` e services HTTP base

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar migrations `roles`, `users`, `categories`, `products`, `orders`, `payments`, `cash`, `kds`, `sync`, `audit`.
3. Implementar o front-end previsto: Nenhuma tela nova; apenas preparar mocks de contratos.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Garantir transições válidas de status e não gerar ticket para item `SEM_PREPARO`.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- usar UUID nas tabelas principais.
- usar BigDecimal/NUMERIC para valores monetários.
- usar created_at e updated_at nas entidades principais.
- evitar exclusão física de registros críticos.

**Testes obrigatórios:**
- Back-end: Migration executa em banco vazio; migration duplicada falha; tabelas esperadas existem.
- Front-end: Testes de contratos mockados validam modelos TypeScript.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S04-H01: Como dev, quero migrations Flyway iniciais.

Contexto obrigatório:
- Sprint: Sprint 04 — Banco e domínio base
- Domínio: Banco e domínio base
- Documentos de referência: BD
- Back-end esperado: Criar migrations `roles`, `users`, `categories`, `products`, `orders`, `payments`, `cash`, `kds`, `sync`, `audit`.
- Front-end esperado: Nenhuma tela nova; apenas preparar mocks de contratos.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: BD.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Migration executa em banco vazio; migration duplicada falha; tabelas esperadas existem..
5. Crie/ajuste testes unitários do front-end: Testes de contratos mockados validam modelos TypeScript..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S04-H02 — Como dev, quero entidades JPA base.

**Domínio técnico:** Banco e domínio base

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `banco-de-dados.md`: PostgreSQL, Flyway, UUID, timestamps, NUMERIC para dinheiro, tabelas e índices iniciais.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar entidades com UUID, `createdAt`, `updatedAt`, enums e BigDecimal.
- Front-end planejado no roadmap: Criar interfaces TypeScript correspondentes.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: core-domain, migrations Flyway, repositories e audit
- `resources/db/migration/Vxxx__*.sql`
- entidades JPA, enums, repositories e migrations Flyway

Front-end:
- `front-end/` — app/feature alvo: models TypeScript, services base e mocks de contrato
- modelos TypeScript em `shared/models` e services HTTP base

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar entidades com UUID, `createdAt`, `updatedAt`, enums e BigDecimal.
3. Implementar o front-end previsto: Criar interfaces TypeScript correspondentes.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- usar UUID nas tabelas principais.
- usar BigDecimal/NUMERIC para valores monetários.
- usar created_at e updated_at nas entidades principais.
- evitar exclusão física de registros críticos.

**Testes obrigatórios:**
- Back-end: Entidade preenche timestamps; BigDecimal não aceita valor negativo onde proibido.
- Front-end: Modelos TypeScript compilam com campos obrigatórios.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S04-H02: Como dev, quero entidades JPA base.

Contexto obrigatório:
- Sprint: Sprint 04 — Banco e domínio base
- Domínio: Banco e domínio base
- Documentos de referência: DOM, BD
- Back-end esperado: Criar entidades com UUID, `createdAt`, `updatedAt`, enums e BigDecimal.
- Front-end esperado: Criar interfaces TypeScript correspondentes.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, BD.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Entidade preenche timestamps; BigDecimal não aceita valor negativo onde proibido..
5. Crie/ajuste testes unitários do front-end: Modelos TypeScript compilam com campos obrigatórios..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S04-H03 — Como dev, quero repositories base.

**Domínio técnico:** Banco e domínio base

**Documentos que justificam a implementação:**
- `banco-de-dados.md`: PostgreSQL, Flyway, UUID, timestamps, NUMERIC para dinheiro, tabelas e índices iniciais.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar repositories principais.
- Front-end planejado no roadmap: Criar services Angular com métodos vazios/mocks.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: core-domain, migrations Flyway, repositories e audit
- `resources/db/migration/Vxxx__*.sql`
- entidades JPA, enums, repositories e migrations Flyway

Front-end:
- `front-end/` — app/feature alvo: models TypeScript, services base e mocks de contrato
- modelos TypeScript em `shared/models` e services HTTP base

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar repositories principais.
3. Implementar o front-end previsto: Criar services Angular com métodos vazios/mocks.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- usar UUID nas tabelas principais.
- usar BigDecimal/NUMERIC para valores monetários.
- usar created_at e updated_at nas entidades principais.
- evitar exclusão física de registros críticos.

**Testes obrigatórios:**
- Back-end: Repository persiste e busca entidade com Testcontainers.
- Front-end: Service usa URL correta e método HTTP esperado.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S04-H03: Como dev, quero repositories base.

Contexto obrigatório:
- Sprint: Sprint 04 — Banco e domínio base
- Domínio: Banco e domínio base
- Documentos de referência: BD
- Back-end esperado: Criar repositories principais.
- Front-end esperado: Criar services Angular com métodos vazios/mocks.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: BD.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Repository persiste e busca entidade com Testcontainers..
5. Crie/ajuste testes unitários do front-end: Service usa URL correta e método HTTP esperado..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S04-H04 — Como sistema, quero seed de roles.

**Domínio técnico:** Banco e domínio base

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `banco-de-dados.md`: PostgreSQL, Flyway, UUID, timestamps, NUMERIC para dinheiro, tabelas e índices iniciais.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar seed de perfis iniciais via Flyway/data initializer.
- Front-end planejado no roadmap: Exibir perfis disponíveis no cadastro de usuário.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: core-domain, migrations Flyway, repositories e audit
- `resources/db/migration/Vxxx__*.sql`
- entidades JPA, enums, repositories e migrations Flyway

Front-end:
- `front-end/` — app/feature alvo: models TypeScript, services base e mocks de contrato
- modelos TypeScript em `shared/models` e services HTTP base

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar seed de perfis iniciais via Flyway/data initializer.
3. Implementar o front-end previsto: Exibir perfis disponíveis no cadastro de usuário.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- usar UUID nas tabelas principais.
- usar BigDecimal/NUMERIC para valores monetários.
- usar created_at e updated_at nas entidades principais.
- evitar exclusão física de registros críticos.

**Testes obrigatórios:**
- Back-end: Roles são criados uma única vez; execução repetida não duplica.
- Front-end: Select de roles carrega opções mockadas.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S04-H04: Como sistema, quero seed de roles.

Contexto obrigatório:
- Sprint: Sprint 04 — Banco e domínio base
- Domínio: Banco e domínio base
- Documentos de referência: DOM, BD
- Back-end esperado: Criar seed de perfis iniciais via Flyway/data initializer.
- Front-end esperado: Exibir perfis disponíveis no cadastro de usuário.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, BD.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Roles são criados uma única vez; execução repetida não duplica..
5. Crie/ajuste testes unitários do front-end: Select de roles carrega opções mockadas..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S04-H05 — Como sistema, quero auditoria técnica base.

**Domínio técnico:** Banco e domínio base

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar `AuditLog` e serviço `AuditService`.
- Front-end planejado no roadmap: Preparar componente futuro de auditoria.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: core-domain, migrations Flyway, repositories e audit
- `resources/db/migration/Vxxx__*.sql`
- entidades JPA, enums, repositories e migrations Flyway

Front-end:
- `front-end/` — app/feature alvo: models TypeScript, services base e mocks de contrato
- modelos TypeScript em `shared/models` e services HTTP base

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar `AuditLog` e serviço `AuditService`.
3. Implementar o front-end previsto: Preparar componente futuro de auditoria.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- usar UUID nas tabelas principais.
- usar BigDecimal/NUMERIC para valores monetários.
- usar created_at e updated_at nas entidades principais.
- evitar exclusão física de registros críticos.

**Testes obrigatórios:**
- Back-end: Audit log grava ação, usuário, entidade e timestamp; não aceita ação vazia.
- Front-end: Componente de tabela renderiza logs mockados.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S04-H05: Como sistema, quero auditoria técnica base.

Contexto obrigatório:
- Sprint: Sprint 04 — Banco e domínio base
- Domínio: Banco e domínio base
- Documentos de referência: DOM, FLUXOS
- Back-end esperado: Criar `AuditLog` e serviço `AuditService`.
- Front-end esperado: Preparar componente futuro de auditoria.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Audit log grava ação, usuário, entidade e timestamp; não aceita ação vazia..
5. Crie/ajuste testes unitários do front-end: Componente de tabela renderiza logs mockados..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~

---

## Sprint 05 — Catálogo: categorias e produtos

**Objetivo da sprint:** cadastrar e consultar categorias/produtos usados por PDV, site, WhatsApp e admin.


### S05-H01 — Como gerente, quero cadastrar categoria.

**Domínio técnico:** Catálogo

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `banco-de-dados.md`: PostgreSQL, Flyway, UUID, timestamps, NUMERIC para dinheiro, tabelas e índices iniciais.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `POST /api/categories`.
- Front-end planejado no roadmap: Tela de criação de categoria.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: catalog/category/product
- controllers e use cases de `Category`, `Product`, `ProductAvailability`
- DTOs request/response e mappers MapStruct

Front-end:
- `front-end/` — app/feature alvo: admin/catalog e integrações iniciais do PDV
- páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `POST /api/categories`.
3. Implementar o front-end previsto: Tela de criação de categoria.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
6. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- categoria inativa não aparece para venda.
- produto inativo não aparece em nenhum canal.
- visibilidade deve respeitar PDV, site e WhatsApp separadamente.
- alteração de preço não pode alterar pedido antigo.

**Testes obrigatórios:**
- Back-end: Nome obrigatório; ordem default; categoria ativa por padrão.
- Front-end: Form valida nome; submit chama service; erro aparece.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S05-H01: Como gerente, quero cadastrar categoria.

Contexto obrigatório:
- Sprint: Sprint 05 — Catálogo: categorias e produtos
- Domínio: Catálogo
- Documentos de referência: DOM, BD, FLUXOS
- Back-end esperado: `POST /api/categories`.
- Front-end esperado: Tela de criação de categoria.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, BD, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Nome obrigatório; ordem default; categoria ativa por padrão..
5. Crie/ajuste testes unitários do front-end: Form valida nome; submit chama service; erro aparece..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S05-H02 — Como gerente, quero editar categoria.

**Domínio técnico:** Catálogo

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `PATCH /api/categories/{id}`.
- Front-end planejado no roadmap: Tela/lista com ação editar.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: catalog/category/product
- controllers e use cases de `Category`, `Product`, `ProductAvailability`
- DTOs request/response e mappers MapStruct

Front-end:
- `front-end/` — app/feature alvo: admin/catalog e integrações iniciais do PDV
- páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `PATCH /api/categories/{id}`.
3. Implementar o front-end previsto: Tela/lista com ação editar.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
6. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- categoria inativa não aparece para venda.
- produto inativo não aparece em nenhum canal.
- visibilidade deve respeitar PDV, site e WhatsApp separadamente.
- alteração de preço não pode alterar pedido antigo.

**Testes obrigatórios:**
- Back-end: Não edita categoria inexistente; atualiza `updatedAt`; preserva ID.
- Front-end: Modal/form edição popula valores.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S05-H02: Como gerente, quero editar categoria.

Contexto obrigatório:
- Sprint: Sprint 05 — Catálogo: categorias e produtos
- Domínio: Catálogo
- Documentos de referência: DOM, FLUXOS
- Back-end esperado: `PATCH /api/categories/{id}`.
- Front-end esperado: Tela/lista com ação editar.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Não edita categoria inexistente; atualiza `updatedAt`; preserva ID..
5. Crie/ajuste testes unitários do front-end: Modal/form edição popula valores..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S05-H03 — Como gerente, quero definir visibilidade da categoria por canal.

**Domínio técnico:** Catálogo

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Campos `showOnline`, `showOnPdv`, `showOnWhatsapp`.
- Front-end planejado no roadmap: Checkboxes de canal.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: catalog/category/product
- controllers e use cases de `Category`, `Product`, `ProductAvailability`
- DTOs request/response e mappers MapStruct

Front-end:
- `front-end/` — app/feature alvo: admin/catalog e integrações iniciais do PDV
- páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Campos `showOnline`, `showOnPdv`, `showOnWhatsapp`.
3. Implementar o front-end previsto: Checkboxes de canal.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
7. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
8. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
9. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- categoria inativa não aparece para venda.
- produto inativo não aparece em nenhum canal.
- visibilidade deve respeitar PDV, site e WhatsApp separadamente.
- alteração de preço não pode alterar pedido antigo.

**Testes obrigatórios:**
- Back-end: Categoria oculta no PDV não aparece em consulta PDV.
- Front-end: Checkboxes refletem estado e atualizam payload.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S05-H03: Como gerente, quero definir visibilidade da categoria por canal.

Contexto obrigatório:
- Sprint: Sprint 05 — Catálogo: categorias e produtos
- Domínio: Catálogo
- Documentos de referência: DOM, FLUXOS
- Back-end esperado: Campos `showOnline`, `showOnPdv`, `showOnWhatsapp`.
- Front-end esperado: Checkboxes de canal.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Categoria oculta no PDV não aparece em consulta PDV..
5. Crie/ajuste testes unitários do front-end: Checkboxes refletem estado e atualizam payload..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S05-H04 — Como gerente, quero cadastrar produto.

**Domínio técnico:** Catálogo

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `banco-de-dados.md`: PostgreSQL, Flyway, UUID, timestamps, NUMERIC para dinheiro, tabelas e índices iniciais.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `POST /api/products`.
- Front-end planejado no roadmap: Tela de criação de produto.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: catalog/category/product
- controllers e use cases de `Category`, `Product`, `ProductAvailability`
- DTOs request/response e mappers MapStruct

Front-end:
- `front-end/` — app/feature alvo: admin/catalog e integrações iniciais do PDV
- páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `POST /api/products`.
3. Implementar o front-end previsto: Tela de criação de produto.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
6. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- categoria inativa não aparece para venda.
- produto inativo não aparece em nenhum canal.
- visibilidade deve respeitar PDV, site e WhatsApp separadamente.
- alteração de preço não pode alterar pedido antigo.

**Testes obrigatórios:**
- Back-end: Preço obrigatório; categoria deve existir; unidade obrigatória; setor obrigatório.
- Front-end: Form valida preço, categoria, unidade e setor.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S05-H04: Como gerente, quero cadastrar produto.

Contexto obrigatório:
- Sprint: Sprint 05 — Catálogo: categorias e produtos
- Domínio: Catálogo
- Documentos de referência: DOM, BD, FLUXOS
- Back-end esperado: `POST /api/products`.
- Front-end esperado: Tela de criação de produto.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, BD, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Preço obrigatório; categoria deve existir; unidade obrigatória; setor obrigatório..
5. Crie/ajuste testes unitários do front-end: Form valida preço, categoria, unidade e setor..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S05-H05 — Como gerente, quero editar produto.

**Domínio técnico:** Catálogo

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `PATCH /api/products/{id}`.
- Front-end planejado no roadmap: Tela/lista com filtros e edição.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: catalog/category/product
- controllers e use cases de `Category`, `Product`, `ProductAvailability`
- DTOs request/response e mappers MapStruct

Front-end:
- `front-end/` — app/feature alvo: admin/catalog e integrações iniciais do PDV
- páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `PATCH /api/products/{id}`.
3. Implementar o front-end previsto: Tela/lista com filtros e edição.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
6. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- categoria inativa não aparece para venda.
- produto inativo não aparece em nenhum canal.
- visibilidade deve respeitar PDV, site e WhatsApp separadamente.
- alteração de preço não pode alterar pedido antigo.

**Testes obrigatórios:**
- Back-end: Alterar preço gera evento `PRODUCT_PRICE_CHANGED`; produto inexistente retorna erro.
- Front-end: Filtro por nome/categoria funciona com dados mockados.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S05-H05: Como gerente, quero editar produto.

Contexto obrigatório:
- Sprint: Sprint 05 — Catálogo: categorias e produtos
- Domínio: Catálogo
- Documentos de referência: DOM, SYNC
- Back-end esperado: `PATCH /api/products/{id}`.
- Front-end esperado: Tela/lista com filtros e edição.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, SYNC.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Alterar preço gera evento `PRODUCT_PRICE_CHANGED`; produto inexistente retorna erro..
5. Crie/ajuste testes unitários do front-end: Filtro por nome/categoria funciona com dados mockados..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S05-H06 — Como operador, quero buscar produto por código de barras.

**Domínio técnico:** Catálogo

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `GET /api/products/barcode/{barcode}`.
- Front-end planejado no roadmap: Campo de leitura/código no PDV.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: catalog/category/product
- controllers e use cases de `Category`, `Product`, `ProductAvailability`
- DTOs request/response e mappers MapStruct

Front-end:
- `front-end/` — app/feature alvo: admin/catalog e integrações iniciais do PDV
- páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `GET /api/products/barcode/{barcode}`.
3. Implementar o front-end previsto: Campo de leitura/código no PDV.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- categoria inativa não aparece para venda.
- produto inativo não aparece em nenhum canal.
- visibilidade deve respeitar PDV, site e WhatsApp separadamente.
- alteração de preço não pode alterar pedido antigo.

**Testes obrigatórios:**
- Back-end: Produto inativo não retorna como vendável; barcode inexistente retorna 404.
- Front-end: Campo captura Enter e chama service.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S05-H06: Como operador, quero buscar produto por código de barras.

Contexto obrigatório:
- Sprint: Sprint 05 — Catálogo: categorias e produtos
- Domínio: Catálogo
- Documentos de referência: PDV, DOM
- Back-end esperado: `GET /api/products/barcode/{barcode}`.
- Front-end esperado: Campo de leitura/código no PDV.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV, DOM.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Produto inativo não retorna como vendável; barcode inexistente retorna 404..
5. Crie/ajuste testes unitários do front-end: Campo captura Enter e chama service..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~

---

## Sprint 06 — Disponibilidade e cardápio interno

**Objetivo da sprint:** controlar disponibilidade real dos produtos e refletir por canal.


### S06-H01 — Como atendente, quero marcar produto indisponível.

**Domínio técnico:** Disponibilidade

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `PATCH /api/products/{id}/availability`.
- Front-end planejado no roadmap: Tela de disponibilidade por produto.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: catalog/availability e sync event
- controllers e use cases de `Category`, `Product`, `ProductAvailability`
- DTOs request/response e mappers MapStruct

Front-end:
- `front-end/` — app/feature alvo: admin/availability e indicadores no PDV
- páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `PATCH /api/products/{id}/availability`.
3. Implementar o front-end previsto: Tela de disponibilidade por produto.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- ambiente local manda na disponibilidade real.
- produto indisponível localmente deve refletir no online por sincronização.
- alteração crítica de disponibilidade deve gerar auditoria.
- produto sob encomenda pode ter regra diferente de estoque imediato.

**Testes obrigatórios:**
- Back-end: Produto indisponível exige motivo conforme status; salva usuário responsável.
- Front-end: Toggle altera estado visual e envia motivo.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S06-H01: Como atendente, quero marcar produto indisponível.

Contexto obrigatório:
- Sprint: Sprint 06 — Disponibilidade e cardápio interno
- Domínio: Disponibilidade
- Documentos de referência: DOM, FLUXOS
- Back-end esperado: `PATCH /api/products/{id}/availability`.
- Front-end esperado: Tela de disponibilidade por produto.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Produto indisponível exige motivo conforme status; salva usuário responsável..
5. Crie/ajuste testes unitários do front-end: Toggle altera estado visual e envia motivo..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S06-H02 — Como sistema, quero bloquear produto indisponível no online.

**Domínio técnico:** Disponibilidade

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Regra de consulta pública filtra indisponíveis.
- Front-end planejado no roadmap: Preview de produto indisponível no admin.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: catalog/availability e sync event
- controllers e use cases de `Category`, `Product`, `ProductAvailability`
- DTOs request/response e mappers MapStruct

Front-end:
- `front-end/` — app/feature alvo: admin/availability e indicadores no PDV
- páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Regra de consulta pública filtra indisponíveis.
3. Implementar o front-end previsto: Preview de produto indisponível no admin.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- ambiente local manda na disponibilidade real.
- produto indisponível localmente deve refletir no online por sincronização.
- alteração crítica de disponibilidade deve gerar auditoria.
- produto sob encomenda pode ter regra diferente de estoque imediato.

**Testes obrigatórios:**
- Back-end: Produto `UNAVAILABLE` não aparece em `sellOnline`.
- Front-end: Badge “Indisponível” aparece corretamente.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S06-H02: Como sistema, quero bloquear produto indisponível no online.

Contexto obrigatório:
- Sprint: Sprint 06 — Disponibilidade e cardápio interno
- Domínio: Disponibilidade
- Documentos de referência: DOM, SYNC
- Back-end esperado: Regra de consulta pública filtra indisponíveis.
- Front-end esperado: Preview de produto indisponível no admin.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, SYNC.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Produto `UNAVAILABLE` não aparece em `sellOnline`..
5. Crie/ajuste testes unitários do front-end: Badge “Indisponível” aparece corretamente..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S06-H03 — Como operador, quero listar apenas produtos vendáveis no PDV.

**Domínio técnico:** Disponibilidade

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `GET /api/pdv/products`.
- Front-end planejado no roadmap: Grid/lista de produtos no PDV.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: catalog/availability e sync event
- controllers e use cases de `Category`, `Product`, `ProductAvailability`
- DTOs request/response e mappers MapStruct

Front-end:
- `front-end/` — app/feature alvo: admin/availability e indicadores no PDV
- páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `GET /api/pdv/products`.
3. Implementar o front-end previsto: Grid/lista de produtos no PDV.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- ambiente local manda na disponibilidade real.
- produto indisponível localmente deve refletir no online por sincronização.
- alteração crítica de disponibilidade deve gerar auditoria.
- produto sob encomenda pode ter regra diferente de estoque imediato.

**Testes obrigatórios:**
- Back-end: Produto inativo é removido; produto sem `sellOnPdv` não aparece.
- Front-end: Lista renderiza produtos agrupados por categoria.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S06-H03: Como operador, quero listar apenas produtos vendáveis no PDV.

Contexto obrigatório:
- Sprint: Sprint 06 — Disponibilidade e cardápio interno
- Domínio: Disponibilidade
- Documentos de referência: PDV, DOM
- Back-end esperado: `GET /api/pdv/products`.
- Front-end esperado: Grid/lista de produtos no PDV.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV, DOM.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Produto inativo é removido; produto sem `sellOnPdv` não aparece..
5. Crie/ajuste testes unitários do front-end: Lista renderiza produtos agrupados por categoria..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S06-H04 — Como sistema, quero criar evento de sync ao mudar disponibilidade.

**Domínio técnico:** Disponibilidade

**Documentos que justificam a implementação:**
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar `SyncEvent PRODUCT_UNAVAILABLE/AVAILABLE`.
- Front-end planejado no roadmap: Exibir indicador “pendente de sincronização”.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: catalog/availability e sync event
- controllers e use cases de `Category`, `Product`, `ProductAvailability`
- DTOs request/response e mappers MapStruct

Front-end:
- `front-end/` — app/feature alvo: admin/availability e indicadores no PDV
- páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar `SyncEvent PRODUCT_UNAVAILABLE/AVAILABLE`.
3. Implementar o front-end previsto: Exibir indicador “pendente de sincronização”.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- ambiente local manda na disponibilidade real.
- produto indisponível localmente deve refletir no online por sincronização.
- alteração crítica de disponibilidade deve gerar auditoria.
- produto sob encomenda pode ter regra diferente de estoque imediato.

**Testes obrigatórios:**
- Back-end: Alteração cria evento PENDING; falha não desfaz alteração local.
- Front-end: Indicador renderiza pendente/sincronizado.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S06-H04: Como sistema, quero criar evento de sync ao mudar disponibilidade.

Contexto obrigatório:
- Sprint: Sprint 06 — Disponibilidade e cardápio interno
- Domínio: Disponibilidade
- Documentos de referência: SYNC, FLUXOS
- Back-end esperado: Criar `SyncEvent PRODUCT_UNAVAILABLE/AVAILABLE`.
- Front-end esperado: Exibir indicador “pendente de sincronização”.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: SYNC, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Alteração cria evento PENDING; falha não desfaz alteração local..
5. Crie/ajuste testes unitários do front-end: Indicador renderiza pendente/sincronizado..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S06-H05 — Como gerente, quero auditar alteração de disponibilidade.

**Domínio técnico:** Disponibilidade

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar `AuditLog` na alteração.
- Front-end planejado no roadmap: Futuro painel mostra histórico.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: catalog/availability e sync event
- controllers e use cases de `Category`, `Product`, `ProductAvailability`
- DTOs request/response e mappers MapStruct

Front-end:
- `front-end/` — app/feature alvo: admin/availability e indicadores no PDV
- páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar `AuditLog` na alteração.
3. Implementar o front-end previsto: Futuro painel mostra histórico.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
6. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- ambiente local manda na disponibilidade real.
- produto indisponível localmente deve refletir no online por sincronização.
- alteração crítica de disponibilidade deve gerar auditoria.
- produto sob encomenda pode ter regra diferente de estoque imediato.

**Testes obrigatórios:**
- Back-end: Audit contém usuário, oldValue, newValue e entityId.
- Front-end: Tabela de histórico renderiza mocks.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S06-H05: Como gerente, quero auditar alteração de disponibilidade.

Contexto obrigatório:
- Sprint: Sprint 06 — Disponibilidade e cardápio interno
- Domínio: Disponibilidade
- Documentos de referência: DOM, FLUXOS
- Back-end esperado: Criar `AuditLog` na alteração.
- Front-end esperado: Futuro painel mostra histórico.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Audit contém usuário, oldValue, newValue e entityId..
5. Crie/ajuste testes unitários do front-end: Tabela de histórico renderiza mocks..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~

---

## Sprint 07 — Caixa

**Objetivo da sprint:** controlar abertura, movimentações e fechamento do caixa.


### S07-H01 — Como operador, quero abrir caixa.

**Domínio técnico:** Caixa

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `POST /api/cash-register/open`.
- Front-end planejado no roadmap: Tela/modal de abertura no PDV.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: cash/cash-register/cash-movement
- `CashRegisterController`, `CashRegisterService`, `CashMovementService`, repositories e DTOs

Front-end:
- `front-end/` — app/feature alvo: pdv/cash e admin/cash-summary
- componentes de abertura/fechamento, sangria, suprimento e resumo de caixa no app PDV

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `POST /api/cash-register/open`.
3. Implementar o front-end previsto: Tela/modal de abertura no PDV.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- não vender sem caixa aberto quando a configuração exigir.
- caixa fechado não recebe movimentação.
- diferença de caixa deve ser registrada.
- sangria e suprimento devem ter usuário e motivo quando exigido.

**Testes obrigatórios:**
- Back-end: Valor inicial obrigatório; usuário sem permissão falha; caixa aberto duplicado é bloqueado.
- Front-end: Form exige valor inicial; sucesso libera PDV.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S07-H01: Como operador, quero abrir caixa.

Contexto obrigatório:
- Sprint: Sprint 07 — Caixa
- Domínio: Caixa
- Documentos de referência: PDV, DOM, FLUXOS
- Back-end esperado: `POST /api/cash-register/open`.
- Front-end esperado: Tela/modal de abertura no PDV.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV, DOM, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Valor inicial obrigatório; usuário sem permissão falha; caixa aberto duplicado é bloqueado..
5. Crie/ajuste testes unitários do front-end: Form exige valor inicial; sucesso libera PDV..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S07-H02 — Como operador, quero consultar caixa atual.

**Domínio técnico:** Caixa

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `GET /api/cash-register/current`.
- Front-end planejado no roadmap: Header do PDV mostra caixa aberto/fechado.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: cash/cash-register/cash-movement
- `CashRegisterController`, `CashRegisterService`, `CashMovementService`, repositories e DTOs

Front-end:
- `front-end/` — app/feature alvo: pdv/cash e admin/cash-summary
- componentes de abertura/fechamento, sangria, suprimento e resumo de caixa no app PDV

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `GET /api/cash-register/current`.
3. Implementar o front-end previsto: Header do PDV mostra caixa aberto/fechado.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- não vender sem caixa aberto quando a configuração exigir.
- caixa fechado não recebe movimentação.
- diferença de caixa deve ser registrada.
- sangria e suprimento devem ter usuário e motivo quando exigido.

**Testes obrigatórios:**
- Back-end: Retorna caixa aberto do operador; sem caixa retorna estado vazio.
- Front-end: Header renderiza status corretamente.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S07-H02: Como operador, quero consultar caixa atual.

Contexto obrigatório:
- Sprint: Sprint 07 — Caixa
- Domínio: Caixa
- Documentos de referência: PDV
- Back-end esperado: `GET /api/cash-register/current`.
- Front-end esperado: Header do PDV mostra caixa aberto/fechado.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Retorna caixa aberto do operador; sem caixa retorna estado vazio..
5. Crie/ajuste testes unitários do front-end: Header renderiza status corretamente..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S07-H03 — Como gerente, quero registrar sangria.

**Domínio técnico:** Caixa

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `POST /api/cash-register/{id}/movement` com `CASH_OUT`.
- Front-end planejado no roadmap: Botão/tela de sangria.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: cash/cash-register/cash-movement
- `CashRegisterController`, `CashRegisterService`, `CashMovementService`, repositories e DTOs

Front-end:
- `front-end/` — app/feature alvo: pdv/cash e admin/cash-summary
- componentes de abertura/fechamento, sangria, suprimento e resumo de caixa no app PDV

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `POST /api/cash-register/{id}/movement` com `CASH_OUT`.
3. Implementar o front-end previsto: Botão/tela de sangria.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
6. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- não vender sem caixa aberto quando a configuração exigir.
- caixa fechado não recebe movimentação.
- diferença de caixa deve ser registrada.
- sangria e suprimento devem ter usuário e motivo quando exigido.

**Testes obrigatórios:**
- Back-end: Motivo obrigatório; valor positivo; caixa fechado bloqueia.
- Front-end: Form valida valor e motivo.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S07-H03: Como gerente, quero registrar sangria.

Contexto obrigatório:
- Sprint: Sprint 07 — Caixa
- Domínio: Caixa
- Documentos de referência: PDV, FLUXOS
- Back-end esperado: `POST /api/cash-register/{id}/movement` com `CASH_OUT`.
- Front-end esperado: Botão/tela de sangria.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Motivo obrigatório; valor positivo; caixa fechado bloqueia..
5. Crie/ajuste testes unitários do front-end: Form valida valor e motivo..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S07-H04 — Como gerente, quero registrar suprimento.

**Domínio técnico:** Caixa

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Movimento `CASH_IN`.
- Front-end planejado no roadmap: Botão/tela de suprimento.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: cash/cash-register/cash-movement
- `CashRegisterController`, `CashRegisterService`, `CashMovementService`, repositories e DTOs

Front-end:
- `front-end/` — app/feature alvo: pdv/cash e admin/cash-summary
- componentes de abertura/fechamento, sangria, suprimento e resumo de caixa no app PDV

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Movimento `CASH_IN`.
3. Implementar o front-end previsto: Botão/tela de suprimento.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
6. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- não vender sem caixa aberto quando a configuração exigir.
- caixa fechado não recebe movimentação.
- diferença de caixa deve ser registrada.
- sangria e suprimento devem ter usuário e motivo quando exigido.

**Testes obrigatórios:**
- Back-end: Valor positivo; usuário autorizado; movimento vinculado ao caixa.
- Front-end: Submit chama endpoint correto.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S07-H04: Como gerente, quero registrar suprimento.

Contexto obrigatório:
- Sprint: Sprint 07 — Caixa
- Domínio: Caixa
- Documentos de referência: PDV, FLUXOS
- Back-end esperado: Movimento `CASH_IN`.
- Front-end esperado: Botão/tela de suprimento.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Valor positivo; usuário autorizado; movimento vinculado ao caixa..
5. Crie/ajuste testes unitários do front-end: Submit chama endpoint correto..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S07-H05 — Como operador, quero fechar caixa.

**Domínio técnico:** Caixa

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `POST /api/cash-register/{id}/close`.
- Front-end planejado no roadmap: Tela de fechamento com totais por forma.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: cash/cash-register/cash-movement
- `CashRegisterController`, `CashRegisterService`, `CashMovementService`, repositories e DTOs

Front-end:
- `front-end/` — app/feature alvo: pdv/cash e admin/cash-summary
- componentes de abertura/fechamento, sangria, suprimento e resumo de caixa no app PDV

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `POST /api/cash-register/{id}/close`.
3. Implementar o front-end previsto: Tela de fechamento com totais por forma.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- não vender sem caixa aberto quando a configuração exigir.
- caixa fechado não recebe movimentação.
- diferença de caixa deve ser registrada.
- sangria e suprimento devem ter usuário e motivo quando exigido.

**Testes obrigatórios:**
- Back-end: Calcula esperado; diferença registrada; caixa fechado não recebe movimento.
- Front-end: Tela calcula diferença visualmente; exige justificativa se divergente.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S07-H05: Como operador, quero fechar caixa.

Contexto obrigatório:
- Sprint: Sprint 07 — Caixa
- Domínio: Caixa
- Documentos de referência: PDV, FLUXOS
- Back-end esperado: `POST /api/cash-register/{id}/close`.
- Front-end esperado: Tela de fechamento com totais por forma.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Calcula esperado; diferença registrada; caixa fechado não recebe movimento..
5. Crie/ajuste testes unitários do front-end: Tela calcula diferença visualmente; exige justificativa se divergente..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S07-H06 — Como operador, quero ver resumo do caixa.

**Domínio técnico:** Caixa

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `GET /api/cash-register/{id}/summary`.
- Front-end planejado no roadmap: Tela/impressão de resumo.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: cash/cash-register/cash-movement
- `CashRegisterController`, `CashRegisterService`, `CashMovementService`, repositories e DTOs

Front-end:
- `front-end/` — app/feature alvo: pdv/cash e admin/cash-summary
- componentes de abertura/fechamento, sangria, suprimento e resumo de caixa no app PDV

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `GET /api/cash-register/{id}/summary`.
3. Implementar o front-end previsto: Tela/impressão de resumo.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- não vender sem caixa aberto quando a configuração exigir.
- caixa fechado não recebe movimentação.
- diferença de caixa deve ser registrada.
- sangria e suprimento devem ter usuário e motivo quando exigido.

**Testes obrigatórios:**
- Back-end: Agrupa por método; inclui sangria/suprimento/diferença.
- Front-end: Tabela de resumo renderiza totais.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S07-H06: Como operador, quero ver resumo do caixa.

Contexto obrigatório:
- Sprint: Sprint 07 — Caixa
- Domínio: Caixa
- Documentos de referência: PDV
- Back-end esperado: `GET /api/cash-register/{id}/summary`.
- Front-end esperado: Tela/impressão de resumo.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Agrupa por método; inclui sangria/suprimento/diferença..
5. Crie/ajuste testes unitários do front-end: Tabela de resumo renderiza totais..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~

---

## Sprint 08 — PDV: venda e carrinho

**Objetivo da sprint:** criar venda presencial e manipular itens no carrinho.


### S08-H01 — Como operador, quero iniciar venda.

**Domínio técnico:** PDV carrinho

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `POST /api/pdv/sales`.
- Front-end planejado no roadmap: Botão “Nova venda”.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: pdv/sales/order/order-item
- `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`
- regras de totalização, desconto, finalização e cancelamento

Front-end:
- `front-end/` — app/feature alvo: pdv/sale-screen, product-search e cart
- tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `POST /api/pdv/sales`.
3. Implementar o front-end previsto: Botão “Nova venda”.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- venda não pode ser finalizada sem itens.
- item deve guardar snapshot de nome e preço.
- produto inativo ou não vendável no PDV deve ser bloqueado.
- venda finalizada não pode ser editada como carrinho comum.

**Testes obrigatórios:**
- Back-end: Sem caixa aberto falha; cria Order origem PDV; status CREATED.
- Front-end: Botão cria venda e inicializa carrinho.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S08-H01: Como operador, quero iniciar venda.

Contexto obrigatório:
- Sprint: Sprint 08 — PDV: venda e carrinho
- Domínio: PDV carrinho
- Documentos de referência: PDV, FLUXOS
- Back-end esperado: `POST /api/pdv/sales`.
- Front-end esperado: Botão “Nova venda”.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Sem caixa aberto falha; cria Order origem PDV; status CREATED..
5. Crie/ajuste testes unitários do front-end: Botão cria venda e inicializa carrinho..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S08-H02 — Como operador, quero adicionar produto ao carrinho.

**Domínio técnico:** PDV carrinho

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `POST /api/pdv/sales/{saleId}/items`.
- Front-end planejado no roadmap: Click no produto adiciona item.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: pdv/sales/order/order-item
- `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`
- regras de totalização, desconto, finalização e cancelamento

Front-end:
- `front-end/` — app/feature alvo: pdv/sale-screen, product-search e cart
- tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `POST /api/pdv/sales/{saleId}/items`.
3. Implementar o front-end previsto: Click no produto adiciona item.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- venda não pode ser finalizada sem itens.
- item deve guardar snapshot de nome e preço.
- produto inativo ou não vendável no PDV deve ser bloqueado.
- venda finalizada não pode ser editada como carrinho comum.

**Testes obrigatórios:**
- Back-end: Produto inativo bloqueia; salva snapshot de nome/preço; total calculado.
- Front-end: Carrinho adiciona item e atualiza subtotal.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S08-H02: Como operador, quero adicionar produto ao carrinho.

Contexto obrigatório:
- Sprint: Sprint 08 — PDV: venda e carrinho
- Domínio: PDV carrinho
- Documentos de referência: DOM, PDV
- Back-end esperado: `POST /api/pdv/sales/{saleId}/items`.
- Front-end esperado: Click no produto adiciona item.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, PDV.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Produto inativo bloqueia; salva snapshot de nome/preço; total calculado..
5. Crie/ajuste testes unitários do front-end: Carrinho adiciona item e atualiza subtotal..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S08-H03 — Como operador, quero alterar quantidade.

**Domínio técnico:** PDV carrinho

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `PATCH /api/pdv/sales/{saleId}/items/{itemId}`.
- Front-end planejado no roadmap: Botões `+`, `-` e campo quantidade.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: pdv/sales/order/order-item
- `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`
- regras de totalização, desconto, finalização e cancelamento

Front-end:
- `front-end/` — app/feature alvo: pdv/sale-screen, product-search e cart
- tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `PATCH /api/pdv/sales/{saleId}/items/{itemId}`.
3. Implementar o front-end previsto: Botões `+`, `-` e campo quantidade.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- venda não pode ser finalizada sem itens.
- item deve guardar snapshot de nome e preço.
- produto inativo ou não vendável no PDV deve ser bloqueado.
- venda finalizada não pode ser editada como carrinho comum.

**Testes obrigatórios:**
- Back-end: Quantidade zero/negativa falha; total recalcula; venda finalizada bloqueia.
- Front-end: Quantidade altera total visual.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S08-H03: Como operador, quero alterar quantidade.

Contexto obrigatório:
- Sprint: Sprint 08 — PDV: venda e carrinho
- Domínio: PDV carrinho
- Documentos de referência: PDV, DOM
- Back-end esperado: `PATCH /api/pdv/sales/{saleId}/items/{itemId}`.
- Front-end esperado: Botões `+`, `-` e campo quantidade.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV, DOM.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Quantidade zero/negativa falha; total recalcula; venda finalizada bloqueia..
5. Crie/ajuste testes unitários do front-end: Quantidade altera total visual..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S08-H04 — Como operador, quero remover item.

**Domínio técnico:** PDV carrinho

**Documentos que justificam a implementação:**
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `DELETE /api/pdv/sales/{saleId}/items/{itemId}`.
- Front-end planejado no roadmap: Botão remover item.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: pdv/sales/order/order-item
- `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`
- regras de totalização, desconto, finalização e cancelamento

Front-end:
- `front-end/` — app/feature alvo: pdv/sale-screen, product-search e cart
- tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `DELETE /api/pdv/sales/{saleId}/items/{itemId}`.
3. Implementar o front-end previsto: Botão remover item.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- venda não pode ser finalizada sem itens.
- item deve guardar snapshot de nome e preço.
- produto inativo ou não vendável no PDV deve ser bloqueado.
- venda finalizada não pode ser editada como carrinho comum.

**Testes obrigatórios:**
- Back-end: Item inexistente retorna erro; total recalcula; venda finalizada bloqueia.
- Front-end: Item some do carrinho.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S08-H04: Como operador, quero remover item.

Contexto obrigatório:
- Sprint: Sprint 08 — PDV: venda e carrinho
- Domínio: PDV carrinho
- Documentos de referência: FLUXOS
- Back-end esperado: `DELETE /api/pdv/sales/{saleId}/items/{itemId}`.
- Front-end esperado: Botão remover item.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Item inexistente retorna erro; total recalcula; venda finalizada bloqueia..
5. Crie/ajuste testes unitários do front-end: Item some do carrinho..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S08-H05 — Como operador, quero buscar produto por nome/categoria/código.

**Domínio técnico:** PDV carrinho

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Endpoints de busca PDV.
- Front-end planejado no roadmap: Busca lateral, categorias e input código de barras.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: pdv/sales/order/order-item
- `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`
- regras de totalização, desconto, finalização e cancelamento

Front-end:
- `front-end/` — app/feature alvo: pdv/sale-screen, product-search e cart
- tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Endpoints de busca PDV.
3. Implementar o front-end previsto: Busca lateral, categorias e input código de barras.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- venda não pode ser finalizada sem itens.
- item deve guardar snapshot de nome e preço.
- produto inativo ou não vendável no PDV deve ser bloqueado.
- venda finalizada não pode ser editada como carrinho comum.

**Testes obrigatórios:**
- Back-end: Busca ignora produtos não vendáveis; barcode inexistente retorna aviso.
- Front-end: Busca filtra lista; scanner por Enter adiciona item.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S08-H05: Como operador, quero buscar produto por nome/categoria/código.

Contexto obrigatório:
- Sprint: Sprint 08 — PDV: venda e carrinho
- Domínio: PDV carrinho
- Documentos de referência: PDV
- Back-end esperado: Endpoints de busca PDV.
- Front-end esperado: Busca lateral, categorias e input código de barras.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Busca ignora produtos não vendáveis; barcode inexistente retorna aviso..
5. Crie/ajuste testes unitários do front-end: Busca filtra lista; scanner por Enter adiciona item..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S08-H06 — Como operador, quero ver totais em tempo real.

**Domínio técnico:** PDV carrinho

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Retornar subtotal, desconto e total no DTO da venda.
- Front-end planejado no roadmap: Painel direito do PDV com totais.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: pdv/sales/order/order-item
- `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`
- regras de totalização, desconto, finalização e cancelamento

Front-end:
- `front-end/` — app/feature alvo: pdv/sale-screen, product-search e cart
- tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Retornar subtotal, desconto e total no DTO da venda.
3. Implementar o front-end previsto: Painel direito do PDV com totais.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- venda não pode ser finalizada sem itens.
- item deve guardar snapshot de nome e preço.
- produto inativo ou não vendável no PDV deve ser bloqueado.
- venda finalizada não pode ser editada como carrinho comum.

**Testes obrigatórios:**
- Back-end: Total = soma itens - desconto + taxas; arredondamento monetário correto.
- Front-end: Componente totaliza payload recebido.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S08-H06: Como operador, quero ver totais em tempo real.

Contexto obrigatório:
- Sprint: Sprint 08 — PDV: venda e carrinho
- Domínio: PDV carrinho
- Documentos de referência: DOM, PDV
- Back-end esperado: Retornar subtotal, desconto e total no DTO da venda.
- Front-end esperado: Painel direito do PDV com totais.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, PDV.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Total = soma itens - desconto + taxas; arredondamento monetário correto..
5. Crie/ajuste testes unitários do front-end: Componente totaliza payload recebido..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~

---

## Sprint 09 — Pagamento presencial

**Objetivo da sprint:** registrar pagamento simples e misto com validação de total.


### S09-H01 — Como operador, quero registrar pagamento em dinheiro.

**Domínio técnico:** Pagamento presencial

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `POST /api/pdv/sales/{saleId}/payment`.
- Front-end planejado no roadmap: Tela de pagamento dinheiro e troco.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: payment e cash-movement
- `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`
- regras de totalização, desconto, finalização e cancelamento

Front-end:
- `front-end/` — app/feature alvo: pdv/payment-screen
- tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `POST /api/pdv/sales/{saleId}/payment`.
3. Implementar o front-end previsto: Tela de pagamento dinheiro e troco.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pagamento deve bater com o total da venda.
- pagamento misto deve gerar um Payment por forma.
- cada pagamento deve gerar CashMovement.
- troco só se aplica a dinheiro.

**Testes obrigatórios:**
- Back-end: Valor menor que total falha; troco calculado; Payment `PAID`.
- Front-end: Troco é exibido; valor inválido bloqueia botão.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S09-H01: Como operador, quero registrar pagamento em dinheiro.

Contexto obrigatório:
- Sprint: Sprint 09 — Pagamento presencial
- Domínio: Pagamento presencial
- Documentos de referência: PDV, DOM
- Back-end esperado: `POST /api/pdv/sales/{saleId}/payment`.
- Front-end esperado: Tela de pagamento dinheiro e troco.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV, DOM.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Valor menor que total falha; troco calculado; Payment `PAID`..
5. Crie/ajuste testes unitários do front-end: Troco é exibido; valor inválido bloqueia botão..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S09-H02 — Como operador, quero registrar Pix presencial.

**Domínio técnico:** Pagamento presencial

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Método `PIX`.
- Front-end planejado no roadmap: Tela Pix presencial com confirmação manual.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: payment e cash-movement
- `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`
- regras de totalização, desconto, finalização e cancelamento

Front-end:
- `front-end/` — app/feature alvo: pdv/payment-screen
- tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Método `PIX`.
3. Implementar o front-end previsto: Tela Pix presencial com confirmação manual.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pagamento deve bater com o total da venda.
- pagamento misto deve gerar um Payment por forma.
- cada pagamento deve gerar CashMovement.
- troco só se aplica a dinheiro.

**Testes obrigatórios:**
- Back-end: Valor deve bater com total; cria CashMovement.
- Front-end: Confirmação atualiza estado de pagamento.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S09-H02: Como operador, quero registrar Pix presencial.

Contexto obrigatório:
- Sprint: Sprint 09 — Pagamento presencial
- Domínio: Pagamento presencial
- Documentos de referência: PDV
- Back-end esperado: Método `PIX`.
- Front-end esperado: Tela Pix presencial com confirmação manual.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Valor deve bater com total; cria CashMovement..
5. Crie/ajuste testes unitários do front-end: Confirmação atualiza estado de pagamento..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S09-H03 — Como operador, quero registrar débito/crédito/voucher.

**Domínio técnico:** Pagamento presencial

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Métodos `DEBIT_CARD`, `CREDIT_CARD`, `MEAL_VOUCHER`.
- Front-end planejado no roadmap: Seleção de forma de pagamento.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: payment e cash-movement
- `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`
- regras de totalização, desconto, finalização e cancelamento

Front-end:
- `front-end/` — app/feature alvo: pdv/payment-screen
- tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Métodos `DEBIT_CARD`, `CREDIT_CARD`, `MEAL_VOUCHER`.
3. Implementar o front-end previsto: Seleção de forma de pagamento.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pagamento deve bater com o total da venda.
- pagamento misto deve gerar um Payment por forma.
- cada pagamento deve gerar CashMovement.
- troco só se aplica a dinheiro.

**Testes obrigatórios:**
- Back-end: Método inválido falha; valor positivo; status pago.
- Front-end: Select de método monta payload correto.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S09-H03: Como operador, quero registrar débito/crédito/voucher.

Contexto obrigatório:
- Sprint: Sprint 09 — Pagamento presencial
- Domínio: Pagamento presencial
- Documentos de referência: PDV, DOM
- Back-end esperado: Métodos `DEBIT_CARD`, `CREDIT_CARD`, `MEAL_VOUCHER`.
- Front-end esperado: Seleção de forma de pagamento.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV, DOM.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Método inválido falha; valor positivo; status pago..
5. Crie/ajuste testes unitários do front-end: Select de método monta payload correto..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S09-H04 — Como operador, quero pagamento misto.

**Domínio técnico:** Pagamento presencial

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar múltiplos `Payment` para uma venda.
- Front-end planejado no roadmap: Tela com múltiplas formas e saldo restante.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: payment e cash-movement
- `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`
- regras de totalização, desconto, finalização e cancelamento

Front-end:
- `front-end/` — app/feature alvo: pdv/payment-screen
- tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar múltiplos `Payment` para uma venda.
3. Implementar o front-end previsto: Tela com múltiplas formas e saldo restante.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pagamento deve bater com o total da venda.
- pagamento misto deve gerar um Payment por forma.
- cada pagamento deve gerar CashMovement.
- troco só se aplica a dinheiro.

**Testes obrigatórios:**
- Back-end: Soma menor/maior que total falha; cada pagamento gera CashMovement.
- Front-end: Saldo restante recalcula; não permite finalizar com saldo aberto.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S09-H04: Como operador, quero pagamento misto.

Contexto obrigatório:
- Sprint: Sprint 09 — Pagamento presencial
- Domínio: Pagamento presencial
- Documentos de referência: PDV, FLUXOS
- Back-end esperado: Criar múltiplos `Payment` para uma venda.
- Front-end esperado: Tela com múltiplas formas e saldo restante.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Soma menor/maior que total falha; cada pagamento gera CashMovement..
5. Crie/ajuste testes unitários do front-end: Saldo restante recalcula; não permite finalizar com saldo aberto..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S09-H05 — Como sistema, quero movimentação de caixa por pagamento.

**Domínio técnico:** Pagamento presencial

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar `CashMovement SALE`.
- Front-end planejado no roadmap: Exibir pagamentos associados à venda.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: payment e cash-movement
- `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`
- regras de totalização, desconto, finalização e cancelamento

Front-end:
- `front-end/` — app/feature alvo: pdv/payment-screen
- tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar `CashMovement SALE`.
3. Implementar o front-end previsto: Exibir pagamentos associados à venda.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pagamento deve bater com o total da venda.
- pagamento misto deve gerar um Payment por forma.
- cada pagamento deve gerar CashMovement.
- troco só se aplica a dinheiro.

**Testes obrigatórios:**
- Back-end: Cada Payment cria uma movimentação; caixa fechado bloqueia.
- Front-end: Lista de pagamentos renderiza por método.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S09-H05: Como sistema, quero movimentação de caixa por pagamento.

Contexto obrigatório:
- Sprint: Sprint 09 — Pagamento presencial
- Domínio: Pagamento presencial
- Documentos de referência: DOM, PDV
- Back-end esperado: Criar `CashMovement SALE`.
- Front-end esperado: Exibir pagamentos associados à venda.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, PDV.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Cada Payment cria uma movimentação; caixa fechado bloqueia..
5. Crie/ajuste testes unitários do front-end: Lista de pagamentos renderiza por método..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~

---

## Sprint 10 — Finalização, impressão e cancelamento

**Objetivo da sprint:** finalizar venda, imprimir comprovante, abrir gaveta e cancelar com auditoria.


### S10-H01 — Como operador, quero finalizar venda paga.

**Domínio técnico:** Finalização, impressão, desconto e cancelamento

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `POST /api/pdv/sales/{saleId}/finish`.
- Front-end planejado no roadmap: Botão finalizar venda.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: pdv/finish, printing, discount, cancellation e audit
- `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`
- regras de totalização, desconto, finalização e cancelamento

Front-end:
- `front-end/` — app/feature alvo: pdv/finalization, discount-modal, cancel-modal e print actions
- tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `POST /api/pdv/sales/{saleId}/finish`.
3. Implementar o front-end previsto: Botão finalizar venda.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- falha de impressão não pode duplicar venda.
- cancelamento de venda finalizada exige motivo e permissão.
- desconto acima do limite exige gerente.
- gaveta só deve abrir em pagamento dinheiro ou ação autorizada.

**Testes obrigatórios:**
- Back-end: Sem itens falha; sem pagamento falha; status final correto.
- Front-end: Botão desabilita sem itens/pagamento.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S10-H01: Como operador, quero finalizar venda paga.

Contexto obrigatório:
- Sprint: Sprint 10 — Finalização, impressão e cancelamento
- Domínio: Finalização, impressão, desconto e cancelamento
- Documentos de referência: PDV, FLUXOS
- Back-end esperado: `POST /api/pdv/sales/{saleId}/finish`.
- Front-end esperado: Botão finalizar venda.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Sem itens falha; sem pagamento falha; status final correto..
5. Crie/ajuste testes unitários do front-end: Botão desabilita sem itens/pagamento..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S10-H02 — Como sistema, quero criar evento de sync ao finalizar venda.

**Domínio técnico:** Finalização, impressão, desconto e cancelamento

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar `SyncEvent SALE_FINISHED`.
- Front-end planejado no roadmap: Mostrar status “pendente de sync”.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: pdv/finish, printing, discount, cancellation e audit
- `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`
- regras de totalização, desconto, finalização e cancelamento

Front-end:
- `front-end/` — app/feature alvo: pdv/finalization, discount-modal, cancel-modal e print actions
- tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar `SyncEvent SALE_FINISHED`.
3. Implementar o front-end previsto: Mostrar status “pendente de sync”.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- falha de impressão não pode duplicar venda.
- cancelamento de venda finalizada exige motivo e permissão.
- desconto acima do limite exige gerente.
- gaveta só deve abrir em pagamento dinheiro ou ação autorizada.

**Testes obrigatórios:**
- Back-end: Evento PENDING criado; falha de sync não desfaz venda.
- Front-end: Badge de pendência exibido.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S10-H02: Como sistema, quero criar evento de sync ao finalizar venda.

Contexto obrigatório:
- Sprint: Sprint 10 — Finalização, impressão e cancelamento
- Domínio: Finalização, impressão, desconto e cancelamento
- Documentos de referência: PDV, SYNC
- Back-end esperado: Criar `SyncEvent SALE_FINISHED`.
- Front-end esperado: Mostrar status “pendente de sync”.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV, SYNC.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Evento PENDING criado; falha de sync não desfaz venda..
5. Crie/ajuste testes unitários do front-end: Badge de pendência exibido..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S10-H03 — Como operador, quero imprimir comprovante.

**Domínio técnico:** Finalização, impressão, desconto e cancelamento

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- `deploy-local.md`: Docker Compose local, Nginx, health checks, backup, IP fixo e segurança local.
- `hardware.md`: servidor local, PDV, KDS, impressoras, gaveta, rede, nobreak e contingência.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar serviço de impressão abstrato/adapter.
- Front-end planejado no roadmap: Botão imprimir/reimprimir comprovante.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: pdv/finish, printing, discount, cancellation e audit
- `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`
- regras de totalização, desconto, finalização e cancelamento

Front-end:
- `front-end/` — app/feature alvo: pdv/finalization, discount-modal, cancel-modal e print actions
- tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar serviço de impressão abstrato/adapter.
3. Implementar o front-end previsto: Botão imprimir/reimprimir comprovante.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- falha de impressão não pode duplicar venda.
- cancelamento de venda finalizada exige motivo e permissão.
- desconto acima do limite exige gerente.
- gaveta só deve abrir em pagamento dinheiro ou ação autorizada.

**Testes obrigatórios:**
- Back-end: Geração de comando não duplica venda; falha de impressão retorna erro controlado.
- Front-end: Estado de impressão mostra sucesso/erro.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S10-H03: Como operador, quero imprimir comprovante.

Contexto obrigatório:
- Sprint: Sprint 10 — Finalização, impressão e cancelamento
- Domínio: Finalização, impressão, desconto e cancelamento
- Documentos de referência: PDV, DL, HW
- Back-end esperado: Criar serviço de impressão abstrato/adapter.
- Front-end esperado: Botão imprimir/reimprimir comprovante.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV, DL, HW.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Geração de comando não duplica venda; falha de impressão retorna erro controlado..
5. Crie/ajuste testes unitários do front-end: Estado de impressão mostra sucesso/erro..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S10-H04 — Como operador, quero abrir gaveta em pagamento dinheiro.

**Domínio técnico:** Finalização, impressão, desconto e cancelamento

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- `hardware.md`: servidor local, PDV, KDS, impressoras, gaveta, rede, nobreak e contingência.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Adapter envia comando de gaveta.
- Front-end planejado no roadmap: Indicação visual de gaveta acionada.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: pdv/finish, printing, discount, cancellation e audit
- `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`
- regras de totalização, desconto, finalização e cancelamento

Front-end:
- `front-end/` — app/feature alvo: pdv/finalization, discount-modal, cancel-modal e print actions
- tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Adapter envia comando de gaveta.
3. Implementar o front-end previsto: Indicação visual de gaveta acionada.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- falha de impressão não pode duplicar venda.
- cancelamento de venda finalizada exige motivo e permissão.
- desconto acima do limite exige gerente.
- gaveta só deve abrir em pagamento dinheiro ou ação autorizada.

**Testes obrigatórios:**
- Back-end: Só aciona gaveta para dinheiro; não aciona para Pix/cartão.
- Front-end: Componente mostra ação apenas quando aplicável.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S10-H04: Como operador, quero abrir gaveta em pagamento dinheiro.

Contexto obrigatório:
- Sprint: Sprint 10 — Finalização, impressão e cancelamento
- Domínio: Finalização, impressão, desconto e cancelamento
- Documentos de referência: PDV, HW
- Back-end esperado: Adapter envia comando de gaveta.
- Front-end esperado: Indicação visual de gaveta acionada.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV, HW.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Só aciona gaveta para dinheiro; não aciona para Pix/cartão..
5. Crie/ajuste testes unitários do front-end: Componente mostra ação apenas quando aplicável..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S10-H05 — Como operador, quero aplicar desconto.

**Domínio técnico:** Finalização, impressão, desconto e cancelamento

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `POST /api/pdv/sales/{saleId}/discount`.
- Front-end planejado no roadmap: Modal de desconto por valor/percentual.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: pdv/finish, printing, discount, cancellation e audit
- `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`
- regras de totalização, desconto, finalização e cancelamento

Front-end:
- `front-end/` — app/feature alvo: pdv/finalization, discount-modal, cancel-modal e print actions
- tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `POST /api/pdv/sales/{saleId}/discount`.
3. Implementar o front-end previsto: Modal de desconto por valor/percentual.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- falha de impressão não pode duplicar venda.
- cancelamento de venda finalizada exige motivo e permissão.
- desconto acima do limite exige gerente.
- gaveta só deve abrir em pagamento dinheiro ou ação autorizada.

**Testes obrigatórios:**
- Back-end: Limite permitido aplica; acima exige gerente; desconto negativo falha.
- Front-end: Form alterna valor/percentual; calcula preview.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S10-H05: Como operador, quero aplicar desconto.

Contexto obrigatório:
- Sprint: Sprint 10 — Finalização, impressão e cancelamento
- Domínio: Finalização, impressão, desconto e cancelamento
- Documentos de referência: PDV, FLUXOS
- Back-end esperado: `POST /api/pdv/sales/{saleId}/discount`.
- Front-end esperado: Modal de desconto por valor/percentual.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Limite permitido aplica; acima exige gerente; desconto negativo falha..
5. Crie/ajuste testes unitários do front-end: Form alterna valor/percentual; calcula preview..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S10-H06 — Como gerente, quero cancelar venda finalizada.

**Domínio técnico:** Finalização, impressão, desconto e cancelamento

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `POST /api/pdv/sales/{saleId}/cancel`.
- Front-end planejado no roadmap: Modal motivo + autorização.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: pdv/finish, printing, discount, cancellation e audit
- `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`
- regras de totalização, desconto, finalização e cancelamento

Front-end:
- `front-end/` — app/feature alvo: pdv/finalization, discount-modal, cancel-modal e print actions
- tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `POST /api/pdv/sales/{saleId}/cancel`.
3. Implementar o front-end previsto: Modal motivo + autorização.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
6. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- falha de impressão não pode duplicar venda.
- cancelamento de venda finalizada exige motivo e permissão.
- desconto acima do limite exige gerente.
- gaveta só deve abrir em pagamento dinheiro ou ação autorizada.

**Testes obrigatórios:**
- Back-end: Motivo obrigatório; sem permissão falha; gera AuditLog e ajuste financeiro.
- Front-end: Form exige motivo; usuário sem perfil não vê ação.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S10-H06: Como gerente, quero cancelar venda finalizada.

Contexto obrigatório:
- Sprint: Sprint 10 — Finalização, impressão e cancelamento
- Domínio: Finalização, impressão, desconto e cancelamento
- Documentos de referência: PDV, FLUXOS
- Back-end esperado: `POST /api/pdv/sales/{saleId}/cancel`.
- Front-end esperado: Modal motivo + autorização.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Motivo obrigatório; sem permissão falha; gera AuditLog e ajuste financeiro..
5. Crie/ajuste testes unitários do front-end: Form exige motivo; usuário sem perfil não vê ação..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~

---

## Sprint 11 — KDS: tickets e tempo real

**Objetivo da sprint:** gerar tickets por setor e exibir no KDS em tempo real.


### S11-H01 — Como sistema, quero criar tickets KDS ao finalizar venda com preparo.

**Domínio técnico:** KDS tickets e tempo real

**Documentos que justificam a implementação:**
- `kds.md`: tickets por setor, WebSocket, status de preparo, tempo de espera e operação local da cozinha.
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Serviço agrupa itens por `preparationSector`.
- Front-end planejado no roadmap: KDS mostra tickets recebidos.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: kds/tickets, websocket e integração com order
- `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs

Front-end:
- `front-end/` — app/feature alvo: kds/board, sector-filter e websocket-client
- app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Serviço agrupa itens por `preparationSector`.
3. Implementar o front-end previsto: KDS mostra tickets recebidos.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
6. Garantir transições válidas de status e não gerar ticket para item `SEM_PREPARO`.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- KDS deve funcionar na rede local sem internet.
- itens SEM_PREPARO não geram ticket.
- itens de setores diferentes geram tickets diferentes.
- WebSocket notifica sem quebrar a transação principal.

**Testes obrigatórios:**
- Back-end: Itens `SEM_PREPARO` não geram ticket; setores diferentes geram tickets diferentes.
- Front-end: Lista renderiza tickets por setor.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S11-H01: Como sistema, quero criar tickets KDS ao finalizar venda com preparo.

Contexto obrigatório:
- Sprint: Sprint 11 — KDS: tickets e tempo real
- Domínio: KDS tickets e tempo real
- Documentos de referência: KDS, PDV, DOM
- Back-end esperado: Serviço agrupa itens por `preparationSector`.
- Front-end esperado: KDS mostra tickets recebidos.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: KDS, PDV, DOM.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Itens `SEM_PREPARO` não geram ticket; setores diferentes geram tickets diferentes..
5. Crie/ajuste testes unitários do front-end: Lista renderiza tickets por setor..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S11-H02 — Como cozinha, quero filtrar tickets por setor.

**Domínio técnico:** KDS tickets e tempo real

**Documentos que justificam a implementação:**
- `kds.md`: tickets por setor, WebSocket, status de preparo, tempo de espera e operação local da cozinha.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `GET /api/kds/tickets?sector=CHAPA`.
- Front-end planejado no roadmap: Filtro/setor atual no KDS.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: kds/tickets, websocket e integração com order
- `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs

Front-end:
- `front-end/` — app/feature alvo: kds/board, sector-filter e websocket-client
- app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `GET /api/kds/tickets?sector=CHAPA`.
3. Implementar o front-end previsto: Filtro/setor atual no KDS.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir transições válidas de status e não gerar ticket para item `SEM_PREPARO`.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- KDS deve funcionar na rede local sem internet.
- itens SEM_PREPARO não geram ticket.
- itens de setores diferentes geram tickets diferentes.
- WebSocket notifica sem quebrar a transação principal.

**Testes obrigatórios:**
- Back-end: Filtro retorna apenas setor; setor inválido falha.
- Front-end: Seleção de setor atualiza lista.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S11-H02: Como cozinha, quero filtrar tickets por setor.

Contexto obrigatório:
- Sprint: Sprint 11 — KDS: tickets e tempo real
- Domínio: KDS tickets e tempo real
- Documentos de referência: KDS
- Back-end esperado: `GET /api/kds/tickets?sector=CHAPA`.
- Front-end esperado: Filtro/setor atual no KDS.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: KDS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Filtro retorna apenas setor; setor inválido falha..
5. Crie/ajuste testes unitários do front-end: Seleção de setor atualiza lista..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S11-H03 — Como KDS, quero receber ticket via WebSocket.

**Domínio técnico:** KDS tickets e tempo real

**Documentos que justificam a implementação:**
- `kds.md`: tickets por setor, WebSocket, status de preparo, tempo de espera e operação local da cozinha.
- `arquitetura.md`: arquitetura híbrida local + online, monólito modular, back-end em camadas, front-end por features/camadas, comunicação HTTPS e separação entre operação local e canais online.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Publicar evento `KDS_TICKET_CREATED`.
- Front-end planejado no roadmap: Cliente WebSocket no Angular KDS.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: kds/tickets, websocket e integração com order
- `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs

Front-end:
- `front-end/` — app/feature alvo: kds/board, sector-filter e websocket-client
- app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Publicar evento `KDS_TICKET_CREATED`.
3. Implementar o front-end previsto: Cliente WebSocket no Angular KDS.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Garantir transições válidas de status e não gerar ticket para item `SEM_PREPARO`.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- KDS deve funcionar na rede local sem internet.
- itens SEM_PREPARO não geram ticket.
- itens de setores diferentes geram tickets diferentes.
- WebSocket notifica sem quebrar a transação principal.

**Testes obrigatórios:**
- Back-end: Evento emitido com payload esperado; erro de WebSocket não quebra transação.
- Front-end: Ao receber evento, card é adicionado.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S11-H03: Como KDS, quero receber ticket via WebSocket.

Contexto obrigatório:
- Sprint: Sprint 11 — KDS: tickets e tempo real
- Domínio: KDS tickets e tempo real
- Documentos de referência: KDS, ARQ
- Back-end esperado: Publicar evento `KDS_TICKET_CREATED`.
- Front-end esperado: Cliente WebSocket no Angular KDS.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: KDS, ARQ.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Evento emitido com payload esperado; erro de WebSocket não quebra transação..
5. Crie/ajuste testes unitários do front-end: Ao receber evento, card é adicionado..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S11-H04 — Como produção, quero ver tempo desde criação.

**Domínio técnico:** KDS tickets e tempo real

**Documentos que justificam a implementação:**
- `kds.md`: tickets por setor, WebSocket, status de preparo, tempo de espera e operação local da cozinha.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: DTO traz `createdAt` e tempo calculável.
- Front-end planejado no roadmap: Card mostra minutos de espera.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: kds/tickets, websocket e integração com order
- `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs

Front-end:
- `front-end/` — app/feature alvo: kds/board, sector-filter e websocket-client
- app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: DTO traz `createdAt` e tempo calculável.
3. Implementar o front-end previsto: Card mostra minutos de espera.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- KDS deve funcionar na rede local sem internet.
- itens SEM_PREPARO não geram ticket.
- itens de setores diferentes geram tickets diferentes.
- WebSocket notifica sem quebrar a transação principal.

**Testes obrigatórios:**
- Back-end: Tempo calculado corretamente; timezone não altera ordem.
- Front-end: Timer atualiza visualmente sem recarregar.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S11-H04: Como produção, quero ver tempo desde criação.

Contexto obrigatório:
- Sprint: Sprint 11 — KDS: tickets e tempo real
- Domínio: KDS tickets e tempo real
- Documentos de referência: KDS
- Back-end esperado: DTO traz `createdAt` e tempo calculável.
- Front-end esperado: Card mostra minutos de espera.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: KDS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Tempo calculado corretamente; timezone não altera ordem..
5. Crie/ajuste testes unitários do front-end: Timer atualiza visualmente sem recarregar..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S11-H05 — Como produção, quero layout em colunas.

**Domínio técnico:** KDS tickets e tempo real

**Documentos que justificam a implementação:**
- `kds.md`: tickets por setor, WebSocket, status de preparo, tempo de espera e operação local da cozinha.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Endpoint retorna status dos tickets.
- Front-end planejado no roadmap: Colunas Aguardando, Em preparo, Pronto.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: kds/tickets, websocket e integração com order
- `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs

Front-end:
- `front-end/` — app/feature alvo: kds/board, sector-filter e websocket-client
- app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Endpoint retorna status dos tickets.
3. Implementar o front-end previsto: Colunas Aguardando, Em preparo, Pronto.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir transições válidas de status e não gerar ticket para item `SEM_PREPARO`.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- KDS deve funcionar na rede local sem internet.
- itens SEM_PREPARO não geram ticket.
- itens de setores diferentes geram tickets diferentes.
- WebSocket notifica sem quebrar a transação principal.

**Testes obrigatórios:**
- Back-end: Status mapeado corretamente no DTO.
- Front-end: Ticket aparece na coluna certa.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S11-H05: Como produção, quero layout em colunas.

Contexto obrigatório:
- Sprint: Sprint 11 — KDS: tickets e tempo real
- Domínio: KDS tickets e tempo real
- Documentos de referência: KDS
- Back-end esperado: Endpoint retorna status dos tickets.
- Front-end esperado: Colunas Aguardando, Em preparo, Pronto.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: KDS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Status mapeado corretamente no DTO..
5. Crie/ajuste testes unitários do front-end: Ticket aparece na coluna certa..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~

---

## Sprint 12 — KDS: preparo, pronto e expedição

**Objetivo da sprint:** atualizar status de tickets/itens e refletir pedido pronto no PDV.


### S12-H01 — Como produção, quero iniciar preparo do ticket.

**Domínio técnico:** KDS preparo e expedição

**Documentos que justificam a implementação:**
- `kds.md`: tickets por setor, WebSocket, status de preparo, tempo de espera e operação local da cozinha.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `PATCH /api/kds/tickets/{id}/start`.
- Front-end planejado no roadmap: Botão “Iniciar preparo”.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: kds/status transitions e order readiness
- `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs

Front-end:
- `front-end/` — app/feature alvo: kds/actions, pdv/ready-notifications e expedition view
- app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `PATCH /api/kds/tickets/{id}/start`.
3. Implementar o front-end previsto: Botão “Iniciar preparo”.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir transições válidas de status e não gerar ticket para item `SEM_PREPARO`.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pedido só fica READY quando todos tickets obrigatórios estiverem prontos.
- transições inválidas de status devem ser bloqueadas.
- cancelamento de item no KDS exige permissão.
- data/hora de início, pronto e finalização devem ser preservadas.

**Testes obrigatórios:**
- Back-end: WAITING → IN_PREPARATION permitido; READY → start bloqueado.
- Front-end: Card muda para coluna Em preparo.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S12-H01: Como produção, quero iniciar preparo do ticket.

Contexto obrigatório:
- Sprint: Sprint 12 — KDS: preparo, pronto e expedição
- Domínio: KDS preparo e expedição
- Documentos de referência: KDS, FLUXOS
- Back-end esperado: `PATCH /api/kds/tickets/{id}/start`.
- Front-end esperado: Botão “Iniciar preparo”.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: KDS, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: WAITING → IN_PREPARATION permitido; READY → start bloqueado..
5. Crie/ajuste testes unitários do front-end: Card muda para coluna Em preparo..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S12-H02 — Como produção, quero marcar item pronto.

**Domínio técnico:** KDS preparo e expedição

**Documentos que justificam a implementação:**
- `kds.md`: tickets por setor, WebSocket, status de preparo, tempo de espera e operação local da cozinha.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `PATCH /api/kds/items/{id}/ready`.
- Front-end planejado no roadmap: Botão pronto por item.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: kds/status transitions e order readiness
- `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs

Front-end:
- `front-end/` — app/feature alvo: kds/actions, pdv/ready-notifications e expedition view
- app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `PATCH /api/kds/items/{id}/ready`.
3. Implementar o front-end previsto: Botão pronto por item.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir transições válidas de status e não gerar ticket para item `SEM_PREPARO`.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pedido só fica READY quando todos tickets obrigatórios estiverem prontos.
- transições inválidas de status devem ser bloqueadas.
- cancelamento de item no KDS exige permissão.
- data/hora de início, pronto e finalização devem ser preservadas.

**Testes obrigatórios:**
- Back-end: Item em preparo vira READY; item cancelado não pode ficar pronto.
- Front-end: Item aparece concluído.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S12-H02: Como produção, quero marcar item pronto.

Contexto obrigatório:
- Sprint: Sprint 12 — KDS: preparo, pronto e expedição
- Domínio: KDS preparo e expedição
- Documentos de referência: KDS
- Back-end esperado: `PATCH /api/kds/items/{id}/ready`.
- Front-end esperado: Botão pronto por item.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: KDS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Item em preparo vira READY; item cancelado não pode ficar pronto..
5. Crie/ajuste testes unitários do front-end: Item aparece concluído..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S12-H03 — Como produção, quero marcar ticket pronto.

**Domínio técnico:** KDS preparo e expedição

**Documentos que justificam a implementação:**
- `kds.md`: tickets por setor, WebSocket, status de preparo, tempo de espera e operação local da cozinha.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `PATCH /api/kds/tickets/{id}/ready`.
- Front-end planejado no roadmap: Botão “Ticket pronto”.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: kds/status transitions e order readiness
- `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs

Front-end:
- `front-end/` — app/feature alvo: kds/actions, pdv/ready-notifications e expedition view
- app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `PATCH /api/kds/tickets/{id}/ready`.
3. Implementar o front-end previsto: Botão “Ticket pronto”.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir transições válidas de status e não gerar ticket para item `SEM_PREPARO`.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pedido só fica READY quando todos tickets obrigatórios estiverem prontos.
- transições inválidas de status devem ser bloqueadas.
- cancelamento de item no KDS exige permissão.
- data/hora de início, pronto e finalização devem ser preservadas.

**Testes obrigatórios:**
- Back-end: Todos itens ficam READY; `readyAt` preenchido.
- Front-end: Ticket muda para coluna Pronto.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S12-H03: Como produção, quero marcar ticket pronto.

Contexto obrigatório:
- Sprint: Sprint 12 — KDS: preparo, pronto e expedição
- Domínio: KDS preparo e expedição
- Documentos de referência: KDS
- Back-end esperado: `PATCH /api/kds/tickets/{id}/ready`.
- Front-end esperado: Botão “Ticket pronto”.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: KDS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Todos itens ficam READY; `readyAt` preenchido..
5. Crie/ajuste testes unitários do front-end: Ticket muda para coluna Pronto..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S12-H04 — Como sistema, quero pedido READY quando todos tickets estiverem prontos.

**Domínio técnico:** KDS preparo e expedição

**Documentos que justificam a implementação:**
- `kds.md`: tickets por setor, WebSocket, status de preparo, tempo de espera e operação local da cozinha.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Serviço atualiza `OrderStatus.READY`.
- Front-end planejado no roadmap: PDV/expedição recebe atualização.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: kds/status transitions e order readiness
- `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs

Front-end:
- `front-end/` — app/feature alvo: kds/actions, pdv/ready-notifications e expedition view
- app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Serviço atualiza `OrderStatus.READY`.
3. Implementar o front-end previsto: PDV/expedição recebe atualização.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir transições válidas de status e não gerar ticket para item `SEM_PREPARO`.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pedido só fica READY quando todos tickets obrigatórios estiverem prontos.
- transições inválidas de status devem ser bloqueadas.
- cancelamento de item no KDS exige permissão.
- data/hora de início, pronto e finalização devem ser preservadas.

**Testes obrigatórios:**
- Back-end: Com um ticket pendente, pedido não fica READY; todos prontos, fica READY.
- Front-end: Banner/alerta no PDV indica pedido pronto.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S12-H04: Como sistema, quero pedido READY quando todos tickets estiverem prontos.

Contexto obrigatório:
- Sprint: Sprint 12 — KDS: preparo, pronto e expedição
- Domínio: KDS preparo e expedição
- Documentos de referência: KDS, FLUXOS
- Back-end esperado: Serviço atualiza `OrderStatus.READY`.
- Front-end esperado: PDV/expedição recebe atualização.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: KDS, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Com um ticket pendente, pedido não fica READY; todos prontos, fica READY..
5. Crie/ajuste testes unitários do front-end: Banner/alerta no PDV indica pedido pronto..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S12-H05 — Como expedição, quero finalizar pedido pronto.

**Domínio técnico:** KDS preparo e expedição

**Documentos que justificam a implementação:**
- `kds.md`: tickets por setor, WebSocket, status de preparo, tempo de espera e operação local da cozinha.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Endpoint `finish` para pedido/ticket.
- Front-end planejado no roadmap: Tela/ação de expedição.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: kds/status transitions e order readiness
- `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs

Front-end:
- `front-end/` — app/feature alvo: kds/actions, pdv/ready-notifications e expedition view
- app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Endpoint `finish` para pedido/ticket.
3. Implementar o front-end previsto: Tela/ação de expedição.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir transições válidas de status e não gerar ticket para item `SEM_PREPARO`.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pedido só fica READY quando todos tickets obrigatórios estiverem prontos.
- transições inválidas de status devem ser bloqueadas.
- cancelamento de item no KDS exige permissão.
- data/hora de início, pronto e finalização devem ser preservadas.

**Testes obrigatórios:**
- Back-end: READY → FINISHED permitido; CREATED → FINISHED bloqueado.
- Front-end: Ação some após finalização.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S12-H05: Como expedição, quero finalizar pedido pronto.

Contexto obrigatório:
- Sprint: Sprint 12 — KDS: preparo, pronto e expedição
- Domínio: KDS preparo e expedição
- Documentos de referência: KDS, FLUXOS
- Back-end esperado: Endpoint `finish` para pedido/ticket.
- Front-end esperado: Tela/ação de expedição.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: KDS, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: READY → FINISHED permitido; CREATED → FINISHED bloqueado..
5. Crie/ajuste testes unitários do front-end: Ação some após finalização..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~

---

## Sprint 13 — Sincronização base local → online

**Objetivo da sprint:** criar outbox local e envio seguro de eventos locais para o online.


### S13-H01 — Como sistema, quero registrar SyncEvent.

**Domínio técnico:** Sincronização local → online

**Documentos que justificam a implementação:**
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.
- `banco-de-dados.md`: PostgreSQL, Flyway, UUID, timestamps, NUMERIC para dinheiro, tabelas e índices iniciais.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Serviço `SyncEventService.createPending`.
- Front-end planejado no roadmap: Mostrar status de sync na venda.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: sync/outbox, worker local, endpoint online e HMAC
- `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`
- política de retry e endpoints de ACK/fail/status

Front-end:
- `front-end/` — app/feature alvo: sync status badges e painel futuro
- badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Serviço `SyncEventService.createPending`.
3. Implementar o front-end previsto: Mostrar status de sync na venda.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- sync não pode bloquear a venda local.
- evento deve ser idempotente.
- payload deve ser assinado com HMAC.
- falha deve virar retry sem perder dados.

**Testes obrigatórios:**
- Back-end: Evento tem UUID, tipo, entidade, payload, status PENDING.
- Front-end: Badge exibe PENDING/SYNCED/FAILED.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S13-H01: Como sistema, quero registrar SyncEvent.

Contexto obrigatório:
- Sprint: Sprint 13 — Sincronização base local → online
- Domínio: Sincronização local → online
- Documentos de referência: SYNC, BD
- Back-end esperado: Serviço `SyncEventService.createPending`.
- Front-end esperado: Mostrar status de sync na venda.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: SYNC, BD.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Evento tem UUID, tipo, entidade, payload, status PENDING..
5. Crie/ajuste testes unitários do front-end: Badge exibe PENDING/SYNCED/FAILED..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S13-H02 — Como worker local, quero enviar evento para online.

**Domínio técnico:** Sincronização local → online

**Documentos que justificam a implementação:**
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Worker consome PENDING e chama `POST /api/sync/events`.
- Front-end planejado no roadmap: Nenhuma tela nova; status atualiza.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: sync/outbox, worker local, endpoint online e HMAC
- `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`
- política de retry e endpoints de ACK/fail/status

Front-end:
- `front-end/` — app/feature alvo: sync status badges e painel futuro
- badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Worker consome PENDING e chama `POST /api/sync/events`.
3. Implementar o front-end previsto: Nenhuma tela nova; status atualiza.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- sync não pode bloquear a venda local.
- evento deve ser idempotente.
- payload deve ser assinado com HMAC.
- falha deve virar retry sem perder dados.

**Testes obrigatórios:**
- Back-end: Envio sucesso marca SYNCED; falha marca RETRYING/FAILED.
- Front-end: Tela reflete mudança de status mockada.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S13-H02: Como worker local, quero enviar evento para online.

Contexto obrigatório:
- Sprint: Sprint 13 — Sincronização base local → online
- Domínio: Sincronização local → online
- Documentos de referência: SYNC
- Back-end esperado: Worker consome PENDING e chama `POST /api/sync/events`.
- Front-end esperado: Nenhuma tela nova; status atualiza.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: SYNC.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Envio sucesso marca SYNCED; falha marca RETRYING/FAILED..
5. Crie/ajuste testes unitários do front-end: Tela reflete mudança de status mockada..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S13-H03 — Como API online, quero receber evento local.

**Domínio técnico:** Sincronização local → online

**Documentos que justificam a implementação:**
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.
- `arquitetura.md`: arquitetura híbrida local + online, monólito modular, back-end em camadas, front-end por features/camadas, comunicação HTTPS e separação entre operação local e canais online.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Endpoint online valida payload e salva.
- Front-end planejado no roadmap: Admin online lista eventos recebidos futuramente.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: sync/outbox, worker local, endpoint online e HMAC
- `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`
- política de retry e endpoints de ACK/fail/status

Front-end:
- `front-end/` — app/feature alvo: sync status badges e painel futuro
- badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Endpoint online valida payload e salva.
3. Implementar o front-end previsto: Admin online lista eventos recebidos futuramente.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- sync não pode bloquear a venda local.
- evento deve ser idempotente.
- payload deve ser assinado com HMAC.
- falha deve virar retry sem perder dados.

**Testes obrigatórios:**
- Back-end: Evento duplicado não reprocessa; assinatura inválida retorna 401.
- Front-end: Tabela de eventos mockados renderiza.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S13-H03: Como API online, quero receber evento local.

Contexto obrigatório:
- Sprint: Sprint 13 — Sincronização base local → online
- Domínio: Sincronização local → online
- Documentos de referência: SYNC, ARQ
- Back-end esperado: Endpoint online valida payload e salva.
- Front-end esperado: Admin online lista eventos recebidos futuramente.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: SYNC, ARQ.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Evento duplicado não reprocessa; assinatura inválida retorna 401..
5. Crie/ajuste testes unitários do front-end: Tabela de eventos mockados renderiza..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S13-H04 — Como sistema, quero assinar payload com HMAC.

**Domínio técnico:** Sincronização local → online

**Documentos que justificam a implementação:**
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Implementar assinatura com `STORE_ID` e `STORE_SECRET`.
- Front-end planejado no roadmap: Nenhuma tela.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: sync/outbox, worker local, endpoint online e HMAC
- `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`
- política de retry e endpoints de ACK/fail/status

Front-end:
- `front-end/` — app/feature alvo: sync status badges e painel futuro
- badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Implementar assinatura com `STORE_ID` e `STORE_SECRET`.
3. Implementar o front-end previsto: Nenhuma tela.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- sync não pode bloquear a venda local.
- evento deve ser idempotente.
- payload deve ser assinado com HMAC.
- falha deve virar retry sem perder dados.

**Testes obrigatórios:**
- Back-end: Assinatura válida passa; payload alterado falha; timestamp antigo pode falhar.
- Front-end: N/A.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S13-H04: Como sistema, quero assinar payload com HMAC.

Contexto obrigatório:
- Sprint: Sprint 13 — Sincronização base local → online
- Domínio: Sincronização local → online
- Documentos de referência: SYNC
- Back-end esperado: Implementar assinatura com `STORE_ID` e `STORE_SECRET`.
- Front-end esperado: Nenhuma tela.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: SYNC.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Assinatura válida passa; payload alterado falha; timestamp antigo pode falhar..
5. Crie/ajuste testes unitários do front-end: N/A..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S13-H05 — Como sistema, quero retry inicial.

**Domínio técnico:** Sincronização local → online

**Documentos que justificam a implementação:**
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Implementar política 1m/5m/15m/1h.
- Front-end planejado no roadmap: Exibir retry count no painel futuro.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: sync/outbox, worker local, endpoint online e HMAC
- `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`
- política de retry e endpoints de ACK/fail/status

Front-end:
- `front-end/` — app/feature alvo: sync status badges e painel futuro
- badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Implementar política 1m/5m/15m/1h.
3. Implementar o front-end previsto: Exibir retry count no painel futuro.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- sync não pode bloquear a venda local.
- evento deve ser idempotente.
- payload deve ser assinado com HMAC.
- falha deve virar retry sem perder dados.

**Testes obrigatórios:**
- Back-end: Retry incrementa contador; excedeu tentativas vira FAILED.
- Front-end: Componente mostra contador mockado.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S13-H05: Como sistema, quero retry inicial.

Contexto obrigatório:
- Sprint: Sprint 13 — Sincronização base local → online
- Domínio: Sincronização local → online
- Documentos de referência: SYNC
- Back-end esperado: Implementar política 1m/5m/15m/1h.
- Front-end esperado: Exibir retry count no painel futuro.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: SYNC.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Retry incrementa contador; excedeu tentativas vira FAILED..
5. Crie/ajuste testes unitários do front-end: Componente mostra contador mockado..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~

---

## Sprint 14 — Site e cardápio público

**Objetivo da sprint:** publicar site institucional e cardápio online filtrado por disponibilidade.


### S14-H01 — Como cliente, quero acessar site institucional.

**Domínio técnico:** Site e cardápio público

**Documentos que justificam a implementação:**
- `README.md`: visão geral do projeto, módulos sugeridos e ordem de leitura da documentação.
- `arquitetura.md`: arquitetura híbrida local + online, monólito modular, back-end em camadas, front-end por features/camadas, comunicação HTTPS e separação entre operação local e canais online.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Endpoint/config para dados públicos básicos.
- Front-end planejado no roadmap: Criar home com informações da padaria.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: public-menu e catalog public API
- controllers e use cases de `Category`, `Product`, `ProductAvailability`
- DTOs request/response e mappers MapStruct

Front-end:
- `front-end/` — app/feature alvo: site-publico/home, menu e product cards
- páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Endpoint/config para dados públicos básicos.
3. Implementar o front-end previsto: Criar home com informações da padaria.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- endpoint público de cardápio não exige login.
- somente produtos ativos, disponíveis e sellOnline aparecem.
- categoria precisa estar ativa e showOnline=true.
- preço promocional deve ser exibido sem alterar preço base.

**Testes obrigatórios:**
- Back-end: Endpoint público não exige login; payload básico válido.
- Front-end: Home renderiza nome, seções e CTA.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S14-H01: Como cliente, quero acessar site institucional.

Contexto obrigatório:
- Sprint: Sprint 14 — Site e cardápio público
- Domínio: Site e cardápio público
- Documentos de referência: README, ARQ
- Back-end esperado: Endpoint/config para dados públicos básicos.
- Front-end esperado: Criar home com informações da padaria.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: README, ARQ.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Endpoint público não exige login; payload básico válido..
5. Crie/ajuste testes unitários do front-end: Home renderiza nome, seções e CTA..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S14-H02 — Como cliente, quero ver categorias online.

**Domínio técnico:** Site e cardápio público

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `GET /api/public/menu`.
- Front-end planejado no roadmap: Tela de cardápio por categoria.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: public-menu e catalog public API
- controllers e use cases de `Category`, `Product`, `ProductAvailability`
- DTOs request/response e mappers MapStruct

Front-end:
- `front-end/` — app/feature alvo: site-publico/home, menu e product cards
- páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `GET /api/public/menu`.
3. Implementar o front-end previsto: Tela de cardápio por categoria.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- endpoint público de cardápio não exige login.
- somente produtos ativos, disponíveis e sellOnline aparecem.
- categoria precisa estar ativa e showOnline=true.
- preço promocional deve ser exibido sem alterar preço base.

**Testes obrigatórios:**
- Back-end: Só categorias ativas e `showOnline=true`.
- Front-end: Categorias aparecem ordenadas.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S14-H02: Como cliente, quero ver categorias online.

Contexto obrigatório:
- Sprint: Sprint 14 — Site e cardápio público
- Domínio: Site e cardápio público
- Documentos de referência: DOM, FLUXOS
- Back-end esperado: `GET /api/public/menu`.
- Front-end esperado: Tela de cardápio por categoria.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Só categorias ativas e `showOnline=true`..
5. Crie/ajuste testes unitários do front-end: Categorias aparecem ordenadas..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S14-H03 — Como cliente, quero ver produtos disponíveis.

**Domínio técnico:** Site e cardápio público

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Filtro por `active`, `sellOnline`, disponibilidade.
- Front-end planejado no roadmap: Cards de produto com preço/imagem.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: public-menu e catalog public API
- controllers e use cases de `Category`, `Product`, `ProductAvailability`
- DTOs request/response e mappers MapStruct

Front-end:
- `front-end/` — app/feature alvo: site-publico/home, menu e product cards
- páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Filtro por `active`, `sellOnline`, disponibilidade.
3. Implementar o front-end previsto: Cards de produto com preço/imagem.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- endpoint público de cardápio não exige login.
- somente produtos ativos, disponíveis e sellOnline aparecem.
- categoria precisa estar ativa e showOnline=true.
- preço promocional deve ser exibido sem alterar preço base.

**Testes obrigatórios:**
- Back-end: Produto indisponível não aparece; promocional usa preço correto.
- Front-end: Card mostra preço/promocional corretamente.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S14-H03: Como cliente, quero ver produtos disponíveis.

Contexto obrigatório:
- Sprint: Sprint 14 — Site e cardápio público
- Domínio: Site e cardápio público
- Documentos de referência: DOM, FLUXOS
- Back-end esperado: Filtro por `active`, `sellOnline`, disponibilidade.
- Front-end esperado: Cards de produto com preço/imagem.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Produto indisponível não aparece; promocional usa preço correto..
5. Crie/ajuste testes unitários do front-end: Card mostra preço/promocional corretamente..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S14-H04 — Como cliente, quero buscar produto no cardápio.

**Domínio técnico:** Site e cardápio público

**Documentos que justificam a implementação:**
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Endpoint com filtro ou front filtra lista.
- Front-end planejado no roadmap: Campo busca no site.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: public-menu e catalog public API
- controllers e use cases de `Category`, `Product`, `ProductAvailability`
- DTOs request/response e mappers MapStruct

Front-end:
- `front-end/` — app/feature alvo: site-publico/home, menu e product cards
- páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Endpoint com filtro ou front filtra lista.
3. Implementar o front-end previsto: Campo busca no site.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- endpoint público de cardápio não exige login.
- somente produtos ativos, disponíveis e sellOnline aparecem.
- categoria precisa estar ativa e showOnline=true.
- preço promocional deve ser exibido sem alterar preço base.

**Testes obrigatórios:**
- Back-end: Busca ignora inativos; resultado vazio retorna lista vazia.
- Front-end: Busca filtra por nome.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S14-H04: Como cliente, quero buscar produto no cardápio.

Contexto obrigatório:
- Sprint: Sprint 14 — Site e cardápio público
- Domínio: Site e cardápio público
- Documentos de referência: FLUXOS
- Back-end esperado: Endpoint com filtro ou front filtra lista.
- Front-end esperado: Campo busca no site.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Busca ignora inativos; resultado vazio retorna lista vazia..
5. Crie/ajuste testes unitários do front-end: Busca filtra por nome..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S14-H05 — Como admin, quero preview do cardápio online.

**Domínio técnico:** Site e cardápio público

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Reusar endpoint público/admin.
- Front-end planejado no roadmap: Admin mostra como produto aparece no site.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: public-menu e catalog public API
- controllers e use cases de `Category`, `Product`, `ProductAvailability`
- DTOs request/response e mappers MapStruct

Front-end:
- `front-end/` — app/feature alvo: site-publico/home, menu e product cards
- páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Reusar endpoint público/admin.
3. Implementar o front-end previsto: Admin mostra como produto aparece no site.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
6. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- endpoint público de cardápio não exige login.
- somente produtos ativos, disponíveis e sellOnline aparecem.
- categoria precisa estar ativa e showOnline=true.
- preço promocional deve ser exibido sem alterar preço base.

**Testes obrigatórios:**
- Back-end: Preview usa mesmas regras do público.
- Front-end: Preview exibe status e canal.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S14-H05: Como admin, quero preview do cardápio online.

Contexto obrigatório:
- Sprint: Sprint 14 — Site e cardápio público
- Domínio: Site e cardápio público
- Documentos de referência: DOM
- Back-end esperado: Reusar endpoint público/admin.
- Front-end esperado: Admin mostra como produto aparece no site.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Preview usa mesmas regras do público..
5. Crie/ajuste testes unitários do front-end: Preview exibe status e canal..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~

---

## Sprint 15 — Pedido online

**Objetivo da sprint:** permitir criação de pedido online para retirada/entrega.


### S15-H01 — Como cliente, quero adicionar produto ao carrinho online.

**Domínio técnico:** Pedido online

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Validação server-side no checkout.
- Front-end planejado no roadmap: Carrinho no site.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: online/order, customer, address e checkout validation
- `PublicOrderController`, `CheckoutService`, `CustomerService`, `OrderService`, `PaymentService`, `SyncEventService`

Front-end:
- `front-end/` — app/feature alvo: site-publico/cart, checkout e order-status
- carrinho do site, checkout, formulário de cliente/endereço e página de status

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Validação server-side no checkout.
3. Implementar o front-end previsto: Carrinho no site.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pedido online deve ter cliente.
- pedido delivery exige endereço.
- servidor recalcula valores e não confia no carrinho do front-end.
- pedido deve gerar SyncEvent para a loja.

**Testes obrigatórios:**
- Back-end: Produto indisponível é recusado; preço recalculado no servidor.
- Front-end: Carrinho adiciona/remove item e recalcula visual.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S15-H01: Como cliente, quero adicionar produto ao carrinho online.

Contexto obrigatório:
- Sprint: Sprint 15 — Pedido online
- Domínio: Pedido online
- Documentos de referência: DOM, FLUXOS
- Back-end esperado: Validação server-side no checkout.
- Front-end esperado: Carrinho no site.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Produto indisponível é recusado; preço recalculado no servidor..
5. Crie/ajuste testes unitários do front-end: Carrinho adiciona/remove item e recalcula visual..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S15-H02 — Como cliente, quero informar meus dados.

**Domínio técnico:** Pedido online

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar/atualizar `Customer`.
- Front-end planejado no roadmap: Form de dados do cliente.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: online/order, customer, address e checkout validation
- `PublicOrderController`, `CheckoutService`, `CustomerService`, `OrderService`, `PaymentService`, `SyncEventService`

Front-end:
- `front-end/` — app/feature alvo: site-publico/cart, checkout e order-status
- carrinho do site, checkout, formulário de cliente/endereço e página de status

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar/atualizar `Customer`.
3. Implementar o front-end previsto: Form de dados do cliente.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pedido online deve ter cliente.
- pedido delivery exige endereço.
- servidor recalcula valores e não confia no carrinho do front-end.
- pedido deve gerar SyncEvent para a loja.

**Testes obrigatórios:**
- Back-end: Nome e telefone obrigatórios; e-mail inválido falha se informado.
- Front-end: Form valida campos obrigatórios.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S15-H02: Como cliente, quero informar meus dados.

Contexto obrigatório:
- Sprint: Sprint 15 — Pedido online
- Domínio: Pedido online
- Documentos de referência: DOM
- Back-end esperado: Criar/atualizar `Customer`.
- Front-end esperado: Form de dados do cliente.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Nome e telefone obrigatórios; e-mail inválido falha se informado..
5. Crie/ajuste testes unitários do front-end: Form valida campos obrigatórios..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S15-H03 — Como cliente, quero informar endereço para entrega.

**Domínio técnico:** Pedido online

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar `CustomerAddress` quando `DELIVERY`.
- Front-end planejado no roadmap: Form de endereço condicional.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: online/order, customer, address e checkout validation
- `PublicOrderController`, `CheckoutService`, `CustomerService`, `OrderService`, `PaymentService`, `SyncEventService`

Front-end:
- `front-end/` — app/feature alvo: site-publico/cart, checkout e order-status
- carrinho do site, checkout, formulário de cliente/endereço e página de status

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar `CustomerAddress` quando `DELIVERY`.
3. Implementar o front-end previsto: Form de endereço condicional.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pedido online deve ter cliente.
- pedido delivery exige endereço.
- servidor recalcula valores e não confia no carrinho do front-end.
- pedido deve gerar SyncEvent para a loja.

**Testes obrigatórios:**
- Back-end: Delivery sem endereço falha; pickup não exige endereço.
- Front-end: Campo endereço aparece apenas em entrega.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S15-H03: Como cliente, quero informar endereço para entrega.

Contexto obrigatório:
- Sprint: Sprint 15 — Pedido online
- Domínio: Pedido online
- Documentos de referência: DOM, FLUXOS
- Back-end esperado: Criar `CustomerAddress` quando `DELIVERY`.
- Front-end esperado: Form de endereço condicional.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Delivery sem endereço falha; pickup não exige endereço..
5. Crie/ajuste testes unitários do front-end: Campo endereço aparece apenas em entrega..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S15-H04 — Como cliente, quero criar pedido online.

**Domínio técnico:** Pedido online

**Documentos que justificam a implementação:**
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `POST /api/orders`.
- Front-end planejado no roadmap: Checkout confirma pedido.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: online/order, customer, address e checkout validation
- `PublicOrderController`, `CheckoutService`, `CustomerService`, `OrderService`, `PaymentService`, `SyncEventService`

Front-end:
- `front-end/` — app/feature alvo: site-publico/cart, checkout e order-status
- carrinho do site, checkout, formulário de cliente/endereço e página de status

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `POST /api/orders`.
3. Implementar o front-end previsto: Checkout confirma pedido.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pedido online deve ter cliente.
- pedido delivery exige endereço.
- servidor recalcula valores e não confia no carrinho do front-end.
- pedido deve gerar SyncEvent para a loja.

**Testes obrigatórios:**
- Back-end: Pedido sem itens falha; snapshot de nome/preço salvo; status inicial correto.
- Front-end: Confirmação mostra número/status.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S15-H04: Como cliente, quero criar pedido online.

Contexto obrigatório:
- Sprint: Sprint 15 — Pedido online
- Domínio: Pedido online
- Documentos de referência: FLUXOS, DOM
- Back-end esperado: `POST /api/orders`.
- Front-end esperado: Checkout confirma pedido.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: FLUXOS, DOM.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Pedido sem itens falha; snapshot de nome/preço salvo; status inicial correto..
5. Crie/ajuste testes unitários do front-end: Confirmação mostra número/status..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S15-H05 — Como sistema, quero criar sync event do pedido online.

**Domínio técnico:** Pedido online

**Documentos que justificam a implementação:**
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar `SyncEvent ORDER_CREATED` online.
- Front-end planejado no roadmap: Mostrar status “aguardando loja”.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: online/order, customer, address e checkout validation
- `PublicOrderController`, `CheckoutService`, `CustomerService`, `OrderService`, `PaymentService`, `SyncEventService`

Front-end:
- `front-end/` — app/feature alvo: site-publico/cart, checkout e order-status
- carrinho do site, checkout, formulário de cliente/endereço e página de status

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar `SyncEvent ORDER_CREATED` online.
3. Implementar o front-end previsto: Mostrar status “aguardando loja”.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pedido online deve ter cliente.
- pedido delivery exige endereço.
- servidor recalcula valores e não confia no carrinho do front-end.
- pedido deve gerar SyncEvent para a loja.

**Testes obrigatórios:**
- Back-end: Evento PENDING criado; pedido fica SENT_TO_STORE/aguardando envio.
- Front-end: Página de status exibe “aguardando confirmação da loja”.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S15-H05: Como sistema, quero criar sync event do pedido online.

Contexto obrigatório:
- Sprint: Sprint 15 — Pedido online
- Domínio: Pedido online
- Documentos de referência: SYNC, FLUXOS
- Back-end esperado: Criar `SyncEvent ORDER_CREATED` online.
- Front-end esperado: Mostrar status “aguardando loja”.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: SYNC, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Evento PENDING criado; pedido fica SENT_TO_STORE/aguardando envio..
5. Crie/ajuste testes unitários do front-end: Página de status exibe “aguardando confirmação da loja”..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~

---

## Sprint 16 — Pagamento online e webhook

**Objetivo da sprint:** controlar pagamento online sem confiar no front-end.


### S16-H01 — Como cliente, quero escolher pagamento online.

**Domínio técnico:** Pagamento online e webhook

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `POST /api/payments/online`.
- Front-end planejado no roadmap: Opção Pix/cartão online no checkout.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: payment online, gateway adapter e webhook
- `OnlinePaymentController`, `PaymentGatewayService`, integração mock/inicial, `PaymentWebhookController`

Front-end:
- `front-end/` — app/feature alvo: checkout/payment e order status
- etapa de pagamento no checkout, instruções de pagamento e status do pedido

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `POST /api/payments/online`.
3. Implementar o front-end previsto: Opção Pix/cartão online no checkout.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pagamento online não pode ser confirmado pelo front-end.
- webhook deve validar assinatura/token.
- webhook duplicado deve ser idempotente.
- payload bruto deve ser registrado para auditoria técnica.

**Testes obrigatórios:**
- Back-end: Cria Payment PENDING; pedido fica PAYMENT_PENDING.
- Front-end: Seleção de método altera etapa de pagamento.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S16-H01: Como cliente, quero escolher pagamento online.

Contexto obrigatório:
- Sprint: Sprint 16 — Pagamento online e webhook
- Domínio: Pagamento online e webhook
- Documentos de referência: DOM, FLUXOS
- Back-end esperado: `POST /api/payments/online`.
- Front-end esperado: Opção Pix/cartão online no checkout.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Cria Payment PENDING; pedido fica PAYMENT_PENDING..
5. Crie/ajuste testes unitários do front-end: Seleção de método altera etapa de pagamento..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S16-H02 — Como sistema, quero integrar gateway via adapter.

**Domínio técnico:** Pagamento online e webhook

**Documentos que justificam a implementação:**
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Interface `PaymentGatewayService`.
- Front-end planejado no roadmap: Tela mostra instruções/dados retornados.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: payment online, gateway adapter e webhook
- `OnlinePaymentController`, `PaymentGatewayService`, integração mock/inicial, `PaymentWebhookController`

Front-end:
- `front-end/` — app/feature alvo: checkout/payment e order status
- etapa de pagamento no checkout, instruções de pagamento e status do pedido

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Interface `PaymentGatewayService`.
3. Implementar o front-end previsto: Tela mostra instruções/dados retornados.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pagamento online não pode ser confirmado pelo front-end.
- webhook deve validar assinatura/token.
- webhook duplicado deve ser idempotente.
- payload bruto deve ser registrado para auditoria técnica.

**Testes obrigatórios:**
- Back-end: Adapter mock retorna cobrança; falha do gateway retorna erro controlado.
- Front-end: Componente renderiza QR/instruções mockadas.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S16-H02: Como sistema, quero integrar gateway via adapter.

Contexto obrigatório:
- Sprint: Sprint 16 — Pagamento online e webhook
- Domínio: Pagamento online e webhook
- Documentos de referência: FLUXOS
- Back-end esperado: Interface `PaymentGatewayService`.
- Front-end esperado: Tela mostra instruções/dados retornados.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Adapter mock retorna cobrança; falha do gateway retorna erro controlado..
5. Crie/ajuste testes unitários do front-end: Componente renderiza QR/instruções mockadas..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S16-H03 — Como gateway, quero enviar webhook.

**Domínio técnico:** Pagamento online e webhook

**Documentos que justificam a implementação:**
- `deploy-online.md`: VPS Hostinger, Nginx, HTTPS, Docker Compose, webhooks, CI/CD, backup e segurança online.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `POST /api/payments/webhook`.
- Front-end planejado no roadmap: Página de status consulta pedido.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: payment online, gateway adapter e webhook
- `OnlinePaymentController`, `PaymentGatewayService`, integração mock/inicial, `PaymentWebhookController`

Front-end:
- `front-end/` — app/feature alvo: checkout/payment e order status
- etapa de pagamento no checkout, instruções de pagamento e status do pedido

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `POST /api/payments/webhook`.
3. Implementar o front-end previsto: Página de status consulta pedido.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pagamento online não pode ser confirmado pelo front-end.
- webhook deve validar assinatura/token.
- webhook duplicado deve ser idempotente.
- payload bruto deve ser registrado para auditoria técnica.

**Testes obrigatórios:**
- Back-end: Assinatura inválida falha; payload bruto é registrado; webhook duplicado é idempotente.
- Front-end: Status muda conforme polling/mock.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S16-H03: Como gateway, quero enviar webhook.

Contexto obrigatório:
- Sprint: Sprint 16 — Pagamento online e webhook
- Domínio: Pagamento online e webhook
- Documentos de referência: DO, FLUXOS
- Back-end esperado: `POST /api/payments/webhook`.
- Front-end esperado: Página de status consulta pedido.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DO, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Assinatura inválida falha; payload bruto é registrado; webhook duplicado é idempotente..
5. Crie/ajuste testes unitários do front-end: Status muda conforme polling/mock..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S16-H04 — Como sistema, quero aprovar pagamento por webhook.

**Domínio técnico:** Pagamento online e webhook

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Atualizar `PaymentStatus.PAID` e `Order.PAID`.
- Front-end planejado no roadmap: Página de pedido mostra pago.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: payment online, gateway adapter e webhook
- `OnlinePaymentController`, `PaymentGatewayService`, integração mock/inicial, `PaymentWebhookController`

Front-end:
- `front-end/` — app/feature alvo: checkout/payment e order status
- etapa de pagamento no checkout, instruções de pagamento e status do pedido

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Atualizar `PaymentStatus.PAID` e `Order.PAID`.
3. Implementar o front-end previsto: Página de pedido mostra pago.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pagamento online não pode ser confirmado pelo front-end.
- webhook deve validar assinatura/token.
- webhook duplicado deve ser idempotente.
- payload bruto deve ser registrado para auditoria técnica.

**Testes obrigatórios:**
- Back-end: Front-end não consegue confirmar pagamento; só webhook altera para PAID.
- Front-end: Status “Pago” aparece após atualização.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S16-H04: Como sistema, quero aprovar pagamento por webhook.

Contexto obrigatório:
- Sprint: Sprint 16 — Pagamento online e webhook
- Domínio: Pagamento online e webhook
- Documentos de referência: DOM, FLUXOS
- Back-end esperado: Atualizar `PaymentStatus.PAID` e `Order.PAID`.
- Front-end esperado: Página de pedido mostra pago.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Front-end não consegue confirmar pagamento; só webhook altera para PAID..
5. Crie/ajuste testes unitários do front-end: Status “Pago” aparece após atualização..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S16-H05 — Como sistema, quero tratar pagamento recusado/expirado.

**Domínio técnico:** Pagamento online e webhook

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Atualizar para REFUSED/EXPIRED.
- Front-end planejado no roadmap: Mostrar instrução para nova tentativa.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: payment online, gateway adapter e webhook
- `OnlinePaymentController`, `PaymentGatewayService`, integração mock/inicial, `PaymentWebhookController`

Front-end:
- `front-end/` — app/feature alvo: checkout/payment e order status
- etapa de pagamento no checkout, instruções de pagamento e status do pedido

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Atualizar para REFUSED/EXPIRED.
3. Implementar o front-end previsto: Mostrar instrução para nova tentativa.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- pagamento online não pode ser confirmado pelo front-end.
- webhook deve validar assinatura/token.
- webhook duplicado deve ser idempotente.
- payload bruto deve ser registrado para auditoria técnica.

**Testes obrigatórios:**
- Back-end: Status recusado não cria envio para loja; expirado bloqueia pagamento antigo.
- Front-end: Mensagem de recusado/expirado aparece.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S16-H05: Como sistema, quero tratar pagamento recusado/expirado.

Contexto obrigatório:
- Sprint: Sprint 16 — Pagamento online e webhook
- Domínio: Pagamento online e webhook
- Documentos de referência: DOM, FLUXOS
- Back-end esperado: Atualizar para REFUSED/EXPIRED.
- Front-end esperado: Mostrar instrução para nova tentativa.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Status recusado não cria envio para loja; expirado bloqueia pagamento antigo..
5. Crie/ajuste testes unitários do front-end: Mensagem de recusado/expirado aparece..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~

---

## Sprint 17 — Sync online → local e painel de sincronização

**Objetivo da sprint:** baixar pedidos online para a loja e monitorar falhas.


### S17-H01 — Como worker local, quero buscar pendências online.

**Domínio técnico:** Sync online → local e painel

**Documentos que justificam a implementação:**
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `GET /api/sync/pending?storeId=...`.
- Front-end planejado no roadmap: Painel mostra pedidos aguardando loja.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: sync pull, ACK, inbox, painel status e reprocessamento
- `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`
- política de retry e endpoints de ACK/fail/status

Front-end:
- `front-end/` — app/feature alvo: admin/sync-panel, pdv/online-orders e kds/origin
- badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `GET /api/sync/pending?storeId=...`.
3. Implementar o front-end previsto: Painel mostra pedidos aguardando loja.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- local faz pull porque pode estar atrás de NAT.
- evento duplicado não pode criar pedido duplicado.
- ACK deve ser seguro para repetição.
- ignorar evento exige permissão e motivo.

**Testes obrigatórios:**
- Back-end: Retorna apenas eventos da loja; sem assinatura válida falha.
- Front-end: Lista de pendências renderiza.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S17-H01: Como worker local, quero buscar pendências online.

Contexto obrigatório:
- Sprint: Sprint 17 — Sync online → local e painel de sincronização
- Domínio: Sync online → local e painel
- Documentos de referência: SYNC
- Back-end esperado: `GET /api/sync/pending?storeId=...`.
- Front-end esperado: Painel mostra pedidos aguardando loja.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: SYNC.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Retorna apenas eventos da loja; sem assinatura válida falha..
5. Crie/ajuste testes unitários do front-end: Lista de pendências renderiza..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S17-H02 — Como API local, quero processar pedido online recebido.

**Domínio técnico:** Sync online → local e painel

**Documentos que justificam a implementação:**
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Gravar Order/Items/Payment local.
- Front-end planejado no roadmap: PDV mostra pedido online recebido.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: sync pull, ACK, inbox, painel status e reprocessamento
- `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`
- política de retry e endpoints de ACK/fail/status

Front-end:
- `front-end/` — app/feature alvo: admin/sync-panel, pdv/online-orders e kds/origin
- badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Gravar Order/Items/Payment local.
3. Implementar o front-end previsto: PDV mostra pedido online recebido.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- local faz pull porque pode estar atrás de NAT.
- evento duplicado não pode criar pedido duplicado.
- ACK deve ser seguro para repetição.
- ignorar evento exige permissão e motivo.

**Testes obrigatórios:**
- Back-end: Evento duplicado não cria pedido duplicado; cliente/endereço persistem.
- Front-end: Pedido aparece na lista local.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S17-H02: Como API local, quero processar pedido online recebido.

Contexto obrigatório:
- Sprint: Sprint 17 — Sync online → local e painel de sincronização
- Domínio: Sync online → local e painel
- Documentos de referência: SYNC, DOM
- Back-end esperado: Gravar Order/Items/Payment local.
- Front-end esperado: PDV mostra pedido online recebido.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: SYNC, DOM.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Evento duplicado não cria pedido duplicado; cliente/endereço persistem..
5. Crie/ajuste testes unitários do front-end: Pedido aparece na lista local..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S17-H03 — Como sistema, quero confirmar recebimento.

**Domínio técnico:** Sync online → local e painel

**Documentos que justificam a implementação:**
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `POST /api/sync/events/{id}/ack`.
- Front-end planejado no roadmap: Status muda para recebido pela loja.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: sync pull, ACK, inbox, painel status e reprocessamento
- `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`
- política de retry e endpoints de ACK/fail/status

Front-end:
- `front-end/` — app/feature alvo: admin/sync-panel, pdv/online-orders e kds/origin
- badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `POST /api/sync/events/{id}/ack`.
3. Implementar o front-end previsto: Status muda para recebido pela loja.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- local faz pull porque pode estar atrás de NAT.
- evento duplicado não pode criar pedido duplicado.
- ACK deve ser seguro para repetição.
- ignorar evento exige permissão e motivo.

**Testes obrigatórios:**
- Back-end: ACK marca online como RECEIVED_BY_STORE; ACK duplicado é seguro.
- Front-end: Badge de status atualiza.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S17-H03: Como sistema, quero confirmar recebimento.

Contexto obrigatório:
- Sprint: Sprint 17 — Sync online → local e painel de sincronização
- Domínio: Sync online → local e painel
- Documentos de referência: SYNC
- Back-end esperado: `POST /api/sync/events/{id}/ack`.
- Front-end esperado: Status muda para recebido pela loja.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: SYNC.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: ACK marca online como RECEIVED_BY_STORE; ACK duplicado é seguro..
5. Crie/ajuste testes unitários do front-end: Badge de status atualiza..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S17-H04 — Como sistema, quero gerar KDS para pedido online.

**Domínio técnico:** Sync online → local e painel

**Documentos que justificam a implementação:**
- `kds.md`: tickets por setor, WebSocket, status de preparo, tempo de espera e operação local da cozinha.
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar tickets locais se houver preparo.
- Front-end planejado no roadmap: KDS exibe pedido origem SITE/WHATSAPP.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: sync pull, ACK, inbox, painel status e reprocessamento
- `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`
- política de retry e endpoints de ACK/fail/status

Front-end:
- `front-end/` — app/feature alvo: admin/sync-panel, pdv/online-orders e kds/origin
- badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar tickets locais se houver preparo.
3. Implementar o front-end previsto: KDS exibe pedido origem SITE/WHATSAPP.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Garantir transições válidas de status e não gerar ticket para item `SEM_PREPARO`.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- local faz pull porque pode estar atrás de NAT.
- evento duplicado não pode criar pedido duplicado.
- ACK deve ser seguro para repetição.
- ignorar evento exige permissão e motivo.

**Testes obrigatórios:**
- Back-end: Pedido online com setores gera tickets; sem preparo não gera.
- Front-end: Card mostra origem correta.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S17-H04: Como sistema, quero gerar KDS para pedido online.

Contexto obrigatório:
- Sprint: Sprint 17 — Sync online → local e painel de sincronização
- Domínio: Sync online → local e painel
- Documentos de referência: KDS, SYNC
- Back-end esperado: Criar tickets locais se houver preparo.
- Front-end esperado: KDS exibe pedido origem SITE/WHATSAPP.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: KDS, SYNC.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Pedido online com setores gera tickets; sem preparo não gera..
5. Crie/ajuste testes unitários do front-end: Card mostra origem correta..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S17-H05 — Como gerente, quero painel de sincronização.

**Domínio técnico:** Sync online → local e painel

**Documentos que justificam a implementação:**
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `GET /api/sync/status` e listagem por status.
- Front-end planejado no roadmap: Tela com pendentes, erros e retries.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: sync pull, ACK, inbox, painel status e reprocessamento
- `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`
- política de retry e endpoints de ACK/fail/status

Front-end:
- `front-end/` — app/feature alvo: admin/sync-panel, pdv/online-orders e kds/origin
- badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `GET /api/sync/status` e listagem por status.
3. Implementar o front-end previsto: Tela com pendentes, erros e retries.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- local faz pull porque pode estar atrás de NAT.
- evento duplicado não pode criar pedido duplicado.
- ACK deve ser seguro para repetição.
- ignorar evento exige permissão e motivo.

**Testes obrigatórios:**
- Back-end: Filtra PENDING/FAILED; mensagem de erro retornada.
- Front-end: Filtros funcionam; cards mostram contadores.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S17-H05: Como gerente, quero painel de sincronização.

Contexto obrigatório:
- Sprint: Sprint 17 — Sync online → local e painel de sincronização
- Domínio: Sync online → local e painel
- Documentos de referência: SYNC, FLUXOS
- Back-end esperado: `GET /api/sync/status` e listagem por status.
- Front-end esperado: Tela com pendentes, erros e retries.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: SYNC, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Filtra PENDING/FAILED; mensagem de erro retornada..
5. Crie/ajuste testes unitários do front-end: Filtros funcionam; cards mostram contadores..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S17-H06 — Como gerente, quero reprocessar ou ignorar evento.

**Domínio técnico:** Sync online → local e painel

**Documentos que justificam a implementação:**
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Endpoints reprocessar/ignorar com auditoria.
- Front-end planejado no roadmap: Botões “Reprocessar” e “Ignorar”.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: sync pull, ACK, inbox, painel status e reprocessamento
- `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`
- política de retry e endpoints de ACK/fail/status

Front-end:
- `front-end/` — app/feature alvo: admin/sync-panel, pdv/online-orders e kds/origin
- badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Endpoints reprocessar/ignorar com auditoria.
3. Implementar o front-end previsto: Botões “Reprocessar” e “Ignorar”.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- local faz pull porque pode estar atrás de NAT.
- evento duplicado não pode criar pedido duplicado.
- ACK deve ser seguro para repetição.
- ignorar evento exige permissão e motivo.

**Testes obrigatórios:**
- Back-end: Sem permissão falha; ignorar exige motivo; reprocessar altera status.
- Front-end: Modal exige motivo para ignorar.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- O fluxo local continua independente da internet quando a história fizer parte da operação local.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S17-H06: Como gerente, quero reprocessar ou ignorar evento.

Contexto obrigatório:
- Sprint: Sprint 17 — Sync online → local e painel de sincronização
- Domínio: Sync online → local e painel
- Documentos de referência: SYNC, FLUXOS
- Back-end esperado: Endpoints reprocessar/ignorar com auditoria.
- Front-end esperado: Botões “Reprocessar” e “Ignorar”.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: SYNC, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Sem permissão falha; ignorar exige motivo; reprocessar altera status..
5. Crie/ajuste testes unitários do front-end: Modal exige motivo para ignorar..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~

---

## Sprint 18 — Observabilidade, deploy e homologação MVP

**Objetivo da sprint:** preparar operação real com health checks, backup, deploy e validação fim a fim.


### S18-H01 — Como admin, quero health check operacional local.

**Domínio técnico:** Observabilidade, deploy e homologação

**Documentos que justificam a implementação:**
- `deploy-local.md`: Docker Compose local, Nginx, health checks, backup, IP fixo e segurança local.
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `GET /api/sync/health` com DB/Rabbit/Redis/sync.
- Front-end planejado no roadmap: Painel operacional local.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: actuator, health, version, scripts e pipeline
- `HealthController`/Actuator config, `/version`, scripts, pipeline e documentação operacional

Front-end:
- `front-end/` — app/feature alvo: operational panels, version footer e roteiros de homologação
- painéis operacionais, footer de versão e telas de status

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `GET /api/sync/health` com DB/Rabbit/Redis/sync.
3. Implementar o front-end previsto: Painel operacional local.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- health check deve refletir dependências reais.
- backup e restore precisam estar documentados.
- pipeline deve rodar testes antes de gerar imagem.
- homologação deve validar fluxo local e online fim a fim.

**Testes obrigatórios:**
- Back-end: Health reflete dependência falha; pendências são contadas.
- Front-end: Painel renderiza status e alertas.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S18-H01: Como admin, quero health check operacional local.

Contexto obrigatório:
- Sprint: Sprint 18 — Observabilidade, deploy e homologação MVP
- Domínio: Observabilidade, deploy e homologação
- Documentos de referência: DL, SYNC
- Back-end esperado: `GET /api/sync/health` com DB/Rabbit/Redis/sync.
- Front-end esperado: Painel operacional local.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DL, SYNC.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Health reflete dependência falha; pendências são contadas..
5. Crie/ajuste testes unitários do front-end: Painel renderiza status e alertas..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S18-H02 — Como admin, quero health check online.

**Domínio técnico:** Observabilidade, deploy e homologação

**Documentos que justificam a implementação:**
- `deploy-online.md`: VPS Hostinger, Nginx, HTTPS, Docker Compose, webhooks, CI/CD, backup e segurança online.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Health online com certificado/webhooks/eventos.
- Front-end planejado no roadmap: Painel online simples.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: actuator, health, version, scripts e pipeline
- `HealthController`/Actuator config, `/version`, scripts, pipeline e documentação operacional

Front-end:
- `front-end/` — app/feature alvo: operational panels, version footer e roteiros de homologação
- painéis operacionais, footer de versão e telas de status

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Health online com certificado/webhooks/eventos.
3. Implementar o front-end previsto: Painel online simples.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- health check deve refletir dependências reais.
- backup e restore precisam estar documentados.
- pipeline deve rodar testes antes de gerar imagem.
- homologação deve validar fluxo local e online fim a fim.

**Testes obrigatórios:**
- Back-end: Webhook indisponível afeta status; DB down retorna DOWN.
- Front-end: Tela exibe status online.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S18-H02: Como admin, quero health check online.

Contexto obrigatório:
- Sprint: Sprint 18 — Observabilidade, deploy e homologação MVP
- Domínio: Observabilidade, deploy e homologação
- Documentos de referência: DO, FLUXOS
- Back-end esperado: Health online com certificado/webhooks/eventos.
- Front-end esperado: Painel online simples.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DO, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Webhook indisponível afeta status; DB down retorna DOWN..
5. Crie/ajuste testes unitários do front-end: Tela exibe status online..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S18-H03 — Como dev, quero pipeline de build e deploy.

**Domínio técnico:** Observabilidade, deploy e homologação

**Documentos que justificam a implementação:**
- `deploy-online.md`: VPS Hostinger, Nginx, HTTPS, Docker Compose, webhooks, CI/CD, backup e segurança online.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Build, test, Docker image e push.
- Front-end planejado no roadmap: Build Angular e publicação estática.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: actuator, health, version, scripts e pipeline
- `HealthController`/Actuator config, `/version`, scripts, pipeline e documentação operacional

Front-end:
- `front-end/` — app/feature alvo: operational panels, version footer e roteiros de homologação
- painéis operacionais, footer de versão e telas de status

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Build, test, Docker image e push.
3. Implementar o front-end previsto: Build Angular e publicação estática.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- health check deve refletir dependências reais.
- backup e restore precisam estar documentados.
- pipeline deve rodar testes antes de gerar imagem.
- homologação deve validar fluxo local e online fim a fim.

**Testes obrigatórios:**
- Back-end: Pipeline falha com testes quebrados; imagem recebe tag.
- Front-end: Build front falha com lint/test quebrado.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S18-H03: Como dev, quero pipeline de build e deploy.

Contexto obrigatório:
- Sprint: Sprint 18 — Observabilidade, deploy e homologação MVP
- Domínio: Observabilidade, deploy e homologação
- Documentos de referência: DO
- Back-end esperado: Build, test, Docker image e push.
- Front-end esperado: Build Angular e publicação estática.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DO.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Pipeline falha com testes quebrados; imagem recebe tag..
5. Crie/ajuste testes unitários do front-end: Build front falha com lint/test quebrado..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S18-H04 — Como operador, quero validar fluxo fim a fim local.

**Domínio técnico:** Observabilidade, deploy e homologação

**Documentos que justificam a implementação:**
- `pdv.md`: frente de caixa local, carrinho, pagamento, impressão, gaveta, KDS e modo offline.
- `kds.md`: tickets por setor, WebSocket, status de preparo, tempo de espera e operação local da cozinha.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Script/cenário: login → caixa → venda → pagamento → impressão → KDS → sync pending.
- Front-end planejado no roadmap: Roteiro de homologação no front.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: actuator, health, version, scripts e pipeline
- `HealthController`/Actuator config, `/version`, scripts, pipeline e documentação operacional

Front-end:
- `front-end/` — app/feature alvo: operational panels, version footer e roteiros de homologação
- painéis operacionais, footer de versão e telas de status

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Script/cenário: login → caixa → venda → pagamento → impressão → KDS → sync pending.
3. Implementar o front-end previsto: Roteiro de homologação no front.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
7. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
8. Garantir transições válidas de status e não gerar ticket para item `SEM_PREPARO`.
9. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
10. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- health check deve refletir dependências reais.
- backup e restore precisam estar documentados.
- pipeline deve rodar testes antes de gerar imagem.
- homologação deve validar fluxo local e online fim a fim.

**Testes obrigatórios:**
- Back-end: Testes de serviços cobrem cada etapa principal.
- Front-end: Testes de componentes cobrem fluxo simulado.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S18-H04: Como operador, quero validar fluxo fim a fim local.

Contexto obrigatório:
- Sprint: Sprint 18 — Observabilidade, deploy e homologação MVP
- Domínio: Observabilidade, deploy e homologação
- Documentos de referência: PDV, KDS, FLUXOS
- Back-end esperado: Script/cenário: login → caixa → venda → pagamento → impressão → KDS → sync pending.
- Front-end esperado: Roteiro de homologação no front.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: PDV, KDS, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Testes de serviços cobrem cada etapa principal..
5. Crie/ajuste testes unitários do front-end: Testes de componentes cobrem fluxo simulado..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S18-H05 — Como admin, quero validar fluxo online fim a fim.

**Domínio técnico:** Observabilidade, deploy e homologação

**Documentos que justificam a implementação:**
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.
- `deploy-online.md`: VPS Hostinger, Nginx, HTTPS, Docker Compose, webhooks, CI/CD, backup e segurança online.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Site → pedido → pagamento → webhook → sync → KDS.
- Front-end planejado no roadmap: Roteiro de homologação site/admin.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: actuator, health, version, scripts e pipeline
- `HealthController`/Actuator config, `/version`, scripts, pipeline e documentação operacional

Front-end:
- `front-end/` — app/feature alvo: operational panels, version footer e roteiros de homologação
- painéis operacionais, footer de versão e telas de status

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Site → pedido → pagamento → webhook → sync → KDS.
3. Implementar o front-end previsto: Roteiro de homologação site/admin.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
7. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
8. Garantir transições válidas de status e não gerar ticket para item `SEM_PREPARO`.
9. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
10. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- health check deve refletir dependências reais.
- backup e restore precisam estar documentados.
- pipeline deve rodar testes antes de gerar imagem.
- homologação deve validar fluxo local e online fim a fim.

**Testes obrigatórios:**
- Back-end: Webhook + sync idempotente em sequência.
- Front-end: Checkout e status renderizam etapas.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S18-H05: Como admin, quero validar fluxo online fim a fim.

Contexto obrigatório:
- Sprint: Sprint 18 — Observabilidade, deploy e homologação MVP
- Domínio: Observabilidade, deploy e homologação
- Documentos de referência: FLUXOS, SYNC, DO
- Back-end esperado: Site → pedido → pagamento → webhook → sync → KDS.
- Front-end esperado: Roteiro de homologação site/admin.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: FLUXOS, SYNC, DO.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Webhook + sync idempotente em sequência..
5. Crie/ajuste testes unitários do front-end: Checkout e status renderizam etapas..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S18-H06 — Como suporte, quero documentação de implantação.

**Domínio técnico:** Observabilidade, deploy e homologação

**Documentos que justificam a implementação:**
- `deploy-local.md`: Docker Compose local, Nginx, health checks, backup, IP fixo e segurança local.
- `deploy-online.md`: VPS Hostinger, Nginx, HTTPS, Docker Compose, webhooks, CI/CD, backup e segurança online.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Atualizar README e scripts.
- Front-end planejado no roadmap: Tela/links de status e versão.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: actuator, health, version, scripts e pipeline
- `HealthController`/Actuator config, `/version`, scripts, pipeline e documentação operacional

Front-end:
- `front-end/` — app/feature alvo: operational panels, version footer e roteiros de homologação
- painéis operacionais, footer de versão e telas de status

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Atualizar README e scripts.
3. Implementar o front-end previsto: Tela/links de status e versão.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- health check deve refletir dependências reais.
- backup e restore precisam estar documentados.
- pipeline deve rodar testes antes de gerar imagem.
- homologação deve validar fluxo local e online fim a fim.

**Testes obrigatórios:**
- Back-end: Endpoint `/version` retorna versão/commit.
- Front-end: Footer mostra versão.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S18-H06: Como suporte, quero documentação de implantação.

Contexto obrigatório:
- Sprint: Sprint 18 — Observabilidade, deploy e homologação MVP
- Domínio: Observabilidade, deploy e homologação
- Documentos de referência: DL, DO
- Back-end esperado: Atualizar README e scripts.
- Front-end esperado: Tela/links de status e versão.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DL, DO.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Endpoint `/version` retorna versão/commit..
5. Crie/ajuste testes unitários do front-end: Footer mostra versão..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~

---

## Sprint 19 — Estoque básico

**Objetivo da sprint:** registrar movimentações de estoque e refletir disponibilidade.


### S19-H01 — Como gerente, quero registrar entrada de estoque.

**Domínio técnico:** Estoque básico

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar `StockMovement IN`.
- Front-end planejado no roadmap: Tela de ajuste de estoque.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: stock-movement, baixa por venda e availability automation
- `StockMovementService`, `StockController`, integração com finalização de venda e disponibilidade

Front-end:
- `front-end/` — app/feature alvo: admin/stock-adjustment e stock badges
- tela de ajuste de estoque, badges de estoque e histórico

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar `StockMovement IN`.
3. Implementar o front-end previsto: Tela de ajuste de estoque.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- estoque físico é fonte local.
- ajuste manual exige motivo.
- estoque zero pode indisponibilizar produto conforme regra.
- movimento de estoque deve ser rastreável.

**Testes obrigatórios:**
- Back-end: Quantidade positiva; produto obrigatório; usuário obrigatório.
- Front-end: Form valida produto/quantidade.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S19-H01: Como gerente, quero registrar entrada de estoque.

Contexto obrigatório:
- Sprint: Sprint 19 — Estoque básico
- Domínio: Estoque básico
- Documentos de referência: DOM, FLUXOS
- Back-end esperado: Criar `StockMovement IN`.
- Front-end esperado: Tela de ajuste de estoque.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Quantidade positiva; produto obrigatório; usuário obrigatório..
5. Crie/ajuste testes unitários do front-end: Form valida produto/quantidade..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S19-H02 — Como gerente, quero registrar perda.

**Domínio técnico:** Estoque básico

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar `StockMovement LOSS`.
- Front-end planejado no roadmap: Ação “perda”.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: stock-movement, baixa por venda e availability automation
- `StockMovementService`, `StockController`, integração com finalização de venda e disponibilidade

Front-end:
- `front-end/` — app/feature alvo: admin/stock-adjustment e stock badges
- tela de ajuste de estoque, badges de estoque e histórico

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar `StockMovement LOSS`.
3. Implementar o front-end previsto: Ação “perda”.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Aplicar autenticação/autorização no back-end e refletir a permissão no front-end apenas como apoio visual.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- estoque físico é fonte local.
- ajuste manual exige motivo.
- estoque zero pode indisponibilizar produto conforme regra.
- movimento de estoque deve ser rastreável.

**Testes obrigatórios:**
- Back-end: Motivo obrigatório; movimento audita usuário.
- Front-end: Modal exige motivo.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S19-H02: Como gerente, quero registrar perda.

Contexto obrigatório:
- Sprint: Sprint 19 — Estoque básico
- Domínio: Estoque básico
- Documentos de referência: DOM
- Back-end esperado: Criar `StockMovement LOSS`.
- Front-end esperado: Ação “perda”.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Motivo obrigatório; movimento audita usuário..
5. Crie/ajuste testes unitários do front-end: Modal exige motivo..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S19-H03 — Como sistema, quero baixar estoque por venda.

**Domínio técnico:** Estoque básico

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Ao finalizar venda, gerar `SALE`.
- Front-end planejado no roadmap: Mostrar estoque estimado no admin.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: stock-movement, baixa por venda e availability automation
- `StockMovementService`, `StockController`, integração com finalização de venda e disponibilidade

Front-end:
- `front-end/` — app/feature alvo: admin/stock-adjustment e stock badges
- tela de ajuste de estoque, badges de estoque e histórico

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Ao finalizar venda, gerar `SALE`.
3. Implementar o front-end previsto: Mostrar estoque estimado no admin.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Validar valores monetários com BigDecimal no back-end e nunca confiar em total calculado apenas no front-end.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- estoque físico é fonte local.
- ajuste manual exige motivo.
- estoque zero pode indisponibilizar produto conforme regra.
- movimento de estoque deve ser rastreável.

**Testes obrigatórios:**
- Back-end: Venda baixa quantidade correta; venda cancelada ajusta conforme regra.
- Front-end: Coluna estoque renderiza quantidade.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S19-H03: Como sistema, quero baixar estoque por venda.

Contexto obrigatório:
- Sprint: Sprint 19 — Estoque básico
- Domínio: Estoque básico
- Documentos de referência: DOM, FLUXOS
- Back-end esperado: Ao finalizar venda, gerar `SALE`.
- Front-end esperado: Mostrar estoque estimado no admin.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Venda baixa quantidade correta; venda cancelada ajusta conforme regra..
5. Crie/ajuste testes unitários do front-end: Coluna estoque renderiza quantidade..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S19-H04 — Como sistema, quero indisponibilizar produto sem estoque.

**Domínio técnico:** Estoque básico

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Regra automática atualiza availability.
- Front-end planejado no roadmap: Badge “acabou”.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: stock-movement, baixa por venda e availability automation
- `StockMovementService`, `StockController`, integração com finalização de venda e disponibilidade

Front-end:
- `front-end/` — app/feature alvo: admin/stock-adjustment e stock badges
- tela de ajuste de estoque, badges de estoque e histórico

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Regra automática atualiza availability.
3. Implementar o front-end previsto: Badge “acabou”.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- estoque físico é fonte local.
- ajuste manual exige motivo.
- estoque zero pode indisponibilizar produto conforme regra.
- movimento de estoque deve ser rastreável.

**Testes obrigatórios:**
- Back-end: Estoque zero cria `PRODUCT_UNAVAILABLE`; sob encomenda não bloqueia.
- Front-end: Badge atualiza no catálogo.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S19-H04: Como sistema, quero indisponibilizar produto sem estoque.

Contexto obrigatório:
- Sprint: Sprint 19 — Estoque básico
- Domínio: Estoque básico
- Documentos de referência: DOM, SYNC
- Back-end esperado: Regra automática atualiza availability.
- Front-end esperado: Badge “acabou”.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, SYNC.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Estoque zero cria `PRODUCT_UNAVAILABLE`; sob encomenda não bloqueia..
5. Crie/ajuste testes unitários do front-end: Badge atualiza no catálogo..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S19-H05 — Como sistema, quero sincronizar estoque/disponibilidade.

**Domínio técnico:** Estoque básico

**Documentos que justificam a implementação:**
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar `STOCK_MOVED` e `PRODUCT_UNAVAILABLE`.
- Front-end planejado no roadmap: Painel mostra pendente de sync.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: stock-movement, baixa por venda e availability automation
- `StockMovementService`, `StockController`, integração com finalização de venda e disponibilidade

Front-end:
- `front-end/` — app/feature alvo: admin/stock-adjustment e stock badges
- tela de ajuste de estoque, badges de estoque e histórico

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar `STOCK_MOVED` e `PRODUCT_UNAVAILABLE`.
3. Implementar o front-end previsto: Painel mostra pendente de sync.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- estoque físico é fonte local.
- ajuste manual exige motivo.
- estoque zero pode indisponibilizar produto conforme regra.
- movimento de estoque deve ser rastreável.

**Testes obrigatórios:**
- Back-end: Evento criado e idempotente.
- Front-end: Status de sync aparece.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S19-H05: Como sistema, quero sincronizar estoque/disponibilidade.

Contexto obrigatório:
- Sprint: Sprint 19 — Estoque básico
- Domínio: Estoque básico
- Documentos de referência: SYNC
- Back-end esperado: Criar `STOCK_MOVED` e `PRODUCT_UNAVAILABLE`.
- Front-end esperado: Painel mostra pendente de sync.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: SYNC.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Evento criado e idempotente..
5. Crie/ajuste testes unitários do front-end: Status de sync aparece..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~

---

## Sprint 20 — WhatsApp inicial e atendimento assistido

**Objetivo da sprint:** iniciar pedidos via WhatsApp com atendimento assistido antes de chatbot completo.


### S20-H01 — Como provider, quero enviar webhook WhatsApp.

**Domínio técnico:** WhatsApp inicial

**Documentos que justificam a implementação:**
- `deploy-online.md`: VPS Hostinger, Nginx, HTTPS, Docker Compose, webhooks, CI/CD, backup e segurança online.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: `POST /api/whatsapp/webhook`.
- Front-end planejado no roadmap: Painel lista conversas recebidas.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: whatsapp webhook, conversation, assisted order e message adapter
- `WhatsappWebhookController`, `ConversationService`, `AssistedOrderService`, serviço de mensagens

Front-end:
- `front-end/` — app/feature alvo: admin/whatsapp conversations e assisted cart
- painel de conversas, busca de produtos WhatsApp e carrinho assistido

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: `POST /api/whatsapp/webhook`.
3. Implementar o front-end previsto: Painel lista conversas recebidas.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- webhook deve ser validado e idempotente.
- pedido WhatsApp usa origem WHATSAPP.
- somente produtos sellOnWhatsapp aparecem.
- falha ao enviar mensagem não deve cancelar pedido salvo.

**Testes obrigatórios:**
- Back-end: Assinatura inválida falha; mensagem duplicada é ignorada.
- Front-end: Lista renderiza conversa e última mensagem.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S20-H01: Como provider, quero enviar webhook WhatsApp.

Contexto obrigatório:
- Sprint: Sprint 20 — WhatsApp inicial e atendimento assistido
- Domínio: WhatsApp inicial
- Documentos de referência: DO, FLUXOS
- Back-end esperado: `POST /api/whatsapp/webhook`.
- Front-end esperado: Painel lista conversas recebidas.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DO, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Assinatura inválida falha; mensagem duplicada é ignorada..
5. Crie/ajuste testes unitários do front-end: Lista renderiza conversa e última mensagem..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S20-H02 — Como atendente, quero ver produtos habilitados para WhatsApp.

**Domínio técnico:** WhatsApp inicial

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Endpoint filtra `sellOnWhatsapp`.
- Front-end planejado no roadmap: Busca produto dentro da conversa.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: whatsapp webhook, conversation, assisted order e message adapter
- `WhatsappWebhookController`, `ConversationService`, `AssistedOrderService`, serviço de mensagens

Front-end:
- `front-end/` — app/feature alvo: admin/whatsapp conversations e assisted cart
- painel de conversas, busca de produtos WhatsApp e carrinho assistido

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Endpoint filtra `sellOnWhatsapp`.
3. Implementar o front-end previsto: Busca produto dentro da conversa.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Respeitar filtros por canal: PDV, site/online e WhatsApp, além dos campos `active` e `available`.
7. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
8. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- webhook deve ser validado e idempotente.
- pedido WhatsApp usa origem WHATSAPP.
- somente produtos sellOnWhatsapp aparecem.
- falha ao enviar mensagem não deve cancelar pedido salvo.

**Testes obrigatórios:**
- Back-end: Produto sem canal WhatsApp não aparece.
- Front-end: Busca filtra catálogo WhatsApp.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S20-H02: Como atendente, quero ver produtos habilitados para WhatsApp.

Contexto obrigatório:
- Sprint: Sprint 20 — WhatsApp inicial e atendimento assistido
- Domínio: WhatsApp inicial
- Documentos de referência: DOM, FLUXOS
- Back-end esperado: Endpoint filtra `sellOnWhatsapp`.
- Front-end esperado: Busca produto dentro da conversa.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Produto sem canal WhatsApp não aparece..
5. Crie/ajuste testes unitários do front-end: Busca filtra catálogo WhatsApp..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S20-H03 — Como atendente, quero montar pedido assistido.

**Domínio técnico:** WhatsApp inicial

**Documentos que justificam a implementação:**
- `modelo-de-dominio.md`: entidades, agregados, enums, regras de negócio e prioridade de implementação do domínio.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar pedido origem `WHATSAPP`.
- Front-end planejado no roadmap: Carrinho assistido no admin/WhatsApp.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: whatsapp webhook, conversation, assisted order e message adapter
- `WhatsappWebhookController`, `ConversationService`, `AssistedOrderService`, serviço de mensagens

Front-end:
- `front-end/` — app/feature alvo: admin/whatsapp conversations e assisted cart
- painel de conversas, busca de produtos WhatsApp e carrinho assistido

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar pedido origem `WHATSAPP`.
3. Implementar o front-end previsto: Carrinho assistido no admin/WhatsApp.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- webhook deve ser validado e idempotente.
- pedido WhatsApp usa origem WHATSAPP.
- somente produtos sellOnWhatsapp aparecem.
- falha ao enviar mensagem não deve cancelar pedido salvo.

**Testes obrigatórios:**
- Back-end: Pedido exige cliente/telefone; snapshot salvo.
- Front-end: Carrinho assistido adiciona itens.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S20-H03: Como atendente, quero montar pedido assistido.

Contexto obrigatório:
- Sprint: Sprint 20 — WhatsApp inicial e atendimento assistido
- Domínio: WhatsApp inicial
- Documentos de referência: DOM, FLUXOS
- Back-end esperado: Criar pedido origem `WHATSAPP`.
- Front-end esperado: Carrinho assistido no admin/WhatsApp.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: DOM, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Pedido exige cliente/telefone; snapshot salvo..
5. Crie/ajuste testes unitários do front-end: Carrinho assistido adiciona itens..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S20-H04 — Como cliente, quero receber confirmação.

**Domínio técnico:** WhatsApp inicial

**Documentos que justificam a implementação:**
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Adapter de envio de mensagem.
- Front-end planejado no roadmap: Botão enviar resumo.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: whatsapp webhook, conversation, assisted order e message adapter
- `WhatsappWebhookController`, `ConversationService`, `AssistedOrderService`, serviço de mensagens

Front-end:
- `front-end/` — app/feature alvo: admin/whatsapp conversations e assisted cart
- painel de conversas, busca de produtos WhatsApp e carrinho assistido

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Adapter de envio de mensagem.
3. Implementar o front-end previsto: Botão enviar resumo.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
6. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- webhook deve ser validado e idempotente.
- pedido WhatsApp usa origem WHATSAPP.
- somente produtos sellOnWhatsapp aparecem.
- falha ao enviar mensagem não deve cancelar pedido salvo.

**Testes obrigatórios:**
- Back-end: Falha do provider não cancela pedido; erro é registrado.
- Front-end: Preview do resumo formatado.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S20-H04: Como cliente, quero receber confirmação.

Contexto obrigatório:
- Sprint: Sprint 20 — WhatsApp inicial e atendimento assistido
- Domínio: WhatsApp inicial
- Documentos de referência: FLUXOS
- Back-end esperado: Adapter de envio de mensagem.
- Front-end esperado: Botão enviar resumo.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Falha do provider não cancela pedido; erro é registrado..
5. Crie/ajuste testes unitários do front-end: Preview do resumo formatado..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~


### S20-H05 — Como sistema, quero sincronizar pedido WhatsApp para loja.

**Domínio técnico:** WhatsApp inicial

**Documentos que justificam a implementação:**
- `sincronizacao.md`: Outbox, Inbox, Retry, HMAC, idempotência, push local → online e pull online → local.
- `fluxos-e-casos-de-uso.md`: fluxos funcionais, casos de uso, regras transversais, APIs sugeridas e critérios de aceite.

**Escopo funcional detalhado:**
- Implementar exatamente a capacidade descrita na história, sem antecipar histórias futuras.
- Back-end planejado no roadmap: Criar SyncEvent ORDER_CREATED.
- Front-end planejado no roadmap: Status “aguardando loja”.
- A história deve deixar uma fatia funcional testável no ambiente correspondente.

**Arquivos/módulos prováveis:**

Back-end:
- `back-end/` — módulo alvo: whatsapp webhook, conversation, assisted order e message adapter
- `WhatsappWebhookController`, `ConversationService`, `AssistedOrderService`, serviço de mensagens

Front-end:
- `front-end/` — app/feature alvo: admin/whatsapp conversations e assisted cart
- painel de conversas, busca de produtos WhatsApp e carrinho assistido

**Tarefas de implementação:**
1. Ler a história, identificar se ela pertence ao ambiente local, online ou compartilhado e confirmar o módulo alvo antes de alterar arquivos.
2. Implementar o back-end previsto: Criar SyncEvent ORDER_CREATED.
3. Implementar o front-end previsto: Status “aguardando loja”.
4. Criar ou ajustar DTOs, mappers, services/use cases, controllers/endpoints e modelos TypeScript necessários.
5. Garantir idempotência por UUID/eventId e status controlado para evitar duplicidade.
6. Atualizar OpenAPI/contratos, quando houver endpoint novo ou alteração de payload.
7. Rodar testes unitários e, quando houver persistência, usar teste de integração com banco/container se o projeto já estiver preparado.

**Regras e cuidados obrigatórios:**
- webhook deve ser validado e idempotente.
- pedido WhatsApp usa origem WHATSAPP.
- somente produtos sellOnWhatsapp aparecem.
- falha ao enviar mensagem não deve cancelar pedido salvo.

**Testes obrigatórios:**
- Back-end: Evento idempotente; ACK atualiza status.
- Front-end: Status renderiza corretamente.
- Adicionar testes negativos para validações críticas quando a história alterar regra de negócio.
- Não remover testes existentes para fazer a suíte passar.

**Critérios de aceite da história:**
- A história compila e passa nos testes automatizados existentes.
- A implementação cobre as regras descritas no roadmap e nos documentos de referência.
- As validações críticas ficam no back-end, não apenas no front-end.
- DTOs e contratos HTTP ficam explícitos e compatíveis com o front-end.
- Eventos externos, webhooks ou sincronizações são idempotentes quando aplicável.
- Ações críticas registram auditoria ou evento de sincronização quando previsto.

**Prompt para colar no Codex CLI:**

~~~text
Implemente somente a história S20-H05: Como sistema, quero sincronizar pedido WhatsApp para loja.

Contexto obrigatório:
- Sprint: Sprint 20 — WhatsApp inicial e atendimento assistido
- Domínio: WhatsApp inicial
- Documentos de referência: SYNC, FLUXOS
- Back-end esperado: Criar SyncEvent ORDER_CREATED.
- Front-end esperado: Status “aguardando loja”.

Regras de execução:
1. Antes de alterar código, leia `docs/roadmap-sprints-sistema-mnss.md` e os documentos citados: SYNC, FLUXOS.
2. Preserve a arquitetura de monólito modular local + online, com back-end em camadas e front-end por features/camadas.
3. Não implemente histórias futuras nem refatore fora do escopo necessário.
4. Crie/ajuste testes unitários do back-end: Evento idempotente; ACK atualiza status..
5. Crie/ajuste testes unitários do front-end: Status renderiza corretamente..
6. Ao final, rode os testes aplicáveis e informe arquivos alterados, testes executados e pendências.
~~~
