# Backlog — Home Page / Landing Page
## Padaria e Lanchonete Nova Aliança

**Projeto:** Sistema MNSS / Nova Aliança  
**Contexto:** site público da Padaria e Lanchonete Nova Aliança  
**Objetivo:** transformar a home em uma landing page comercial focada em cardápio, WhatsApp, encomendas, delivery e localização.  
**Público-alvo:** clientes da região de Parque Veneza, Magé, clientes de delivery, clientes de balcão e clientes interessados em encomendas.

---

## 1. Objetivo da evolução

A home deve deixar claro, nos primeiros segundos:

1. O que a padaria vende.
2. Onde fica.
3. Horário de funcionamento.
4. Como chamar no WhatsApp.
5. Como acessar o cardápio.
6. Como pedir delivery.
7. Como fazer encomendas.
8. Como chegar pelo Google Maps.

A página deve funcionar como uma landing page de conversão, não apenas como uma tela institucional.

---

## 2. Estado atual da home

A home atual possui:

- Header com nome da padaria.
- Link para cardápio.
- Link para login.
- Hero com título e descrição.
- Botão principal para cardápio.
- Cards de informação:
  - Onde estamos.
  - Horário de funcionamento.
  - Contato.

### Limitações atuais

- O visual ainda parece genérico e pouco conectado ao universo de padaria.
- O WhatsApp não aparece como chamada principal no hero.
- O login aparece no topo, competindo com ações comerciais.
- Não há destaque para produtos mais pedidos.
- Não há bloco de delivery.
- Não há bloco de encomendas.
- Não há rodapé comercial completo.
- Não há botão flutuante de WhatsApp.
- Não há seção explicando como comprar.
- Não há seção de diferenciais da padaria.

---

## 3. Diretriz de UX e conversão

A home deve seguir a lógica:

```text
Atenção → Desejo → Confiança → Ação
```

### Atenção

Usar uma chamada forte no topo:

```text
Pães quentinhos, lanches e bolos frescos todos os dias
```

### Desejo

Mostrar produtos de interesse imediato:

- Pão francês.
- Misto quente.
- Pão com queijo.
- Café.
- Salgados.
- Bolos e tortas.
- Rocamboles.
- Kits de café.

### Confiança

Mostrar dados reais:

- Nome da padaria.
- Endereço real.
- Horário real.
- Telefone/WhatsApp real.
- Presença em Parque Veneza, Magé.

### Ação

Levar o cliente para:

- Ver cardápio.
- Chamar no WhatsApp.
- Fazer encomenda.
- Abrir rota no Google Maps.

---

## 4. Estrutura ideal da home

```text
1. Header comercial
2. Hero principal com CTA para cardápio e WhatsApp
3. Cards rápidos: localização, horário e WhatsApp
4. Seção “Mais pedidos”
5. Seção “Delivery”
6. Seção “Encomendas”
7. Seção “Diferenciais”
8. Seção “Como fazer seu pedido”
9. Localização detalhada
10. Rodapé comercial
11. Botão flutuante de WhatsApp
```

---

# 5. Backlog detalhado

---

# EPIC HOME-01 — Navegação comercial

## Objetivo

Ajustar o topo da home para ser orientado ao cliente final e não à administração do sistema.

---

## HOME-01.01 — Ajustar menu principal

**Prioridade:** Alta  
**Tipo:** Front-end  
**Valor de negócio:** Reduzir distrações e guiar o cliente para ações comerciais.

### Descrição

Alterar o header para priorizar ações úteis ao cliente.

### Estado atual

```text
Ver Cardápio | Login
```

### Estado desejado

```text
Cardápio | Encomendas | WhatsApp | Como chegar
```

O login deve sair do topo principal e ir para o rodapé como:

```text
Área interna
```

### Tarefas técnicas

- Alterar template do `site-home.component.ts`.
- Criar links para âncoras internas:
  - `#cardapio`
  - `#encomendas`
  - `#contato`
  - `#localizacao`
- Mover link `/login` para o rodapé.
- Ajustar estilos do header.
- Garantir responsividade no mobile.

### Critérios de aceite

- O header não deve destacar login.
- O cliente deve ver rapidamente cardápio e WhatsApp.
- O layout não deve quebrar em telas pequenas.
- O link de área interna deve continuar acessível no rodapé.

---

## HOME-01.02 — Adicionar botão de WhatsApp no header

**Prioridade:** Alta  
**Tipo:** Front-end  
**Valor de negócio:** Aumentar pedidos diretos pelo WhatsApp.

### Descrição

Adicionar botão direto para WhatsApp no header.

### Texto sugerido

