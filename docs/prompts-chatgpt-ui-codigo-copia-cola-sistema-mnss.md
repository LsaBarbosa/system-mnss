# Prompts para ChatGPT UI — Código Copia e Cola — Sistema MNSS / Nova Aliança

> Documento para usar no cliente web do ChatGPT quando a implementação for feita manualmente copiando e colando código.

Este arquivo contém prompts para as 106 histórias do roadmap. Cada prompt foi montado para pedir código completo por arquivo, com back-end, front-end e testes.

---

## 1. Como usar no ChatGPT web

Use **uma história por conversa ou por bloco de trabalho**.

Fluxo recomendado:

1. Copie o prompt da história.
2. Cole no ChatGPT web.
3. Anexe ou cole os arquivos existentes relevantes do seu projeto, quando houver.
4. Peça para gerar o código completo por arquivo.
5. Copie cada bloco para o caminho indicado.
6. Rode os testes.
7. Volte com erros de compilação/teste para correção incremental.

### Prompt auxiliar antes de qualquer história

Copie este prompt antes da primeira história, caso a conversa esteja vazia:

~~~text
Você vai me ajudar a implementar o Sistema Nova Aliança / MNSS.
Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript, RxJS.
Arquitetura: monólito modular local + online, monólito modular no back-end e front-end Angular por features/camadas.
Regra central: PDV, caixa, KDS, impressão e banco local precisam funcionar sem internet.
Sempre gere código copiável por arquivo, com o caminho do arquivo antes do bloco de código.
Não omita imports.
Não use pseudocódigo.
Não implemente histórias futuras sem eu pedir.
Inclua testes unitários.
Quando faltar contexto do meu projeto, peça o arquivo necessário ou gere uma versão mínima adaptável.
~~~

---

## 2. Regras globais para todos os prompts

- Não confiar no front-end para regra crítica.
- Usar UUID para IDs principais.
- Usar `BigDecimal` para dinheiro no Java.
- Usar `NUMERIC(12,2)` para dinheiro no PostgreSQL.
- Usar Flyway para schema.
- Eventos, sync e webhooks devem ser idempotentes.
- Ações críticas devem gerar auditoria quando previsto.
- Não expor PostgreSQL, RabbitMQ e Redis na internet.
- Separar ambiente local e online conforme o domínio.
- Back-end deve seguir arquitetura modular em camadas (web, service, entity, repository, dto).
- Controllers não devem conter regra de negócio; Services concentram a lógica de aplicação e negócio; Entities representam persistência; Repositories ficam isolados por módulo; DTOs não expõem entidades diretamente.
- Front-end deve seguir arquitetura Angular por features: `domain`, `application`, `data-access`, `ui` e `pages`.
- Componentes de UI não acessam `HttpClient`; chamadas HTTP ficam em `data-access`.

---

# 3. Prompts por história



---

## Sprint 01 — Fundação técnica

**Objetivo da sprint:** deixar a base do projeto preparada para evolução modular.


### S01-H01 — Como dev, quero criar o monorepo para organizar back-end, front-end, infra e docs.

**Resumo do que deve ser feito:**
- Back-end: Criar estrutura `back-end/`, `infra/`, `docs/`.
- Front-end: Manter `front-end/` como app Angular unico, com features `admin`, `pdv`, `kds` e `site-publico` em `src/app/features/`.
- Testes back-end: Teste de arquitetura verificando pacotes obrigatórios com ArchUnit.
- Testes front-end: Teste simples validando que cada app Angular inicial renderiza o shell.
- Documentos-base: ARQ, README

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: back-end/core-domain, back-end/local-app, back-end/online-app e back-end/shared-infra
- Front-end: `front-end/` — app/feature alvo: front-end/src/app/features/admin, front-end/src/app/features/pdv, front-end/src/app/features/kds e front-end/src/app/features/site-publico

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S01-H01: Como dev, quero criar o monorepo para organizar back-end, front-end, infra e docs.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Fundação técnica.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: ARQ, README.
- Regras importantes desta história: manter monólito modular, sem criar microserviços; aplicar estrutura modular no back-end, separar domínio, serviços e controladores, e aplicar front-end por features/camadas; deixar build e testes executáveis desde a primeira entrega; não implementar regra funcional de negócio antes da base estar compilando.

O que gerar:
1. Back-end: Criar estrutura `back-end/`, `infra/`, `docs/`.
2. Front-end: Manter `front-end/` como app Angular unico, com features `admin`, `pdv`, `kds` e `site-publico` em `src/app/features/`.
3. Testes unitários back-end: Teste de arquitetura verificando pacotes obrigatórios com ArchUnit.
4. Testes unitários front-end: Teste simples validando que cada app Angular inicial renderiza o shell.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S01-H02 — Como dev, quero criar o bootstrap da API local.

**Resumo do que deve ser feito:**
- Back-end: Criar `local-app` Spring Boot 3.x com `/actuator/health` e `/api/ping`.
- Front-end: Criar environment local apontando para API local.
- Testes back-end: Context load; `/api/ping` retorna 200; profile `local` carrega.
- Testes front-end: Service HTTP chama `/api/ping` e trata sucesso/erro.
- Documentos-base: ARQ, DL

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: back-end/core-domain, back-end/local-app, back-end/online-app e back-end/shared-infra
- Front-end: `front-end/` — app/feature alvo: front-end/src/app/features/admin, front-end/src/app/features/pdv, front-end/src/app/features/kds e front-end/src/app/features/site-publico

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S01-H02: Como dev, quero criar o bootstrap da API local.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Fundação técnica.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: ARQ, DL.
- Regras importantes desta história: manter monólito modular, sem criar microserviços; aplicar estrutura modular no back-end, separar domínio, serviços e controladores, e aplicar front-end por features/camadas; deixar build e testes executáveis desde a primeira entrega; não implementar regra funcional de negócio antes da base estar compilando.

O que gerar:
1. Back-end: Criar `local-app` Spring Boot 3.x com `/actuator/health` e `/api/ping`.
2. Front-end: Criar environment local apontando para API local.
3. Testes unitários back-end: Context load; `/api/ping` retorna 200; profile `local` carrega.
4. Testes unitários front-end: Service HTTP chama `/api/ping` e trata sucesso/erro.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S01-H03 — Como dev, quero criar o bootstrap da API online.

**Resumo do que deve ser feito:**
- Back-end: Criar `online-app` Spring Boot 3.x com profile `online`.
- Front-end: Criar environment online para `site-publico` e `admin`.
- Testes back-end: Context load; profile `online` exige variáveis obrigatórias.
- Testes front-end: Configuração de environment é carregada corretamente.
- Documentos-base: ARQ, DO

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: back-end/core-domain, back-end/local-app, back-end/online-app e back-end/shared-infra
- Front-end: `front-end/` — app/feature alvo: front-end/src/app/features/admin, front-end/src/app/features/pdv, front-end/src/app/features/kds e front-end/src/app/features/site-publico

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S01-H03: Como dev, quero criar o bootstrap da API online.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Fundação técnica.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: ARQ, DO.
- Regras importantes desta história: manter monólito modular, sem criar microserviços; aplicar estrutura modular no back-end, separar domínio, serviços e controladores, e aplicar front-end por features/camadas; deixar build e testes executáveis desde a primeira entrega; não implementar regra funcional de negócio antes da base estar compilando.

O que gerar:
1. Back-end: Criar `online-app` Spring Boot 3.x com profile `online`.
2. Front-end: Criar environment online para `site-publico` e `admin`.
3. Testes unitários back-end: Context load; profile `online` exige variáveis obrigatórias.
4. Testes unitários front-end: Configuração de environment é carregada corretamente.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S01-H04 — Como dev, quero padronizar resposta de erro.

**Resumo do que deve ser feito:**
- Back-end: Criar `ApiError`, `BusinessException`, `GlobalExceptionHandler`.
- Front-end: Criar componente/shared service para exibir erro padronizado.
- Testes back-end: Validação retorna 400; regra de negócio retorna código esperado; erro inesperado não vaza stacktrace.
- Testes front-end: Interceptor converte erro HTTP em mensagem exibível.
- Documentos-base: FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: back-end/core-domain, back-end/local-app, back-end/online-app e back-end/shared-infra
- Front-end: `front-end/` — app/feature alvo: front-end/src/app/features/admin, front-end/src/app/features/pdv, front-end/src/app/features/kds e front-end/src/app/features/site-publico

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S01-H04: Como dev, quero padronizar resposta de erro.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Fundação técnica.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: FLUXOS.
- Regras importantes desta história: manter monólito modular, sem criar microserviços; aplicar estrutura modular no back-end, separar domínio, serviços e controladores, e aplicar front-end por features/camadas; deixar build e testes executáveis desde a primeira entrega; não implementar regra funcional de negócio antes da base estar compilando.

O que gerar:
1. Back-end: Criar `ApiError`, `BusinessException`, `GlobalExceptionHandler`.
2. Front-end: Criar componente/shared service para exibir erro padronizado.
3. Testes unitários back-end: Validação retorna 400; regra de negócio retorna código esperado; erro inesperado não vaza stacktrace.
4. Testes unitários front-end: Interceptor converte erro HTTP em mensagem exibível.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S01-H05 — Como dev, quero configurar qualidade mínima.

**Resumo do que deve ser feito:**
- Back-end: Adicionar JUnit 5, Mockito, Testcontainers, MapStruct, OpenAPI.
- Front-end: Adicionar ESLint, Prettier, testes Angular.
- Testes back-end: Build falha com teste quebrado; cobertura mínima configurada.
- Testes front-end: `ng test` executa; componente base renderiza sem erro.
- Documentos-base: ARQ

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: back-end/core-domain, back-end/local-app, back-end/online-app e back-end/shared-infra
- Front-end: `front-end/` — app/feature alvo: front-end/src/app/features/admin, front-end/src/app/features/pdv, front-end/src/app/features/kds e front-end/src/app/features/site-publico

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S01-H05: Como dev, quero configurar qualidade mínima.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Fundação técnica.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: ARQ.
- Regras importantes desta história: manter monólito modular, sem criar microserviços; aplicar estrutura modular no back-end, separar domínio, serviços e controladores, e aplicar front-end por features/camadas; deixar build e testes executáveis desde a primeira entrega; não implementar regra funcional de negócio antes da base estar compilando.

O que gerar:
1. Back-end: Adicionar JUnit 5, Mockito, Testcontainers, MapStruct, OpenAPI.
2. Front-end: Adicionar ESLint, Prettier, testes Angular.
3. Testes unitários back-end: Build falha com teste quebrado; cobertura mínima configurada.
4. Testes unitários front-end: `ng test` executa; componente base renderiza sem erro.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.


---

## Sprint 02 — Infra local e online mínima

**Objetivo da sprint:** preparar containers, variáveis e health checks dos ambientes.


### S02-H01 — Como dev, quero subir infraestrutura local com Docker Compose.

**Resumo do que deve ser feito:**
- Back-end: Criar `infra/local/docker-compose.yml`.
- Front-end: Configurar front local para acessar Nginx/API local.
- Testes back-end: Validar propriedades de conexão por profile; falha rápida sem `DB_HOST`.
- Testes front-end: Environment local possui URL válida.
- Documentos-base: DL, ARQ

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: infra/local, infra/online, Docker Compose, Nginx e configurações de profile
- Front-end: `front-end/` — app/feature alvo: front-ends servidos por Nginx/containers e environments Angular

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S02-H01: Como dev, quero subir infraestrutura local com Docker Compose.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Infraestrutura local/online.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DL, ARQ.
- Regras importantes desta história: não expor PostgreSQL, RabbitMQ ou Redis publicamente; usar variáveis de ambiente para segredos; health checks devem indicar estado real das dependências; ambiente local deve continuar operável sem internet.

O que gerar:
1. Back-end: Criar `infra/local/docker-compose.yml`.
2. Front-end: Configurar front local para acessar Nginx/API local.
3. Testes unitários back-end: Validar propriedades de conexão por profile; falha rápida sem `DB_HOST`.
4. Testes unitários front-end: Environment local possui URL válida.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S02-H02 — Como dev, quero subir infraestrutura online base.

**Resumo do que deve ser feito:**
- Back-end: Criar `infra/online/docker-compose.yml`.
- Front-end: Configurar URLs online para site/admin.
- Testes back-end: Profile online exige `JWT_SECRET` e `SYNC_MASTER_SECRET`.
- Testes front-end: Environment produção não usa localhost.
- Documentos-base: DO, ARQ

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: infra/local, infra/online, Docker Compose, Nginx e configurações de profile
- Front-end: `front-end/` — app/feature alvo: front-ends servidos por Nginx/containers e environments Angular

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S02-H02: Como dev, quero subir infraestrutura online base.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Infraestrutura local/online.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DO, ARQ.
- Regras importantes desta história: não expor PostgreSQL, RabbitMQ ou Redis publicamente; usar variáveis de ambiente para segredos; health checks devem indicar estado real das dependências; ambiente local deve continuar operável sem internet.

O que gerar:
1. Back-end: Criar `infra/online/docker-compose.yml`.
2. Front-end: Configurar URLs online para site/admin.
3. Testes unitários back-end: Profile online exige `JWT_SECRET` e `SYNC_MASTER_SECRET`.
4. Testes unitários front-end: Environment produção não usa localhost.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S02-H03 — Como operador técnico, quero health check técnico.

**Resumo do que deve ser feito:**
- Back-end: Configurar Actuator health para DB, Redis e RabbitMQ.
- Front-end: Criar tela simples de status técnico no admin.
- Testes back-end: Health retorna `UP` com mocks; retorna `DOWN` quando dependência falha.
- Testes front-end: Componente mostra `Online`, `Instável`, `Offline`.
- Documentos-base: DL, DO, SYNC

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: infra/local, infra/online, Docker Compose, Nginx e configurações de profile
- Front-end: `front-end/` — app/feature alvo: front-ends servidos por Nginx/containers e environments Angular

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S02-H03: Como operador técnico, quero health check técnico.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Infraestrutura local/online.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DL, DO, SYNC.
- Regras importantes desta história: não expor PostgreSQL, RabbitMQ ou Redis publicamente; usar variáveis de ambiente para segredos; health checks devem indicar estado real das dependências; ambiente local deve continuar operável sem internet.

O que gerar:
1. Back-end: Configurar Actuator health para DB, Redis e RabbitMQ.
2. Front-end: Criar tela simples de status técnico no admin.
3. Testes unitários back-end: Health retorna `UP` com mocks; retorna `DOWN` quando dependência falha.
4. Testes unitários front-end: Componente mostra `Online`, `Instável`, `Offline`.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S02-H04 — Como dev, quero Nginx local inicial.

**Resumo do que deve ser feito:**
- Back-end: Configurar proxy para API local.
- Front-end: Configurar build estático dos apps locais.
- Testes back-end: Validação de propriedades de CORS/proxy.
- Testes front-end: Testar carregamento de base href.
- Documentos-base: DL

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: infra/local, infra/online, Docker Compose, Nginx e configurações de profile
- Front-end: `front-end/` — app/feature alvo: front-ends servidos por Nginx/containers e environments Angular

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S02-H04: Como dev, quero Nginx local inicial.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Infraestrutura local/online.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DL.
- Regras importantes desta história: não expor PostgreSQL, RabbitMQ ou Redis publicamente; usar variáveis de ambiente para segredos; health checks devem indicar estado real das dependências; ambiente local deve continuar operável sem internet.

O que gerar:
1. Back-end: Configurar proxy para API local.
2. Front-end: Configurar build estático dos apps locais.
3. Testes unitários back-end: Validação de propriedades de CORS/proxy.
4. Testes unitários front-end: Testar carregamento de base href.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S02-H05 — Como admin, quero scripts básicos de backup.

**Resumo do que deve ser feito:**
- Back-end: Criar scripts `backup-postgres.sh` para local e online.
- Front-end: Exibir status do último backup futuramente.
- Testes back-end: Service de metadados de backup calcula status válido/atrasado.
- Testes front-end: Pipe/formatação de data do último backup.
- Documentos-base: DL, DO

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: infra/local, infra/online, Docker Compose, Nginx e configurações de profile
- Front-end: `front-end/` — app/feature alvo: front-ends servidos por Nginx/containers e environments Angular

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S02-H05: Como admin, quero scripts básicos de backup.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Infraestrutura local/online.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DL, DO.
- Regras importantes desta história: não expor PostgreSQL, RabbitMQ ou Redis publicamente; usar variáveis de ambiente para segredos; health checks devem indicar estado real das dependências; ambiente local deve continuar operável sem internet.

O que gerar:
1. Back-end: Criar scripts `backup-postgres.sh` para local e online.
2. Front-end: Exibir status do último backup futuramente.
3. Testes unitários back-end: Service de metadados de backup calcula status válido/atrasado.
4. Testes unitários front-end: Pipe/formatação de data do último backup.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.


---

## Sprint 03 — Segurança e usuários

**Objetivo da sprint:** permitir login e controle inicial de acesso por perfil.


### S03-H01 — Como usuário interno, quero fazer login.

**Resumo do que deve ser feito:**
- Back-end: Criar endpoint `POST /api/auth/login`.
- Front-end: Criar tela de login.
- Testes back-end: Login válido retorna token; senha inválida retorna 401; usuário inativo é bloqueado.
- Testes front-end: Form inválido bloqueia submit; sucesso armazena token; erro exibe mensagem.
- Documentos-base: DOM, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: security, users, roles, auth e audit; `AuthController`, `AuthService`, `UserService`, `RoleService`, `SecurityConfig` quando aplicável; `User`, `Role`, `UserRepository`, `RoleRepository`, DTOs e mappers
- Front-end: `front-end/` — app/feature alvo: admin/auth, guards, interceptors e tela de login; `auth.service.ts`, `auth.interceptor.ts`, `auth.guard.ts`, tela de login e componentes de usuário

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S03-H01: Como usuário interno, quero fazer login.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Segurança e usuários.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, FLUXOS.
- Regras importantes desta história: senha nunca pode ser persistida em texto puro; usuário inativo não autentica; ações críticas devem validar permissão no back-end; front-end pode ocultar ação, mas nunca substituir validação server-side.

