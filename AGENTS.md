# AGENTS.md — Sistema MNSS / Nova Aliança

## 1. Identidade do projeto

Este repositório implementa o **Sistema MNSS / Nova Aliança**, uma plataforma integrada para a Padaria e Lanchonete Nova Aliança.

O sistema será híbrido:

```text
Ambiente local
+
Ambiente online
```

O objetivo é atender:

- site institucional;
- cardápio online;
- pedidos online;
- PDV;
- frente de caixa;
- KDS;
- WhatsApp;
- chatbot;
- pagamentos online;
- controle de disponibilidade de produtos;
- sincronização local ↔ online;
- relatórios operacionais;
- operação presencial mesmo sem internet.

## 2. Regra central obrigatória

```text
A loja deve continuar vendendo presencialmente mesmo sem internet.
```

Consequências obrigatórias:

- PDV deve funcionar localmente.
- Caixa deve funcionar localmente.
- KDS deve funcionar localmente.
- Impressão deve funcionar localmente.
- Gaveta de dinheiro deve funcionar localmente.
- Banco local deve armazenar vendas, pedidos e eventos pendentes.
- A sincronização com a nuvem deve ser assíncrona.
- Pedidos online só chegam à loja quando houver internet.
- Nenhuma venda presencial pode depender da VPS, do site, do WhatsApp ou de serviço externo.

## 3. Stack oficial do projeto

### Back-end

- Java 21
- Spring Boot 3.x
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

### Front-end

- Angular
- TypeScript
- RxJS
- SCSS customizado (Angular Material ou PrimeNG podem ser adotados incrementalmente se necessário)
- SCSS ou Tailwind
- PWA quando aplicável
- WebSocket client para KDS e atualizações em tempo real

### Infraestrutura

- Docker
- Docker Compose
- Nginx
- Ubuntu Server
- Hostinger VPS para ambiente online
- Servidor local na padaria para operação crítica
- PostgreSQL local e online
- RabbitMQ local e online
- Redis local e online
- Certbot / Let's Encrypt no ambiente online

## 4. Arquitetura obrigatória

A arquitetura inicial deve ser:

```text
Monólito modular local
+
Monólito modular online
+
Módulo de sincronização
```

Regras:

- Não criar microserviços no início.
- Não separar serviços antes de necessidade real de escala ou isolamento.
- Organizar o monólito modular em pacote plano por domínio: `XController`, `XService`, `XEntity`, `XRepository`, `XRequest`/`XResponse` no mesmo pacote. Sub-pacotes só quando o domínio ultrapassar ~10 arquivos.
- Manter módulos coesos por domínio.
- Evitar acoplamento direto entre front-end e regras críticas de negócio.
- Toda regra crítica deve ficar no back-end.

## 5. Estrutura esperada do repositório

```text
nova-alianca-system/
├── back-end/
│   ├── core-domain/
│   ├── local-app/
│   ├── online-app/
│   ├── sync-module/
│   └── shared-infra/
├── front-end/
│   └── src/app/features/
│       ├── site-publico/
│       ├── admin/
│       ├── pdv/
│       └── kds/
├── infra/
│   ├── local/
│   └── online/
├── docs/
└── AGENTS.md
```

## 6. Documentação obrigatória de referência

Antes de implementar qualquer história, leia a documentação relacionada ao escopo:

| Código | Documento | Quando usar |
|---|---|---|
| README | `docs/README.md` | Visão geral, módulos e ordem de leitura |
| ARQ | `docs/arquitetura.md` | Decisões arquiteturais local + online |
| DOM | `docs/modelo-de-dominio.md` | Entidades, agregados, enums e regras de negócio |
| BD | `docs/banco-de-dados.md` | Schema, Flyway, UUID, índices e convenções |
| SYNC | `docs/sincronizacao.md` | Outbox, inbox, retry, HMAC e idempotência |
| PDV | `docs/pdv.md` | Frente de caixa, venda presencial, pagamento e impressão |
| KDS | `docs/kds.md` | Tickets, setores, preparo e WebSocket |
| FLUXOS | `docs/fluxos-e-casos-de-uso.md` | Fluxos funcionais, casos de uso e critérios de aceite |
| DL | `docs/deploy-local.md` | Docker Compose local, Nginx e servidor da padaria |
| DO | `docs/deploy-online.md` | VPS, HTTPS, Nginx, webhooks e backup online |
| HW | `docs/hardware.md` | Servidor, PDV, KDS, impressoras, rede e nobreak |
| ROADMAP | `docs/roadmap-sprints-sistema-mnss.md` | Sprints, histórias e critérios por entrega |

## 7. Modo de trabalho obrigatório no Codex

Implemente sempre **uma história por vez**.

