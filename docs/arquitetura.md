# Arquitetura — Sistema Nova Aliança

## 1. Objetivo

Este documento descreve a arquitetura geral do **Sistema Nova Aliança**, uma solução híbrida **local + online** para operação da Padaria e Lanchonete Nova Aliança.

A arquitetura foi definida para atender dois objetivos principais:

1. Garantir que a loja continue operando mesmo sem internet.
2. Permitir venda online por site, WhatsApp, chatbot e pagamentos digitais.

## 2. Decisão arquitetural principal

O sistema será dividido em dois ambientes:

```text
Ambiente local
+
Ambiente online
```

O ambiente local é responsável pela operação crítica da padaria.

O ambiente online é responsável pelos canais externos e integração com clientes.

## 3. Regra central

> A loja não pode depender da internet para vender presencialmente.

Portanto:

- PDV deve funcionar localmente.
- KDS deve funcionar localmente.
- Caixa deve funcionar localmente.
- Impressão deve funcionar localmente.
- Banco local deve armazenar vendas e pedidos.
- A sincronização com a nuvem deve ocorrer quando houver internet.

## 4. Visão macro

```text
                         INTERNET / NUVEM
┌────────────────────────────────────────────────────────────┐
│ Hostinger VPS                                              │
│                                                            │
│  ┌────────────────────┐   ┌─────────────────────────────┐  │
│  │ Angular Site/Admin  │   │ Java Online API             │  │
│  │ Site público        │   │ Pedidos online              │  │
│  │ Cardápio online     │   │ WhatsApp                    │  │
│  │ Painel externo      │   │ Pagamentos                  │  │
│  └────────────────────┘   │ Sincronização                │  │
│                           └──────────────┬──────────────┘  │
│                                          │                 │
│  ┌────────────┐   ┌────────────┐   ┌─────▼──────┐          │
│  │ PostgreSQL │   │ RabbitMQ   │   │ Redis      │          │
│  └────────────┘   └────────────┘   └────────────┘          │
└────────────────────────────────────────────────────────────┘
                          ▲
                          │ HTTPS / Sync API
                          ▼
                    REDE LOCAL DA PADARIA
┌────────────────────────────────────────────────────────────┐
│ Servidor local                                             │
│                                                            │
│  ┌────────────────────┐   ┌─────────────────────────────┐  │
│  │ Angular PDV/KDS     │   │ Java Local API              │  │
│  │ PDV                 │   │ Caixa                       │  │
│  │ KDS                 │   │ Pedidos locais              │  │
│  │ Admin local         │   │ Impressão                   │  │
│  └────────────────────┘   │ Sincronização                │  │
│                           └──────────────┬──────────────┘  │
│                                          │                 │
│  ┌────────────┐   ┌────────────┐   ┌─────▼──────┐          │
│  │ PostgreSQL │   │ RabbitMQ   │   │ Redis      │          │
│  └────────────┘   └────────────┘   └────────────┘          │
└────────────────────────────────────────────────────────────┘
          ▲                         ▲
          │                         │
          ▼                         ▼
     PDV Caixa                 KDS Cozinha
```

## 5. Ambiente local

O ambiente local roda dentro da padaria.

### 5.1 Responsabilidades

- PDV
- Frente de caixa
- KDS
- Impressão
- Gaveta de dinheiro
- Vendas presenciais
- Pedidos internos
- Controle de caixa
- Controle operacional de produtos
- Controle de disponibilidade
- Banco local
- Eventos pendentes de sincronização

### 5.2 Componentes

```text
local/
├── Java Local API
├── PostgreSQL Local
├── RabbitMQ Local
├── Redis Local
├── Nginx Local
├── Angular PDV
├── Angular KDS
├── Angular Admin Local
└── Sync Worker Local
```

### 5.3 Serviços locais

| Serviço | Responsabilidade |
|---|---|
| `local-api` | API principal da loja |
| `postgres-local` | Banco operacional da loja |
| `rabbitmq-local` | Eventos internos da loja |
| `redis-local` | Cache e apoio operacional |
| `nginx-local` | Proxy reverso local |
| `sync-worker-local` | Envio e recebimento de sincronizações |
| `pdv` | Interface do caixa |
| `kds` | Interface de produção |
| `admin-local` | Gestão local |

## 6. Ambiente online

O ambiente online roda na VPS Hostinger.

### 6.1 Responsabilidades

- Site institucional
- Cardápio online
- Pedidos online
- WhatsApp
- Chatbot
- Pagamento online
- Webhooks
- Relatórios remotos
- Backup
- Sincronização com a loja