O que gerar:
1. Back-end: Criar endpoint `POST /api/auth/login`.
2. Front-end: Criar tela de login.
3. Testes unitários back-end: Login válido retorna token; senha inválida retorna 401; usuário inativo é bloqueado.
4. Testes unitários front-end: Form inválido bloqueia submit; sucesso armazena token; erro exibe mensagem.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S03-H02 — Como sistema, quero carregar usuário autenticado.

**Resumo do que deve ser feito:**
- Back-end: Criar `GET /api/auth/me`.
- Front-end: Criar AuthService e estado do usuário.
- Testes back-end: Token válido retorna usuário/perfis; token expirado retorna 401.
- Testes front-end: Guard redireciona não autenticado para login.
- Documentos-base: FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: security, users, roles, auth e audit; `AuthController`, `AuthService`, `UserService`, `RoleService`, `SecurityConfig` quando aplicável; `User`, `Role`, `UserRepository`, `RoleRepository`, DTOs e mappers
- Front-end: `front-end/` — app/feature alvo: admin/auth, guards, interceptors e tela de login; `auth.service.ts`, `auth.interceptor.ts`, `auth.guard.ts`, tela de login e componentes de usuário

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S03-H02: Como sistema, quero carregar usuário autenticado.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Segurança e usuários.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: FLUXOS.
- Regras importantes desta história: senha nunca pode ser persistida em texto puro; usuário inativo não autentica; ações críticas devem validar permissão no back-end; front-end pode ocultar ação, mas nunca substituir validação server-side.

O que gerar:
1. Back-end: Criar `GET /api/auth/me`.
2. Front-end: Criar AuthService e estado do usuário.
3. Testes unitários back-end: Token válido retorna usuário/perfis; token expirado retorna 401.
4. Testes unitários front-end: Guard redireciona não autenticado para login.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S03-H03 — Como admin, quero cadastrar usuário interno.

**Resumo do que deve ser feito:**
- Back-end: Criar CRUD básico de usuários.
- Front-end: Criar tela de listagem/criação de usuário.
- Testes back-end: Username duplicado falha; senha é hasheada; perfil obrigatório.
- Testes front-end: Form exige campos obrigatórios; lista renderiza usuários.
- Documentos-base: DOM, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: security, users, roles, auth e audit; `AuthController`, `AuthService`, `UserService`, `RoleService`, `SecurityConfig` quando aplicável; `User`, `Role`, `UserRepository`, `RoleRepository`, DTOs e mappers
- Front-end: `front-end/` — app/feature alvo: admin/auth, guards, interceptors e tela de login; `auth.service.ts`, `auth.interceptor.ts`, `auth.guard.ts`, tela de login e componentes de usuário

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S03-H03: Como admin, quero cadastrar usuário interno.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Segurança e usuários.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, FLUXOS.
- Regras importantes desta história: senha nunca pode ser persistida em texto puro; usuário inativo não autentica; ações críticas devem validar permissão no back-end; front-end pode ocultar ação, mas nunca substituir validação server-side.

O que gerar:
1. Back-end: Criar CRUD básico de usuários.
2. Front-end: Criar tela de listagem/criação de usuário.
3. Testes unitários back-end: Username duplicado falha; senha é hasheada; perfil obrigatório.
4. Testes unitários front-end: Form exige campos obrigatórios; lista renderiza usuários.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S03-H04 — Como admin, quero atribuir perfis.

**Resumo do que deve ser feito:**
- Back-end: Criar vínculo `user_roles`.
- Front-end: Criar seleção de perfis no formulário.
- Testes back-end: Usuário sem perfil operacional é recusado; perfil inexistente falha.
- Testes front-end: Seleção múltipla mantém estado correto.
- Documentos-base: DOM, BD

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: security, users, roles, auth e audit; `AuthController`, `AuthService`, `UserService`, `RoleService`, `SecurityConfig` quando aplicável; `User`, `Role`, `UserRepository`, `RoleRepository`, DTOs e mappers
- Front-end: `front-end/` — app/feature alvo: admin/auth, guards, interceptors e tela de login; `auth.service.ts`, `auth.interceptor.ts`, `auth.guard.ts`, tela de login e componentes de usuário

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S03-H04: Como admin, quero atribuir perfis.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Segurança e usuários.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, BD.
- Regras importantes desta história: senha nunca pode ser persistida em texto puro; usuário inativo não autentica; ações críticas devem validar permissão no back-end; front-end pode ocultar ação, mas nunca substituir validação server-side.

O que gerar:
1. Back-end: Criar vínculo `user_roles`.
2. Front-end: Criar seleção de perfis no formulário.
3. Testes unitários back-end: Usuário sem perfil operacional é recusado; perfil inexistente falha.
4. Testes unitários front-end: Seleção múltipla mantém estado correto.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S03-H05 — Como gerente, quero permissões para ações críticas.

**Resumo do que deve ser feito:**
- Back-end: Criar anotação/validador de permissão.
- Front-end: Criar diretiva/guard por perfil.
- Testes back-end: Usuário sem permissão recebe 403; admin acessa tudo.
- Testes front-end: Botão crítico oculta/desabilita sem perfil.
- Documentos-base: ARQ, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: security, users, roles, auth e audit; `AuthController`, `AuthService`, `UserService`, `RoleService`, `SecurityConfig` quando aplicável; `User`, `Role`, `UserRepository`, `RoleRepository`, DTOs e mappers
- Front-end: `front-end/` — app/feature alvo: admin/auth, guards, interceptors e tela de login; `auth.service.ts`, `auth.interceptor.ts`, `auth.guard.ts`, tela de login e componentes de usuário

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S03-H05: Como gerente, quero permissões para ações críticas.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Segurança e usuários.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: ARQ, FLUXOS.
- Regras importantes desta história: senha nunca pode ser persistida em texto puro; usuário inativo não autentica; ações críticas devem validar permissão no back-end; front-end pode ocultar ação, mas nunca substituir validação server-side.

O que gerar:
1. Back-end: Criar anotação/validador de permissão.
2. Front-end: Criar diretiva/guard por perfil.
3. Testes unitários back-end: Usuário sem permissão recebe 403; admin acessa tudo.
4. Testes unitários front-end: Botão crítico oculta/desabilita sem perfil.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.


---

## Sprint 04 — Banco e domínio base

**Objetivo da sprint:** criar o schema inicial e as entidades centrais.


### S04-H01 — Como dev, quero migrations Flyway iniciais.

**Resumo do que deve ser feito:**
- Back-end: Criar migrations `roles`, `users`, `categories`, `products`, `orders`, `payments`, `cash`, `kds`, `sync`, `audit`.
- Front-end: Nenhuma tela nova; apenas preparar mocks de contratos.
- Testes back-end: Migration executa em banco vazio; migration duplicada falha; tabelas esperadas existem.
- Testes front-end: Testes de contratos mockados validam modelos TypeScript.
- Documentos-base: BD

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: core-domain, migrations Flyway, repositories e audit; `resources/db/migration/Vxxx__*.sql`; entidades JPA, enums, repositories e migrations Flyway
- Front-end: `front-end/` — app/feature alvo: models TypeScript, services base e mocks de contrato; modelos TypeScript em `shared/models` e services HTTP base

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S04-H01: Como dev, quero migrations Flyway iniciais.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Banco e domínio base.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: BD.
- Regras importantes desta história: usar UUID nas tabelas principais; usar BigDecimal/NUMERIC para valores monetários; usar created_at e updated_at nas entidades principais; evitar exclusão física de registros críticos.

O que gerar:
1. Back-end: Criar migrations `roles`, `users`, `categories`, `products`, `orders`, `payments`, `cash`, `kds`, `sync`, `audit`.
2. Front-end: Nenhuma tela nova; apenas preparar mocks de contratos.
3. Testes unitários back-end: Migration executa em banco vazio; migration duplicada falha; tabelas esperadas existem.
4. Testes unitários front-end: Testes de contratos mockados validam modelos TypeScript.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S04-H02 — Como dev, quero entidades JPA base.

**Resumo do que deve ser feito:**
- Back-end: Criar entidades com UUID, `createdAt`, `updatedAt`, enums e BigDecimal.
- Front-end: Criar interfaces TypeScript correspondentes.
- Testes back-end: Entidade preenche timestamps; BigDecimal não aceita valor negativo onde proibido.
- Testes front-end: Modelos TypeScript compilam com campos obrigatórios.
- Documentos-base: DOM, BD

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: core-domain, migrations Flyway, repositories e audit; `resources/db/migration/Vxxx__*.sql`; entidades JPA, enums, repositories e migrations Flyway
- Front-end: `front-end/` — app/feature alvo: models TypeScript, services base e mocks de contrato; modelos TypeScript em `shared/models` e services HTTP base

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S04-H02: Como dev, quero entidades JPA base.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Banco e domínio base.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, BD.
- Regras importantes desta história: usar UUID nas tabelas principais; usar BigDecimal/NUMERIC para valores monetários; usar created_at e updated_at nas entidades principais; evitar exclusão física de registros críticos.

O que gerar:
1. Back-end: Criar entidades com UUID, `createdAt`, `updatedAt`, enums e BigDecimal.
2. Front-end: Criar interfaces TypeScript correspondentes.
3. Testes unitários back-end: Entidade preenche timestamps; BigDecimal não aceita valor negativo onde proibido.
4. Testes unitários front-end: Modelos TypeScript compilam com campos obrigatórios.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S04-H03 — Como dev, quero repositories base.

**Resumo do que deve ser feito:**
- Back-end: Criar repositories principais.
- Front-end: Criar services Angular com métodos vazios/mocks.
- Testes back-end: Repository persiste e busca entidade com Testcontainers.
- Testes front-end: Service usa URL correta e método HTTP esperado.
- Documentos-base: BD

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: core-domain, migrations Flyway, repositories e audit; `resources/db/migration/Vxxx__*.sql`; entidades JPA, enums, repositories e migrations Flyway
- Front-end: `front-end/` — app/feature alvo: models TypeScript, services base e mocks de contrato; modelos TypeScript em `shared/models` e services HTTP base

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S04-H03: Como dev, quero repositories base.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Banco e domínio base.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: BD.
- Regras importantes desta história: usar UUID nas tabelas principais; usar BigDecimal/NUMERIC para valores monetários; usar created_at e updated_at nas entidades principais; evitar exclusão física de registros críticos.

O que gerar:
1. Back-end: Criar repositories principais.
2. Front-end: Criar services Angular com métodos vazios/mocks.
3. Testes unitários back-end: Repository persiste e busca entidade com Testcontainers.
4. Testes unitários front-end: Service usa URL correta e método HTTP esperado.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S04-H04 — Como sistema, quero seed de roles.

**Resumo do que deve ser feito:**
- Back-end: Criar seed de perfis iniciais via Flyway/data initializer.
- Front-end: Exibir perfis disponíveis no cadastro de usuário.
- Testes back-end: Roles são criados uma única vez; execução repetida não duplica.
- Testes front-end: Select de roles carrega opções mockadas.
- Documentos-base: DOM, BD

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: core-domain, migrations Flyway, repositories e audit; `resources/db/migration/Vxxx__*.sql`; entidades JPA, enums, repositories e migrations Flyway
- Front-end: `front-end/` — app/feature alvo: models TypeScript, services base e mocks de contrato; modelos TypeScript em `shared/models` e services HTTP base

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S04-H04: Como sistema, quero seed de roles.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Banco e domínio base.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, BD.
- Regras importantes desta história: usar UUID nas tabelas principais; usar BigDecimal/NUMERIC para valores monetários; usar created_at e updated_at nas entidades principais; evitar exclusão física de registros críticos.

O que gerar:
1. Back-end: Criar seed de perfis iniciais via Flyway/data initializer.
2. Front-end: Exibir perfis disponíveis no cadastro de usuário.
3. Testes unitários back-end: Roles são criados uma única vez; execução repetida não duplica.
4. Testes unitários front-end: Select de roles carrega opções mockadas.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S04-H05 — Como sistema, quero auditoria técnica base.

**Resumo do que deve ser feito:**
- Back-end: Criar `AuditLog` e serviço `AuditService`.
- Front-end: Preparar componente futuro de auditoria.
- Testes back-end: Audit log grava ação, usuário, entidade e timestamp; não aceita ação vazia.
- Testes front-end: Componente de tabela renderiza logs mockados.
- Documentos-base: DOM, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: core-domain, migrations Flyway, repositories e audit; `resources/db/migration/Vxxx__*.sql`; entidades JPA, enums, repositories e migrations Flyway
- Front-end: `front-end/` — app/feature alvo: models TypeScript, services base e mocks de contrato; modelos TypeScript em `shared/models` e services HTTP base

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S04-H05: Como sistema, quero auditoria técnica base.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Banco e domínio base.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, FLUXOS.
- Regras importantes desta história: usar UUID nas tabelas principais; usar BigDecimal/NUMERIC para valores monetários; usar created_at e updated_at nas entidades principais; evitar exclusão física de registros críticos.

O que gerar:
1. Back-end: Criar `AuditLog` e serviço `AuditService`.
2. Front-end: Preparar componente futuro de auditoria.
3. Testes unitários back-end: Audit log grava ação, usuário, entidade e timestamp; não aceita ação vazia.
4. Testes unitários front-end: Componente de tabela renderiza logs mockados.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.


---

## Sprint 05 — Catálogo: categorias e produtos

**Objetivo da sprint:** cadastrar e consultar categorias/produtos usados por PDV, site, WhatsApp e admin.


### S05-H01 — Como gerente, quero cadastrar categoria.

**Resumo do que deve ser feito:**
- Back-end: `POST /api/categories`.
- Front-end: Tela de criação de categoria.
- Testes back-end: Nome obrigatório; ordem default; categoria ativa por padrão.
- Testes front-end: Form valida nome; submit chama service; erro aparece.
- Documentos-base: DOM, BD, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: catalog/category/product; controllers e use cases de `Category`, `Product`, `ProductAvailability`; DTOs request/response e mappers MapStruct
- Front-end: `front-end/` — app/feature alvo: admin/catalog e integrações iniciais do PDV; páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S05-H01: Como gerente, quero cadastrar categoria.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Catálogo.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, BD, FLUXOS.
- Regras importantes desta história: categoria inativa não aparece para venda; produto inativo não aparece em nenhum canal; visibilidade deve respeitar PDV, site e WhatsApp separadamente; alteração de preço não pode alterar pedido antigo.

O que gerar:
1. Back-end: `POST /api/categories`.
2. Front-end: Tela de criação de categoria.
3. Testes unitários back-end: Nome obrigatório; ordem default; categoria ativa por padrão.
4. Testes unitários front-end: Form valida nome; submit chama service; erro aparece.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S05-H02 — Como gerente, quero editar categoria.

**Resumo do que deve ser feito:**
- Back-end: `PATCH /api/categories/{id}`.
- Front-end: Tela/lista com ação editar.
- Testes back-end: Não edita categoria inexistente; atualiza `updatedAt`; preserva ID.
- Testes front-end: Modal/form edição popula valores.
- Documentos-base: DOM, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: catalog/category/product; controllers e use cases de `Category`, `Product`, `ProductAvailability`; DTOs request/response e mappers MapStruct
- Front-end: `front-end/` — app/feature alvo: admin/catalog e integrações iniciais do PDV; páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S05-H02: Como gerente, quero editar categoria.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Catálogo.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, FLUXOS.
- Regras importantes desta história: categoria inativa não aparece para venda; produto inativo não aparece em nenhum canal; visibilidade deve respeitar PDV, site e WhatsApp separadamente; alteração de preço não pode alterar pedido antigo.

O que gerar:
1. Back-end: `PATCH /api/categories/{id}`.
2. Front-end: Tela/lista com ação editar.
3. Testes unitários back-end: Não edita categoria inexistente; atualiza `updatedAt`; preserva ID.
4. Testes unitários front-end: Modal/form edição popula valores.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S05-H03 — Como gerente, quero definir visibilidade da categoria por canal.

**Resumo do que deve ser feito:**
- Back-end: Campos `showOnline`, `showOnPdv`, `showOnWhatsapp`.
- Front-end: Checkboxes de canal.
- Testes back-end: Categoria oculta no PDV não aparece em consulta PDV.
- Testes front-end: Checkboxes refletem estado e atualizam payload.
- Documentos-base: DOM, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: catalog/category/product; controllers e use cases de `Category`, `Product`, `ProductAvailability`; DTOs request/response e mappers MapStruct
- Front-end: `front-end/` — app/feature alvo: admin/catalog e integrações iniciais do PDV; páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S05-H03: Como gerente, quero definir visibilidade da categoria por canal.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Catálogo.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, FLUXOS.
- Regras importantes desta história: categoria inativa não aparece para venda; produto inativo não aparece em nenhum canal; visibilidade deve respeitar PDV, site e WhatsApp separadamente; alteração de preço não pode alterar pedido antigo.

O que gerar:
1. Back-end: Campos `showOnline`, `showOnPdv`, `showOnWhatsapp`.
2. Front-end: Checkboxes de canal.
3. Testes unitários back-end: Categoria oculta no PDV não aparece em consulta PDV.
4. Testes unitários front-end: Checkboxes refletem estado e atualizam payload.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S05-H04 — Como gerente, quero cadastrar produto.

**Resumo do que deve ser feito:**
- Back-end: `POST /api/products`.
- Front-end: Tela de criação de produto.
- Testes back-end: Preço obrigatório; categoria deve existir; unidade obrigatória; setor obrigatório.
- Testes front-end: Form valida preço, categoria, unidade e setor.
- Documentos-base: DOM, BD, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: catalog/category/product; controllers e use cases de `Category`, `Product`, `ProductAvailability`; DTOs request/response e mappers MapStruct
- Front-end: `front-end/` — app/feature alvo: admin/catalog e integrações iniciais do PDV; páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S05-H04: Como gerente, quero cadastrar produto.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Catálogo.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, BD, FLUXOS.
- Regras importantes desta história: categoria inativa não aparece para venda; produto inativo não aparece em nenhum canal; visibilidade deve respeitar PDV, site e WhatsApp separadamente; alteração de preço não pode alterar pedido antigo.

