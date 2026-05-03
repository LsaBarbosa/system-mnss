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

- Java 21
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

A arquitetura será um monólito modular:

```text
Monólito modular local
+ Monólito modular online
+ módulos de domínio/aplicação/infra compartilhados
```

### Motivos

- Menor complexidade
- Menor custo operacional
- Menos pontos de falha
- Deploy mais simples
- Melhor para sistema local
- Mais fácil de evoluir no início

## 9. Estrutura de módulos

O back-end deve ser organizado de forma modular, separando as responsabilidades técnicas.

### 9.1 Camadas sugeridas

Cada domínio funcional deve ser organizado de forma clara:

```text
<dominio>/
├── model/          # entidades, objetos de valor e enums
├── service/        # lógica de negócio e serviços
└── web/            # controladores e DTOs de entrada
```

### 9.2 Módulos Gradle e responsabilidades

| Módulo | Responsabilidade |
|---|---|
| `core-domain` | Domínio e regras compartilhadas. |
| `sync-module` | Lógica de sincronização. |
| `shared-infra` | Infraestrutura técnica compartilhada. |
| `local-app` | Ponto de entrada do ambiente local. |
| `online-app` | Ponto de entrada do ambiente online. |

### 9.3 DTOs e entidades

- Request/response DTOs pertencem às camadas de entrada (web).
- Entidades JPA são usadas para persistência.
- Objetos de domínio contêm a lógica de negócio pura.
- MapStruct deve mapear entre esses diferentes modelos.

## 10. Arquitetura do front-end

O front-end Angular deve seguir arquitetura por features com camadas internas. A aplicação pode começar como um único app Angular, mas cada área funcional deve ficar isolada por feature.

### 10.1 Estrutura por feature

```text
src/app/
├── core/
│   ├── config/         # environment, tokens e configuração global
│   ├── http/           # interceptors, erro HTTP e clients base
│   ├── auth/           # sessão, guards e autorização global
│   └── layout/         # shell e navegação global
├── shared/
│   ├── ui/             # componentes visuais reutilizáveis
│   ├── pipes/
│   └── testing/
└── features/
    └── <feature>/
        ├── domain/      # modelos TS e tipos
        ├── application/ # facades e serviços de tela
        ├── data-access/ # services HTTP e DTOs
        ├── ui/          # componentes presentacionais
        └── pages/       # componentes roteáveis
```

## 11. Estrutura do repositório

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
│   ├── src/app/
│
├── infra/
│   ├── local/
│   └── online/
│
└── docs/
```

## 12. Comunicação local ↔ online

A comunicação será feita via API HTTPS segura.

Não será permitido expor diretamente:

- PostgreSQL
- RabbitMQ
- Redis

## 13. Comunicação em tempo real

O KDS usará WebSocket para receber pedidos em tempo real no ambiente local.

## 14. Banco de dados

Haverá dois bancos:

```text
PostgreSQL Local
PostgreSQL Online
```

## 15. Fonte de verdade por domínio

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

## 16. Segurança

### Local

- Acesso apenas na rede interna
- Banco não exposto publicamente
- Login por operador
- Permissões por perfil
- Logs de auditoria

### Online

- HTTPS obrigatório
- Firewall
- Banco não público
- Validação de webhooks
- Rate limit
- Backup

## 17. Decisões finais

- Sistema híbrido local + online.
- Operação crítica fica local.
- Nuvem atende vendas externas e gestão remota.
- Comunicação entre ambientes via HTTPS.
- PostgreSQL como fonte principal.
- Redis como apoio.
- Docker Compose para local e online.
- Monólito modular.
- Front-end Angular organizado por features.
- KDS com WebSocket.
- Impressão local.
