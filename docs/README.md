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

- Java 21
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
| `arquitetura.md` | Visão arquitetural geral, organização modular do back-end e arquitetura por features do front-end |
| `modelo-de-dominio.md` | Domínios, entidades, agregados e regras de negócio |
| `banco-de-dados.md` | Modelo inicial de banco, tabelas e convenções |
| `sincronizacao.md` | Estratégia de sincronização entre loja e nuvem |
| `pdv.md` | Especificação funcional e técnica do PDV |
| `kds.md` | Especificação funcional e técnica do KDS |
| `hardware.md` | Estrutura física necessária para loja, PDV, KDS e rede |
| `deploy-local.md` | Deploy do ambiente local dentro da padaria |
| `deploy-online.md` | Deploy do ambiente online na VPS Hostinger |
| `roadmap-sprints-sistema-mnss.md` | Organização do desenvolvimento por fases e sprints |

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
10. `roadmap-sprints-sistema-mnss.md`

## Padrão obrigatório de implementação

Back-end:

- Organização por domínio com pacote plano: `XController`, `XService`, `XEntity`, `XRepository`, `XRequest` e `XResponse`.
- Controllers recebem requests e delegam; não concentram regra de negócio.
- Services concentram a lógica de aplicação e negócio.
- Entities representam persistência/estado e não devem ser expostas diretamente na API.
- Repositories ficam isolados por módulo/domínio.

Front-end:

```text
features/<feature>/
  domain/       # modelos TypeScript e interfaces
  data-access/  # services HTTP e DTOs
  components/   # componentes presentacionais (opcional)
  pages/        # componentes roteáveis
```

- `domain` concentra modelos TypeScript, interfaces e tipos da feature.
- `data-access` concentra services HTTP, DTOs e acesso à API.
- `components` é opcional; use quando há componentes presentacionais reutilizáveis dentro da feature.
- `pages` contem componentes roteáveis e composição de tela.
- `core` guarda infraestrutura global; `shared` guarda peças reutilizáveis sem depender de features.

### Decisão atual do front-end

O front-end atual deve ser mantido como **um único app Angular** (`front-end/`) com isolamento por feature/layer.

```text
front-end/src/app/features/
  admin/
  site-publico/
  pdv/
  kds/
```

Cada área funcional continua separada por rotas, guards e módulos internos, sem dividir o workspace em múltiplos apps Angular neste estágio.

## Nome técnico sugerido

```text
nova-alianca-system
```

## Módulos sugeridos

```text
nova-alianca-local-api
nova-alianca-online-api
nova-alianca-sync
nova-alianca-front-end-unificado (features: admin, site, pdv e kds)
```