Fluxo esperado:

```bash
git checkout -b feature/SXX-HYY-descricao-curta
codex
```

Dentro do Codex, implementar somente a história solicitada.

Regras:

1. Ler `AGENTS.md` antes de alterar código.
2. Ler `docs/roadmap-sprints-sistema-mnss.md`.
3. Ler os documentos citados na história.
4. Confirmar mentalmente o módulo alvo: local, online, sync, domínio compartilhado ou front-end.
5. Não implementar histórias futuras.
6. Não refatorar fora do escopo necessário.
7. Não remover testes existentes para fazer a suíte passar.
8. Não esconder limitação, pendência ou teste não executado.
9. Entregar código funcional e testável.
10. Informar arquivos alterados, testes executados e pendências.

## 8. Regras globais de implementação

### 8.1 Back-end

- Toda regra crítica deve ficar no back-end.
- Front-end pode validar para UX, mas não substitui validação server-side.
- Usar `BigDecimal` para valores monetários.
- Usar UUID para IDs principais.
- Usar `Instant`, `LocalDateTime` ou tipos equivalentes com consistência definida no projeto.
- Usar DTOs explícitos para entrada e saída de APIs.
- Não expor entidades JPA diretamente em controllers.
- Controllers não devem conter regra de negócio.
- Services concentram regra de aplicação e negócio.
- Entities representam persistência e estado de domínio.
- Repositories ficam isolados por módulo.
- DTOs não devem expor entidades diretamente.
- Criar testes unitários para regras de negócio.
- Criar testes de integração quando houver persistência relevante.

### 8.2 Front-end

- Manter models TypeScript alinhados aos DTOs do back-end.
- Usar services HTTP para comunicação com APIs.
- Usar guards/interceptors quando aplicável.
- Não colocar regra crítica apenas no Angular.
- Validar formulário no front-end apenas como apoio à experiência do usuário.
- Exibir erros padronizados recebidos do back-end.
- Criar testes para services, guards, interceptors e componentes alterados.

### 8.3 Banco de dados

- Usar PostgreSQL.
- Usar Flyway para versionamento de schema.
- Usar `snake_case` para tabelas e colunas.
- Usar UUID em tabelas principais.
- Usar `NUMERIC(12,2)` para valores monetários.
- Entidades principais devem possuir `created_at` e `updated_at`.
- Registros críticos devem evitar exclusão física.
- Criar índices para buscas frequentes.
- Não alterar migrations já aplicadas em ambiente compartilhado; criar nova migration.

### 8.4 Sincronização

A sincronização deve seguir o padrão:

```text
Outbox + Inbox + Retry
```

Regras:

- Eventos devem ser idempotentes.
- Webhooks devem ser idempotentes.
- Integrações devem ser idempotentes.
- Todo evento deve ter UUID.
- O destino deve registrar evento recebido antes ou durante o processamento.
- Reenvio do mesmo evento não pode duplicar venda, pedido, pagamento ou movimentação.
- Falhas devem gerar retry controlado.
- Falhas permanentes devem aparecer em painel operacional.
- A comunicação local ↔ online deve ser feita por HTTPS.
- Não expor PostgreSQL, RabbitMQ ou Redis diretamente na internet.

### 8.5 Segurança

- Nunca persistir senha em texto puro.
- Usar hash forte para senhas.
- Usuário inativo não autentica.
- Ações críticas exigem perfil/permissão adequada.
- Cancelamento, desconto acima do limite, sangria e ajustes críticos podem exigir gerente.
- Não armazenar secrets no repositório.
- Não colocar tokens, chaves, senhas, JWT secrets ou store secrets em arquivos versionados.
- Configurações sensíveis devem vir de variáveis de ambiente.
- Validar permissões no back-end, não apenas no front-end.

### 8.6 Infraestrutura

- PostgreSQL, RabbitMQ e Redis não devem ficar públicos.
- Ambiente local deve operar na rede da padaria.
- Servidor local deve ter IP fixo.
- Nginx local deve expor somente o necessário.
- Ambiente online deve usar HTTPS.
- Webhooks online devem validar assinatura ou segredo.
- Backups devem ser automatizados quando a história tratar de deploy/infra.
- Health checks devem refletir o estado real das dependências.

## 9. Domínios principais

Domínios previstos:

```text
Product
Category
Availability
Customer
Order
OrderItem
Payment
Cash
CashMovement
KDS
Stock
Sync
Security
Audit
Notification
Admin
Observability
```

## 10. Regras específicas de PDV

