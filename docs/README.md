# Sistema Nova Aliança — Documentação Técnica

Este diretório contém a documentação inicial do projeto **Sistema Nova Aliança**, sistema híbrido **local + online** para a Padaria e Lanchonete Nova Aliança.

## Objetivo do projeto

Criar uma plataforma integrada para:

- Site institucional
- Cardápio online
- Pedidos online
- PDV
- Frente de caixa
- KDS
- WhatsApp
- Chatbot
- Pagamentos online
- Controle de disponibilidade de produtos
- Sincronização local ↔ online

## Decisão central

O sistema será híbrido:

```text
Ambiente local:
- PDV
- Caixa
- KDS
- Impressão
- Banco local
- Operação offline

Ambiente online:
- Site
- Pedidos online
- WhatsApp
- Pagamentos
- Relatórios remotos
- Sincronização
```

A regra mais importante:

> A loja deve continuar vendendo presencialmente mesmo sem internet.

## Stack principal

- Java 25
- Spring Boot
- PostgreSQL
- RabbitMQ
- Redis
- Angular
- TypeScript
- Docker
- Docker Compose
- Nginx
- Hostinger VPS
- Ubuntu Server

## Documentos

| Documento | Finalidade |
|---|---|
| `arquitetura.md` | Visão arquitetural geral do sistema local + online |
| `modelo-de-dominio.md` | Domínios, entidades, agregados e regras de negócio |
| `banco-de-dados.md` | Modelo inicial de banco, tabelas e convenções |
| `sincronizacao.md` | Estratégia de sincronização entre loja e nuvem |
| `pdv.md` | Especificação funcional e técnica do PDV |
| `kds.md` | Especificação funcional e técnica do KDS |
| `hardware.md` | Estrutura física necessária para loja, PDV, KDS e rede |
| `deploy-local.md` | Deploy do ambiente local dentro da padaria |
| `deploy-online.md` | Deploy do ambiente online na VPS Hostinger |
| `roadmap-sprints.md` | Organização do desenvolvimento por fases e sprints |

## Ordem recomendada de leitura

1. `arquitetura.md`
2. `modelo-de-dominio.md`
3. `banco-de-dados.md`
4. `sincronizacao.md`
5. `pdv.md`
6. `kds.md`
7. `hardware.md`
8. `deploy-local.md`
9. `deploy-online.md`
10. `roadmap-sprints.md`

## Nome técnico sugerido

```text
nova-alianca-system
```

## Módulos sugeridos

```text
nova-alianca-local-api
nova-alianca-online-api
nova-alianca-sync
nova-alianca-admin
nova-alianca-site
nova-alianca-pdv
nova-alianca-kds
```