O que gerar:
1. Back-end: `POST /api/products`.
2. Front-end: Tela de criação de produto.
3. Testes unitários back-end: Preço obrigatório; categoria deve existir; unidade obrigatória; setor obrigatório.
4. Testes unitários front-end: Form valida preço, categoria, unidade e setor.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S05-H05 — Como gerente, quero editar produto.

**Resumo do que deve ser feito:**
- Back-end: `PATCH /api/products/{id}`.
- Front-end: Tela/lista com filtros e edição.
- Testes back-end: Alterar preço gera evento `PRODUCT_PRICE_CHANGED`; produto inexistente retorna erro.
- Testes front-end: Filtro por nome/categoria funciona com dados mockados.
- Documentos-base: DOM, SYNC

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: catalog/category/product; controllers e use cases de `Category`, `Product`, `ProductAvailability`; DTOs request/response e mappers MapStruct
- Front-end: `front-end/` — app/feature alvo: admin/catalog e integrações iniciais do PDV; páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S05-H05: Como gerente, quero editar produto.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Catálogo.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, SYNC.
- Regras importantes desta história: categoria inativa não aparece para venda; produto inativo não aparece em nenhum canal; visibilidade deve respeitar PDV, site e WhatsApp separadamente; alteração de preço não pode alterar pedido antigo.

O que gerar:
1. Back-end: `PATCH /api/products/{id}`.
2. Front-end: Tela/lista com filtros e edição.
3. Testes unitários back-end: Alterar preço gera evento `PRODUCT_PRICE_CHANGED`; produto inexistente retorna erro.
4. Testes unitários front-end: Filtro por nome/categoria funciona com dados mockados.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S05-H06 — Como operador, quero buscar produto por código de barras.

**Resumo do que deve ser feito:**
- Back-end: `GET /api/products/barcode/{barcode}`.
- Front-end: Campo de leitura/código no PDV.
- Testes back-end: Produto inativo não retorna como vendável; barcode inexistente retorna 404.
- Testes front-end: Campo captura Enter e chama service.
- Documentos-base: PDV, DOM

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: catalog/category/product; controllers e use cases de `Category`, `Product`, `ProductAvailability`; DTOs request/response e mappers MapStruct
- Front-end: `front-end/` — app/feature alvo: admin/catalog e integrações iniciais do PDV; páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S05-H06: Como operador, quero buscar produto por código de barras.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Catálogo.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV, DOM.
- Regras importantes desta história: categoria inativa não aparece para venda; produto inativo não aparece em nenhum canal; visibilidade deve respeitar PDV, site e WhatsApp separadamente; alteração de preço não pode alterar pedido antigo.

O que gerar:
1. Back-end: `GET /api/products/barcode/{barcode}`.
2. Front-end: Campo de leitura/código no PDV.
3. Testes unitários back-end: Produto inativo não retorna como vendável; barcode inexistente retorna 404.
4. Testes unitários front-end: Campo captura Enter e chama service.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.


---

## Sprint 06 — Disponibilidade e cardápio interno

**Objetivo da sprint:** controlar disponibilidade real dos produtos e refletir por canal.


### S06-H01 — Como atendente, quero marcar produto indisponível.

**Resumo do que deve ser feito:**
- Back-end: `PATCH /api/products/{id}/availability`.
- Front-end: Tela de disponibilidade por produto.
- Testes back-end: Produto indisponível exige motivo conforme status; salva usuário responsável.
- Testes front-end: Toggle altera estado visual e envia motivo.
- Documentos-base: DOM, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: catalog/availability e sync event; controllers e use cases de `Category`, `Product`, `ProductAvailability`; DTOs request/response e mappers MapStruct
- Front-end: `front-end/` — app/feature alvo: admin/availability e indicadores no PDV; páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S06-H01: Como atendente, quero marcar produto indisponível.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Disponibilidade.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, FLUXOS.
- Regras importantes desta história: ambiente local manda na disponibilidade real; produto indisponível localmente deve refletir no online por sincronização; alteração crítica de disponibilidade deve gerar auditoria; produto sob encomenda pode ter regra diferente de estoque imediato.

O que gerar:
1. Back-end: `PATCH /api/products/{id}/availability`.
2. Front-end: Tela de disponibilidade por produto.
3. Testes unitários back-end: Produto indisponível exige motivo conforme status; salva usuário responsável.
4. Testes unitários front-end: Toggle altera estado visual e envia motivo.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S06-H02 — Como sistema, quero bloquear produto indisponível no online.

**Resumo do que deve ser feito:**
- Back-end: Regra de consulta pública filtra indisponíveis.
- Front-end: Preview de produto indisponível no admin.
- Testes back-end: Produto `UNAVAILABLE` não aparece em `sellOnline`.
- Testes front-end: Badge “Indisponível” aparece corretamente.
- Documentos-base: DOM, SYNC

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: catalog/availability e sync event; controllers e use cases de `Category`, `Product`, `ProductAvailability`; DTOs request/response e mappers MapStruct
- Front-end: `front-end/` — app/feature alvo: admin/availability e indicadores no PDV; páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S06-H02: Como sistema, quero bloquear produto indisponível no online.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Disponibilidade.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, SYNC.
- Regras importantes desta história: ambiente local manda na disponibilidade real; produto indisponível localmente deve refletir no online por sincronização; alteração crítica de disponibilidade deve gerar auditoria; produto sob encomenda pode ter regra diferente de estoque imediato.

O que gerar:
1. Back-end: Regra de consulta pública filtra indisponíveis.
2. Front-end: Preview de produto indisponível no admin.
3. Testes unitários back-end: Produto `UNAVAILABLE` não aparece em `sellOnline`.
4. Testes unitários front-end: Badge “Indisponível” aparece corretamente.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S06-H03 — Como operador, quero listar apenas produtos vendáveis no PDV.

**Resumo do que deve ser feito:**
- Back-end: `GET /api/pdv/products`.
- Front-end: Grid/lista de produtos no PDV.
- Testes back-end: Produto inativo é removido; produto sem `sellOnPdv` não aparece.
- Testes front-end: Lista renderiza produtos agrupados por categoria.
- Documentos-base: PDV, DOM

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: catalog/availability e sync event; controllers e use cases de `Category`, `Product`, `ProductAvailability`; DTOs request/response e mappers MapStruct
- Front-end: `front-end/` — app/feature alvo: admin/availability e indicadores no PDV; páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S06-H03: Como operador, quero listar apenas produtos vendáveis no PDV.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Disponibilidade.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV, DOM.
- Regras importantes desta história: ambiente local manda na disponibilidade real; produto indisponível localmente deve refletir no online por sincronização; alteração crítica de disponibilidade deve gerar auditoria; produto sob encomenda pode ter regra diferente de estoque imediato.

O que gerar:
1. Back-end: `GET /api/pdv/products`.
2. Front-end: Grid/lista de produtos no PDV.
3. Testes unitários back-end: Produto inativo é removido; produto sem `sellOnPdv` não aparece.
4. Testes unitários front-end: Lista renderiza produtos agrupados por categoria.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S06-H04 — Como sistema, quero criar evento de sync ao mudar disponibilidade.

**Resumo do que deve ser feito:**
- Back-end: Criar `SyncEvent PRODUCT_UNAVAILABLE/AVAILABLE`.
- Front-end: Exibir indicador “pendente de sincronização”.
- Testes back-end: Alteração cria evento PENDING; falha não desfaz alteração local.
- Testes front-end: Indicador renderiza pendente/sincronizado.
- Documentos-base: SYNC, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: catalog/availability e sync event; controllers e use cases de `Category`, `Product`, `ProductAvailability`; DTOs request/response e mappers MapStruct
- Front-end: `front-end/` — app/feature alvo: admin/availability e indicadores no PDV; páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S06-H04: Como sistema, quero criar evento de sync ao mudar disponibilidade.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Disponibilidade.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: SYNC, FLUXOS.
- Regras importantes desta história: ambiente local manda na disponibilidade real; produto indisponível localmente deve refletir no online por sincronização; alteração crítica de disponibilidade deve gerar auditoria; produto sob encomenda pode ter regra diferente de estoque imediato.

O que gerar:
1. Back-end: Criar `SyncEvent PRODUCT_UNAVAILABLE/AVAILABLE`.
2. Front-end: Exibir indicador “pendente de sincronização”.
3. Testes unitários back-end: Alteração cria evento PENDING; falha não desfaz alteração local.
4. Testes unitários front-end: Indicador renderiza pendente/sincronizado.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S06-H05 — Como gerente, quero auditar alteração de disponibilidade.

**Resumo do que deve ser feito:**
- Back-end: Criar `AuditLog` na alteração.
- Front-end: Futuro painel mostra histórico.
- Testes back-end: Audit contém usuário, oldValue, newValue e entityId.
- Testes front-end: Tabela de histórico renderiza mocks.
- Documentos-base: DOM, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: catalog/availability e sync event; controllers e use cases de `Category`, `Product`, `ProductAvailability`; DTOs request/response e mappers MapStruct
- Front-end: `front-end/` — app/feature alvo: admin/availability e indicadores no PDV; páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S06-H05: Como gerente, quero auditar alteração de disponibilidade.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Disponibilidade.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, FLUXOS.
- Regras importantes desta história: ambiente local manda na disponibilidade real; produto indisponível localmente deve refletir no online por sincronização; alteração crítica de disponibilidade deve gerar auditoria; produto sob encomenda pode ter regra diferente de estoque imediato.

O que gerar:
1. Back-end: Criar `AuditLog` na alteração.
2. Front-end: Futuro painel mostra histórico.
3. Testes unitários back-end: Audit contém usuário, oldValue, newValue e entityId.
4. Testes unitários front-end: Tabela de histórico renderiza mocks.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.


---

## Sprint 07 — Caixa

**Objetivo da sprint:** controlar abertura, movimentações e fechamento do caixa.


### S07-H01 — Como operador, quero abrir caixa.

**Resumo do que deve ser feito:**
- Back-end: `POST /api/cash-register/open`.
- Front-end: Tela/modal de abertura no PDV.
- Testes back-end: Valor inicial obrigatório; usuário sem permissão falha; caixa aberto duplicado é bloqueado.
- Testes front-end: Form exige valor inicial; sucesso libera PDV.
- Documentos-base: PDV, DOM, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: cash/cash-register/cash-movement; `CashRegisterController`, `CashRegisterService`, `CashMovementService`, repositories e DTOs
- Front-end: `front-end/` — app/feature alvo: pdv/cash e admin/cash-summary; componentes de abertura/fechamento, sangria, suprimento e resumo de caixa no app PDV

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S07-H01: Como operador, quero abrir caixa.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Caixa.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV, DOM, FLUXOS.
- Regras importantes desta história: não vender sem caixa aberto quando a configuração exigir; caixa fechado não recebe movimentação; diferença de caixa deve ser registrada; sangria e suprimento devem ter usuário e motivo quando exigido.

O que gerar:
1. Back-end: `POST /api/cash-register/open`.
2. Front-end: Tela/modal de abertura no PDV.
3. Testes unitários back-end: Valor inicial obrigatório; usuário sem permissão falha; caixa aberto duplicado é bloqueado.
4. Testes unitários front-end: Form exige valor inicial; sucesso libera PDV.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S07-H02 — Como operador, quero consultar caixa atual.

**Resumo do que deve ser feito:**
- Back-end: `GET /api/cash-register/current`.
- Front-end: Header do PDV mostra caixa aberto/fechado.
- Testes back-end: Retorna caixa aberto do operador; sem caixa retorna estado vazio.
- Testes front-end: Header renderiza status corretamente.
- Documentos-base: PDV

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: cash/cash-register/cash-movement; `CashRegisterController`, `CashRegisterService`, `CashMovementService`, repositories e DTOs
- Front-end: `front-end/` — app/feature alvo: pdv/cash e admin/cash-summary; componentes de abertura/fechamento, sangria, suprimento e resumo de caixa no app PDV

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S07-H02: Como operador, quero consultar caixa atual.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Caixa.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV.
- Regras importantes desta história: não vender sem caixa aberto quando a configuração exigir; caixa fechado não recebe movimentação; diferença de caixa deve ser registrada; sangria e suprimento devem ter usuário e motivo quando exigido.

O que gerar:
1. Back-end: `GET /api/cash-register/current`.
2. Front-end: Header do PDV mostra caixa aberto/fechado.
3. Testes unitários back-end: Retorna caixa aberto do operador; sem caixa retorna estado vazio.
4. Testes unitários front-end: Header renderiza status corretamente.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S07-H03 — Como gerente, quero registrar sangria.

**Resumo do que deve ser feito:**
- Back-end: `POST /api/cash-register/{id}/movement` com `CASH_OUT`.
- Front-end: Botão/tela de sangria.
- Testes back-end: Motivo obrigatório; valor positivo; caixa fechado bloqueia.
- Testes front-end: Form valida valor e motivo.
- Documentos-base: PDV, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: cash/cash-register/cash-movement; `CashRegisterController`, `CashRegisterService`, `CashMovementService`, repositories e DTOs
- Front-end: `front-end/` — app/feature alvo: pdv/cash e admin/cash-summary; componentes de abertura/fechamento, sangria, suprimento e resumo de caixa no app PDV

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S07-H03: Como gerente, quero registrar sangria.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Caixa.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV, FLUXOS.
- Regras importantes desta história: não vender sem caixa aberto quando a configuração exigir; caixa fechado não recebe movimentação; diferença de caixa deve ser registrada; sangria e suprimento devem ter usuário e motivo quando exigido.

O que gerar:
1. Back-end: `POST /api/cash-register/{id}/movement` com `CASH_OUT`.
2. Front-end: Botão/tela de sangria.
3. Testes unitários back-end: Motivo obrigatório; valor positivo; caixa fechado bloqueia.
4. Testes unitários front-end: Form valida valor e motivo.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S07-H04 — Como gerente, quero registrar suprimento.

**Resumo do que deve ser feito:**
- Back-end: Movimento `CASH_IN`.
- Front-end: Botão/tela de suprimento.
- Testes back-end: Valor positivo; usuário autorizado; movimento vinculado ao caixa.
- Testes front-end: Submit chama endpoint correto.
- Documentos-base: PDV, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: cash/cash-register/cash-movement; `CashRegisterController`, `CashRegisterService`, `CashMovementService`, repositories e DTOs
- Front-end: `front-end/` — app/feature alvo: pdv/cash e admin/cash-summary; componentes de abertura/fechamento, sangria, suprimento e resumo de caixa no app PDV

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S07-H04: Como gerente, quero registrar suprimento.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Caixa.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV, FLUXOS.
- Regras importantes desta história: não vender sem caixa aberto quando a configuração exigir; caixa fechado não recebe movimentação; diferença de caixa deve ser registrada; sangria e suprimento devem ter usuário e motivo quando exigido.

O que gerar:
1. Back-end: Movimento `CASH_IN`.
2. Front-end: Botão/tela de suprimento.
3. Testes unitários back-end: Valor positivo; usuário autorizado; movimento vinculado ao caixa.
4. Testes unitários front-end: Submit chama endpoint correto.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S07-H05 — Como operador, quero fechar caixa.

**Resumo do que deve ser feito:**
- Back-end: `POST /api/cash-register/{id}/close`.
- Front-end: Tela de fechamento com totais por forma.
- Testes back-end: Calcula esperado; diferença registrada; caixa fechado não recebe movimento.
- Testes front-end: Tela calcula diferença visualmente; exige justificativa se divergente.
- Documentos-base: PDV, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: cash/cash-register/cash-movement; `CashRegisterController`, `CashRegisterService`, `CashMovementService`, repositories e DTOs
- Front-end: `front-end/` — app/feature alvo: pdv/cash e admin/cash-summary; componentes de abertura/fechamento, sangria, suprimento e resumo de caixa no app PDV

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S07-H05: Como operador, quero fechar caixa.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Caixa.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV, FLUXOS.
- Regras importantes desta história: não vender sem caixa aberto quando a configuração exigir; caixa fechado não recebe movimentação; diferença de caixa deve ser registrada; sangria e suprimento devem ter usuário e motivo quando exigido.

O que gerar:
1. Back-end: `POST /api/cash-register/{id}/close`.
2. Front-end: Tela de fechamento com totais por forma.
3. Testes unitários back-end: Calcula esperado; diferença registrada; caixa fechado não recebe movimento.
4. Testes unitários front-end: Tela calcula diferença visualmente; exige justificativa se divergente.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S07-H06 — Como operador, quero ver resumo do caixa.

**Resumo do que deve ser feito:**
- Back-end: `GET /api/cash-register/{id}/summary`.
- Front-end: Tela/impressão de resumo.
- Testes back-end: Agrupa por método; inclui sangria/suprimento/diferença.
- Testes front-end: Tabela de resumo renderiza totais.
- Documentos-base: PDV

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: cash/cash-register/cash-movement; `CashRegisterController`, `CashRegisterService`, `CashMovementService`, repositories e DTOs
- Front-end: `front-end/` — app/feature alvo: pdv/cash e admin/cash-summary; componentes de abertura/fechamento, sangria, suprimento e resumo de caixa no app PDV

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S07-H06: Como operador, quero ver resumo do caixa.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Caixa.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV.
- Regras importantes desta história: não vender sem caixa aberto quando a configuração exigir; caixa fechado não recebe movimentação; diferença de caixa deve ser registrada; sangria e suprimento devem ter usuário e motivo quando exigido.

O que gerar:
1. Back-end: `GET /api/cash-register/{id}/summary`.
2. Front-end: Tela/impressão de resumo.
3. Testes unitários back-end: Agrupa por método; inclui sangria/suprimento/diferença.
4. Testes unitários front-end: Tabela de resumo renderiza totais.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.


---

## Sprint 08 — PDV: venda e carrinho

**Objetivo da sprint:** criar venda presencial e manipular itens no carrinho.