```text
Pedir pelo WhatsApp
```

### Tarefas técnicas

- Criar getter `whatsappUrl`.
- Remover caracteres não numéricos do telefone vindo da API.
- Garantir prefixo `55`.
- Criar mensagem inicial.
- Aplicar `target="_blank"`.
- Aplicar `rel="noopener noreferrer"`.

### Mensagem inicial sugerida

```text
Olá! Vim pelo site da Padaria Nova Aliança e gostaria de fazer um pedido.
```

### Critérios de aceite

- Clique abre WhatsApp Web no desktop.
- Clique abre aplicativo no celular.
- Número deve estar no formato internacional.
- Mensagem inicial deve aparecer preenchida.

---

## HOME-01.03 — Ajustar header no mobile

**Prioridade:** Média  
**Tipo:** Front-end  
**Valor de negócio:** Melhorar experiência de clientes no celular.

### Descrição

O header deve funcionar bem em telas pequenas.

### Opção MVP

Manter visíveis apenas:

```text
Cardápio | WhatsApp
```

### Opção futura

Criar menu hambúrguer.

### Tarefas técnicas

- Criar media query para telas até 768px.
- Reduzir espaçamento entre links.
- Ajustar tamanho do nome da padaria.
- Evitar quebra desorganizada.

### Critérios de aceite

- Sem scroll horizontal.
- Links principais continuam acessíveis.
- Nome da padaria não quebra de forma ruim.

---

# EPIC HOME-02 — Hero principal

## Objetivo

Transformar o primeiro bloco da página em uma chamada comercial forte.

---

## HOME-02.01 — Reescrever título e subtítulo

**Prioridade:** Alta  
**Tipo:** Conteúdo + Front-end

### Título sugerido

```text
Pães quentinhos, lanches e bolos frescos todos os dias
```

### Subtítulo sugerido

```text
A Padaria e Lanchonete Nova Aliança prepara seu café, lanche e encomendas com qualidade e carinho em Parque Veneza, Magé.
```

### Tarefas técnicas

- Alterar o `<h1>` do hero.
- Alterar o subtítulo.
- Manter fallback caso `storeInfo.description` venha vazio.
- Avaliar se a descrição da API deve ser usada no hero ou em seção institucional.

### Critérios de aceite

- O cliente entende rapidamente o que a padaria oferece.
- O texto não parece genérico.
- O texto funciona em desktop e mobile.

---

## HOME-02.02 — Adicionar CTA de WhatsApp no hero

**Prioridade:** Alta  
**Tipo:** Front-end

### Descrição

O hero deve ter dois botões principais:

```text
Ver Cardápio
Pedir pelo WhatsApp
```

### Tarefas técnicas

- Adicionar segundo botão no `cta-group`.
- Reutilizar `whatsappUrl`.
- Criar estilo de botão secundário.
- Ajustar espaçamento entre botões.

### Critérios de aceite

- Botão “Ver Cardápio” continua funcionando.
- Botão “Pedir pelo WhatsApp” abre conversa.
- Em mobile, os botões ficam empilhados ou bem ajustados.

---

## HOME-02.03 — Adicionar CTA “Como chegar”

**Prioridade:** Média  
**Tipo:** Front-end

### Descrição

Adicionar uma ação para abrir Google Maps.

### Texto sugerido

```text
Como chegar
```

### Tarefas técnicas

- Criar link usando `googleMapsUrl`.
- Abrir em nova aba.
- Usar `rel="noopener noreferrer"`.
- Criar estilo visual menos chamativo que WhatsApp e Cardápio.

### Critérios de aceite

- Link abre Google Maps.
- Não compete visualmente com os botões principais.

---

## HOME-02.04 — Alterar paleta visual para padaria

**Prioridade:** Alta  
**Tipo:** UI/UX

### Descrição

Substituir aparência fria/corporativa por cores mais quentes.

### Paleta sugerida

```text
Creme:        #fff7e8
Dourado:      #f5a623
Marrom:       #6b3f1d
Marrom claro: #9c6b3f
Laranja:      #e67e22
Branco:       #ffffff
Texto:        #3d2b1f
```

### Tarefas técnicas

- Ajustar background do hero.
- Ajustar cor dos botões.
- Ajustar contraste dos textos.
- Revisar hover e focus.

### Critérios de aceite

- A página deve parecer de padaria.
- Textos devem continuar legíveis.
- Botões devem ter contraste adequado.

---

## HOME-02.05 — Preparar hero com imagem de produtos

**Prioridade:** Média  
**Tipo:** Front-end + Conteúdo

### Layout desktop

