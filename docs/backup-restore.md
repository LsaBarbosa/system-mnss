# Backup e Restore — Banco de Dados Local

Este guia documenta o procedimento de backup e restauração do banco PostgreSQL local (`nova_alianca_local`).

## Scripts disponíveis

| Script | Finalidade |
|--------|-----------|
| `infra/local/scripts/backup-postgres.sh` | Gera backup em SQL e mantém os últimos 15 dias |
| `infra/local/scripts/restore-postgres.sh` | Restaura um arquivo de backup SQL |

## Backup manual

```bash
cd infra/local
bash scripts/backup-postgres.sh
```

O arquivo de backup é gravado em `infra/local/postgres/backup/backup_YYYY-MM-DD_HH-MM-SS.sql`.

Variáveis de ambiente opcionais:

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `BACKUP_DIR` | `infra/local/postgres/backup` | Diretório de destino |
| `POSTGRES_DB` | `nova_alianca_local` | Nome do banco |
| `POSTGRES_USER` | `nova_alianca` | Usuário PostgreSQL |

## Backup via pg_dump (formato customizado)

O formato customizado (`-Fc`) é recomendado para bancos maiores pois comprime o arquivo e permite restauração paralela:

```bash
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
docker exec postgres-local pg_dump \
  -U nova_alianca nova_alianca_local \
  -F c -f /tmp/backup_${TIMESTAMP}.dump

docker cp postgres-local:/tmp/backup_${TIMESTAMP}.dump ./backup_${TIMESTAMP}.dump
```

## Restore — Local

```bash
cd infra/local
bash scripts/restore-postgres.sh <caminho/para/backup.sql>
```

Exemplo:

```bash
bash scripts/restore-postgres.sh postgres/backup/backup_2026-05-08_10-00-00.sql
```

O script:
1. Exige confirmação textual: `RESTAURAR-nova_alianca_local`
2. Cria backup pré-restore automático antes de qualquer alteração
3. Falha se o arquivo de backup não existir ou o container não estiver rodando
4. Encerra conexões ativas ao banco
5. Remove o banco existente
6. Recria o banco vazio
7. Aplica o dump SQL

## Restore — Online

```bash
cd infra/online
bash scripts/restore-postgres.sh <caminho/para/backup.sql>
```

O script online segue o mesmo protocolo do local, mas opera sobre `postgres-online` e `nova_alianca_online`:
1. Exige confirmação textual: `RESTAURAR-nova_alianca_online`
2. Cria backup pré-restore automático
3. Só prossegue se o container `postgres-online` estiver rodando

## Restore de backup no formato customizado

```bash
docker cp backup_20260508_100000.dump postgres-local:/tmp/

docker exec postgres-local pg_restore \
  -U nova_alianca -d nova_alianca_local \
  --clean --if-exists /tmp/backup_20260508_100000.dump
```

## Backup automático via cron

Adicione ao crontab do host para backup diário às 02h:

```cron
0 2 * * * cd /caminho/para/infra/local && bash scripts/backup-postgres.sh >> /var/log/mnss-backup.log 2>&1
```

## Retenção

O script `backup-postgres.sh` apaga automaticamente backups com mais de 15 dias. Para alterar, edite a variável no script ou defina `BACKUP_RETENTION_DAYS` no `.env`.

## Verificação pós-restore

```bash
# Confirmar que as tabelas existem
docker exec postgres-local psql -U nova_alianca -d nova_alianca_local \
  -c "SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY 1;"

# Confirmar que as migrations foram aplicadas
docker exec postgres-local psql -U nova_alianca -d nova_alianca_local \
  -c "SELECT version, description, installed_on FROM flyway_schema_history ORDER BY installed_rank;"
```