### S08-H01 — Como operador, quero iniciar venda.

**Resumo do que deve ser feito:**
- Back-end: `POST /api/pdv/sales`.
- Front-end: Botão “Nova venda”.
- Testes back-end: Sem caixa aberto falha; cria Order origem PDV; status CREATED.
- Testes front-end: Botão cria venda e inicializa carrinho.
- Documentos-base: PDV, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: pdv/sales/order/order-item; `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`; regras de totalização, desconto, finalização e cancelamento
- Front-end: `front-end/` — app/feature alvo: pdv/sale-screen, product-search e cart; tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S08-H01: Como operador, quero iniciar venda.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: PDV carrinho.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV, FLUXOS.
- Regras importantes desta história: venda não pode ser finalizada sem itens; item deve guardar snapshot de nome e preço; produto inativo ou não vendável no PDV deve ser bloqueado; venda finalizada não pode ser editada como carrinho comum.

O que gerar:
1. Back-end: `POST /api/pdv/sales`.
2. Front-end: Botão “Nova venda”.
3. Testes unitários back-end: Sem caixa aberto falha; cria Order origem PDV; status CREATED.
4. Testes unitários front-end: Botão cria venda e inicializa carrinho.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S08-H02 — Como operador, quero adicionar produto ao carrinho.

**Resumo do que deve ser feito:**
- Back-end: `POST /api/pdv/sales/{saleId}/items`.
- Front-end: Click no produto adiciona item.
- Testes back-end: Produto inativo bloqueia; salva snapshot de nome/preço; total calculado.
- Testes front-end: Carrinho adiciona item e atualiza subtotal.
- Documentos-base: DOM, PDV

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: pdv/sales/order/order-item; `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`; regras de totalização, desconto, finalização e cancelamento
- Front-end: `front-end/` — app/feature alvo: pdv/sale-screen, product-search e cart; tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S08-H02: Como operador, quero adicionar produto ao carrinho.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: PDV carrinho.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, PDV.
- Regras importantes desta história: venda não pode ser finalizada sem itens; item deve guardar snapshot de nome e preço; produto inativo ou não vendável no PDV deve ser bloqueado; venda finalizada não pode ser editada como carrinho comum.

O que gerar:
1. Back-end: `POST /api/pdv/sales/{saleId}/items`.
2. Front-end: Click no produto adiciona item.
3. Testes unitários back-end: Produto inativo bloqueia; salva snapshot de nome/preço; total calculado.
4. Testes unitários front-end: Carrinho adiciona item e atualiza subtotal.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S08-H03 — Como operador, quero alterar quantidade.

**Resumo do que deve ser feito:**
- Back-end: `PATCH /api/pdv/sales/{saleId}/items/{itemId}`.
- Front-end: Botões `+`, `-` e campo quantidade.
- Testes back-end: Quantidade zero/negativa falha; total recalcula; venda finalizada bloqueia.
- Testes front-end: Quantidade altera total visual.
- Documentos-base: PDV, DOM

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: pdv/sales/order/order-item; `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`; regras de totalização, desconto, finalização e cancelamento
- Front-end: `front-end/` — app/feature alvo: pdv/sale-screen, product-search e cart; tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S08-H03: Como operador, quero alterar quantidade.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: PDV carrinho.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV, DOM.
- Regras importantes desta história: venda não pode ser finalizada sem itens; item deve guardar snapshot de nome e preço; produto inativo ou não vendável no PDV deve ser bloqueado; venda finalizada não pode ser editada como carrinho comum.

O que gerar:
1. Back-end: `PATCH /api/pdv/sales/{saleId}/items/{itemId}`.
2. Front-end: Botões `+`, `-` e campo quantidade.
3. Testes unitários back-end: Quantidade zero/negativa falha; total recalcula; venda finalizada bloqueia.
4. Testes unitários front-end: Quantidade altera total visual.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S08-H04 — Como operador, quero remover item.

**Resumo do que deve ser feito:**
- Back-end: `DELETE /api/pdv/sales/{saleId}/items/{itemId}`.
- Front-end: Botão remover item.
- Testes back-end: Item inexistente retorna erro; total recalcula; venda finalizada bloqueia.
- Testes front-end: Item some do carrinho.
- Documentos-base: FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: pdv/sales/order/order-item; `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`; regras de totalização, desconto, finalização e cancelamento
- Front-end: `front-end/` — app/feature alvo: pdv/sale-screen, product-search e cart; tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S08-H04: Como operador, quero remover item.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: PDV carrinho.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: FLUXOS.
- Regras importantes desta história: venda não pode ser finalizada sem itens; item deve guardar snapshot de nome e preço; produto inativo ou não vendável no PDV deve ser bloqueado; venda finalizada não pode ser editada como carrinho comum.

O que gerar:
1. Back-end: `DELETE /api/pdv/sales/{saleId}/items/{itemId}`.
2. Front-end: Botão remover item.
3. Testes unitários back-end: Item inexistente retorna erro; total recalcula; venda finalizada bloqueia.
4. Testes unitários front-end: Item some do carrinho.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S08-H05 — Como operador, quero buscar produto por nome/categoria/código.

**Resumo do que deve ser feito:**
- Back-end: Endpoints de busca PDV.
- Front-end: Busca lateral, categorias e input código de barras.
- Testes back-end: Busca ignora produtos não vendáveis; barcode inexistente retorna aviso.
- Testes front-end: Busca filtra lista; scanner por Enter adiciona item.
- Documentos-base: PDV

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: pdv/sales/order/order-item; `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`; regras de totalização, desconto, finalização e cancelamento
- Front-end: `front-end/` — app/feature alvo: pdv/sale-screen, product-search e cart; tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S08-H05: Como operador, quero buscar produto por nome/categoria/código.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: PDV carrinho.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV.
- Regras importantes desta história: venda não pode ser finalizada sem itens; item deve guardar snapshot de nome e preço; produto inativo ou não vendável no PDV deve ser bloqueado; venda finalizada não pode ser editada como carrinho comum.

O que gerar:
1. Back-end: Endpoints de busca PDV.
2. Front-end: Busca lateral, categorias e input código de barras.
3. Testes unitários back-end: Busca ignora produtos não vendáveis; barcode inexistente retorna aviso.
4. Testes unitários front-end: Busca filtra lista; scanner por Enter adiciona item.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S08-H06 — Como operador, quero ver totais em tempo real.

**Resumo do que deve ser feito:**
- Back-end: Retornar subtotal, desconto e total no DTO da venda.
- Front-end: Painel direito do PDV com totais.
- Testes back-end: Total = soma itens - desconto + taxas; arredondamento monetário correto.
- Testes front-end: Componente totaliza payload recebido.
- Documentos-base: DOM, PDV

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: pdv/sales/order/order-item; `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`; regras de totalização, desconto, finalização e cancelamento
- Front-end: `front-end/` — app/feature alvo: pdv/sale-screen, product-search e cart; tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S08-H06: Como operador, quero ver totais em tempo real.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: PDV carrinho.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, PDV.
- Regras importantes desta história: venda não pode ser finalizada sem itens; item deve guardar snapshot de nome e preço; produto inativo ou não vendável no PDV deve ser bloqueado; venda finalizada não pode ser editada como carrinho comum.

O que gerar:
1. Back-end: Retornar subtotal, desconto e total no DTO da venda.
2. Front-end: Painel direito do PDV com totais.
3. Testes unitários back-end: Total = soma itens - desconto + taxas; arredondamento monetário correto.
4. Testes unitários front-end: Componente totaliza payload recebido.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.


---

## Sprint 09 — Pagamento presencial

**Objetivo da sprint:** registrar pagamento simples e misto com validação de total.


### S09-H01 — Como operador, quero registrar pagamento em dinheiro.

**Resumo do que deve ser feito:**
- Back-end: `POST /api/pdv/sales/{saleId}/payment`.
- Front-end: Tela de pagamento dinheiro e troco.
- Testes back-end: Valor menor que total falha; troco calculado; Payment `PAID`.
- Testes front-end: Troco é exibido; valor inválido bloqueia botão.
- Documentos-base: PDV, DOM

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: payment e cash-movement; `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`; regras de totalização, desconto, finalização e cancelamento
- Front-end: `front-end/` — app/feature alvo: pdv/payment-screen; tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S09-H01: Como operador, quero registrar pagamento em dinheiro.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Pagamento presencial.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV, DOM.
- Regras importantes desta história: pagamento deve bater com o total da venda; pagamento misto deve gerar um Payment por forma; cada pagamento deve gerar CashMovement; troco só se aplica a dinheiro.

O que gerar:
1. Back-end: `POST /api/pdv/sales/{saleId}/payment`.
2. Front-end: Tela de pagamento dinheiro e troco.
3. Testes unitários back-end: Valor menor que total falha; troco calculado; Payment `PAID`.
4. Testes unitários front-end: Troco é exibido; valor inválido bloqueia botão.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S09-H02 — Como operador, quero registrar Pix presencial.

**Resumo do que deve ser feito:**
- Back-end: Método `PIX`.
- Front-end: Tela Pix presencial com confirmação manual.
- Testes back-end: Valor deve bater com total; cria CashMovement.
- Testes front-end: Confirmação atualiza estado de pagamento.
- Documentos-base: PDV

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: payment e cash-movement; `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`; regras de totalização, desconto, finalização e cancelamento
- Front-end: `front-end/` — app/feature alvo: pdv/payment-screen; tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S09-H02: Como operador, quero registrar Pix presencial.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Pagamento presencial.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV.
- Regras importantes desta história: pagamento deve bater com o total da venda; pagamento misto deve gerar um Payment por forma; cada pagamento deve gerar CashMovement; troco só se aplica a dinheiro.

O que gerar:
1. Back-end: Método `PIX`.
2. Front-end: Tela Pix presencial com confirmação manual.
3. Testes unitários back-end: Valor deve bater com total; cria CashMovement.
4. Testes unitários front-end: Confirmação atualiza estado de pagamento.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S09-H03 — Como operador, quero registrar débito/crédito/voucher.

**Resumo do que deve ser feito:**
- Back-end: Métodos `DEBIT_CARD`, `CREDIT_CARD`, `MEAL_VOUCHER`.
- Front-end: Seleção de forma de pagamento.
- Testes back-end: Método inválido falha; valor positivo; status pago.
- Testes front-end: Select de método monta payload correto.
- Documentos-base: PDV, DOM

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: payment e cash-movement; `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`; regras de totalização, desconto, finalização e cancelamento
- Front-end: `front-end/` — app/feature alvo: pdv/payment-screen; tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S09-H03: Como operador, quero registrar débito/crédito/voucher.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Pagamento presencial.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV, DOM.
- Regras importantes desta história: pagamento deve bater com o total da venda; pagamento misto deve gerar um Payment por forma; cada pagamento deve gerar CashMovement; troco só se aplica a dinheiro.

O que gerar:
1. Back-end: Métodos `DEBIT_CARD`, `CREDIT_CARD`, `MEAL_VOUCHER`.
2. Front-end: Seleção de forma de pagamento.
3. Testes unitários back-end: Método inválido falha; valor positivo; status pago.
4. Testes unitários front-end: Select de método monta payload correto.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S09-H04 — Como operador, quero pagamento misto.

**Resumo do que deve ser feito:**
- Back-end: Criar múltiplos `Payment` para uma venda.
- Front-end: Tela com múltiplas formas e saldo restante.
- Testes back-end: Soma menor/maior que total falha; cada pagamento gera CashMovement.
- Testes front-end: Saldo restante recalcula; não permite finalizar com saldo aberto.
- Documentos-base: PDV, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: payment e cash-movement; `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`; regras de totalização, desconto, finalização e cancelamento
- Front-end: `front-end/` — app/feature alvo: pdv/payment-screen; tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S09-H04: Como operador, quero pagamento misto.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Pagamento presencial.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV, FLUXOS.
- Regras importantes desta história: pagamento deve bater com o total da venda; pagamento misto deve gerar um Payment por forma; cada pagamento deve gerar CashMovement; troco só se aplica a dinheiro.

O que gerar:
1. Back-end: Criar múltiplos `Payment` para uma venda.
2. Front-end: Tela com múltiplas formas e saldo restante.
3. Testes unitários back-end: Soma menor/maior que total falha; cada pagamento gera CashMovement.
4. Testes unitários front-end: Saldo restante recalcula; não permite finalizar com saldo aberto.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S09-H05 — Como sistema, quero movimentação de caixa por pagamento.

**Resumo do que deve ser feito:**
- Back-end: Criar `CashMovement SALE`.
- Front-end: Exibir pagamentos associados à venda.
- Testes back-end: Cada Payment cria uma movimentação; caixa fechado bloqueia.
- Testes front-end: Lista de pagamentos renderiza por método.
- Documentos-base: DOM, PDV

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: payment e cash-movement; `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`; regras de totalização, desconto, finalização e cancelamento
- Front-end: `front-end/` — app/feature alvo: pdv/payment-screen; tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S09-H05: Como sistema, quero movimentação de caixa por pagamento.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Pagamento presencial.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, PDV.
- Regras importantes desta história: pagamento deve bater com o total da venda; pagamento misto deve gerar um Payment por forma; cada pagamento deve gerar CashMovement; troco só se aplica a dinheiro.

O que gerar:
1. Back-end: Criar `CashMovement SALE`.
2. Front-end: Exibir pagamentos associados à venda.
3. Testes unitários back-end: Cada Payment cria uma movimentação; caixa fechado bloqueia.
4. Testes unitários front-end: Lista de pagamentos renderiza por método.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.


---

## Sprint 10 — Finalização, impressão e cancelamento

**Objetivo da sprint:** finalizar venda, imprimir comprovante, abrir gaveta e cancelar com auditoria.


### S10-H01 — Como operador, quero finalizar venda paga.

**Resumo do que deve ser feito:**
- Back-end: `POST /api/pdv/sales/{saleId}/finish`.
- Front-end: Botão finalizar venda.
- Testes back-end: Sem itens falha; sem pagamento falha; status final correto.
- Testes front-end: Botão desabilita sem itens/pagamento.
- Documentos-base: PDV, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: pdv/finish, printing, discount, cancellation e audit; `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`; regras de totalização, desconto, finalização e cancelamento
- Front-end: `front-end/` — app/feature alvo: pdv/finalization, discount-modal, cancel-modal e print actions; tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S10-H01: Como operador, quero finalizar venda paga.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Finalização, impressão, desconto e cancelamento.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV, FLUXOS.
- Regras importantes desta história: falha de impressão não pode duplicar venda; cancelamento de venda finalizada exige motivo e permissão; desconto acima do limite exige gerente; gaveta só deve abrir em pagamento dinheiro ou ação autorizada.

O que gerar:
1. Back-end: `POST /api/pdv/sales/{saleId}/finish`.
2. Front-end: Botão finalizar venda.
3. Testes unitários back-end: Sem itens falha; sem pagamento falha; status final correto.
4. Testes unitários front-end: Botão desabilita sem itens/pagamento.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S10-H02 — Como sistema, quero criar evento de sync ao finalizar venda.

**Resumo do que deve ser feito:**
- Back-end: Criar `SyncEvent SALE_FINISHED`.
- Front-end: Mostrar status “pendente de sync”.
- Testes back-end: Evento PENDING criado; falha de sync não desfaz venda.
- Testes front-end: Badge de pendência exibido.
- Documentos-base: PDV, SYNC

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: pdv/finish, printing, discount, cancellation e audit; `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`; regras de totalização, desconto, finalização e cancelamento
- Front-end: `front-end/` — app/feature alvo: pdv/finalization, discount-modal, cancel-modal e print actions; tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S10-H02: Como sistema, quero criar evento de sync ao finalizar venda.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Finalização, impressão, desconto e cancelamento.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV, SYNC.
- Regras importantes desta história: falha de impressão não pode duplicar venda; cancelamento de venda finalizada exige motivo e permissão; desconto acima do limite exige gerente; gaveta só deve abrir em pagamento dinheiro ou ação autorizada.

O que gerar:
1. Back-end: Criar `SyncEvent SALE_FINISHED`.
2. Front-end: Mostrar status “pendente de sync”.
3. Testes unitários back-end: Evento PENDING criado; falha de sync não desfaz venda.
4. Testes unitários front-end: Badge de pendência exibido.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S10-H03 — Como operador, quero imprimir comprovante.

**Resumo do que deve ser feito:**
- Back-end: Criar serviço de impressão abstrato/adapter.
- Front-end: Botão imprimir/reimprimir comprovante.
- Testes back-end: Geração de comando não duplica venda; falha de impressão retorna erro controlado.
- Testes front-end: Estado de impressão mostra sucesso/erro.
- Documentos-base: PDV, DL, HW

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: pdv/finish, printing, discount, cancellation e audit; `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`; regras de totalização, desconto, finalização e cancelamento
- Front-end: `front-end/` — app/feature alvo: pdv/finalization, discount-modal, cancel-modal e print actions; tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S10-H03: Como operador, quero imprimir comprovante.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Finalização, impressão, desconto e cancelamento.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV, DL, HW.
- Regras importantes desta história: falha de impressão não pode duplicar venda; cancelamento de venda finalizada exige motivo e permissão; desconto acima do limite exige gerente; gaveta só deve abrir em pagamento dinheiro ou ação autorizada.

O que gerar:
1. Back-end: Criar serviço de impressão abstrato/adapter.
2. Front-end: Botão imprimir/reimprimir comprovante.
3. Testes unitários back-end: Geração de comando não duplica venda; falha de impressão retorna erro controlado.
4. Testes unitários front-end: Estado de impressão mostra sucesso/erro.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S10-H04 — Como operador, quero abrir gaveta em pagamento dinheiro.