```text
[Texto + botões] [Imagem de pães/lanches/bolos]
```

### Layout mobile

```text
[Imagem]
[Texto]
[Botões]
```

### Tarefas técnicas

- Criar `hero-layout`.
- Criar `hero-text`.
- Criar `hero-image`.
- Usar imagem real ou placeholder temporário.
- Adicionar `alt` descritivo.

### Critérios de aceite

- Layout aceita imagem sem quebrar.
- Imagem não pesa excessivamente.
- Mobile mantém boa leitura.

---

# EPIC HOME-03 — Cards rápidos

## Objetivo

Transformar os cards de informação em ações clicáveis.

---

## HOME-03.01 — Card de localização com Google Maps

**Prioridade:** Alta  
**Tipo:** Front-end

### Endereço

```text
Estr. Mineira, 703 - Parque Veneza, Magé - RJ, 25930-790
```

### Tarefas técnicas

- Trocar `<div class="info-card">` por `<a>`.
- Criar getter `googleMapsUrl`.
- Usar endereço vindo de `storeInfo.address` quando possível.
- Abrir em nova aba.
- Adicionar texto “Abrir no Google Maps”.
- Adicionar `aria-label`.

### Critérios de aceite

- Card inteiro é clicável.
- Google Maps abre com o endereço real.
- O visual do card permanece.
- Link externo usa `rel="noopener noreferrer"`.

---

## HOME-03.02 — Card de contato com WhatsApp

**Prioridade:** Alta  
**Tipo:** Front-end

### Descrição

O card de contato deve abrir conversa no WhatsApp.

### Tarefas técnicas

- Trocar `<div class="info-card">` por `<a>`.
- Usar `whatsappUrl`.
- Adicionar texto “Chamar no WhatsApp”.
- Usar `target="_blank"`.
- Adicionar `aria-label`.

### Critérios de aceite

- Card inteiro é clicável.
- WhatsApp abre com mensagem inicial.
- Número vem de `storeInfo.phone`.
- Funciona em celular e desktop.

---

## HOME-03.03 — Melhorar card de horário

**Prioridade:** Média  
**Tipo:** Front-end + Conteúdo

### Conteúdo recomendado

```text
Segunda a Domingo
05:30 às 21:00
```

### Tarefas técnicas

- Melhorar apresentação visual do horário.
- Destacar horário principal.
- Manter compatibilidade com `storeInfo.hours`.

### Critérios de aceite

- Horário deve ser lido rapidamente.
- Layout não quebra em mobile.

---

## HOME-03.04 — Indicar “Aberto agora”

**Prioridade:** Baixa no MVP  
**Tipo:** Front-end + regra de horário

### Descrição

Mostrar status calculado:

```text
Aberto agora
Fecha às 21:00
```

### Tarefas técnicas futuras

- Criar configuração estruturada de horário.
- Calcular status pelo horário local.
- Exibir selo no card.

### Critérios de aceite futuros

- Status deve estar correto por dia da semana.
- Deve considerar feriados quando o sistema evoluir.

---

# EPIC HOME-04 — Mais pedidos

## Objetivo

Mostrar produtos de desejo e levar o cliente para o cardápio ou WhatsApp.

---

## HOME-04.01 — Criar seção “Mais pedidos da Nova Aliança”

**Prioridade:** Alta  
**Tipo:** Front-end

### Título

```text
Mais pedidos da Nova Aliança
```

### Subtítulo

```text
Escolha seu café, lanche ou doce preferido e peça pelo cardápio ou WhatsApp.
```

### Produtos iniciais sugeridos

- Pão francês.
- Misto quente.
- Pão com queijo.
- Café.
- Salgados.
- Bolos e tortas.

### Tarefas técnicas

- Criar seção `featured-products-section`.
- Criar array `featuredProducts`.
- Renderizar cards com `*ngFor`.
- Adicionar botão “Ver no cardápio”.
- Adicionar botão opcional “Pedir pelo WhatsApp”.

### Critérios de aceite

- Deve exibir pelo menos 6 cards.
- Cards devem ser responsivos.
- Visual deve ser coerente com padaria.
- Nenhum produto deve usar texto genérico.

---

## HOME-04.02 — Criar modelo `FeaturedProduct`

**Prioridade:** Média  
**Tipo:** Front-end

### Interface sugerida

```ts
interface FeaturedProduct {
  name: string;
  description: string;
  emoji: string;
  category: string;
  ctaLabel: string;
}
```

### Critérios de aceite

- Lista deve ser tipada.
- Código deve ficar simples de manter.
- Conteúdo deve ser fácil de alterar.

---

## HOME-04.03 — Preparar integração futura com produtos reais

