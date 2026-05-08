# Segurança HMAC dos endpoints técnicos de sincronização

Documento operacional para o estado atual dos endpoints técnicos de sync.

## Regra atual

Os endpoints técnicos abaixo ficam `permitAll` no Spring Security para não exigir JWT da loja local:

- `POST /api/sync/events`
- `GET /api/sync/pending`
- `POST /api/sync/events/{id}/ack`

Essa liberação não torna os endpoints públicos funcionalmente. O controller valida obrigatoriamente:

- `X-Store-ID`
- `X-Signature`
- `X-Idempotency-Key` quando aplicável ao envio de evento
- segredo HMAC configurado por loja
- propriedade `payload.storeId` contra `X-Store-ID`, quando enviada

Requisições sem HMAC válido devem ser rejeitadas antes de qualquer gravação ou retorno de eventos.

## Migração futura

Uma migração futura pode mover essa validação do controller para um filtro dedicado. Essa mudança deve preservar os testes negativos atuais:

- envio de evento sem assinatura;
- loja desconhecida;
- assinatura inválida no envio;
- assinatura inválida no pull;
- assinatura inválida no ACK;
- `payload.storeId` divergente do header.

Não exigir JWT nesses endpoints técnicos sem uma estratégia compatível com a operação local offline e com o worker de sincronização.