**Resumo do que deve ser feito:**
- Back-end: Adapter envia comando de gaveta.
- Front-end: Indicação visual de gaveta acionada.
- Testes back-end: Só aciona gaveta para dinheiro; não aciona para Pix/cartão.
- Testes front-end: Componente mostra ação apenas quando aplicável.
- Documentos-base: PDV, HW

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: pdv/finish, printing, discount, cancellation e audit; `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`; regras de totalização, desconto, finalização e cancelamento
- Front-end: `front-end/` — app/feature alvo: pdv/finalization, discount-modal, cancel-modal e print actions; tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S10-H04: Como operador, quero abrir gaveta em pagamento dinheiro.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Finalização, impressão, desconto e cancelamento.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV, HW.
- Regras importantes desta história: falha de impressão não pode duplicar venda; cancelamento de venda finalizada exige motivo e permissão; desconto acima do limite exige gerente; gaveta só deve abrir em pagamento dinheiro ou ação autorizada.

O que gerar:
1. Back-end: Adapter envia comando de gaveta.
2. Front-end: Indicação visual de gaveta acionada.
3. Testes unitários back-end: Só aciona gaveta para dinheiro; não aciona para Pix/cartão.
4. Testes unitários front-end: Componente mostra ação apenas quando aplicável.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S10-H05 — Como operador, quero aplicar desconto.

**Resumo do que deve ser feito:**
- Back-end: `POST /api/pdv/sales/{saleId}/discount`.
- Front-end: Modal de desconto por valor/percentual.
- Testes back-end: Limite permitido aplica; acima exige gerente; desconto negativo falha.
- Testes front-end: Form alterna valor/percentual; calcula preview.
- Documentos-base: PDV, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: pdv/finish, printing, discount, cancellation e audit; `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`; regras de totalização, desconto, finalização e cancelamento
- Front-end: `front-end/` — app/feature alvo: pdv/finalization, discount-modal, cancel-modal e print actions; tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S10-H05: Como operador, quero aplicar desconto.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Finalização, impressão, desconto e cancelamento.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV, FLUXOS.
- Regras importantes desta história: falha de impressão não pode duplicar venda; cancelamento de venda finalizada exige motivo e permissão; desconto acima do limite exige gerente; gaveta só deve abrir em pagamento dinheiro ou ação autorizada.

O que gerar:
1. Back-end: `POST /api/pdv/sales/{saleId}/discount`.
2. Front-end: Modal de desconto por valor/percentual.
3. Testes unitários back-end: Limite permitido aplica; acima exige gerente; desconto negativo falha.
4. Testes unitários front-end: Form alterna valor/percentual; calcula preview.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S10-H06 — Como gerente, quero cancelar venda finalizada.

**Resumo do que deve ser feito:**
- Back-end: `POST /api/pdv/sales/{saleId}/cancel`.
- Front-end: Modal motivo + autorização.
- Testes back-end: Motivo obrigatório; sem permissão falha; gera AuditLog e ajuste financeiro.
- Testes front-end: Form exige motivo; usuário sem perfil não vê ação.
- Documentos-base: PDV, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: pdv/finish, printing, discount, cancellation e audit; `PdvController`, `SaleService`, `OrderService`, `OrderItemService`, `PaymentService`, `CashMovementService`; regras de totalização, desconto, finalização e cancelamento
- Front-end: `front-end/` — app/feature alvo: pdv/finalization, discount-modal, cancel-modal e print actions; tela principal do PDV, carrinho, busca de produto, pagamento, desconto, cancelamento e impressão

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S10-H06: Como gerente, quero cancelar venda finalizada.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Finalização, impressão, desconto e cancelamento.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV, FLUXOS.
- Regras importantes desta história: falha de impressão não pode duplicar venda; cancelamento de venda finalizada exige motivo e permissão; desconto acima do limite exige gerente; gaveta só deve abrir em pagamento dinheiro ou ação autorizada.

O que gerar:
1. Back-end: `POST /api/pdv/sales/{saleId}/cancel`.
2. Front-end: Modal motivo + autorização.
3. Testes unitários back-end: Motivo obrigatório; sem permissão falha; gera AuditLog e ajuste financeiro.
4. Testes unitários front-end: Form exige motivo; usuário sem perfil não vê ação.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.


---

## Sprint 11 — KDS: tickets e tempo real

**Objetivo da sprint:** gerar tickets por setor e exibir no KDS em tempo real.


### S11-H01 — Como sistema, quero criar tickets KDS ao finalizar venda com preparo.

**Resumo do que deve ser feito:**
- Back-end: Serviço agrupa itens por `preparationSector`.
- Front-end: KDS mostra tickets recebidos.
- Testes back-end: Itens `SEM_PREPARO` não geram ticket; setores diferentes geram tickets diferentes.
- Testes front-end: Lista renderiza tickets por setor.
- Documentos-base: KDS, PDV, DOM

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: kds/tickets, websocket e integração com order; `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs
- Front-end: `front-end/` — app/feature alvo: kds/board, sector-filter e websocket-client; app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S11-H01: Como sistema, quero criar tickets KDS ao finalizar venda com preparo.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: KDS tickets e tempo real.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: KDS, PDV, DOM.
- Regras importantes desta história: KDS deve funcionar na rede local sem internet; itens SEM_PREPARO não geram ticket; itens de setores diferentes geram tickets diferentes; WebSocket notifica sem quebrar a transação principal.

O que gerar:
1. Back-end: Serviço agrupa itens por `preparationSector`.
2. Front-end: KDS mostra tickets recebidos.
3. Testes unitários back-end: Itens `SEM_PREPARO` não geram ticket; setores diferentes geram tickets diferentes.
4. Testes unitários front-end: Lista renderiza tickets por setor.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S11-H02 — Como cozinha, quero filtrar tickets por setor.

**Resumo do que deve ser feito:**
- Back-end: `GET /api/kds/tickets?sector=CHAPA`.
- Front-end: Filtro/setor atual no KDS.
- Testes back-end: Filtro retorna apenas setor; setor inválido falha.
- Testes front-end: Seleção de setor atualiza lista.
- Documentos-base: KDS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: kds/tickets, websocket e integração com order; `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs
- Front-end: `front-end/` — app/feature alvo: kds/board, sector-filter e websocket-client; app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S11-H02: Como cozinha, quero filtrar tickets por setor.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: KDS tickets e tempo real.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: KDS.
- Regras importantes desta história: KDS deve funcionar na rede local sem internet; itens SEM_PREPARO não geram ticket; itens de setores diferentes geram tickets diferentes; WebSocket notifica sem quebrar a transação principal.

O que gerar:
1. Back-end: `GET /api/kds/tickets?sector=CHAPA`.
2. Front-end: Filtro/setor atual no KDS.
3. Testes unitários back-end: Filtro retorna apenas setor; setor inválido falha.
4. Testes unitários front-end: Seleção de setor atualiza lista.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S11-H03 — Como KDS, quero receber ticket via WebSocket.

**Resumo do que deve ser feito:**
- Back-end: Publicar evento `KDS_TICKET_CREATED`.
- Front-end: Cliente WebSocket no Angular KDS.
- Testes back-end: Evento emitido com payload esperado; erro de WebSocket não quebra transação.
- Testes front-end: Ao receber evento, card é adicionado.
- Documentos-base: KDS, ARQ

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: kds/tickets, websocket e integração com order; `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs
- Front-end: `front-end/` — app/feature alvo: kds/board, sector-filter e websocket-client; app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S11-H03: Como KDS, quero receber ticket via WebSocket.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: KDS tickets e tempo real.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: KDS, ARQ.
- Regras importantes desta história: KDS deve funcionar na rede local sem internet; itens SEM_PREPARO não geram ticket; itens de setores diferentes geram tickets diferentes; WebSocket notifica sem quebrar a transação principal.

O que gerar:
1. Back-end: Publicar evento `KDS_TICKET_CREATED`.
2. Front-end: Cliente WebSocket no Angular KDS.
3. Testes unitários back-end: Evento emitido com payload esperado; erro de WebSocket não quebra transação.
4. Testes unitários front-end: Ao receber evento, card é adicionado.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S11-H04 — Como produção, quero ver tempo desde criação.

**Resumo do que deve ser feito:**
- Back-end: DTO traz `createdAt` e tempo calculável.
- Front-end: Card mostra minutos de espera.
- Testes back-end: Tempo calculado corretamente; timezone não altera ordem.
- Testes front-end: Timer atualiza visualmente sem recarregar.
- Documentos-base: KDS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: kds/tickets, websocket e integração com order; `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs
- Front-end: `front-end/` — app/feature alvo: kds/board, sector-filter e websocket-client; app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S11-H04: Como produção, quero ver tempo desde criação.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: KDS tickets e tempo real.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: KDS.
- Regras importantes desta história: KDS deve funcionar na rede local sem internet; itens SEM_PREPARO não geram ticket; itens de setores diferentes geram tickets diferentes; WebSocket notifica sem quebrar a transação principal.

O que gerar:
1. Back-end: DTO traz `createdAt` e tempo calculável.
2. Front-end: Card mostra minutos de espera.
3. Testes unitários back-end: Tempo calculado corretamente; timezone não altera ordem.
4. Testes unitários front-end: Timer atualiza visualmente sem recarregar.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S11-H05 — Como produção, quero layout em colunas.

**Resumo do que deve ser feito:**
- Back-end: Endpoint retorna status dos tickets.
- Front-end: Colunas Aguardando, Em preparo, Pronto.
- Testes back-end: Status mapeado corretamente no DTO.
- Testes front-end: Ticket aparece na coluna certa.
- Documentos-base: KDS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: kds/tickets, websocket e integração com order; `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs
- Front-end: `front-end/` — app/feature alvo: kds/board, sector-filter e websocket-client; app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S11-H05: Como produção, quero layout em colunas.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: KDS tickets e tempo real.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: KDS.
- Regras importantes desta história: KDS deve funcionar na rede local sem internet; itens SEM_PREPARO não geram ticket; itens de setores diferentes geram tickets diferentes; WebSocket notifica sem quebrar a transação principal.

O que gerar:
1. Back-end: Endpoint retorna status dos tickets.
2. Front-end: Colunas Aguardando, Em preparo, Pronto.
3. Testes unitários back-end: Status mapeado corretamente no DTO.
4. Testes unitários front-end: Ticket aparece na coluna certa.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.


---

## Sprint 12 — KDS: preparo, pronto e expedição

**Objetivo da sprint:** atualizar status de tickets/itens e refletir pedido pronto no PDV.


### S12-H01 — Como produção, quero iniciar preparo do ticket.

**Resumo do que deve ser feito:**
- Back-end: `PATCH /api/kds/tickets/{id}/start`.
- Front-end: Botão “Iniciar preparo”.
- Testes back-end: WAITING → IN_PREPARATION permitido; READY → start bloqueado.
- Testes front-end: Card muda para coluna Em preparo.
- Documentos-base: KDS, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: kds/status transitions e order readiness; `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs
- Front-end: `front-end/` — app/feature alvo: kds/actions, pdv/ready-notifications e expedition view; app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S12-H01: Como produção, quero iniciar preparo do ticket.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: KDS preparo e expedição.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: KDS, FLUXOS.
- Regras importantes desta história: pedido só fica READY quando todos tickets obrigatórios estiverem prontos; transições inválidas de status devem ser bloqueadas; cancelamento de item no KDS exige permissão; data/hora de início, pronto e finalização devem ser preservadas.

O que gerar:
1. Back-end: `PATCH /api/kds/tickets/{id}/start`.
2. Front-end: Botão “Iniciar preparo”.
3. Testes unitários back-end: WAITING → IN_PREPARATION permitido; READY → start bloqueado.
4. Testes unitários front-end: Card muda para coluna Em preparo.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S12-H02 — Como produção, quero marcar item pronto.

**Resumo do que deve ser feito:**
- Back-end: `PATCH /api/kds/items/{id}/ready`.
- Front-end: Botão pronto por item.
- Testes back-end: Item em preparo vira READY; item cancelado não pode ficar pronto.
- Testes front-end: Item aparece concluído.
- Documentos-base: KDS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: kds/status transitions e order readiness; `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs
- Front-end: `front-end/` — app/feature alvo: kds/actions, pdv/ready-notifications e expedition view; app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S12-H02: Como produção, quero marcar item pronto.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: KDS preparo e expedição.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: KDS.
- Regras importantes desta história: pedido só fica READY quando todos tickets obrigatórios estiverem prontos; transições inválidas de status devem ser bloqueadas; cancelamento de item no KDS exige permissão; data/hora de início, pronto e finalização devem ser preservadas.

O que gerar:
1. Back-end: `PATCH /api/kds/items/{id}/ready`.
2. Front-end: Botão pronto por item.
3. Testes unitários back-end: Item em preparo vira READY; item cancelado não pode ficar pronto.
4. Testes unitários front-end: Item aparece concluído.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S12-H03 — Como produção, quero marcar ticket pronto.

**Resumo do que deve ser feito:**
- Back-end: `PATCH /api/kds/tickets/{id}/ready`.
- Front-end: Botão “Ticket pronto”.
- Testes back-end: Todos itens ficam READY; `readyAt` preenchido.
- Testes front-end: Ticket muda para coluna Pronto.
- Documentos-base: KDS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: kds/status transitions e order readiness; `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs
- Front-end: `front-end/` — app/feature alvo: kds/actions, pdv/ready-notifications e expedition view; app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S12-H03: Como produção, quero marcar ticket pronto.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: KDS preparo e expedição.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: KDS.
- Regras importantes desta história: pedido só fica READY quando todos tickets obrigatórios estiverem prontos; transições inválidas de status devem ser bloqueadas; cancelamento de item no KDS exige permissão; data/hora de início, pronto e finalização devem ser preservadas.

O que gerar:
1. Back-end: `PATCH /api/kds/tickets/{id}/ready`.
2. Front-end: Botão “Ticket pronto”.
3. Testes unitários back-end: Todos itens ficam READY; `readyAt` preenchido.
4. Testes unitários front-end: Ticket muda para coluna Pronto.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S12-H04 — Como sistema, quero pedido READY quando todos tickets estiverem prontos.