**Prioridade:** Média/futura  
**Tipo:** Back-end + Front-end

### Descrição

No futuro, os produtos em destaque devem vir do back-end.

### Tarefas futuras

- Adicionar campo `featured` no produto.
- Adicionar campo `featuredOrder`.
- Criar endpoint público para produtos em destaque.
- Permitir configuração no admin.

### Critérios futuros

- Home não deve precisar de hardcode.
- Admin controla produtos destacados.

---

# EPIC HOME-05 — Delivery

## Objetivo

Comunicar que a padaria aceita pedidos por WhatsApp e pode entregar conforme disponibilidade.

---

## HOME-05.01 — Criar bloco de delivery

**Prioridade:** Alta  
**Tipo:** Front-end + Conteúdo

### Título

```text
Delivery todos os dias
```

### Texto

```text
Receba pães, lanches, bolos, salgados e bebidas no conforto da sua casa. Chame no WhatsApp e consulte a disponibilidade para sua região.
```

### CTA

```text
Pedir pelo WhatsApp
```

### Tarefas técnicas

- Criar seção `delivery-section`.
- Adicionar texto comercial.
- Adicionar botão para `whatsappUrl`.
- Criar destaque visual.

### Critérios de aceite

- CTA abre WhatsApp.
- Texto não promete entrega sem confirmar região.
- Seção deve ficar antes das encomendas ou logo após mais pedidos.

---

## HOME-05.02 — Aviso de taxa e região

**Prioridade:** Média  
**Tipo:** Conteúdo

### Texto sugerido

```text
Consulte taxa de entrega e disponibilidade para o seu endereço.
```

### Critérios de aceite

- Cliente entende que a taxa pode variar.
- Texto evita promessa indevida.

---

# EPIC HOME-06 — Encomendas

## Objetivo

Destacar produtos de maior ticket médio.

---

## HOME-06.01 — Criar seção “Encomendas”

**Prioridade:** Alta  
**Tipo:** Front-end + Conteúdo

### Título

```text
Encomendas para festas, cafés e momentos especiais
```

### Texto

```text
Faça sua encomenda de tortas, bolos, salgados, rocamboles e kits de café com antecedência pelo WhatsApp.
```

### Tarefas técnicas

- Criar seção `orders-section`.
- Criar âncora `#encomendas`.
- Criar CTA específico para encomenda.
- Criar cards de categorias.

### Critérios de aceite

- Cliente entende que pode fazer encomenda.
- CTA abre WhatsApp com mensagem específica.
- Seção funciona em mobile.

---

## HOME-06.02 — Categorias de encomenda

**Prioridade:** Alta  
**Tipo:** Front-end

### Categorias sugeridas

- Tortas.
- Bolos.
- Rocamboles.
- Salgados.
- Kits de café da manhã.
- Doces.

### Tarefas técnicas

- Criar array `orderCategories`.
- Renderizar cards.
- Usar ícones ou emojis.
- Criar descrições curtas.

### Critérios de aceite

- Pelo menos 5 categorias aparecem.
- Cards devem ficar equilibrados visualmente.

---

## HOME-06.03 — Link de WhatsApp para encomendas

**Prioridade:** Média  
**Tipo:** Front-end

### Mensagem

```text
Olá! Vim pelo site da Padaria Nova Aliança e gostaria de fazer uma encomenda.
```

### Tarefas técnicas

- Criar getter `whatsappOrderUrl`.
- Reutilizar lógica de telefone.
- Alterar apenas a mensagem.

### Critérios de aceite

- O botão de encomenda abre WhatsApp.
- A mensagem deve indicar encomenda.

---

# EPIC HOME-07 — Diferenciais

## Objetivo

Criar confiança e reforçar motivos para comprar na Nova Aliança.

---

## HOME-07.01 — Criar seção “Por que escolher a Nova Aliança?”

**Prioridade:** Média  
**Tipo:** Front-end + Conteúdo

### Diferenciais sugeridos

- Produtos frescos todos os dias.
- Café e lanches preparados na hora.
- Bolos e tortas para encomenda.
- Atendimento por WhatsApp.
- Fácil acesso em Parque Veneza, Magé.

### Tarefas técnicas

- Criar seção `benefits-section`.
- Criar grid de benefícios.
- Criar cards com ícone, título e descrição.

### Critérios de aceite

- Deve haver pelo menos 5 diferenciais.
- Texto deve ser específico da padaria.
- Visual deve ser limpo.

---

# EPIC HOME-08 — Como comprar

## Objetivo

Explicar o fluxo de pedido de forma simples.

---