### 6.2 Componentes

```text
online/
├── Java Online API
├── PostgreSQL Online
├── RabbitMQ Online
├── Redis Online
├── Nginx
├── Certbot
├── Angular Site
├── Angular Admin Online
└── Sync Worker Online
```

### 6.3 Serviços online

| Serviço | Responsabilidade |
|---|---|
| `online-api` | API externa |
| `postgres-online` | Banco online |
| `rabbitmq-online` | Eventos online |
| `redis-online` | Cache e rate limit |
| `nginx` | Proxy reverso e entrada HTTP/HTTPS |
| `certbot` | Certificados SSL |
| `site` | Site institucional e cardápio |
| `admin-online` | Gestão remota |
| `sync-worker-online` | Processamento de eventos externos |

## 7. Stack definida

### Backend

- Java 25
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- RabbitMQ
- Redis
- Flyway
- MapStruct
- OpenAPI
- JUnit 5
- Mockito
- Testcontainers

### Frontend

- Angular
- TypeScript
- RxJS
- Angular Material ou PrimeNG
- SCSS ou Tailwind
- PWA
- WebSocket client

### Infraestrutura

- Hostinger VPS
- Ubuntu Server
- Docker
- Docker Compose
- Nginx
- Certbot / Let's Encrypt
- UFW
- SSH com chave
- Prometheus
- Grafana
- Loki
- Restic ou pg_dump

## 8. Monólito modular

Foi decidido não iniciar com microserviços.

A arquitetura será:

```text
Monólito modular local
+
Monólito modular online
+
Módulo de sincronização
```

### Motivos

- Menor complexidade
- Menor custo operacional
- Menos pontos de falha
- Deploy mais simples
- Melhor para sistema local
- Mais fácil de evoluir no início

## 9. Estrutura sugerida do repositório

```text
nova-alianca-system/
│
├── backend/
│   ├── core-domain/
│   ├── local-app/
│   ├── online-app/
│   ├── sync-module/
│   └── shared-infra/
│
├── frontend/
│   ├── site-publico/
│   ├── admin/
│   ├── pdv/
│   └── kds/
│
├── infra/
│   ├── local/
│   └── online/
│
└── docs/
```

## 10. Comunicação local ↔ online

A comunicação será feita via API HTTPS segura.

Não será permitido expor diretamente:

- PostgreSQL
- RabbitMQ
- Redis

Fluxo recomendado:

```text
Local → HTTPS → API Online
Online → Dados disponíveis → Local consulta/baixa via HTTPS
```

## 11. Comunicação em tempo real

O KDS usará WebSocket para receber pedidos em tempo real no ambiente local.

```text
PDV cria pedido
↓
API local salva pedido
↓
API local publica evento
↓
WebSocket notifica KDS
↓
KDS atualiza tela
```

## 12. Banco de dados

Haverá dois bancos:

```text
PostgreSQL Local
PostgreSQL Online
```

### 12.1 Banco local

Fonte principal para:

- Vendas presenciais
- Caixa
- KDS
- Estoque operacional
- Disponibilidade real
- Eventos locais

### 12.2 Banco online

Fonte principal para:

- Pedidos online
- Clientes online
- Pagamentos online
- Site
- WhatsApp
- Relatórios remotos

## 13. Fonte de verdade por domínio

| Domínio | Fonte principal |
|---|---|
| Vendas presenciais | Local |
| Caixa | Local |
| KDS | Local |
| Estoque físico | Local |
| Disponibilidade real | Local |
| Site | Online |
| Pedidos online | Online |
| Pagamentos online | Online/gateway |
| Relatórios consolidados | Online |
| Cadastro de produtos | Admin sincronizado |

## 14. Segurança arquitetural

### Local

- Acesso apenas na rede interna
- Banco não exposto publicamente
- Servidor em local protegido
- Login por operador
- Permissões por perfil
- Logs de auditoria

### Online

- HTTPS obrigatório
- Firewall
- SSH com chave
- Banco não público
- RabbitMQ não público
- Redis não público
- Validação de webhooks
- Rate limit
- Backup

## 15. Decisões finais

- Sistema híbrido local + online.
- Operação crítica fica local.
- Nuvem atende vendas externas e gestão remota.
- Comunicação entre ambientes via HTTPS.
- RabbitMQ usado internamente em cada ambiente.
- PostgreSQL como fonte principal.
- Redis como apoio.
- Docker Compose para local e online.
- Monólito modular, não microserviços inicialmente.
- KDS com WebSocket.
- Impressão local.