**Resumo do que deve ser feito:**
- Back-end: Serviço atualiza `OrderStatus.READY`.
- Front-end: PDV/expedição recebe atualização.
- Testes back-end: Com um ticket pendente, pedido não fica READY; todos prontos, fica READY.
- Testes front-end: Banner/alerta no PDV indica pedido pronto.
- Documentos-base: KDS, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: kds/status transitions e order readiness; `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs
- Front-end: `front-end/` — app/feature alvo: kds/actions, pdv/ready-notifications e expedition view; app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S12-H04: Como sistema, quero pedido READY quando todos tickets estiverem prontos.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: KDS preparo e expedição.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: KDS, FLUXOS.
- Regras importantes desta história: pedido só fica READY quando todos tickets obrigatórios estiverem prontos; transições inválidas de status devem ser bloqueadas; cancelamento de item no KDS exige permissão; data/hora de início, pronto e finalização devem ser preservadas.

O que gerar:
1. Back-end: Serviço atualiza `OrderStatus.READY`.
2. Front-end: PDV/expedição recebe atualização.
3. Testes unitários back-end: Com um ticket pendente, pedido não fica READY; todos prontos, fica READY.
4. Testes unitários front-end: Banner/alerta no PDV indica pedido pronto.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S12-H05 — Como expedição, quero finalizar pedido pronto.

**Resumo do que deve ser feito:**
- Back-end: Endpoint `finish` para pedido/ticket.
- Front-end: Tela/ação de expedição.
- Testes back-end: READY → FINISHED permitido; CREATED → FINISHED bloqueado.
- Testes front-end: Ação some após finalização.
- Documentos-base: KDS, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: kds/status transitions e order readiness; `KdsController`, `KdsTicketService`, `KdsWebSocketPublisher`, entidades KDS e DTOs
- Front-end: `front-end/` — app/feature alvo: kds/actions, pdv/ready-notifications e expedition view; app KDS, board por colunas, filtro por setor, cards de pedido e cliente WebSocket

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S12-H05: Como expedição, quero finalizar pedido pronto.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: KDS preparo e expedição.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: KDS, FLUXOS.
- Regras importantes desta história: pedido só fica READY quando todos tickets obrigatórios estiverem prontos; transições inválidas de status devem ser bloqueadas; cancelamento de item no KDS exige permissão; data/hora de início, pronto e finalização devem ser preservadas.

O que gerar:
1. Back-end: Endpoint `finish` para pedido/ticket.
2. Front-end: Tela/ação de expedição.
3. Testes unitários back-end: READY → FINISHED permitido; CREATED → FINISHED bloqueado.
4. Testes unitários front-end: Ação some após finalização.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.


---

## Sprint 13 — Sincronização base local → online

**Objetivo da sprint:** criar outbox local e envio seguro de eventos locais para o online.


### S13-H01 — Como sistema, quero registrar SyncEvent.

**Resumo do que deve ser feito:**
- Back-end: Serviço `SyncEventService.createPending`.
- Front-end: Mostrar status de sync na venda.
- Testes back-end: Evento tem UUID, tipo, entidade, payload, status PENDING.
- Testes front-end: Badge exibe PENDING/SYNCED/FAILED.
- Documentos-base: SYNC, BD

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: sync/outbox, worker local, endpoint online e HMAC; `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`; política de retry e endpoints de ACK/fail/status
- Front-end: `front-end/` — app/feature alvo: sync status badges e painel futuro; badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S13-H01: Como sistema, quero registrar SyncEvent.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Sincronização local → online.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: SYNC, BD.
- Regras importantes desta história: sync não pode bloquear a venda local; evento deve ser idempotente; payload deve ser assinado com HMAC; falha deve virar retry sem perder dados.

O que gerar:
1. Back-end: Serviço `SyncEventService.createPending`.
2. Front-end: Mostrar status de sync na venda.
3. Testes unitários back-end: Evento tem UUID, tipo, entidade, payload, status PENDING.
4. Testes unitários front-end: Badge exibe PENDING/SYNCED/FAILED.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S13-H02 — Como worker local, quero enviar evento para online.

**Resumo do que deve ser feito:**
- Back-end: Worker consome PENDING e chama `POST /api/sync/events`.
- Front-end: Nenhuma tela nova; status atualiza.
- Testes back-end: Envio sucesso marca SYNCED; falha marca RETRYING/FAILED.
- Testes front-end: Tela reflete mudança de status mockada.
- Documentos-base: SYNC

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: sync/outbox, worker local, endpoint online e HMAC; `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`; política de retry e endpoints de ACK/fail/status
- Front-end: `front-end/` — app/feature alvo: sync status badges e painel futuro; badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S13-H02: Como worker local, quero enviar evento para online.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Sincronização local → online.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: SYNC.
- Regras importantes desta história: sync não pode bloquear a venda local; evento deve ser idempotente; payload deve ser assinado com HMAC; falha deve virar retry sem perder dados.

O que gerar:
1. Back-end: Worker consome PENDING e chama `POST /api/sync/events`.
2. Front-end: Nenhuma tela nova; status atualiza.
3. Testes unitários back-end: Envio sucesso marca SYNCED; falha marca RETRYING/FAILED.
4. Testes unitários front-end: Tela reflete mudança de status mockada.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S13-H03 — Como API online, quero receber evento local.

**Resumo do que deve ser feito:**
- Back-end: Endpoint online valida payload e salva.
- Front-end: Admin online lista eventos recebidos futuramente.
- Testes back-end: Evento duplicado não reprocessa; assinatura inválida retorna 401.
- Testes front-end: Tabela de eventos mockados renderiza.
- Documentos-base: SYNC, ARQ

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: sync/outbox, worker local, endpoint online e HMAC; `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`; política de retry e endpoints de ACK/fail/status
- Front-end: `front-end/` — app/feature alvo: sync status badges e painel futuro; badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S13-H03: Como API online, quero receber evento local.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Sincronização local → online.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: SYNC, ARQ.
- Regras importantes desta história: sync não pode bloquear a venda local; evento deve ser idempotente; payload deve ser assinado com HMAC; falha deve virar retry sem perder dados.

O que gerar:
1. Back-end: Endpoint online valida payload e salva.
2. Front-end: Admin online lista eventos recebidos futuramente.
3. Testes unitários back-end: Evento duplicado não reprocessa; assinatura inválida retorna 401.
4. Testes unitários front-end: Tabela de eventos mockados renderiza.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S13-H04 — Como sistema, quero assinar payload com HMAC.

**Resumo do que deve ser feito:**
- Back-end: Implementar assinatura com `STORE_ID` e `STORE_SECRET`.
- Front-end: Nenhuma tela.
- Testes back-end: Assinatura válida passa; payload alterado falha; timestamp antigo pode falhar.
- Testes front-end: N/A.
- Documentos-base: SYNC

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: sync/outbox, worker local, endpoint online e HMAC; `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`; política de retry e endpoints de ACK/fail/status
- Front-end: `front-end/` — app/feature alvo: sync status badges e painel futuro; badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S13-H04: Como sistema, quero assinar payload com HMAC.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Sincronização local → online.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: SYNC.
- Regras importantes desta história: sync não pode bloquear a venda local; evento deve ser idempotente; payload deve ser assinado com HMAC; falha deve virar retry sem perder dados.

O que gerar:
1. Back-end: Implementar assinatura com `STORE_ID` e `STORE_SECRET`.
2. Front-end: Nenhuma tela.
3. Testes unitários back-end: Assinatura válida passa; payload alterado falha; timestamp antigo pode falhar.
4. Testes unitários front-end: N/A.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S13-H05 — Como sistema, quero retry inicial.

**Resumo do que deve ser feito:**
- Back-end: Implementar política 1m/5m/15m/1h.
- Front-end: Exibir retry count no painel futuro.
- Testes back-end: Retry incrementa contador; excedeu tentativas vira FAILED.
- Testes front-end: Componente mostra contador mockado.
- Documentos-base: SYNC

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: sync/outbox, worker local, endpoint online e HMAC; `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`; política de retry e endpoints de ACK/fail/status
- Front-end: `front-end/` — app/feature alvo: sync status badges e painel futuro; badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S13-H05: Como sistema, quero retry inicial.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Sincronização local → online.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: SYNC.
- Regras importantes desta história: sync não pode bloquear a venda local; evento deve ser idempotente; payload deve ser assinado com HMAC; falha deve virar retry sem perder dados.

O que gerar:
1. Back-end: Implementar política 1m/5m/15m/1h.
2. Front-end: Exibir retry count no painel futuro.
3. Testes unitários back-end: Retry incrementa contador; excedeu tentativas vira FAILED.
4. Testes unitários front-end: Componente mostra contador mockado.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.


---

## Sprint 14 — Site e cardápio público

**Objetivo da sprint:** publicar site institucional e cardápio online filtrado por disponibilidade.


### S14-H01 — Como cliente, quero acessar site institucional.

**Resumo do que deve ser feito:**
- Back-end: Endpoint/config para dados públicos básicos.
- Front-end: Criar home com informações da padaria.
- Testes back-end: Endpoint público não exige login; payload básico válido.
- Testes front-end: Home renderiza nome, seções e CTA.
- Documentos-base: README, ARQ

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: public-menu e catalog public API; controllers e use cases de `Category`, `Product`, `ProductAvailability`; DTOs request/response e mappers MapStruct
- Front-end: `front-end/` — app/feature alvo: site-publico/home, menu e product cards; páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S14-H01: Como cliente, quero acessar site institucional.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Site e cardápio público.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: README, ARQ.
- Regras importantes desta história: endpoint público de cardápio não exige login; somente produtos ativos, disponíveis e sellOnline aparecem; categoria precisa estar ativa e showOnline=true; preço promocional deve ser exibido sem alterar preço base.

O que gerar:
1. Back-end: Endpoint/config para dados públicos básicos.
2. Front-end: Criar home com informações da padaria.
3. Testes unitários back-end: Endpoint público não exige login; payload básico válido.
4. Testes unitários front-end: Home renderiza nome, seções e CTA.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S14-H02 — Como cliente, quero ver categorias online.

**Resumo do que deve ser feito:**
- Back-end: `GET /api/public/menu`.
- Front-end: Tela de cardápio por categoria.
- Testes back-end: Só categorias ativas e `showOnline=true`.
- Testes front-end: Categorias aparecem ordenadas.
- Documentos-base: DOM, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: public-menu e catalog public API; controllers e use cases de `Category`, `Product`, `ProductAvailability`; DTOs request/response e mappers MapStruct
- Front-end: `front-end/` — app/feature alvo: site-publico/home, menu e product cards; páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S14-H02: Como cliente, quero ver categorias online.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Site e cardápio público.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, FLUXOS.
- Regras importantes desta história: endpoint público de cardápio não exige login; somente produtos ativos, disponíveis e sellOnline aparecem; categoria precisa estar ativa e showOnline=true; preço promocional deve ser exibido sem alterar preço base.

O que gerar:
1. Back-end: `GET /api/public/menu`.
2. Front-end: Tela de cardápio por categoria.
3. Testes unitários back-end: Só categorias ativas e `showOnline=true`.
4. Testes unitários front-end: Categorias aparecem ordenadas.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S14-H03 — Como cliente, quero ver produtos disponíveis.

**Resumo do que deve ser feito:**
- Back-end: Filtro por `active`, `sellOnline`, disponibilidade.
- Front-end: Cards de produto com preço/imagem.
- Testes back-end: Produto indisponível não aparece; promocional usa preço correto.
- Testes front-end: Card mostra preço/promocional corretamente.
- Documentos-base: DOM, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: public-menu e catalog public API; controllers e use cases de `Category`, `Product`, `ProductAvailability`; DTOs request/response e mappers MapStruct
- Front-end: `front-end/` — app/feature alvo: site-publico/home, menu e product cards; páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S14-H03: Como cliente, quero ver produtos disponíveis.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Site e cardápio público.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, FLUXOS.
- Regras importantes desta história: endpoint público de cardápio não exige login; somente produtos ativos, disponíveis e sellOnline aparecem; categoria precisa estar ativa e showOnline=true; preço promocional deve ser exibido sem alterar preço base.

O que gerar:
1. Back-end: Filtro por `active`, `sellOnline`, disponibilidade.
2. Front-end: Cards de produto com preço/imagem.
3. Testes unitários back-end: Produto indisponível não aparece; promocional usa preço correto.
4. Testes unitários front-end: Card mostra preço/promocional corretamente.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S14-H04 — Como cliente, quero buscar produto no cardápio.

**Resumo do que deve ser feito:**
- Back-end: Endpoint com filtro ou front filtra lista.
- Front-end: Campo busca no site.
- Testes back-end: Busca ignora inativos; resultado vazio retorna lista vazia.
- Testes front-end: Busca filtra por nome.
- Documentos-base: FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: public-menu e catalog public API; controllers e use cases de `Category`, `Product`, `ProductAvailability`; DTOs request/response e mappers MapStruct
- Front-end: `front-end/` — app/feature alvo: site-publico/home, menu e product cards; páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S14-H04: Como cliente, quero buscar produto no cardápio.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Site e cardápio público.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: FLUXOS.
- Regras importantes desta história: endpoint público de cardápio não exige login; somente produtos ativos, disponíveis e sellOnline aparecem; categoria precisa estar ativa e showOnline=true; preço promocional deve ser exibido sem alterar preço base.

O que gerar:
1. Back-end: Endpoint com filtro ou front filtra lista.
2. Front-end: Campo busca no site.
3. Testes unitários back-end: Busca ignora inativos; resultado vazio retorna lista vazia.
4. Testes unitários front-end: Busca filtra por nome.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S14-H05 — Como admin, quero preview do cardápio online.

**Resumo do que deve ser feito:**
- Back-end: Reusar endpoint público/admin.
- Front-end: Admin mostra como produto aparece no site.
- Testes back-end: Preview usa mesmas regras do público.
- Testes front-end: Preview exibe status e canal.
- Documentos-base: DOM

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: public-menu e catalog public API; controllers e use cases de `Category`, `Product`, `ProductAvailability`; DTOs request/response e mappers MapStruct
- Front-end: `front-end/` — app/feature alvo: site-publico/home, menu e product cards; páginas admin de catálogo, componentes de formulário/listagem e services de catálogo

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S14-H05: Como admin, quero preview do cardápio online.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Site e cardápio público.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM.
- Regras importantes desta história: endpoint público de cardápio não exige login; somente produtos ativos, disponíveis e sellOnline aparecem; categoria precisa estar ativa e showOnline=true; preço promocional deve ser exibido sem alterar preço base.

O que gerar:
1. Back-end: Reusar endpoint público/admin.
2. Front-end: Admin mostra como produto aparece no site.
3. Testes unitários back-end: Preview usa mesmas regras do público.
4. Testes unitários front-end: Preview exibe status e canal.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.


---

## Sprint 15 — Pedido online

**Objetivo da sprint:** permitir criação de pedido online para retirada/entrega.


### S15-H01 — Como cliente, quero adicionar produto ao carrinho online.

**Resumo do que deve ser feito:**
- Back-end: Validação server-side no checkout.
- Front-end: Carrinho no site.
- Testes back-end: Produto indisponível é recusado; preço recalculado no servidor.
- Testes front-end: Carrinho adiciona/remove item e recalcula visual.
- Documentos-base: DOM, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: online/order, customer, address e checkout validation; `PublicOrderController`, `CheckoutService`, `CustomerService`, `OrderService`, `PaymentService`, `SyncEventService`
- Front-end: `front-end/` — app/feature alvo: site-publico/cart, checkout e order-status; carrinho do site, checkout, formulário de cliente/endereço e página de status

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S15-H01: Como cliente, quero adicionar produto ao carrinho online.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Pedido online.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, FLUXOS.
- Regras importantes desta história: pedido online deve ter cliente; pedido delivery exige endereço; servidor recalcula valores e não confia no carrinho do front-end; pedido deve gerar SyncEvent para a loja.

O que gerar:
1. Back-end: Validação server-side no checkout.
2. Front-end: Carrinho no site.
3. Testes unitários back-end: Produto indisponível é recusado; preço recalculado no servidor.
4. Testes unitários front-end: Carrinho adiciona/remove item e recalcula visual.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S15-H02 — Como cliente, quero informar meus dados.

**Resumo do que deve ser feito:**
- Back-end: Criar/atualizar `Customer`.
- Front-end: Form de dados do cliente.
- Testes back-end: Nome e telefone obrigatórios; e-mail inválido falha se informado.
- Testes front-end: Form valida campos obrigatórios.
- Documentos-base: DOM

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: online/order, customer, address e checkout validation; `PublicOrderController`, `CheckoutService`, `CustomerService`, `OrderService`, `PaymentService`, `SyncEventService`
- Front-end: `front-end/` — app/feature alvo: site-publico/cart, checkout e order-status; carrinho do site, checkout, formulário de cliente/endereço e página de status

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S15-H02: Como cliente, quero informar meus dados.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Pedido online.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM.
- Regras importantes desta história: pedido online deve ter cliente; pedido delivery exige endereço; servidor recalcula valores e não confia no carrinho do front-end; pedido deve gerar SyncEvent para a loja.

O que gerar:
1. Back-end: Criar/atualizar `Customer`.
2. Front-end: Form de dados do cliente.
3. Testes unitários back-end: Nome e telefone obrigatórios; e-mail inválido falha se informado.
4. Testes unitários front-end: Form valida campos obrigatórios.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S15-H03 — Como cliente, quero informar endereço para entrega.

**Resumo do que deve ser feito:**
- Back-end: Criar `CustomerAddress` quando `DELIVERY`.
- Front-end: Form de endereço condicional.
- Testes back-end: Delivery sem endereço falha; pickup não exige endereço.
- Testes front-end: Campo endereço aparece apenas em entrega.
- Documentos-base: DOM, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: online/order, customer, address e checkout validation; `PublicOrderController`, `CheckoutService`, `CustomerService`, `OrderService`, `PaymentService`, `SyncEventService`
- Front-end: `front-end/` — app/feature alvo: site-publico/cart, checkout e order-status; carrinho do site, checkout, formulário de cliente/endereço e página de status

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S15-H03: Como cliente, quero informar endereço para entrega.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Pedido online.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, FLUXOS.
- Regras importantes desta história: pedido online deve ter cliente; pedido delivery exige endereço; servidor recalcula valores e não confia no carrinho do front-end; pedido deve gerar SyncEvent para a loja.

O que gerar:
1. Back-end: Criar `CustomerAddress` quando `DELIVERY`.
2. Front-end: Form de endereço condicional.
3. Testes unitários back-end: Delivery sem endereço falha; pickup não exige endereço.
4. Testes unitários front-end: Campo endereço aparece apenas em entrega.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S15-H04 — Como cliente, quero criar pedido online.

**Resumo do que deve ser feito:**
- Back-end: `POST /api/public/orders`.
- Front-end: Checkout confirma pedido.
- Testes back-end: Pedido sem itens falha; snapshot de nome/preço salvo; status inicial correto.
- Testes front-end: Confirmação mostra número/status.
- Documentos-base: FLUXOS, DOM

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: online/order, customer, address e checkout validation; `PublicOrderController`, `CheckoutService`, `CustomerService`, `OrderService`, `PaymentService`, `SyncEventService`
- Front-end: `front-end/` — app/feature alvo: site-publico/cart, checkout e order-status; carrinho do site, checkout, formulário de cliente/endereço e página de status

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S15-H04: Como cliente, quero criar pedido online.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Pedido online.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: FLUXOS, DOM.
- Regras importantes desta história: pedido online deve ter cliente; pedido delivery exige endereço; servidor recalcula valores e não confia no carrinho do front-end; pedido deve gerar SyncEvent para a loja.

O que gerar:
1. Back-end: `POST /api/public/orders`.
2. Front-end: Checkout confirma pedido.
3. Testes unitários back-end: Pedido sem itens falha; snapshot de nome/preço salvo; status inicial correto.
4. Testes unitários front-end: Confirmação mostra número/status.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S15-H05 — Como sistema, quero criar sync event do pedido online.

**Resumo do que deve ser feito:**
- Back-end: Criar `SyncEvent ORDER_CREATED` online.
- Front-end: Mostrar status “aguardando loja”.
- Testes back-end: Evento PENDING criado; pedido fica SENT_TO_STORE/aguardando envio.
- Testes front-end: Página de status exibe “aguardando confirmação da loja”.
- Documentos-base: SYNC, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: online/order, customer, address e checkout validation; `PublicOrderController`, `CheckoutService`, `CustomerService`, `OrderService`, `PaymentService`, `SyncEventService`
- Front-end: `front-end/` — app/feature alvo: site-publico/cart, checkout e order-status; carrinho do site, checkout, formulário de cliente/endereço e página de status

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S15-H05: Como sistema, quero criar sync event do pedido online.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Pedido online.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: SYNC, FLUXOS.
- Regras importantes desta história: pedido online deve ter cliente; pedido delivery exige endereço; servidor recalcula valores e não confia no carrinho do front-end; pedido deve gerar SyncEvent para a loja.

O que gerar:
1. Back-end: Criar `SyncEvent ORDER_CREATED` online.
2. Front-end: Mostrar status “aguardando loja”.
3. Testes unitários back-end: Evento PENDING criado; pedido fica SENT_TO_STORE/aguardando envio.
4. Testes unitários front-end: Página de status exibe “aguardando confirmação da loja”.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.


---

## Sprint 16 — Pagamento online e webhook

**Objetivo da sprint:** controlar pagamento online sem confiar no front-end.


### S16-H01 — Como cliente, quero escolher pagamento online.

**Resumo do que deve ser feito:**
- Back-end: `POST /api/public/payments/online`.
- Front-end: Opção Pix/cartão online no checkout.
- Testes back-end: Cria Payment PENDING; pedido fica PAYMENT_PENDING.
- Testes front-end: Seleção de método altera etapa de pagamento.
- Documentos-base: DOM, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: payment online, integração com gateway e webhook; `OnlinePaymentController`, `PaymentGatewayService`, integração mock/inicial, `PaymentWebhookController`
- Front-end: `front-end/` — app/feature alvo: checkout/payment e order status; etapa de pagamento no checkout, instruções de pagamento e status do pedido

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S16-H01: Como cliente, quero escolher pagamento online.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Pagamento online e webhook.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, FLUXOS.
- Regras importantes desta história: pagamento online não pode ser confirmado pelo front-end; webhook deve validar assinatura/token; webhook duplicado deve ser idempotente; payload bruto deve ser registrado para auditoria técnica.

O que gerar:
1. Back-end: `POST /api/public/payments/online`.
2. Front-end: Opção Pix/cartão online no checkout.
3. Testes unitários back-end: Cria Payment PENDING; pedido fica PAYMENT_PENDING.
4. Testes unitários front-end: Seleção de método altera etapa de pagamento.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S16-H02 — Como sistema, quero integrar gateway via adapter.

**Resumo do que deve ser feito:**
- Back-end: Interface `PaymentGatewayService`.
- Front-end: Tela mostra instruções/dados retornados.
- Testes back-end: Adapter mock retorna cobrança; falha do gateway retorna erro controlado.
- Testes front-end: Componente renderiza QR/instruções mockadas.
- Documentos-base: FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: payment online, integração com gateway e webhook; `OnlinePaymentController`, `PaymentGatewayService`, integração mock/inicial, `PaymentWebhookController`
- Front-end: `front-end/` — app/feature alvo: checkout/payment e order status; etapa de pagamento no checkout, instruções de pagamento e status do pedido

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S16-H02: Como sistema, quero integrar gateway via adapter.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Pagamento online e webhook.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: FLUXOS.
- Regras importantes desta história: pagamento online não pode ser confirmado pelo front-end; webhook deve validar assinatura/token; webhook duplicado deve ser idempotente; payload bruto deve ser registrado para auditoria técnica.

O que gerar:
1. Back-end: Interface `PaymentGatewayService`.
2. Front-end: Tela mostra instruções/dados retornados.
3. Testes unitários back-end: Adapter mock retorna cobrança; falha do gateway retorna erro controlado.
4. Testes unitários front-end: Componente renderiza QR/instruções mockadas.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S16-H03 — Como gateway, quero enviar webhook.

**Resumo do que deve ser feito:**
- Back-end: `POST /api/public/payments/webhook`.
- Front-end: Página de status consulta pedido.
- Testes back-end: Assinatura inválida falha; payload bruto é registrado; webhook duplicado é idempotente.
- Testes front-end: Status muda conforme polling/mock.
- Documentos-base: DO, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: payment online, integração com gateway e webhook; `OnlinePaymentController`, `PaymentGatewayService`, integração mock/inicial, `PaymentWebhookController`
- Front-end: `front-end/` — app/feature alvo: checkout/payment e order status; etapa de pagamento no checkout, instruções de pagamento e status do pedido

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S16-H03: Como gateway, quero enviar webhook.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Pagamento online e webhook.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DO, FLUXOS.
- Regras importantes desta história: pagamento online não pode ser confirmado pelo front-end; webhook deve validar assinatura/token; webhook duplicado deve ser idempotente; payload bruto deve ser registrado para auditoria técnica.

O que gerar:
1. Back-end: `POST /api/public/payments/webhook`.
2. Front-end: Página de status consulta pedido.
3. Testes unitários back-end: Assinatura inválida falha; payload bruto é registrado; webhook duplicado é idempotente.
4. Testes unitários front-end: Status muda conforme polling/mock.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S16-H04 — Como sistema, quero aprovar pagamento por webhook.

**Resumo do que deve ser feito:**
- Back-end: Atualizar `PaymentStatus.PAID` e `Order.PAID`.
- Front-end: Página de pedido mostra pago.
- Testes back-end: Front-end não consegue confirmar pagamento; só webhook altera para PAID.
- Testes front-end: Status “Pago” aparece após atualização.
- Documentos-base: DOM, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: payment online, integração com gateway e webhook; `OnlinePaymentController`, `PaymentGatewayService`, integração mock/inicial, `PaymentWebhookController`
- Front-end: `front-end/` — app/feature alvo: checkout/payment e order status; etapa de pagamento no checkout, instruções de pagamento e status do pedido

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S16-H04: Como sistema, quero aprovar pagamento por webhook.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Pagamento online e webhook.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, FLUXOS.
- Regras importantes desta história: pagamento online não pode ser confirmado pelo front-end; webhook deve validar assinatura/token; webhook duplicado deve ser idempotente; payload bruto deve ser registrado para auditoria técnica.

O que gerar:
1. Back-end: Atualizar `PaymentStatus.PAID` e `Order.PAID`.
2. Front-end: Página de pedido mostra pago.
3. Testes unitários back-end: Front-end não consegue confirmar pagamento; só webhook altera para PAID.
4. Testes unitários front-end: Status “Pago” aparece após atualização.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S16-H05 — Como sistema, quero tratar pagamento recusado/expirado.

**Resumo do que deve ser feito:**
- Back-end: Atualizar para REFUSED/EXPIRED.
- Front-end: Mostrar instrução para nova tentativa.
- Testes back-end: Status recusado não cria envio para loja; expirado bloqueia pagamento antigo.
- Testes front-end: Mensagem de recusado/expirado aparece.
- Documentos-base: DOM, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: payment online, integração com gateway e webhook; `OnlinePaymentController`, `PaymentGatewayService`, integração mock/inicial, `PaymentWebhookController`
- Front-end: `front-end/` — app/feature alvo: checkout/payment e order status; etapa de pagamento no checkout, instruções de pagamento e status do pedido

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S16-H05: Como sistema, quero tratar pagamento recusado/expirado.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Pagamento online e webhook.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, FLUXOS.
- Regras importantes desta história: pagamento online não pode ser confirmado pelo front-end; webhook deve validar assinatura/token; webhook duplicado deve ser idempotente; payload bruto deve ser registrado para auditoria técnica.

O que gerar:
1. Back-end: Atualizar para REFUSED/EXPIRED.
2. Front-end: Mostrar instrução para nova tentativa.
3. Testes unitários back-end: Status recusado não cria envio para loja; expirado bloqueia pagamento antigo.
4. Testes unitários front-end: Mensagem de recusado/expirado aparece.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.


---

## Sprint 17 — Sync online → local e painel de sincronização

**Objetivo da sprint:** baixar pedidos online para a loja e monitorar falhas.


### S17-H01 — Como worker local, quero buscar pendências online.

**Resumo do que deve ser feito:**
- Back-end: `GET /api/sync/pending?storeId=...`.
- Front-end: Painel mostra pedidos aguardando loja.
- Testes back-end: Retorna apenas eventos da loja; sem assinatura válida falha.
- Testes front-end: Lista de pendências renderiza.
- Documentos-base: SYNC

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: sync pull, ACK, inbox, painel status e reprocessamento; `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`; política de retry e endpoints de ACK/fail/status
- Front-end: `front-end/` — app/feature alvo: admin/sync-panel, pdv/online-orders e kds/origin; badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S17-H01: Como worker local, quero buscar pendências online.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Sync online → local e painel.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: SYNC.
- Regras importantes desta história: local faz pull porque pode estar atrás de NAT; evento duplicado não pode criar pedido duplicado; ACK deve ser seguro para repetição; ignorar evento exige permissão e motivo.

O que gerar:
1. Back-end: `GET /api/sync/pending?storeId=...`.
2. Front-end: Painel mostra pedidos aguardando loja.
3. Testes unitários back-end: Retorna apenas eventos da loja; sem assinatura válida falha.
4. Testes unitários front-end: Lista de pendências renderiza.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S17-H02 — Como API local, quero processar pedido online recebido.

**Resumo do que deve ser feito:**
- Back-end: Gravar Order/Items/Payment local.
- Front-end: PDV mostra pedido online recebido.
- Testes back-end: Evento duplicado não cria pedido duplicado; cliente/endereço persistem.
- Testes front-end: Pedido aparece na lista local.
- Documentos-base: SYNC, DOM

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: sync pull, ACK, inbox, painel status e reprocessamento; `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`; política de retry e endpoints de ACK/fail/status
- Front-end: `front-end/` — app/feature alvo: admin/sync-panel, pdv/online-orders e kds/origin; badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S17-H02: Como API local, quero processar pedido online recebido.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Sync online → local e painel.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: SYNC, DOM.
- Regras importantes desta história: local faz pull porque pode estar atrás de NAT; evento duplicado não pode criar pedido duplicado; ACK deve ser seguro para repetição; ignorar evento exige permissão e motivo.

O que gerar:
1. Back-end: Gravar Order/Items/Payment local.
2. Front-end: PDV mostra pedido online recebido.
3. Testes unitários back-end: Evento duplicado não cria pedido duplicado; cliente/endereço persistem.
4. Testes unitários front-end: Pedido aparece na lista local.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S17-H03 — Como sistema, quero confirmar recebimento.

**Resumo do que deve ser feito:**
- Back-end: `POST /api/sync/events/{id}/ack`.
- Front-end: Status muda para recebido pela loja.
- Testes back-end: ACK marca online como RECEIVED_BY_STORE; ACK duplicado é seguro.
- Testes front-end: Badge de status atualiza.
- Documentos-base: SYNC

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: sync pull, ACK, inbox, painel status e reprocessamento; `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`; política de retry e endpoints de ACK/fail/status
- Front-end: `front-end/` — app/feature alvo: admin/sync-panel, pdv/online-orders e kds/origin; badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S17-H03: Como sistema, quero confirmar recebimento.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Sync online → local e painel.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: SYNC.
- Regras importantes desta história: local faz pull porque pode estar atrás de NAT; evento duplicado não pode criar pedido duplicado; ACK deve ser seguro para repetição; ignorar evento exige permissão e motivo.

O que gerar:
1. Back-end: `POST /api/sync/events/{id}/ack`.
2. Front-end: Status muda para recebido pela loja.
3. Testes unitários back-end: ACK marca online como RECEIVED_BY_STORE; ACK duplicado é seguro.
4. Testes unitários front-end: Badge de status atualiza.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S17-H04 — Como sistema, quero gerar KDS para pedido online.

**Resumo do que deve ser feito:**
- Back-end: Criar tickets locais se houver preparo.
- Front-end: KDS exibe pedido origem SITE/WHATSAPP.
- Testes back-end: Pedido online com setores gera tickets; sem preparo não gera.
- Testes front-end: Card mostra origem correta.
- Documentos-base: KDS, SYNC

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: sync pull, ACK, inbox, painel status e reprocessamento; `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`; política de retry e endpoints de ACK/fail/status
- Front-end: `front-end/` — app/feature alvo: admin/sync-panel, pdv/online-orders e kds/origin; badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S17-H04: Como sistema, quero gerar KDS para pedido online.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Sync online → local e painel.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: KDS, SYNC.
- Regras importantes desta história: local faz pull porque pode estar atrás de NAT; evento duplicado não pode criar pedido duplicado; ACK deve ser seguro para repetição; ignorar evento exige permissão e motivo.

O que gerar:
1. Back-end: Criar tickets locais se houver preparo.
2. Front-end: KDS exibe pedido origem SITE/WHATSAPP.
3. Testes unitários back-end: Pedido online com setores gera tickets; sem preparo não gera.
4. Testes unitários front-end: Card mostra origem correta.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S17-H05 — Como gerente, quero painel de sincronização.

**Resumo do que deve ser feito:**
- Back-end: `GET /api/sync/status` e listagem por status.
- Front-end: Tela com pendentes, erros e retries.
- Testes back-end: Filtra PENDING/FAILED; mensagem de erro retornada.
- Testes front-end: Filtros funcionam; cards mostram contadores.
- Documentos-base: SYNC, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: sync pull, ACK, inbox, painel status e reprocessamento; `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`; política de retry e endpoints de ACK/fail/status
- Front-end: `front-end/` — app/feature alvo: admin/sync-panel, pdv/online-orders e kds/origin; badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S17-H05: Como gerente, quero painel de sincronização.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Sync online → local e painel.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: SYNC, FLUXOS.
- Regras importantes desta história: local faz pull porque pode estar atrás de NAT; evento duplicado não pode criar pedido duplicado; ACK deve ser seguro para repetição; ignorar evento exige permissão e motivo.

O que gerar:
1. Back-end: `GET /api/sync/status` e listagem por status.
2. Front-end: Tela com pendentes, erros e retries.
3. Testes unitários back-end: Filtra PENDING/FAILED; mensagem de erro retornada.
4. Testes unitários front-end: Filtros funcionam; cards mostram contadores.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S17-H06 — Como gerente, quero reprocessar ou ignorar evento.

**Resumo do que deve ser feito:**
- Back-end: Endpoints reprocessar/ignorar com auditoria.
- Front-end: Botões “Reprocessar” e “Ignorar”.
- Testes back-end: Sem permissão falha; ignorar exige motivo; reprocessar altera status.
- Testes front-end: Modal exige motivo para ignorar.
- Documentos-base: SYNC, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: sync pull, ACK, inbox, painel status e reprocessamento; `SyncEventService`, `SyncWorker`, `SyncController`, `InboxService`, `HmacSignatureService`; política de retry e endpoints de ACK/fail/status
- Front-end: `front-end/` — app/feature alvo: admin/sync-panel, pdv/online-orders e kds/origin; badges de status, painel de sincronização, filtros e ações de reprocessar/ignorar

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S17-H06: Como gerente, quero reprocessar ou ignorar evento.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Sync online → local e painel.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: SYNC, FLUXOS.
- Regras importantes desta história: local faz pull porque pode estar atrás de NAT; evento duplicado não pode criar pedido duplicado; ACK deve ser seguro para repetição; ignorar evento exige permissão e motivo.

O que gerar:
1. Back-end: Endpoints reprocessar/ignorar com auditoria.
2. Front-end: Botões “Reprocessar” e “Ignorar”.
3. Testes unitários back-end: Sem permissão falha; ignorar exige motivo; reprocessar altera status.
4. Testes unitários front-end: Modal exige motivo para ignorar.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.


---

## Sprint 18 — Observabilidade, deploy e homologação MVP

**Objetivo da sprint:** preparar operação real com health checks, backup, deploy e validação fim a fim.


### S18-H01 — Como admin, quero health check operacional local.

**Resumo do que deve ser feito:**
- Back-end: `GET /api/sync/health` com DB/Rabbit/Redis/sync.
- Front-end: Painel operacional local.
- Testes back-end: Health reflete dependência falha; pendências são contadas.
- Testes front-end: Painel renderiza status e alertas.
- Documentos-base: DL, SYNC

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: actuator, health, version, scripts e pipeline; `HealthController`/Actuator config, `/version`, scripts, pipeline e documentação operacional
- Front-end: `front-end/` — app/feature alvo: operational panels, version footer e roteiros de homologação; painéis operacionais, footer de versão e telas de status

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S18-H01: Como admin, quero health check operacional local.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Observabilidade, deploy e homologação.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DL, SYNC.
- Regras importantes desta história: health check deve refletir dependências reais; backup e restore precisam estar documentados; pipeline deve rodar testes antes de gerar imagem; homologação deve validar fluxo local e online fim a fim.

O que gerar:
1. Back-end: `GET /api/sync/health` com DB/Rabbit/Redis/sync.
2. Front-end: Painel operacional local.
3. Testes unitários back-end: Health reflete dependência falha; pendências são contadas.
4. Testes unitários front-end: Painel renderiza status e alertas.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S18-H02 — Como admin, quero health check online.

**Resumo do que deve ser feito:**
- Back-end: Health online com certificado/webhooks/eventos.
- Front-end: Painel online simples.
- Testes back-end: Webhook indisponível afeta status; DB down retorna DOWN.
- Testes front-end: Tela exibe status online.
- Documentos-base: DO, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: actuator, health, version, scripts e pipeline; `HealthController`/Actuator config, `/version`, scripts, pipeline e documentação operacional
- Front-end: `front-end/` — app/feature alvo: operational panels, version footer e roteiros de homologação; painéis operacionais, footer de versão e telas de status

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S18-H02: Como admin, quero health check online.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Observabilidade, deploy e homologação.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DO, FLUXOS.
- Regras importantes desta história: health check deve refletir dependências reais; backup e restore precisam estar documentados; pipeline deve rodar testes antes de gerar imagem; homologação deve validar fluxo local e online fim a fim.

O que gerar:
1. Back-end: Health online com certificado/webhooks/eventos.
2. Front-end: Painel online simples.
3. Testes unitários back-end: Webhook indisponível afeta status; DB down retorna DOWN.
4. Testes unitários front-end: Tela exibe status online.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S18-H03 — Como dev, quero pipeline de build e deploy.

**Resumo do que deve ser feito:**
- Back-end: Build, test, Docker image e push.
- Front-end: Build Angular e publicação estática.
- Testes back-end: Pipeline falha com testes quebrados; imagem recebe tag.
- Testes front-end: Build front falha com lint/test quebrado.
- Documentos-base: DO

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: actuator, health, version, scripts e pipeline; `HealthController`/Actuator config, `/version`, scripts, pipeline e documentação operacional
- Front-end: `front-end/` — app/feature alvo: operational panels, version footer e roteiros de homologação; painéis operacionais, footer de versão e telas de status

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S18-H03: Como dev, quero pipeline de build e deploy.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Observabilidade, deploy e homologação.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DO.
- Regras importantes desta história: health check deve refletir dependências reais; backup e restore precisam estar documentados; pipeline deve rodar testes antes de gerar imagem; homologação deve validar fluxo local e online fim a fim.

O que gerar:
1. Back-end: Build, test, Docker image e push.
2. Front-end: Build Angular e publicação estática.
3. Testes unitários back-end: Pipeline falha com testes quebrados; imagem recebe tag.
4. Testes unitários front-end: Build front falha com lint/test quebrado.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S18-H04 — Como operador, quero validar fluxo fim a fim local.

**Resumo do que deve ser feito:**
- Back-end: Script/cenário: login → caixa → venda → pagamento → impressão → KDS → sync pending.
- Front-end: Roteiro de homologação no front.
- Testes back-end: Testes de serviços cobrem cada etapa principal.
- Testes front-end: Testes de componentes cobrem fluxo simulado.
- Documentos-base: PDV, KDS, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: actuator, health, version, scripts e pipeline; `HealthController`/Actuator config, `/version`, scripts, pipeline e documentação operacional
- Front-end: `front-end/` — app/feature alvo: operational panels, version footer e roteiros de homologação; painéis operacionais, footer de versão e telas de status

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S18-H04: Como operador, quero validar fluxo fim a fim local.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Observabilidade, deploy e homologação.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: PDV, KDS, FLUXOS.
- Regras importantes desta história: health check deve refletir dependências reais; backup e restore precisam estar documentados; pipeline deve rodar testes antes de gerar imagem; homologação deve validar fluxo local e online fim a fim.

O que gerar:
1. Back-end: Script/cenário: login → caixa → venda → pagamento → impressão → KDS → sync pending.
2. Front-end: Roteiro de homologação no front.
3. Testes unitários back-end: Testes de serviços cobrem cada etapa principal.
4. Testes unitários front-end: Testes de componentes cobrem fluxo simulado.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S18-H05 — Como admin, quero validar fluxo online fim a fim.

**Resumo do que deve ser feito:**
- Back-end: Site → pedido → pagamento → webhook → sync → KDS.
- Front-end: Roteiro de homologação site/admin.
- Testes back-end: Webhook + sync idempotente em sequência.
- Testes front-end: Checkout e status renderizam etapas.
- Documentos-base: FLUXOS, SYNC, DO

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: actuator, health, version, scripts e pipeline; `HealthController`/Actuator config, `/version`, scripts, pipeline e documentação operacional
- Front-end: `front-end/` — app/feature alvo: operational panels, version footer e roteiros de homologação; painéis operacionais, footer de versão e telas de status

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S18-H05: Como admin, quero validar fluxo online fim a fim.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Observabilidade, deploy e homologação.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: FLUXOS, SYNC, DO.
- Regras importantes desta história: health check deve refletir dependências reais; backup e restore precisam estar documentados; pipeline deve rodar testes antes de gerar imagem; homologação deve validar fluxo local e online fim a fim.

O que gerar:
1. Back-end: Site → pedido → pagamento → webhook → sync → KDS.
2. Front-end: Roteiro de homologação site/admin.
3. Testes unitários back-end: Webhook + sync idempotente em sequência.
4. Testes unitários front-end: Checkout e status renderizam etapas.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S18-H06 — Como suporte, quero documentação de implantação.

**Resumo do que deve ser feito:**
- Back-end: Atualizar README e scripts.
- Front-end: Tela/links de status e versão.
- Testes back-end: Endpoint `/version` retorna versão/commit.
- Testes front-end: Footer mostra versão.
- Documentos-base: DL, DO

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: actuator, health, version, scripts e pipeline; `HealthController`/Actuator config, `/version`, scripts, pipeline e documentação operacional
- Front-end: `front-end/` — app/feature alvo: operational panels, version footer e roteiros de homologação; painéis operacionais, footer de versão e telas de status

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S18-H06: Como suporte, quero documentação de implantação.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Observabilidade, deploy e homologação.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DL, DO.
- Regras importantes desta história: health check deve refletir dependências reais; backup e restore precisam estar documentados; pipeline deve rodar testes antes de gerar imagem; homologação deve validar fluxo local e online fim a fim.

O que gerar:
1. Back-end: Atualizar README e scripts.
2. Front-end: Tela/links de status e versão.
3. Testes unitários back-end: Endpoint `/version` retorna versão/commit.
4. Testes unitários front-end: Footer mostra versão.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.


---

## Sprint 19 — Estoque básico

**Objetivo da sprint:** registrar movimentações de estoque e refletir disponibilidade.


### S19-H01 — Como gerente, quero registrar entrada de estoque.

**Resumo do que deve ser feito:**
- Back-end: Criar `StockMovement IN`.
- Front-end: Tela de ajuste de estoque.
- Testes back-end: Quantidade positiva; produto obrigatório; usuário obrigatório.
- Testes front-end: Form valida produto/quantidade.
- Documentos-base: DOM, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: stock-movement, baixa por venda e availability automation; `StockMovementService`, `StockController`, integração com finalização de venda e disponibilidade
- Front-end: `front-end/` — app/feature alvo: admin/stock-adjustment e stock badges; tela de ajuste de estoque, badges de estoque e histórico

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S19-H01: Como gerente, quero registrar entrada de estoque.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Estoque básico.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, FLUXOS.
- Regras importantes desta história: estoque físico é fonte local; ajuste manual exige motivo; estoque zero pode indisponibilizar produto conforme regra; movimento de estoque deve ser rastreável.

O que gerar:
1. Back-end: Criar `StockMovement IN`.
2. Front-end: Tela de ajuste de estoque.
3. Testes unitários back-end: Quantidade positiva; produto obrigatório; usuário obrigatório.
4. Testes unitários front-end: Form valida produto/quantidade.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S19-H02 — Como gerente, quero registrar perda.

**Resumo do que deve ser feito:**
- Back-end: Criar `StockMovement LOSS`.
- Front-end: Ação “perda”.
- Testes back-end: Motivo obrigatório; movimento audita usuário.
- Testes front-end: Modal exige motivo.
- Documentos-base: DOM

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: stock-movement, baixa por venda e availability automation; `StockMovementService`, `StockController`, integração com finalização de venda e disponibilidade
- Front-end: `front-end/` — app/feature alvo: admin/stock-adjustment e stock badges; tela de ajuste de estoque, badges de estoque e histórico

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S19-H02: Como gerente, quero registrar perda.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Estoque básico.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM.
- Regras importantes desta história: estoque físico é fonte local; ajuste manual exige motivo; estoque zero pode indisponibilizar produto conforme regra; movimento de estoque deve ser rastreável.

O que gerar:
1. Back-end: Criar `StockMovement LOSS`.
2. Front-end: Ação “perda”.
3. Testes unitários back-end: Motivo obrigatório; movimento audita usuário.
4. Testes unitários front-end: Modal exige motivo.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S19-H03 — Como sistema, quero baixar estoque por venda.

**Resumo do que deve ser feito:**
- Back-end: Ao finalizar venda, gerar `SALE`.
- Front-end: Mostrar estoque estimado no admin.
- Testes back-end: Venda baixa quantidade correta; venda cancelada ajusta conforme regra.
- Testes front-end: Coluna estoque renderiza quantidade.
- Documentos-base: DOM, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: stock-movement, baixa por venda e availability automation; `StockMovementService`, `StockController`, integração com finalização de venda e disponibilidade
- Front-end: `front-end/` — app/feature alvo: admin/stock-adjustment e stock badges; tela de ajuste de estoque, badges de estoque e histórico

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S19-H03: Como sistema, quero baixar estoque por venda.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Estoque básico.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, FLUXOS.
- Regras importantes desta história: estoque físico é fonte local; ajuste manual exige motivo; estoque zero pode indisponibilizar produto conforme regra; movimento de estoque deve ser rastreável.

O que gerar:
1. Back-end: Ao finalizar venda, gerar `SALE`.
2. Front-end: Mostrar estoque estimado no admin.
3. Testes unitários back-end: Venda baixa quantidade correta; venda cancelada ajusta conforme regra.
4. Testes unitários front-end: Coluna estoque renderiza quantidade.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S19-H04 — Como sistema, quero indisponibilizar produto sem estoque.

**Resumo do que deve ser feito:**
- Back-end: Regra automática atualiza availability.
- Front-end: Badge “acabou”.
- Testes back-end: Estoque zero cria `PRODUCT_UNAVAILABLE`; sob encomenda não bloqueia.
- Testes front-end: Badge atualiza no catálogo.
- Documentos-base: DOM, SYNC

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: stock-movement, baixa por venda e availability automation; `StockMovementService`, `StockController`, integração com finalização de venda e disponibilidade
- Front-end: `front-end/` — app/feature alvo: admin/stock-adjustment e stock badges; tela de ajuste de estoque, badges de estoque e histórico

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S19-H04: Como sistema, quero indisponibilizar produto sem estoque.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Estoque básico.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, SYNC.
- Regras importantes desta história: estoque físico é fonte local; ajuste manual exige motivo; estoque zero pode indisponibilizar produto conforme regra; movimento de estoque deve ser rastreável.

O que gerar:
1. Back-end: Regra automática atualiza availability.
2. Front-end: Badge “acabou”.
3. Testes unitários back-end: Estoque zero cria `PRODUCT_UNAVAILABLE`; sob encomenda não bloqueia.
4. Testes unitários front-end: Badge atualiza no catálogo.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S19-H05 — Como sistema, quero sincronizar estoque/disponibilidade.

**Resumo do que deve ser feito:**
- Back-end: Criar `STOCK_MOVED` e `PRODUCT_UNAVAILABLE`.
- Front-end: Painel mostra pendente de sync.
- Testes back-end: Evento criado e idempotente.
- Testes front-end: Status de sync aparece.
- Documentos-base: SYNC

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: stock-movement, baixa por venda e availability automation; `StockMovementService`, `StockController`, integração com finalização de venda e disponibilidade
- Front-end: `front-end/` — app/feature alvo: admin/stock-adjustment e stock badges; tela de ajuste de estoque, badges de estoque e histórico

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S19-H05: Como sistema, quero sincronizar estoque/disponibilidade.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: Estoque básico.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: SYNC.
- Regras importantes desta história: estoque físico é fonte local; ajuste manual exige motivo; estoque zero pode indisponibilizar produto conforme regra; movimento de estoque deve ser rastreável.

O que gerar:
1. Back-end: Criar `STOCK_MOVED` e `PRODUCT_UNAVAILABLE`.
2. Front-end: Painel mostra pendente de sync.
3. Testes unitários back-end: Evento criado e idempotente.
4. Testes unitários front-end: Status de sync aparece.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.


---

## Sprint 20 — WhatsApp inicial e atendimento assistido

**Objetivo da sprint:** iniciar pedidos via WhatsApp com atendimento assistido antes de chatbot completo.


### S20-H01 — Como provider, quero enviar webhook WhatsApp.

**Resumo do que deve ser feito:**
- Back-end: `POST /api/whatsapp/webhook`.
- Front-end: Painel lista conversas recebidas.
- Testes back-end: Assinatura inválida falha; mensagem duplicada é ignorada.
- Testes front-end: Lista renderiza conversa e última mensagem.
- Documentos-base: DO, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: whatsapp webhook, conversation, assisted order e message adapter; `WhatsappWebhookController`, `ConversationService`, `AssistedOrderService`, serviço de mensagens
- Front-end: `front-end/` — app/feature alvo: admin/whatsapp conversations e assisted cart; painel de conversas, busca de produtos WhatsApp e carrinho assistido

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S20-H01: Como provider, quero enviar webhook WhatsApp.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: WhatsApp inicial.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DO, FLUXOS.
- Regras importantes desta história: webhook deve ser validado e idempotente; pedido WhatsApp usa origem WHATSAPP; somente produtos sellOnWhatsapp aparecem; falha ao enviar mensagem não deve cancelar pedido salvo.

O que gerar:
1. Back-end: `POST /api/whatsapp/webhook`.
2. Front-end: Painel lista conversas recebidas.
3. Testes unitários back-end: Assinatura inválida falha; mensagem duplicada é ignorada.
4. Testes unitários front-end: Lista renderiza conversa e última mensagem.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S20-H02 — Como atendente, quero ver produtos habilitados para WhatsApp.

**Resumo do que deve ser feito:**
- Back-end: Endpoint filtra `sellOnWhatsapp`.
- Front-end: Busca produto dentro da conversa.
- Testes back-end: Produto sem canal WhatsApp não aparece.
- Testes front-end: Busca filtra catálogo WhatsApp.
- Documentos-base: DOM, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: whatsapp webhook, conversation, assisted order e message adapter; `WhatsappWebhookController`, `ConversationService`, `AssistedOrderService`, serviço de mensagens
- Front-end: `front-end/` — app/feature alvo: admin/whatsapp conversations e assisted cart; painel de conversas, busca de produtos WhatsApp e carrinho assistido

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S20-H02: Como atendente, quero ver produtos habilitados para WhatsApp.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: WhatsApp inicial.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, FLUXOS.
- Regras importantes desta história: webhook deve ser validado e idempotente; pedido WhatsApp usa origem WHATSAPP; somente produtos sellOnWhatsapp aparecem; falha ao enviar mensagem não deve cancelar pedido salvo.

O que gerar:
1. Back-end: Endpoint filtra `sellOnWhatsapp`.
2. Front-end: Busca produto dentro da conversa.
3. Testes unitários back-end: Produto sem canal WhatsApp não aparece.
4. Testes unitários front-end: Busca filtra catálogo WhatsApp.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S20-H03 — Como atendente, quero montar pedido assistido.

**Resumo do que deve ser feito:**
- Back-end: Criar pedido origem `WHATSAPP`.
- Front-end: Carrinho assistido no admin/WhatsApp.
- Testes back-end: Pedido exige cliente/telefone; snapshot salvo.
- Testes front-end: Carrinho assistido adiciona itens.
- Documentos-base: DOM, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: whatsapp webhook, conversation, assisted order e message adapter; `WhatsappWebhookController`, `ConversationService`, `AssistedOrderService`, serviço de mensagens
- Front-end: `front-end/` — app/feature alvo: admin/whatsapp conversations e assisted cart; painel de conversas, busca de produtos WhatsApp e carrinho assistido

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S20-H03: Como atendente, quero montar pedido assistido.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: WhatsApp inicial.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: DOM, FLUXOS.
- Regras importantes desta história: webhook deve ser validado e idempotente; pedido WhatsApp usa origem WHATSAPP; somente produtos sellOnWhatsapp aparecem; falha ao enviar mensagem não deve cancelar pedido salvo.

O que gerar:
1. Back-end: Criar pedido origem `WHATSAPP`.
2. Front-end: Carrinho assistido no admin/WhatsApp.
3. Testes unitários back-end: Pedido exige cliente/telefone; snapshot salvo.
4. Testes unitários front-end: Carrinho assistido adiciona itens.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S20-H04 — Como cliente, quero receber confirmação.

**Resumo do que deve ser feito:**
- Back-end: Adapter de envio de mensagem.
- Front-end: Botão enviar resumo.
- Testes back-end: Falha do provider não cancela pedido; erro é registrado.
- Testes front-end: Preview do resumo formatado.
- Documentos-base: FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: whatsapp webhook, conversation, assisted order e message adapter; `WhatsappWebhookController`, `ConversationService`, `AssistedOrderService`, serviço de mensagens
- Front-end: `front-end/` — app/feature alvo: admin/whatsapp conversations e assisted cart; painel de conversas, busca de produtos WhatsApp e carrinho assistido

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S20-H04: Como cliente, quero receber confirmação.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: WhatsApp inicial.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: FLUXOS.
- Regras importantes desta história: webhook deve ser validado e idempotente; pedido WhatsApp usa origem WHATSAPP; somente produtos sellOnWhatsapp aparecem; falha ao enviar mensagem não deve cancelar pedido salvo.

O que gerar:
1. Back-end: Adapter de envio de mensagem.
2. Front-end: Botão enviar resumo.
3. Testes unitários back-end: Falha do provider não cancela pedido; erro é registrado.
4. Testes unitários front-end: Preview do resumo formatado.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.



### S20-H05 — Como sistema, quero sincronizar pedido WhatsApp para loja.

**Resumo do que deve ser feito:**
- Back-end: Criar SyncEvent ORDER_CREATED.
- Front-end: Status “aguardando loja”.
- Testes back-end: Evento idempotente; ACK atualiza status.
- Testes front-end: Status renderiza corretamente.
- Documentos-base: SYNC, FLUXOS

**Arquivos/módulos esperados:**
- Back-end: `back-end/` — módulo alvo: whatsapp webhook, conversation, assisted order e message adapter; `WhatsappWebhookController`, `ConversationService`, `AssistedOrderService`, serviço de mensagens
- Front-end: `front-end/` — app/feature alvo: admin/whatsapp conversations e assisted cart; painel de conversas, busca de produtos WhatsApp e carrinho assistido

**Prompt para copiar e colar no ChatGPT UI:**

~~~text
Você é um arquiteto/desenvolvedor full-stack Java 21 + Spring Boot 3.x + Angular/TypeScript.

Gere código copiável para a história S20-H05: Como sistema, quero sincronizar pedido WhatsApp para loja.

Contexto do projeto:
- Sistema Nova Aliança / MNSS.
- Arquitetura híbrida local + online.
- Stack: Java 21, Spring Boot 3.x, PostgreSQL, RabbitMQ, Redis, Flyway, MapStruct, OpenAPI, JUnit 5, Mockito, Testcontainers, Angular, TypeScript e RxJS.
- Domínio da sprint: WhatsApp inicial.
- Regra central: a loja precisa vender presencialmente mesmo sem internet.
- Documentos de referência: SYNC, FLUXOS.
- Regras importantes desta história: webhook deve ser validado e idempotente; pedido WhatsApp usa origem WHATSAPP; somente produtos sellOnWhatsapp aparecem; falha ao enviar mensagem não deve cancelar pedido salvo.

O que gerar:
1. Back-end: Criar SyncEvent ORDER_CREATED.
2. Front-end: Status “aguardando loja”.
3. Testes unitários back-end: Evento idempotente; ACK atualiza status.
4. Testes unitários front-end: Status renderiza corretamente.

Entregue a resposta neste formato:
- Primeiro, uma lista objetiva dos arquivos que devem ser criados ou alterados.
- Depois, um bloco de código completo por arquivo, com o caminho do arquivo acima do bloco.
- Não omita imports.
- Não use pseudocódigo.
- Não gere código de histórias futuras.
- Quando faltar alguma classe existente do meu projeto, crie uma versão mínima compatível e explique onde adaptar.
- Inclua validações de negócio no back-end, não apenas no front-end.
- Inclua testes automatizados.
~~~

**Checklist depois de colar o código:**
- Compilar back-end.
- Rodar testes back-end.
- Compilar front-end.
- Rodar testes front-end.
- Testar manualmente a tela ou endpoint principal da história.
- Confirmar que nenhuma história futura foi implementada por acidente.