## HOME-08.01 — Criar seção “Como fazer seu pedido”

**Prioridade:** Média  
**Tipo:** Front-end + Conteúdo

### Passos sugeridos

```text
1. Escolha seus produtos no cardápio
2. Chame a padaria pelo WhatsApp
3. Confirme disponibilidade, pagamento e entrega/retirada
```

### Tarefas técnicas

- Criar seção `how-to-order-section`.
- Criar cards numerados.
- Adicionar CTA final para WhatsApp.

### Critérios de aceite

- Cliente entende o processo sem precisar perguntar.
- Texto não menciona pagamento online se ainda não estiver pronto.

---

# EPIC HOME-09 — Localização detalhada

## Objetivo

Facilitar que o cliente encontre a padaria.

---

## HOME-09.01 — Criar seção de localização completa

**Prioridade:** Alta  
**Tipo:** Front-end

### Título

```text
Estamos em Parque Veneza, Magé
```

### Endereço

```text
Estr. Mineira, 703 - Parque Veneza, Magé - RJ, 25930-790
```

### CTA

```text
Traçar rota no Google Maps
```

### Tarefas técnicas

- Criar seção `location-section`.
- Criar âncora `#localizacao`.
- Adicionar endereço completo.
- Adicionar botão usando `googleMapsUrl`.

### Critérios de aceite

- Endereço aparece completo.
- Link abre rota no Maps.
- Seção funciona em mobile.

---

## HOME-09.02 — Avaliar mapa incorporado

**Prioridade:** Baixa  
**Tipo:** Front-end futuro

### Observações

- Pode impactar performance.
- Pode ser desnecessário no MVP.
- Botão para Maps já resolve a maior parte do problema.

### Critérios futuros

- Mapa não deve deixar a home lenta.
- Deve ter fallback com botão externo.

---

# EPIC HOME-10 — Rodapé comercial

## Objetivo

Organizar informações úteis e mover o login para uma área discreta.

---

## HOME-10.01 — Criar rodapé

**Prioridade:** Alta  
**Tipo:** Front-end

### Conteúdo sugerido

```text
Padaria e Lanchonete Nova Aliança
Estr. Mineira, 703 - Parque Veneza, Magé - RJ
Segunda a Domingo: 05:30 às 21:00
WhatsApp: (21) 99999-9999
```

### Links sugeridos

```text
Cardápio
WhatsApp
Como chegar
Área interna
```

### Tarefas técnicas

- Criar `<footer>`.
- Exibir dados da loja.
- Adicionar links úteis.
- Mover login para `Área interna`.
- Ajustar responsividade.

### Critérios de aceite

- Rodapé aparece corretamente em desktop e mobile.
- Login continua acessível.
- WhatsApp e Maps funcionam.

---

## HOME-10.02 — Adicionar ano automático

**Prioridade:** Baixa  
**Tipo:** Front-end

### Tarefas técnicas

- Criar propriedade `currentYear = new Date().getFullYear()`.
- Exibir no rodapé.

### Exemplo

```text
© 2026 Padaria e Lanchonete Nova Aliança. Todos os direitos reservados.
```

### Critérios de aceite

- Ano atualiza automaticamente.
- Não usar ano fixo no HTML.

---

# EPIC HOME-11 — Botão flutuante de WhatsApp

## Objetivo

Manter o principal canal de conversão sempre disponível.

---

## HOME-11.01 — Criar botão flutuante

**Prioridade:** Alta  
**Tipo:** Front-end

### Texto

```text
Pedir pelo WhatsApp
```

### Tarefas técnicas

- Criar `<a class="floating-whatsapp">`.
- Usar `whatsappUrl`.
- Adicionar `aria-label`.
- Posicionar fixo no canto inferior direito em desktop.
- Em mobile, posicionar no rodapé com largura confortável.

### Critérios de aceite

- Botão aparece durante a navegação.
- Abre WhatsApp.
- Não cobre conteúdo importante.
- Funciona bem em telas pequenas.

---

## HOME-11.02 — Ajustar espaçamento inferior no mobile

**Prioridade:** Média  
**Tipo:** Front-end

### Descrição

Quando o botão flutuante estiver fixo no rodapé, o conteúdo precisa de padding inferior.

### Critérios de aceite

- Nenhum texto fica escondido atrás do botão.
- Rodapé continua acessível.

---

# EPIC HOME-12 — Imagens e conteúdo visual

## Objetivo

Usar imagens reais da padaria para aumentar desejo e confiança.

---

## HOME-12.01 — Listar imagens necessárias

**Prioridade:** Média  
**Tipo:** Conteúdo

### Fotos recomendadas

