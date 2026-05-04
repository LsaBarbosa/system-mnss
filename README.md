# Sistema MNSS / Nova Alianca

Base inicial do sistema Nova Alianca, seguindo a arquitetura hibrida local + online descrita em `docs/`.

## Estrutura

```text
back-end/
  core-domain/   dominio puro, entidades, value objects e regras sem framework
  sync-module/   dominio/portas de idempotencia e sincronizacao
  shared-infra/  infraestrutura tecnica compartilhada sem regra de negocio
  local-app/     composition root local e adapters locais
  online-app/    composition root online e adapters online

front-end/
  Angular standalone app organizado por core, shared e features
  features/<feature>/{domain,application,data-access,ui,pages}

infra/local/
  Docker Compose local para API, front-end estatico, PostgreSQL, RabbitMQ, Redis e Nginx

infra/online/
  Docker Compose base para VPS com API online, site/admin, PostgreSQL, RabbitMQ, Redis, Nginx e Certbot
```

## Decisoes do bootstrap

- Monolito modular separado por ambiente local e online, sem microservicos no inicio.
- Back-end deve ser organizado em módulos funcionais.
- Dominio e aplicacao nao dependem de HTTP, banco, mensageria, hardware ou APIs externas.
- Front-end deve seguir arquitetura por features: `domain`, `application`, `data-access`, `ui` e `pages`.
- Componentes de UI nao acessam `HttpClient`; chamadas HTTP ficam em `data-access`.
- Operacao local tratada como caminho critico.
- IDs principais em UUID.
- Dinheiro em `BigDecimal` no Java e `NUMERIC(12,2)` no PostgreSQL.
- Eventos de sync com `idempotency_key` unica.
- Java configurado em 21.

## Comandos

Back-end:

```bash
cd back-end
./gradlew test
./gradlew build
```

Front-end:

```bash
cd front-end
PATH=/home/kronos/.nvm/versions/node/v24.14.1/bin:$PATH npm install
PATH=/home/kronos/.nvm/versions/node/v24.14.1/bin:$PATH npm test
PATH=/home/kronos/.nvm/versions/node/v24.14.1/bin:$PATH npm run build
```

Infra local:

```bash
cp infra/local/.env.example infra/local/.env
docker compose -f infra/local/docker-compose.yml --env-file infra/local/.env up -d
infra/local/scripts/backup-postgres.sh
```

O ambiente local cria os perfis iniciais e um usuario admin a partir das variaveis
`MNSS_INITIAL_ADMIN_USERNAME` e `MNSS_INITIAL_ADMIN_PASSWORD`.

Infra online base:

```bash
cp infra/online/.env.example infra/online/.env
docker compose -f infra/online/docker-compose.yml --env-file infra/online/.env up -d
infra/online/scripts/backup-postgres.sh
```