- PDV deve acessar a API local.
- PDV não pode depender da nuvem.
- Não vender sem caixa aberto, se essa configuração estiver ativa.
- Não finalizar venda sem itens.
- Não finalizar venda sem pagamento válido.
- Pagamento deve bater com o total da venda.
- Pagamento misto deve salvar cada forma separadamente.
- Produto inativo não pode ser vendido.
- Produto indisponível deve ser bloqueado conforme canal/regra.
- Venda finalizada deve gerar movimentação de caixa.
- Venda com item de preparo deve gerar ticket no KDS.
- Venda finalizada deve gerar evento de sincronização.
- Cancelamento deve exigir motivo, permissão e auditoria quando previsto.

## 11. Regras específicas de KDS

- KDS deve funcionar localmente.
- KDS não pode depender da internet.
- Itens com setor de preparo devem gerar ticket.
- Itens `SEM_PREPARO` não devem gerar ticket.
- Itens de setores diferentes devem gerar tickets diferentes.
- WebSocket deve notificar o KDS sem quebrar a transação principal.
- Pedido só fica pronto quando todos os tickets obrigatórios estiverem prontos.
- Cancelamento de item/ticket deve registrar motivo e permissão quando previsto.

## 12. Regras específicas de pagamentos

- Usar `BigDecimal` no Java.
- Usar `NUMERIC(12,2)` no PostgreSQL.
- Pagamento presencial pode ser confirmado manualmente pelo operador.
- Pagamento online depende de confirmação do gateway/webhook.
- Pagamento online não deve ser confirmado apenas pelo front-end.
- Pagamento cancelado ou estornado deve gerar auditoria.
- Pagamento em dinheiro pode acionar gaveta conforme regra do PDV.

## 13. Padrão de API

- Controllers devem receber DTOs de request.
- Controllers devem retornar DTOs de response.
- Validações de entrada devem usar Bean Validation quando aplicável.
- Erros devem seguir resposta padronizada.
- Endpoints novos devem atualizar OpenAPI/contratos quando o projeto já possuir essa estrutura.
- Não expor detalhes internos de implementação em payload público.

## 14. Padrão de testes

Para cada história, criar ou ajustar testes aplicáveis:

### Back-end

- Testes unitários de services/use cases.
- Testes de controller quando houver endpoint novo.
- Testes de repository ou integração quando houver persistência relevante.
- Testes negativos para validações críticas.
- Testes de idempotência quando houver sync, webhook ou integração.

### Front-end

- Testes de services HTTP.
- Testes de componentes alterados.
- Testes de guards/interceptors quando aplicável.
- Testes de validação de formulário.
- Testes de renderização de estado de erro/sucesso quando aplicável.

## 15. Comandos de verificação

Ajustar os comandos conforme a estrutura real do repositório.

### Back-end

```bash
cd back-end
./gradlew test
./gradlew check
```

### Front-end

```bash
cd front-end
npm install
npm test
npm run build
```

Se o projeto usar Gradle em algum módulo, usar o wrapper do módulo:

```bash
./gradlew test
./gradlew build
```

## 16. Formato obrigatório da resposta final do Codex

Ao finalizar uma história, responder com:

```text
Resumo da implementação
- ...

Arquivos alterados
- caminho/arquivo
- caminho/arquivo

Testes executados
- comando executado: resultado
- comando executado: resultado

Pendências
- Nenhuma
```

Se algum teste não foi executado, informar explicitamente:

```text
Testes não executados
- comando: motivo
```

## 17. Limites de escopo

Não implementar sem solicitação explícita:

- NFC-e.
- Pinpad integrado.
- Balança integrada.
- Programa de fidelidade.
- Multi-PDV avançado.
- Microserviços.
- Kubernetes.
- Multi-tenant avançado.
- Integração real com gateway de pagamento antes da história correspondente.
- Integração real com WhatsApp antes da história correspondente.

## 18. Cuidados com secrets

Nunca criar, imprimir, commitar ou sugerir valor real para:

- `POSTGRES_PASSWORD`
- `RABBITMQ_DEFAULT_PASS`
- `JWT_SECRET`
- `SYNC_MASTER_SECRET`
- `STORE_SECRET`
- tokens de gateway de pagamento
- tokens de WhatsApp
- chaves SSH
- credenciais de VPS

Usar placeholders seguros:

```text
change_me
replace_me
use_environment_variable
```

## 19. Prioridade do MVP

O MVP deve priorizar:

1. Fundação técnica.
2. Infra local/online mínima.
3. Segurança e usuários.
4. Banco e domínio base.
5. Catálogo e disponibilidade.
6. Caixa.
7. PDV.
8. Pagamentos presenciais.
9. Impressão e gaveta.
10. KDS.
11. Sincronização.
12. Site e pedido online.
13. Pagamento online e webhooks.
14. Painel operacional.
15. Deploy e homologação.

A operação local vem antes dos canais online.