1. Foto principal do hero.
2. Foto de pães.
3. Foto de lanches.
4. Foto de salgados.
5. Foto de bolos/tortas.
6. Foto da fachada.
7. Foto do balcão.

### Critérios de aceite

- Fotos claras.
- Produtos bem apresentados.
- Evitar fundo bagunçado.
- Preferir fotos reais da padaria.

---

## HOME-12.02 — Definir padrão de imagens

**Prioridade:** Média  
**Tipo:** UI/Performance

### Regras

- Usar imagens comprimidas.
- Usar proporção consistente.
- Usar bordas arredondadas.
- Usar `alt` descritivo.
- Usar lazy loading em imagens abaixo da dobra.

### Critérios de aceite

- Página não fica pesada.
- Imagens não ficam cortadas de forma ruim.
- Imagens têm texto alternativo.

---

# EPIC HOME-13 — Dados reais da loja

## Objetivo

Evitar dados fictícios e centralizar informações no back-end.

---

## HOME-13.01 — Atualizar dados da loja no back-end

**Prioridade:** Alta  
**Tipo:** Back-end / Configuração

### Configuração recomendada

```yaml
mnss:
  store:
    name: "Padaria e Lanchonete Nova Aliança"
    address: "Estr. Mineira, 703 - Parque Veneza, Magé - RJ, 25930-790"
    hours: "Segunda a Domingo: 05:30 às 21:00"
    phone: "(21) 99999-9999"
    description: "Pães, lanches, bolos, salgados e encomendas preparados com carinho em Parque Veneza, Magé."
```

### Tarefas técnicas

- Atualizar `application.yml` local.
- Atualizar `application.yml` online, se aplicável.
- Validar endpoint `/api/public/menu/info`.
- Rebuildar API se necessário.

### Critérios de aceite

- Não aparece “Rua Exemplo”.
- Não aparece telefone fictício.
- Home consome dados reais.

---

## HOME-13.02 — Usar endereço da API no Google Maps

**Prioridade:** Média  
**Tipo:** Front-end

### Descrição

Evitar endereço duplicado no front-end.

### Tarefas técnicas

- Montar `googleMapsUrl` com `storeInfo.address`.
- Criar fallback apenas se endereço vier vazio.

### Critérios de aceite

- Se o endereço mudar no back-end, o Maps muda automaticamente.
- Não há hardcode desnecessário.

---

## HOME-13.03 — Criar utilitário para WhatsApp

**Prioridade:** Média  
**Tipo:** Front-end

### Descrição

Centralizar criação de links de WhatsApp.

### Tarefas técnicas

- Criar método privado `buildWhatsappUrl(message: string)`.
- Reutilizar para:
  - pedido comum;
  - encomenda;
  - delivery;
  - botão flutuante.

### Critérios de aceite

- Código não duplica lógica.
- Todos os links usam número formatado corretamente.

---

# EPIC HOME-14 — SEO básico

## Objetivo

Melhorar título, descrição e compartilhamento da página.

---

## HOME-14.01 — Ajustar título da página

**Prioridade:** Média  
**Tipo:** Front-end / SEO

### Título sugerido

```text
Padaria Nova Aliança | Pães, Lanches e Encomendas em Magé
```

### Tarefas técnicas

- Usar Angular `Title`.
- Definir título no `ngOnInit`.

### Critérios de aceite

- Aba do navegador mostra título correto.
- Título cita padaria e localização.

---

## HOME-14.02 — Adicionar meta description

**Prioridade:** Média  
**Tipo:** Front-end / SEO

### Description sugerida

```text
Padaria e Lanchonete Nova Aliança em Parque Veneza, Magé. Pães frescos, lanches, bolos, salgados, delivery e encomendas pelo WhatsApp.
```

### Tarefas técnicas

- Usar Angular `Meta`.
- Adicionar `description`.
- Adicionar `og:title`.
- Adicionar `og:description`.

### Critérios de aceite

- Tags aparecem no HTML.
- Texto é específico da padaria.

---

# EPIC HOME-15 — Performance e acessibilidade

## Objetivo

Garantir que a landing page funcione bem em celular, teclado e conexões lentas.

---

## HOME-15.01 — Revisar acessibilidade dos links

**Prioridade:** Alta  
**Tipo:** Front-end / Acessibilidade

### Tarefas técnicas

- Cards clicáveis devem ser `<a>`.
- Links externos devem ter `aria-label`.
- Foco deve ser visível.
- Não usar apenas cor para indicar ação.

### Critérios de aceite

- Navegação por teclado funciona.
- Foco é visível.
- Leitores de tela entendem os links principais.

