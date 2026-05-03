# Sistema MNSS / Nova Alianca

Base inicial do sistema Nova Alianca, seguindo a arquitetura hibrida local + online descrita em `docs/`.

## Estrutura

```text
back-end/
  core-domain/   regras e objetos de dominio sem framework
  sync-module/   contratos iniciais de idempotencia e sincronizacao
  local-app/     API local para PDV, caixa, KDS e operacao offline
  online-app/    API online para canais externos e sincronizacao

front-end/
  Angular standalone app com console operacional inicial

infra/local/
  Docker Compose local para PostgreSQL, RabbitMQ, Redis e Nginx
```

## Decisoes do bootstrap

- Monolito modular separado por ambiente local e online.
- Operacao local tratada como caminho critico.
- IDs principais em UUID.
- Dinheiro em `BigDecimal` no Java e `NUMERIC(12,2)` no PostgreSQL.
- Eventos de sync com `idempotency_key` unica.
- Java configurado em 21 porque e o JDK disponivel neste workspace. Os docs citam Java 25; a troca deve ser feita quando o JDK 25 estiver instalado no ambiente.

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
cp .env.example .env
docker compose -f infra/local/docker-compose.yml --env-file .env up -d
```