---

## HOME-15.02 — Testar breakpoints

**Prioridade:** Alta  
**Tipo:** QA

### Larguras mínimas

```text
360px
390px
768px
1024px
1366px
```

### Critérios de aceite

- Sem scroll horizontal.
- Header não quebra.
- Cards reorganizam corretamente.
- Botão flutuante não cobre conteúdo.
- CTAs continuam clicáveis.

---

# EPIC HOME-16 — Organização técnica

## Objetivo

Evitar que `SiteHomeComponent` fique grande demais.

---

## HOME-16.01 — Separar conteúdo estático em constantes

**Prioridade:** Média  
**Tipo:** Front-end

### Tarefas técnicas

- Criar `featuredProducts` tipado.
- Criar `orderCategories` tipado.
- Criar `benefits` tipado.
- Avaliar criação de arquivo `site-home-content.ts`.

### Critérios de aceite

- Template fica mais limpo.
- Conteúdo fica fácil de editar.
- Código evita duplicação.

---

## HOME-16.02 — Avaliar componentes menores

**Prioridade:** Baixa no MVP  
**Tipo:** Front-end

### Componentes possíveis

```text
HomeHeroComponent
HomeInfoCardsComponent
FeaturedProductsComponent
DeliverySectionComponent
OrdersSectionComponent
BenefitsSectionComponent
LocationSectionComponent
HomeFooterComponent
FloatingWhatsappButtonComponent
```

### Critérios futuros

- `SiteHomeComponent` apenas compõe seções.
- Componentes visuais recebem dados por `@Input`.
- Serviços HTTP continuam em `data-access`.

---

# 6. Roadmap por sprint

---

## Sprint HOME-01 — Conversão básica

### Objetivo

Adicionar os caminhos principais de ação: WhatsApp, Maps e navegação comercial.

### Histórias

- HOME-01.01 — Ajustar menu principal.
- HOME-01.02 — Adicionar botão de WhatsApp no header.
- HOME-02.01 — Reescrever título e subtítulo.
- HOME-02.02 — Adicionar CTA de WhatsApp no hero.
- HOME-03.01 — Card de localização com Google Maps.
- HOME-03.02 — Card de contato com WhatsApp.

### Resultado esperado

A home já permite conversão direta por WhatsApp e localização.

---

## Sprint HOME-02 — Visual e produtos

### Objetivo

Dar aparência de padaria e mostrar produtos de interesse.

### Histórias

- HOME-02.04 — Alterar paleta visual para padaria.
- HOME-02.05 — Preparar hero com imagem.
- HOME-04.01 — Criar seção “Mais pedidos”.
- HOME-04.02 — Criar modelo `FeaturedProduct`.
- HOME-12.01 — Listar imagens necessárias.
- HOME-12.02 — Definir padrão de imagens.

### Resultado esperado

A página passa a gerar desejo visual e apresentar os principais produtos.

---

## Sprint HOME-03 — Delivery e encomendas

### Objetivo

Comunicar delivery e encomendas com CTA direto.

### Histórias

- HOME-05.01 — Criar bloco de delivery.
- HOME-05.02 — Aviso de taxa e região.
- HOME-06.01 — Criar seção “Encomendas”.
- HOME-06.02 — Categorias de encomenda.
- HOME-06.03 — Link de WhatsApp para encomendas.

### Resultado esperado

Cliente entende que pode pedir delivery e fazer encomendas.

---

## Sprint HOME-04 — Confiança e mobile

### Objetivo

Completar conteúdo de confiança e melhorar experiência mobile.

### Histórias

- HOME-07.01 — Criar seção de diferenciais.
- HOME-08.01 — Criar seção “Como fazer seu pedido”.
- HOME-09.01 — Criar seção de localização completa.
- HOME-10.01 — Criar rodapé.
- HOME-11.01 — Criar botão flutuante.
- HOME-11.02 — Ajustar espaçamento inferior no mobile.
- HOME-15.02 — Testar breakpoints.

### Resultado esperado

Home completa e orientada para conversão.

---

## Sprint HOME-05 — Dados, SEO e organização

### Objetivo

Melhorar manutenção, dados reais, SEO e acessibilidade.

### Histórias

- HOME-13.01 — Atualizar dados da loja no back-end.
- HOME-13.02 — Usar endereço da API no Google Maps.
- HOME-13.03 — Criar utilitário para WhatsApp.
- HOME-14.01 — Ajustar título da página.
- HOME-14.02 — Adicionar meta description.
- HOME-15.01 — Revisar acessibilidade dos links.
- HOME-16.01 — Separar conteúdo estático em constantes.

### Resultado esperado

Página mais profissional, sustentável e preparada para busca.

---

# 7. Priorização geral

## Prioridade máxima

1. WhatsApp no hero.
2. Card de contato abrindo WhatsApp.
3. Localização abrindo Google Maps.
4. Header comercial.
5. Hero com texto comercial.

## Prioridade alta

6. Mais pedidos.
7. Delivery.
8. Encomendas.
9. Botão flutuante de WhatsApp.
10. Rodapé comercial.

## Prioridade média

11. Diferenciais.
12. Como comprar.
13. Localização detalhada.
14. Visual quente de padaria.
15. SEO básico.

## Prioridade futura

16. Status aberto/fechado.
17. Produtos em destaque vindos do back-end.
18. Mapa incorporado.
19. Avaliações reais de clientes.
20. Dados estruturados LocalBusiness.

---

# 8. Checklist final da landing page

- [ ] Header tem navegação comercial.
- [ ] Login foi movido para área discreta.
- [ ] Hero comunica produto e benefício.
- [ ] Hero tem botão para cardápio.
- [ ] Hero tem botão para WhatsApp.
- [ ] Card de localização abre Google Maps.
- [ ] Card de contato abre WhatsApp.
- [ ] Horário está correto.
- [ ] Endereço está correto.
- [ ] Telefone está correto.
- [ ] Seção de mais pedidos existe.
- [ ] Seção de delivery existe.
- [ ] Seção de encomendas existe.
- [ ] Seção de diferenciais existe.
- [ ] Seção de localização detalhada existe.
- [ ] Rodapé existe.
- [ ] Botão flutuante de WhatsApp existe.
- [ ] Página funciona em celular.
- [ ] Página não tem scroll horizontal.
- [ ] Página não mostra dados fictícios.
- [ ] Build do front-end passa.
- [ ] Console do navegador não apresenta erros.
- [ ] Links externos usam `target="_blank"` e `rel="noopener noreferrer"`.
- [ ] Links clicáveis possuem `aria-label` quando necessário.

---

# 9. Métricas de sucesso

Após publicação, acompanhar:

- Cliques em “Ver Cardápio”.
- Cliques em “Pedir pelo WhatsApp”.
- Cliques em “Como chegar”.
- Pedidos vindos do site.
- Encomendas vindas do site.
- Dúvidas recorrentes dos clientes.
- Se o cliente pergunta menos sobre endereço e horário.
- Se o WhatsApp recebe mensagens mais qualificadas.

---

# 10. Mensagens padrão de WhatsApp

## Pedido comum

```text
Olá! Vim pelo site da Padaria Nova Aliança e gostaria de fazer um pedido.
```

## Encomenda

```text
Olá! Vim pelo site da Padaria Nova Aliança e gostaria de fazer uma encomenda.
```

## Delivery

```text
Olá! Vim pelo site da Padaria Nova Aliança e gostaria de consultar delivery para o meu endereço.
```

## Dúvida geral

```text
Olá! Vim pelo site da Padaria Nova Aliança e gostaria de tirar uma dúvida.
```

---

# 11. Conteúdo sugerido para a home

## Hero

```text
Pães quentinhos, lanches e bolos frescos todos os dias

A Padaria e Lanchonete Nova Aliança prepara seu café, lanche e encomendas com qualidade e carinho em Parque Veneza, Magé.
```

## Mais pedidos

```text
Mais pedidos da Nova Aliança

Escolha seu café, lanche ou doce preferido e peça pelo cardápio ou WhatsApp.
```

## Delivery

```text
Delivery todos os dias

Receba pães, lanches, bolos, salgados e bebidas no conforto da sua casa. Chame no WhatsApp e consulte a disponibilidade para sua região.
```

## Encomendas

```text
Encomendas para festas, cafés e momentos especiais

Faça sua encomenda de tortas, bolos, salgados, rocamboles e kits de café com antecedência pelo WhatsApp.
```

## Diferenciais

```text
Por que escolher a Nova Aliança?

Produtos frescos todos os dias.
Café e lanches preparados na hora.
Bolos e tortas para encomenda.
Atendimento por WhatsApp.
Fácil acesso em Parque Veneza, Magé.
```

## Localização

```text
Estamos em Parque Veneza, Magé

Estr. Mineira, 703 - Parque Veneza, Magé - RJ, 25930-790
```

---

# 12. Observação final

A prioridade da home deve ser conversão prática:

```text
Cardápio + WhatsApp + Produtos desejados + Encomendas + Localização
```

A página não precisa começar complexa. Ela precisa ser clara, rápida e útil para transformar visita em contato real.
