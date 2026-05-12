# Backlog de Estilização — Cardápio Online Nova Aliança

> Orientado para implementação com Claude  
> Projeto: `system-mnss`  
> Área: Front-end Angular  
> Página alvo: `/cardapio`  
> Objetivo: padronizar a página de cardápio a partir da identidade visual e UX/UI da Home Page.

---

## 1. Contexto do projeto

A Home Page da Padaria e Lanchonete Nova Aliança está sendo evoluída para uma landing page comercial com identidade quente, visual de padaria e experiência mobile semelhante a aplicativo.

A página `/cardapio` precisa seguir o mesmo padrão visual e de experiência.

### Componentes principais envolvidos

```text
front-end/src/app/features/site-publico/pages/public-menu/public-menu.component.ts
front-end/src/app/features/site-publico/components/public-product-card/public-product-card.component.ts
front-end/src/app/features/site-publico/components/public-cart/public-cart.component.ts
front-end/src/app/app.routes.ts
```

### Fluxo funcional atual do cardápio

- A rota `/cardapio` carrega o componente `PublicMenuComponent`.
- O `PublicMenuComponent` busca os dados reais do back-end usando `PublicMenuService.getMenu(search)`.
- Os produtos são renderizados por categoria.
- Cada produto é exibido pelo componente `PublicProductCardComponent`.
- O carrinho público é exibido pelo componente `PublicCartComponent`.
- O checkout é acessado pela rota `/checkout`.

### Regra comercial definida

```text
WhatsApp = dúvidas
/cardapio = pedidos comuns
/encomendas = encomendas
```

Portanto:

- O cardápio deve ser o canal principal para pedidos comuns.
- WhatsApp não deve aparecer como CTA principal para pedido.
- WhatsApp pode aparecer como canal secundário para dúvidas.
- Encomendas devem seguir a rota própria `/encomendas` quando ela existir.

---

## 2. Direção visual obrigatória

### Paleta oficial da área pública

```text
Creme:          #fff7e8
Dourado:        #f5a623
Marrom:         #6b3f1d
Marrom escuro:  #3d2b1f
Marrom claro:   #9c6b3f
Laranja:        #e67e22
Branco:         #ffffff
WhatsApp:       #1e7e34
Erro/esgotado:  #c0392b
Texto principal:#3d2b1f
Texto apoio:    #6b3f1d
Borda quente:   rgba(156, 107, 63, 0.18)
Sombra quente:  rgba(61, 43, 31, 0.12)
```

### Sensação visual esperada

A página deve parecer:

- padaria;
- acolhedora;
- comercial;
- clara;
- simples de comprar;
- confiável;
- mobile-first;
- parecida com aplicativo em celular.

A página não deve parecer:

- sistema administrativo;
- dashboard corporativo;
- cardápio genérico;
- tela fria com azul/cinza;
- página técnica.

---

## 3. Regras globais para o Claude

### Não alterar

```text
Não alterar back-end.
Não alterar endpoints.
Não alterar contratos da API.
Não alterar banco de dados.
Não criar migrations.
Não remover PublicMenuService.getMenu.
Não remover PublicProductCardComponent.
Não remover PublicCartComponent.
Não quebrar checkout.
Não quebrar carrinho.
Não quebrar busca.
Não remover RouterLink.
Não remover CommonModule.
Não instalar biblioteca externa de UI.
Não usar Bootstrap, Tailwind ou Angular Material se o projeto não estiver usando.
```

### Manter

```text
Componentes standalone.
Templates e styles inline, se esse for o padrão atual do componente.
Busca com debounce.
Renderização por categorias.
Carrinho flutuante.
Botão Adicionar.
Estado de produto esgotado.
Checkout em /checkout.
Rota /cardapio carregando produtos reais do back-end.
```

### Melhorar

```text
Identidade visual.
Responsividade.
Acessibilidade.
Feedback de toque.
Estados de loading/vazio.
Experiência mobile.
Consistência com a Home.
```

---

## 4. Arquitetura visual alvo da página `/cardapio`

### Estrutura sugerida

```html
<main class="menu-page">
  <header class="public-menu-header">
    <!-- logo + links -->
  </header>

  <section class="menu-hero">
    <!-- título comercial + texto de apoio -->
  </section>

  <section class="search-section">
    <!-- busca -->
  </section>

  <nav class="category-chips" *ngIf="menu.length">
    <!-- chips de categorias -->
  </nav>

  <section class="menu-content">
    <section class="category-section" *ngFor="let categoryMenu of menu">
      <!-- categoria + produtos -->
    </section>
  </section>

  <app-public-cart></app-public-cart>

  <nav class="mobile-bottom-nav">
    <!-- navegação mobile -->
  </nav>
</main>
```

---

# EPIC CARDAPIO-01 — Identidade visual base

## Objetivo

Fazer o cardápio parecer visualmente conectado à Home da Padaria Nova Aliança.

---

## CARDAPIO-01.01 — Aplicar paleta quente no `PublicMenuComponent`

**Prioridade:** Alta  
**Tipo:** UI/UX  
**Arquivo:** `public-menu.component.ts`

### Problema

O cardápio ainda usa cores frias, principalmente azul corporativo no header, spinner, links e outros detalhes.

### Direção

Substituir a identidade fria por uma identidade quente, usando creme, marrom, dourado e laranja.

### Tarefas técnicas

- Criar tokens CSS no `:host`.
- Aplicar fundo creme na página.
- Trocar azul do header por marrom.
- Trocar textos cinza/azul por marrom.
- Trocar bordas cinzas por bordas quentes.
- Trocar spinner azul por dourado/marrom.
- Garantir contraste.

### CSS sugerido

```css
:host {
  --color-cream: #fff7e8;
  --color-gold: #f5a623;
  --color-brown: #6b3f1d;
  --color-brown-dark: #3d2b1f;
  --color-brown-light: #9c6b3f;
  --color-orange: #e67e22;
  --color-white: #ffffff;
  --color-whatsapp: #1e7e34;
  --color-danger: #c0392b;
  --shadow-soft: 0 12px 32px rgba(61, 43, 31, 0.1);
  --shadow-medium: 0 18px 42px rgba(61, 43, 31, 0.16);

  display: block;
  min-height: 100vh;
  background-color: var(--color-cream);
  color: var(--color-brown-dark);
}
```

### Critérios de aceite

- Azul não é mais cor dominante.
- A página parece parte da área pública da padaria.
- Textos continuam legíveis.
- `npm run build` passa.

---

## CARDAPIO-01.02 — Padronizar header do cardápio com logo

**Prioridade:** Alta  
**Tipo:** UI/UX  
**Arquivo:** `public-menu.component.ts`

### Estado atual

Header exibe texto:

```text
Cardápio Online
```

### Estado desejado

Header com a logo da padaria à esquerda, parecido com a Home.

### Imagem

```text
assets/images/site/logo.png
```

### Tarefas técnicas

- Trocar texto simples do header por logo.
- Manter link para Home.
- Criar header sticky no mobile.
- Aplicar fundo marrom.
- Ajustar altura da logo.
- Garantir que a logo não distorça.
- Adicionar foco visível.

### Template sugerido

```html
<header class="public-menu-header" id="topo-cardapio">
  <a
    routerLink="/"
    class="menu-brand-logo"
    aria-label="Voltar para a home da Padaria Nova Aliança"
  >
    <img
      src="assets/images/site/logo.png"
      alt="Padaria e Lanchonete Nova Aliança"
      decoding="async"
    />
  </a>

  <div class="spacer"></div>

  <nav class="menu-nav" aria-label="Navegação do cardápio">
    <a routerLink="/" class="menu-nav-link">Início</a>
  </nav>
</header>
```

### CSS sugerido

```css
.public-menu-header {
  position: sticky;
  top: 0;
  z-index: 900;
  display: flex;
  align-items: center;
  gap: 16px;
  min-height: 72px;
  padding: 10px 24px;
  background-color: var(--color-brown);
  color: var(--color-white);
  box-shadow: 0 8px 24px rgba(61, 43, 31, 0.18);
}

.menu-brand-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
}

.menu-brand-logo img {
  display: block;
  height: 54px;
  width: auto;
  max-width: 180px;
  object-fit: contain;
}

.menu-nav-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 42px;
  padding: 8px 16px;
  border-radius: 999px;
  color: var(--color-white);
  text-decoration: none;
  font-weight: 800;
}

.menu-nav-link:hover {
  background-color: rgba(255, 247, 232, 0.14);
}

.menu-nav-link:focus-visible,
.menu-brand-logo:focus-visible {
  outline: 3px solid var(--color-gold);
  outline-offset: 4px;
}
```

### Critérios de aceite

- Header usa logo.
- Header não usa mais título textual frio.
- Header fica coerente com a Home.
- Header funciona em mobile.
- Home continua acessível.

---

# EPIC CARDAPIO-02 — Hero comercial do cardápio

## Objetivo

Explicar rapidamente para o cliente que o pedido deve ser feito pelo cardápio online.

---

## CARDAPIO-02.01 — Criar hero compacto

**Prioridade:** Alta  
**Tipo:** Front-end + Conteúdo  
**Arquivo:** `public-menu.component.ts`

### Conteúdo recomendado

```text
Cardápio Online Nova Aliança

Escolha seus produtos, adicione ao carrinho e finalize seu pedido de forma simples.
```

### Regras de conteúdo

- Não dizer “peça pelo WhatsApp”.
- Não usar WhatsApp como canal principal.
- Não mencionar pagamento online se o fluxo ainda não estiver finalizado.
- Não prometer entrega automática.

### Template sugerido

```html
<section class="menu-hero">
  <span class="section-eyebrow">Cardápio Online</span>

  <h1>Cardápio Online Nova Aliança</h1>

  <p>
    Escolha seus produtos, adicione ao carrinho e finalize seu pedido de forma simples.
  </p>
</section>
```

### CSS sugerido

```css
.menu-hero {
  max-width: 980px;
  margin: 0 auto;
  padding: 48px 24px 28px;
  text-align: center;
}

.section-eyebrow {
  display: inline-block;
  margin-bottom: 10px;
  color: var(--color-orange);
  font-weight: 900;
  font-size: 0.82rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.menu-hero h1 {
  margin: 0 0 14px;
  color: var(--color-brown-dark);
  font-size: clamp(2rem, 5vw, 3rem);
  line-height: 1.1;
  font-weight: 900;
}

.menu-hero p {
  max-width: 680px;
  margin: 0 auto;
  color: var(--color-brown);
  font-size: 1.1rem;
  line-height: 1.65;
}
```

### Critérios de aceite

- Cliente entende o fluxo do pedido.
- Hero é compacto.
- Não atrapalha a busca.
- Visual segue a Home.

---

## CARDAPIO-02.02 — Adicionar aviso de disponibilidade

**Prioridade:** Média  
**Tipo:** Conteúdo  
**Arquivo:** `public-menu.component.ts`

### Texto recomendado

```text
Produtos e disponibilidade podem variar conforme o horário e a produção do dia.
```

### Tarefas técnicas

- Adicionar aviso discreto abaixo do hero ou abaixo da busca.
- Usar visual de pill/card pequeno.
- Não usar vermelho.
- Não parecer erro.

### Template sugerido

```html
<p class="availability-note">
  Produtos e disponibilidade podem variar conforme o horário e a produção do dia.
</p>
```

### CSS sugerido

```css
.availability-note {
  max-width: 720px;
  margin: 14px auto 0;
  padding: 10px 14px;
  border-radius: 999px;
  background-color: rgba(245, 166, 35, 0.14);
  color: var(--color-brown);
  text-align: center;
  font-weight: 700;
  font-size: 0.92rem;
}
```

### Critérios de aceite

- O aviso é claro.
- Não polui a tela.
- Não cria promessa indevida.

---

# EPIC CARDAPIO-03 — Busca e navegação por categorias

## Objetivo

Transformar a busca e categorias em uma experiência rápida, clara e mobile-first.

---

## CARDAPIO-03.01 — Estilizar campo de busca como app

**Prioridade:** Alta  
**Tipo:** UI/UX  
**Arquivo:** `public-menu.component.ts`

### Estado atual

Busca simples com borda cinza.

### Estado desejado

Busca em formato de barra arredondada, confortável para toque.

### Tarefas técnicas

- Aumentar altura mínima para 56px.
- Arredondar campo.
- Usar sombra suave.
- Melhorar placeholder.
- Melhorar botão limpar.
- Manter debounce.
- Manter `clearSearch`.
- Adicionar `aria-label` no botão limpar.
- Adicionar foco visível no input.

### Template sugerido

```html
<div class="search-field">
  <span class="search-icon" aria-hidden="true">🔍</span>

  <input
    type="text"
    [(ngModel)]="searchQuery"
    (ngModelChange)="onSearchChange($event)"
    placeholder="Buscar pão, café, bolo ou salgado"
    aria-label="Buscar produtos no cardápio"
  />

  <button
    class="clear-btn"
    *ngIf="searchQuery"
    (click)="clearSearch()"
    type="button"
    aria-label="Limpar busca"
  >
    ✖
  </button>
</div>
```

### CSS sugerido

```css
.search-section {
  max-width: 760px;
  margin: 0 auto 28px;
  padding: 0 24px;
}

.search-field {
  min-height: 56px;
  display: flex;
  align-items: center;
  border: 1px solid rgba(156, 107, 63, 0.18);
  border-radius: 999px;
  padding: 8px 18px;
  background-color: var(--color-white);
  box-shadow: var(--shadow-soft);
}

.search-icon {
  margin-right: 10px;
  color: var(--color-brown-light);
}

.search-field input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  font-size: 1rem;
  color: var(--color-brown-dark);
  background: transparent;
}

.search-field input::placeholder {
  color: var(--color-brown-light);
}

.clear-btn {
  width: 36px;
  height: 36px;
  border-radius: 999px;
  border: none;
  background-color: rgba(156, 107, 63, 0.12);
  color: var(--color-brown);
  cursor: pointer;
  font-weight: 900;
}

.clear-btn:hover {
  background-color: rgba(230, 126, 34, 0.16);
  color: var(--color-orange);
}

.search-field:focus-within {
  border-color: var(--color-gold);
  box-shadow: 0 0 0 4px rgba(245, 166, 35, 0.16);
}
```

### Critérios de aceite

- Busca continua funcionando.
- Campo tem boa área de toque.
- Botão limpar funciona.
- Visual é coerente com a Home.

---

## CARDAPIO-03.02 — Criar chips horizontais de categorias

**Prioridade:** Média  
**Tipo:** Front-end  
**Arquivo:** `public-menu.component.ts`

### Objetivo

Permitir navegação rápida entre categorias retornadas pelo back-end.

### Tarefas técnicas

- Criar chips com base em `menu`.
- Cada chip deve rolar até a categoria.
- Criar IDs seguros para cada categoria.
- Não alterar endpoint.
- Evitar IDs duplicados.
- Mobile deve ter scroll horizontal.

### Método sugerido

```ts
categoryId(categoryName: string, index: number): string {
  const normalized = categoryName
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/(^-|-$)/g, '');

  return `categoria-${normalized || 'item'}-${index}`;
}
```

### Template sugerido

```html
<nav class="category-chips" *ngIf="menu.length" aria-label="Categorias do cardápio">
  <a
    class="category-chip"
    *ngFor="let categoryMenu of menu; let i = index"
    [href]="'#' + categoryId(categoryMenu.category.name, i)"
  >
    {{ categoryMenu.category.name }}
  </a>
</nav>
```

Na categoria:

```html
<section
  class="category-section"
  *ngFor="let categoryMenu of menu; let i = index"
  [id]="categoryId(categoryMenu.category.name, i)"
>
  ...
</section>
```

### CSS sugerido

```css
.category-chips {
  max-width: 1200px;
  margin: 0 auto 32px;
  padding: 0 24px 8px;
  display: flex;
  gap: 10px;
  overflow-x: auto;
  scroll-snap-type: x proximity;
  -webkit-overflow-scrolling: touch;
}

.category-chip {
  flex: 0 0 auto;
  scroll-snap-align: start;
  min-height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  border-radius: 999px;
  background-color: var(--color-white);
  color: var(--color-brown);
  border: 1px solid rgba(156, 107, 63, 0.18);
  text-decoration: none;
  font-weight: 800;
  box-shadow: 0 6px 18px rgba(61, 43, 31, 0.08);
}

.category-chip:hover {
  background-color: var(--color-gold);
  color: var(--color-brown-dark);
}

.category-chip:focus-visible {
  outline: 3px solid var(--color-gold);
  outline-offset: 3px;
}

.category-section {
  scroll-margin-top: 96px;
}
```

### Critérios de aceite

- Chips aparecem quando há categorias.
- Clique leva até a categoria.
- Mobile permite scroll horizontal.
- Não há scroll horizontal da página.

---

# EPIC CARDAPIO-04 — Categorias e grid

## Objetivo

Melhorar a leitura das categorias e a disposição dos produtos.

---

## CARDAPIO-04.01 — Melhorar cabeçalho de categoria

**Prioridade:** Alta  
**Tipo:** UI/UX  
**Arquivo:** `public-menu.component.ts`

### Tarefas técnicas

- Transformar categoria em seção visual.
- Título com marrom escuro.
- Linha/borda dourada discreta.
- Descrição com texto de apoio.
- Melhorar espaçamento entre categorias.

### Template sugerido

```html
<header class="category-header">
  <h2 class="category-title">{{ categoryMenu.category.name }}</h2>

  <p class="category-description" *ngIf="categoryMenu.category.description">
    {{ categoryMenu.category.description }}
  </p>
</header>
```

### CSS sugerido

```css
.category-section {
  max-width: 1200px;
  margin: 0 auto 56px;
  padding: 0 24px;
}

.category-header {
  margin-bottom: 20px;
}

.category-title {
  margin: 0 0 8px;
  padding-bottom: 10px;
  color: var(--color-brown-dark);
  font-size: clamp(1.5rem, 3vw, 2rem);
  font-weight: 900;
  line-height: 1.15;
  border-bottom: 2px solid rgba(245, 166, 35, 0.4);
}

.category-description {
  margin: 0;
  color: var(--color-brown);
  line-height: 1.6;
}
```

### Critérios de aceite

- Categorias ficam mais claras.
- A página fica organizada.
- Não quebra com nomes longos.

---

## CARDAPIO-04.02 — Ajustar grid responsivo

**Prioridade:** Alta  
**Tipo:** Responsividade  
**Arquivo:** `public-menu.component.ts`

### Tarefas técnicas

- Desktop: 3 ou 4 cards por linha conforme espaço.
- Tablet: 2 cards por linha.
- Mobile: 1 card por linha.
- Sem scroll horizontal.
- Gap consistente.

### CSS sugerido

```css
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 24px;
}

@media (max-width: 768px) {
  .products-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}
```

### Critérios de aceite

- Em 360px fica 1 coluna.
- Em 390px fica 1 coluna.
- Em 768px fica legível.
- Em desktop aproveita espaço.
- Não existe scroll horizontal.

---

# EPIC CARDAPIO-05 — Cards de produto

## Objetivo

Deixar os produtos com aparência mais comercial, padronizada e coerente com a Home.

---

## CARDAPIO-05.01 — Reestilizar card de produto

**Prioridade:** Alta  
**Tipo:** UI/UX  
**Arquivo:** `public-product-card.component.ts`

### Estado atual

- Card branco com sombra genérica.
- Hover com borda azul.
- Título azul/cinza.
- Botão azul.
- Visual corporativo.

### Estado desejado

- Card branco quente.
- Borda quente.
- Sombra suave.
- Título marrom.
- Botão dourado.
- Hover discreto.

### CSS sugerido

```css
.product-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 22px;
  overflow: hidden;
  background-color: #ffffff;
  border: 1px solid rgba(156, 107, 63, 0.16);
  box-shadow: 0 12px 32px rgba(61, 43, 31, 0.1);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease;
  position: relative;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 42px rgba(61, 43, 31, 0.16);
  border-color: rgba(245, 166, 35, 0.6);
}

.product-title {
  color: #3d2b1f;
  font-size: 1.15rem;
  font-weight: 800;
}

.product-description {
  color: #6b3f1d;
}
```

### Critérios de aceite

- Card não usa mais azul.
- Visual parece de padaria.
- Card continua funcional.
- Hover não é exagerado.

---

## CARDAPIO-05.02 — Melhorar imagens e placeholders

**Prioridade:** Alta  
**Tipo:** UI/Performance  
**Arquivo:** `public-product-card.component.ts`

### Tarefas técnicas

- Adicionar `loading="lazy"` nas imagens.
- Adicionar `decoding="async"`.
- Manter `alt` com nome do produto.
- Placeholder sem imagem deve usar paleta quente.
- Ícone deve ser de padaria/comida.
- `object-fit: cover` para fotos reais.
- Evitar distorção.

### Template sugerido

```html
<img
  *ngIf="product.imageUrl"
  class="product-image"
  [src]="product.imageUrl"
  [alt]="product.name"
  loading="lazy"
  decoding="async"
/>

<div *ngIf="!product.imageUrl" class="product-placeholder">
  <span class="icon" aria-hidden="true">🥖</span>
</div>
```

### CSS sugerido

```css
.product-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  background-color: #fff7e8;
}

.product-placeholder {
  height: 180px;
  background: linear-gradient(135deg, #fff7e8 0%, #f8ddb0 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b3f1d;
}

.product-placeholder .icon {
  font-size: 52px;
}
```

### Critérios de aceite

- Imagens reais carregam com lazy loading.
- Produtos sem imagem ficam visualmente bons.
- Não há placeholder frio/azul.
- Acessibilidade preservada.

---

## CARDAPIO-05.03 — Melhorar preço e promoção

**Prioridade:** Alta  
**Tipo:** UI/UX  
**Arquivo:** `public-product-card.component.ts`

### Tarefas técnicas

- Preço normal deve ser fácil de ler.
- Promoção deve destacar o preço promocional.
- Preço antigo deve ser riscado e discreto.
- Evitar vermelho agressivo para promoção.

### CSS sugerido

```css
.product-prices {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.price {
  font-weight: 800;
  font-size: 1.15rem;
  color: #3d2b1f;
}

.price.has-promotion {
  text-decoration: line-through;
  color: #9c6b3f;
  font-size: 0.95rem;
  font-weight: 700;
}

.promotional-price {
  font-weight: 900;
  font-size: 1.35rem;
  color: #e67e22;
}
```

### Critérios de aceite

- Preço é claro.
- Promoção é clara.
- Layout não quebra.

---

## CARDAPIO-05.04 — Padronizar botão Adicionar

**Prioridade:** Alta  
**Tipo:** UI/UX  
**Arquivo:** `public-product-card.component.ts`

### Estado atual

Botão azul com gradiente.

### Estado desejado

Botão dourado, com toque confortável e visual de app.

### CSS sugerido

```css
.add-button {
  width: 100%;
  min-height: 48px;
  padding: 12px 16px;
  border-radius: 999px;
  border: none;
  background: #f5a623;
  color: #3d2b1f;
  font-weight: 900;
  font-size: 1rem;
  cursor: pointer;
  transition:
    background-color 0.2s ease,
    color 0.2s ease,
    transform 0.2s ease,
    box-shadow 0.2s ease;
  box-shadow: 0 8px 18px rgba(245, 166, 35, 0.22);
  -webkit-tap-highlight-color: transparent;
}

.add-button:hover {
  background-color: #e67e22;
  color: #ffffff;
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(230, 126, 34, 0.28);
}

.add-button:active {
  transform: scale(0.98);
}

.add-button:focus-visible {
  outline: 3px solid #6b3f1d;
  outline-offset: 4px;
}

.add-button:disabled {
  background: rgba(156, 107, 63, 0.22);
  color: #6b3f1d;
  box-shadow: none;
  cursor: not-allowed;
  transform: none;
}
```

### Critérios de aceite

- Botão não é azul.
- Botão tem no mínimo 48px de altura.
- Produto indisponível é claro.
- Clique continua adicionando ao carrinho.

---

## CARDAPIO-05.05 — Melhorar selo de esgotado

**Prioridade:** Média  
**Tipo:** UI/UX  
**Arquivo:** `public-product-card.component.ts`

### Estado atual

Faixa diagonal vermelha.

### Opção recomendada

Trocar por badge superior mais limpo.

### CSS sugerido

```css
.out-of-stock-badge {
  position: absolute;
  top: 14px;
  right: 14px;
  z-index: 2;
  transform: none;
  padding: 7px 12px;
  border-radius: 999px;
  background-color: #c0392b;
  color: #ffffff;
  font-size: 0.72rem;
  font-weight: 900;
  letter-spacing: 0.04em;
  box-shadow: 0 8px 18px rgba(192, 57, 43, 0.22);
}
```

### Critérios de aceite

- Produto esgotado fica claro.
- Badge não cobre informações importantes.
- Card continua bonito.

---

# EPIC CARDAPIO-06 — Carrinho visual app

## Objetivo

Transformar o carrinho em um elemento coerente com a identidade da Home e confortável no celular.

---

## CARDAPIO-06.01 — Reestilizar carrinho flutuante

**Prioridade:** Alta  
**Tipo:** UI/UX  
**Arquivo:** `public-cart.component.ts`

### Estado atual

Carrinho azul/cinza.

### Estado desejado

Carrinho com marrom, dourado e visual de app.

### CSS sugerido

```css
.cart-container {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 520px;
  background: #3d2b1f;
  color: #ffffff;
  border-radius: 22px;
  box-shadow: 0 18px 48px rgba(61, 43, 31, 0.28);
  z-index: 1000;
  overflow: hidden;
}

.cart-summary {
  padding: 16px 22px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  background: linear-gradient(135deg, #6b3f1d 0%, #3d2b1f 100%);
}

.cart-summary .info .count {
  background: #f5a623;
  color: #3d2b1f;
}

.cart-details {
  background: #ffffff;
  color: #3d2b1f;
}

.checkout-btn {
  display: block;
  width: 100%;
  min-height: 48px;
  padding: 14px;
  background: #f5a623;
  color: #3d2b1f;
  text-align: center;
  text-decoration: none;
  border-radius: 999px;
  font-weight: 900;
}

.checkout-btn:hover {
  background: #e67e22;
  color: #ffffff;
}
```

### Critérios de aceite

- Carrinho combina com a Home.
- Carrinho ainda expande.
- Checkout continua funcionando.
- Carrinho não usa azul.

---

## CARDAPIO-06.02 — Melhorar carrinho no mobile

**Prioridade:** Alta  
**Tipo:** UX Mobile  
**Arquivo:** `public-cart.component.ts`

### Tarefas técnicas

- Ajustar largura para mobile.
- Posicionar acima da bottom nav, se existir.
- Adicionar padding inferior na página do cardápio.
- Garantir que não cobre produto/botões.
- Botões com toque confortável.

### CSS sugerido

```css
@media (max-width: 768px) {
  .cart-container {
    left: 12px;
    right: 12px;
    bottom: 88px;
    width: auto;
    max-width: none;
    transform: none;
    border-radius: 22px;
  }

  .cart-summary {
    padding: 14px 16px;
  }

  .cart-details {
    padding: 14px;
  }

  .items-list {
    max-height: 220px;
  }
}
```

### Critérios de aceite

- Carrinho usável em 360px.
- Carrinho não cobre bottom nav.
- Checkout acessível.
- Sem scroll horizontal.

---

## CARDAPIO-06.03 — Melhorar acessibilidade do carrinho

**Prioridade:** Média  
**Tipo:** Acessibilidade  
**Arquivo:** `public-cart.component.ts`

### Problema

`cart-summary` é uma `div` clicável.

### Direção recomendada

Trocar para `<button>` quando possível, ou adicionar semântica adequada.

### Melhor opção

```html
<button
  class="cart-summary"
  type="button"
  (click)="toggleExpand()"
  [attr.aria-expanded]="expanded"
  aria-controls="cart-details"
>
  ...
</button>
```

E:

```html
<div class="cart-details" id="cart-details" *ngIf="expanded">
  ...
</div>
```

### Critérios de aceite

- Carrinho funciona com teclado.
- Leitor de tela entende expansão.
- Foco é visível.

---

# EPIC CARDAPIO-07 — Mobile App Experience

## Objetivo

Fazer a experiência do cardápio no telefone parecer um aplicativo.

---

## CARDAPIO-07.01 — Criar bottom navigation mobile

**Prioridade:** Alta  
**Tipo:** Mobile UX  
**Arquivo:** `public-menu.component.ts`

### Ações

```text
Home
Topo
Carrinho
Dúvidas
```

### Comportamentos

- Home -> `/`
- Topo -> `#topo-cardapio`
- Carrinho -> `#carrinho` ou ação que leve visualmente ao carrinho quando houver item
- Dúvidas -> WhatsApp de dúvidas

### Observação importante

Se não houver utilitário de WhatsApp no cardápio, criar `whatsappDoubtUrl` usando telefone da loja ou um fallback real, mas sem transformar em pedido.

### Template sugerido

```html
<nav class="mobile-bottom-nav" aria-label="Navegação principal mobile do cardápio">
  <a routerLink="/" class="mobile-bottom-nav-item">
    <span aria-hidden="true">🏠</span>
    <strong>Home</strong>
  </a>

  <a href="#topo-cardapio" class="mobile-bottom-nav-item">
    <span aria-hidden="true">🥖</span>
    <strong>Topo</strong>
  </a>

  <a href="#carrinho" class="mobile-bottom-nav-item">
    <span aria-hidden="true">🛒</span>
    <strong>Carrinho</strong>
  </a>

  <a
    [href]="whatsappDoubtUrl"
    target="_blank"
    rel="noopener noreferrer"
    class="mobile-bottom-nav-item"
    aria-label="Tirar dúvidas pelo WhatsApp da Padaria Nova Aliança"
  >
    <span aria-hidden="true">💬</span>
    <strong>Dúvidas</strong>
  </a>
</nav>
```

### CSS sugerido

```css
.mobile-bottom-nav {
  display: none;
}

@media (max-width: 768px) {
  .mobile-bottom-nav {
    position: fixed;
    left: 12px;
    right: 12px;
    bottom: 12px;
    z-index: 1100;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 4px;
    padding: 8px;
    border-radius: 24px;
    background-color: rgba(255, 255, 255, 0.96);
    border: 1px solid rgba(156, 107, 63, 0.18);
    box-shadow: 0 16px 42px rgba(61, 43, 31, 0.22);
    backdrop-filter: blur(10px);
  }

  .mobile-bottom-nav-item {
    min-height: 56px;
    border-radius: 18px;
    color: #6b3f1d;
    text-decoration: none;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
    font-size: 0.75rem;
    font-weight: 800;
  }

  .mobile-bottom-nav-item span {
    font-size: 1.2rem;
    line-height: 1;
  }

  .mobile-bottom-nav-item strong {
    font-size: 0.72rem;
    line-height: 1;
  }

  .mobile-bottom-nav-item:hover,
  .mobile-bottom-nav-item:active {
    background-color: #fff7e8;
    color: #3d2b1f;
  }

  .mobile-bottom-nav-item:focus-visible {
    outline: 3px solid #f5a623;
    outline-offset: 3px;
  }
}
```

### Critérios de aceite

- Bottom nav aparece apenas no mobile.
- Não cobre carrinho.
- Não cobre checkout.
- Links funcionam.
- Visual parece app.

---

## CARDAPIO-07.02 — Espaçamento inferior seguro

**Prioridade:** Alta  
**Tipo:** UX Mobile  
**Arquivo:** `public-menu.component.ts`

### Tarefas técnicas

- Adicionar padding inferior para acomodar bottom nav e carrinho.
- Evitar conteúdo escondido no final da página.
- Garantir que última categoria continue visível.

### CSS sugerido

```css
@media (max-width: 768px) {
  .menu-page,
  .menu-container {
    padding-bottom: 132px;
  }
}
```

### Critérios de aceite

- Nenhum conteúdo fica escondido.
- Último produto é acessível.
- Carrinho e bottom nav não se sobrepõem.

---

# EPIC CARDAPIO-08 — Estados da página

## Objetivo

Padronizar loading, vazio e mensagens de apoio.

---

## CARDAPIO-08.01 — Melhorar loading

**Prioridade:** Média  
**Tipo:** UI  
**Arquivo:** `public-menu.component.ts`

### Texto atual

```text
Carregando cardápio...
```

### Texto recomendado

```text
Carregando produtos fresquinhos...
```

### Tarefas técnicas

- Alterar texto.
- Trocar spinner azul por dourado.
- Centralizar em card leve.

### CSS sugerido

```css
.loading-state {
  max-width: 420px;
  margin: 48px auto;
  padding: 40px 24px;
  border-radius: 24px;
  background-color: #ffffff;
  color: #6b3f1d;
  text-align: center;
  box-shadow: 0 12px 32px rgba(61, 43, 31, 0.1);
}

.spinner {
  border: 4px solid rgba(245, 166, 35, 0.18);
  border-top-color: #f5a623;
}
```

### Critérios de aceite

- Loading combina com a identidade.
- Não usa azul.
- Texto é mais humano.

---

## CARDAPIO-08.02 — Melhorar empty state

**Prioridade:** Média  
**Tipo:** UX  
**Arquivo:** `public-menu.component.ts`

### Texto recomendado

```text
Nenhum produto encontrado para sua busca.
Tente procurar por pão, café, bolo ou salgado.
```

### Tarefas técnicas

- Melhorar texto.
- Criar botão “Limpar busca” quando houver busca.
- Usar visual de card.
- Não criar erro assustador.

### Template sugerido

```html
<div class="empty-state" *ngIf="!loading && menu.length === 0">
  <div class="icon" aria-hidden="true">🔎</div>

  <p>Nenhum produto encontrado para sua busca.</p>
  <span>Tente procurar por pão, café, bolo ou salgado.</span>

  <button
    class="empty-clear-btn"
    *ngIf="searchQuery"
    type="button"
    (click)="clearSearch()"
  >
    Limpar busca
  </button>
</div>
```

### Critérios de aceite

- Cliente entende o que fazer.
- Busca pode ser limpa.
- Estado vazio não parece erro grave.

---

# EPIC CARDAPIO-09 — Acessibilidade

## Objetivo

Garantir que a página funcione com teclado e leitores de tela.

---

## CARDAPIO-09.01 — Foco visível

**Prioridade:** Alta  
**Tipo:** Acessibilidade  
**Arquivos:** todos os componentes do cardápio

### Elementos obrigatórios

- Logo/header.
- Link Home.
- Campo de busca.
- Botão limpar.
- Chips de categoria.
- Botão Adicionar.
- Carrinho.
- Botão remover item.
- Botão Finalizar Pedido.
- Bottom nav.

### CSS base

```css
a:focus-visible,
button:focus-visible,
input:focus-visible {
  outline: 3px solid #f5a623;
  outline-offset: 4px;
}
```

### Critérios de aceite

- TAB percorre elementos.
- Foco é visível.
- Contraste adequado.

---

## CARDAPIO-09.02 — Labels, alt e semântica

**Prioridade:** Alta  
**Tipo:** Acessibilidade

### Tarefas técnicas

- `aria-label` no botão limpar busca.
- `alt` nas imagens dos produtos.
- `aria-hidden="true"` em emojis decorativos.
- `button type="button"` em botões que não submetem formulário.
- Carrinho expansível com `aria-expanded`.
- Não usar `div` clicável sem semântica.

### Critérios de aceite

- Leitor de tela entende ações principais.
- Nenhum botão depende apenas de ícone.
- Carrinho é navegável por teclado.

---

# EPIC CARDAPIO-10 — Performance

## Objetivo

Evitar que o cardápio fique pesado, principalmente no celular.

---

## CARDAPIO-10.01 — Lazy loading de imagens

**Prioridade:** Alta  
**Tipo:** Performance  
**Arquivo:** `public-product-card.component.ts`

### Tarefas

- Adicionar `loading="lazy"` em imagens de produto.
- Adicionar `decoding="async"`.
- Manter `alt`.

### Critérios de aceite

- Imagens abaixo da dobra não carregam imediatamente.
- Página inicial do cardápio carrega mais rápido.
- Sem quebra visual.

---

## CARDAPIO-10.02 — Evitar animações pesadas

**Prioridade:** Média  
**Tipo:** Performance

### Tarefas

- Usar transições curtas.
- Evitar blur excessivo em muitos elementos.
- Evitar sombras pesadas em muitos cards.
- Usar `transform` com moderação.

### Critérios de aceite

- Scroll mobile fica fluido.
- Cards não travam em celulares simples.

---

# EPIC CARDAPIO-11 — SEO e título da página

## Objetivo

Melhorar a aba do navegador e metadados da página do cardápio.

---

## CARDAPIO-11.01 — Definir título do cardápio

**Prioridade:** Média  
**Tipo:** SEO  
**Arquivo:** `public-menu.component.ts`

### Título sugerido

```text
Cardápio Online | Padaria Nova Aliança em Magé
```

### Tarefas técnicas

- Importar `Title`.
- Injetar no construtor.
- Definir no `ngOnInit`.

### Exemplo

```ts
constructor(
  private readonly menuService: PublicMenuService,
  private readonly title: Title
) {
  ...
}

ngOnInit(): void {
  this.title.setTitle('Cardápio Online | Padaria Nova Aliança em Magé');
  this.loadMenu();
}
```

### Critérios de aceite

- Aba mostra título correto.
- Não quebra busca.
- Build compila.

---

## CARDAPIO-11.02 — Meta description

**Prioridade:** Média  
**Tipo:** SEO  
**Arquivo:** `public-menu.component.ts`

### Description sugerida

```text
Veja o cardápio online da Padaria e Lanchonete Nova Aliança em Parque Veneza, Magé. Pães, lanches, bolos, salgados e bebidas.
```

### Tarefas

- Importar `Meta`.
- Atualizar `description`.
- Atualizar `og:title`.
- Atualizar `og:description`.

### Critérios de aceite

- Tags existem.
- Texto é específico.
- Não usa texto genérico.

---

# EPIC CARDAPIO-12 — Padronização técnica

## Objetivo

Facilitar manutenção sem fazer refatoração prematura.

---

## CARDAPIO-12.01 — Criar tokens CSS locais

**Prioridade:** Média  
**Tipo:** Técnica  
**Arquivos:** componentes do cardápio

### Tarefas

- Criar variáveis no `:host`.
- Substituir hexadecimais repetidos.
- Manter consistência.

### Critérios de aceite

- Cores fáceis de alterar.
- Menos repetição.
- Sem alteração funcional.

---

## CARDAPIO-12.02 — Avaliar estilos compartilhados futuros

**Prioridade:** Baixa  
**Tipo:** Técnica futura

### Observação

A Home e o Cardápio passam a compartilhar padrões:

- header público;
- logo;
- paleta;
- botões;
- cards;
- bottom nav;
- foco visível;
- seção hero.

### Evolução futura

Avaliar:

```text
front-end/src/app/features/site-publico/shared/
front-end/src/styles/public-site.scss
PublicHeaderComponent
PublicBottomNavComponent
PublicButtonComponent
PublicSectionHeaderComponent
```

### Critérios futuros

- Menos duplicação.
- Mais consistência.
- Sem criar complexidade agora.

---

# EPIC CARDAPIO-13 — QA visual e funcional

## Objetivo

Garantir que a página ficou correta, responsiva e sem regressões.

---

## CARDAPIO-13.01 — Checklist de responsividade

**Prioridade:** Alta  
**Tipo:** QA

### Tamanhos obrigatórios

```text
360px
390px
430px
768px
1024px
1366px
```

### Checklist

```text
Sem scroll horizontal.
Header não quebra.
Logo aparece corretamente.
Busca funciona.
Botão limpar busca funciona.
Chips de categoria funcionam, se implementados.
Categorias aparecem bem.
Cards ficam legíveis.
Botão Adicionar tem área de toque confortável.
Produto esgotado fica claro.
Carrinho aparece ao adicionar produto.
Carrinho expande/recolhe.
Checkout continua acessível.
Bottom nav não cobre carrinho.
Último produto continua acessível.
Foco por teclado aparece.
```

### Critérios de aceite

- Página usável em celular.
- Página visualmente coerente no desktop.
- Build sem erros.

---

## CARDAPIO-13.02 — Checklist funcional

**Prioridade:** Alta  
**Tipo:** QA

### Fluxos obrigatórios

```text
Abrir /cardapio.
Carregar produtos do back-end.
Buscar por produto existente.
Limpar busca.
Buscar por produto inexistente.
Adicionar produto disponível.
Tentar adicionar produto indisponível.
Expandir carrinho.
Remover item do carrinho.
Finalizar pedido.
Voltar para Home.
```

### Critérios de aceite

- Nenhum fluxo principal quebrado.
- Não houve regressão visual crítica.
- Não houve regressão funcional.

---

# 5. Ordem recomendada de implementação

## Sprint 1 — Base visual e topo

```text
1. CARDAPIO-01.01 — Aplicar paleta quente no PublicMenuComponent
2. CARDAPIO-01.02 — Padronizar header do cardápio com logo
3. CARDAPIO-02.01 — Criar hero compacto
4. CARDAPIO-02.02 — Adicionar aviso de disponibilidade
5. CARDAPIO-03.01 — Estilizar campo de busca como app
```

## Sprint 2 — Categorias e cards

```text
6. CARDAPIO-03.02 — Criar chips horizontais de categorias
7. CARDAPIO-04.01 — Melhorar cabeçalho de categoria
8. CARDAPIO-04.02 — Ajustar grid responsivo
9. CARDAPIO-05.01 — Reestilizar card de produto
10. CARDAPIO-05.02 — Melhorar imagens e placeholders
11. CARDAPIO-05.03 — Melhorar preço e promoção
12. CARDAPIO-05.04 — Padronizar botão Adicionar
13. CARDAPIO-05.05 — Melhorar selo de esgotado
```

## Sprint 3 — Carrinho e mobile app

```text
14. CARDAPIO-06.01 — Reestilizar carrinho flutuante
15. CARDAPIO-06.02 — Melhorar carrinho no mobile
16. CARDAPIO-06.03 — Melhorar acessibilidade do carrinho
17. CARDAPIO-07.01 — Criar bottom navigation mobile
18. CARDAPIO-07.02 — Espaçamento inferior seguro
```

## Sprint 4 — Estados, acessibilidade e performance

```text
19. CARDAPIO-08.01 — Melhorar loading
20. CARDAPIO-08.02 — Melhorar empty state
21. CARDAPIO-09.01 — Foco visível
22. CARDAPIO-09.02 — Labels, alt e semântica
23. CARDAPIO-10.01 — Lazy loading de imagens
24. CARDAPIO-10.02 — Evitar animações pesadas
```

## Sprint 5 — SEO, técnica e QA

```text
25. CARDAPIO-11.01 — Definir título do cardápio
26. CARDAPIO-11.02 — Meta description
27. CARDAPIO-12.01 — Criar tokens CSS locais
28. CARDAPIO-12.02 — Avaliar estilos compartilhados futuros
29. CARDAPIO-13.01 — Checklist de responsividade
30. CARDAPIO-13.02 — Checklist funcional
```

---

# 6. Prompt para Claude — Sprint 1

```text
Implemente a Sprint 1 do backlog de estilização do Cardápio Online.

Escopo:
1. CARDAPIO-01.01 — Aplicar paleta quente no PublicMenuComponent
2. CARDAPIO-01.02 — Padronizar header do cardápio com logo
3. CARDAPIO-02.01 — Criar hero compacto
4. CARDAPIO-02.02 — Adicionar aviso de disponibilidade
5. CARDAPIO-03.01 — Estilizar campo de busca como app

Arquivos:
- front-end/src/app/features/site-publico/pages/public-menu/public-menu.component.ts

Não alterar:
- back-end
- endpoints
- contratos
- ProductCard
- PublicCart
- checkout
- fluxo de busca
- PublicMenuService.getMenu

Objetivo:
Fazer a página /cardapio parecer visualmente conectada à Home da Padaria Nova Aliança, usando paleta quente, logo no header, hero compacto e busca com aparência de aplicativo mobile.

Critérios obrigatórios:
- /cardapio continua carregando produtos do back-end.
- Busca continua funcionando.
- Header usa logo em assets/images/site/logo.png.
- Azul não é cor principal.
- Funciona em 360px, 390px, 430px, 768px, 1024px e 1366px.
- npm run build passa sem erros.

Ao final, informe:
1. Arquivos alterados.
2. Resumo visual.
3. Comandos executados.
4. Pontos não implementados.
5. Pendências.
```

---

# 7. Prompt para Claude — Sprint 2

```text
Implemente a Sprint 2 do backlog de estilização do Cardápio Online.

Escopo:
1. CARDAPIO-03.02 — Criar chips horizontais de categorias
2. CARDAPIO-04.01 — Melhorar cabeçalho de categoria
3. CARDAPIO-04.02 — Ajustar grid responsivo
4. CARDAPIO-05.01 — Reestilizar card de produto
5. CARDAPIO-05.02 — Melhorar imagens e placeholders
6. CARDAPIO-05.03 — Melhorar preço e promoção
7. CARDAPIO-05.04 — Padronizar botão Adicionar
8. CARDAPIO-05.05 — Melhorar selo de esgotado

Arquivos:
- front-end/src/app/features/site-publico/pages/public-menu/public-menu.component.ts
- front-end/src/app/features/site-publico/components/public-product-card/public-product-card.component.ts

Não alterar:
- back-end
- endpoints
- contratos
- carrinho
- checkout
- lógica de addToCart

Objetivo:
Melhorar categorias, grid e cards de produto para ficarem coerentes com a identidade visual da Home.

Critérios obrigatórios:
- Produto continua sendo adicionado ao carrinho.
- Busca continua funcionando.
- Produtos continuam vindo do back-end.
- Cards não usam azul como cor principal.
- Imagens usam lazy loading.
- Produtos sem imagem têm placeholder quente.
- Produto esgotado continua claro.
- npm run build passa.

Ao final, informe:
1. Arquivos alterados.
2. Ajustes nos cards.
3. Ajustes nas categorias.
4. Testes realizados.
5. Pendências.
```

---

# 8. Prompt para Claude — Sprint 3

```text
Implemente a Sprint 3 do backlog de estilização do Cardápio Online.

Escopo:
1. CARDAPIO-06.01 — Reestilizar carrinho flutuante
2. CARDAPIO-06.02 — Melhorar carrinho no mobile
3. CARDAPIO-06.03 — Melhorar acessibilidade do carrinho
4. CARDAPIO-07.01 — Criar bottom navigation mobile
5. CARDAPIO-07.02 — Espaçamento inferior seguro

Arquivos:
- front-end/src/app/features/site-publico/pages/public-menu/public-menu.component.ts
- front-end/src/app/features/site-publico/components/public-cart/public-cart.component.ts

Não alterar:
- back-end
- endpoints
- contratos
- checkout
- CartService
- lógica de subtotal
- lógica de remover item

Objetivo:
Fazer o carrinho e a navegação mobile parecerem uma experiência de aplicativo, sem quebrar o fluxo de pedido.

Critérios obrigatórios:
- Carrinho aparece ao adicionar produto.
- Carrinho expande e recolhe.
- Remover item continua funcionando.
- Finalizar Pedido continua indo para /checkout.
- Bottom nav aparece somente no mobile.
- Bottom nav não cobre carrinho.
- Nenhum conteúdo fica escondido.
- npm run build passa.

Ao final, informe:
1. Arquivos alterados.
2. Como ficou o carrinho.
3. Como ficou a bottom nav.
4. Como foi evitada sobreposição no mobile.
5. Pendências.
```

---

# 9. Prompt para Claude — Sprint 4

```text
Implemente a Sprint 4 do backlog de estilização do Cardápio Online.

Escopo:
1. CARDAPIO-08.01 — Melhorar loading
2. CARDAPIO-08.02 — Melhorar empty state
3. CARDAPIO-09.01 — Foco visível
4. CARDAPIO-09.02 — Labels, alt e semântica
5. CARDAPIO-10.01 — Lazy loading de imagens
6. CARDAPIO-10.02 — Evitar animações pesadas

Arquivos:
- front-end/src/app/features/site-publico/pages/public-menu/public-menu.component.ts
- front-end/src/app/features/site-publico/components/public-product-card/public-product-card.component.ts
- front-end/src/app/features/site-publico/components/public-cart/public-cart.component.ts

Não alterar:
- back-end
- endpoints
- contratos
- regras de carrinho
- checkout

Objetivo:
Melhorar estados visuais, acessibilidade e performance da página /cardapio.

Critérios obrigatórios:
- Loading usa identidade da padaria.
- Empty state orienta o cliente.
- Botão limpar busca tem aria-label.
- Imagens têm alt, lazy e decoding async.
- Foco visível em links, botões, input e carrinho.
- Carrinho é acessível por teclado.
- npm run build passa.

Ao final, informe:
1. Arquivos alterados.
2. Estados visuais melhorados.
3. Acessibilidade aplicada.
4. Performance ajustada.
5. Pendências.
```

---

# 10. Prompt para Claude — Sprint 5

```text
Implemente a Sprint 5 do backlog de estilização do Cardápio Online.

Escopo:
1. CARDAPIO-11.01 — Definir título do cardápio
2. CARDAPIO-11.02 — Meta description
3. CARDAPIO-12.01 — Criar tokens CSS locais
4. CARDAPIO-12.02 — Avaliar estilos compartilhados futuros
5. CARDAPIO-13.01 — Checklist de responsividade
6. CARDAPIO-13.02 — Checklist funcional

Arquivos:
- front-end/src/app/features/site-publico/pages/public-menu/public-menu.component.ts
- front-end/src/app/features/site-publico/components/public-product-card/public-product-card.component.ts
- front-end/src/app/features/site-publico/components/public-cart/public-cart.component.ts

Não alterar:
- back-end
- endpoints
- contratos

Objetivo:
Finalizar SEO, organização visual e validação da página /cardapio.

Critérios obrigatórios:
- Aba do navegador mostra título específico.
- Meta description é específica da Padaria Nova Aliança.
- Cores ficam organizadas em tokens locais.
- QA em 360px, 390px, 430px, 768px, 1024px e 1366px.
- Sem scroll horizontal.
- Fluxos principais funcionando.
- npm run build passa.

Ao final, informe:
1. Arquivos alterados.
2. SEO aplicado.
3. Tokens criados.
4. Checklist responsivo.
5. Checklist funcional.
6. Pendências.
```

---

# 11. Comandos de validação

```bash
cd front-end
npm install
npm run build
```

## Validação manual

```text
1. Abrir /cardapio.
2. Confirmar que produtos carregam.
3. Buscar produto existente.
4. Limpar busca.
5. Buscar produto inexistente.
6. Adicionar produto disponível.
7. Ver carrinho aparecer.
8. Expandir carrinho.
9. Remover item.
10. Finalizar pedido.
11. Voltar para Home.
12. Testar mobile em 360px.
13. Testar mobile em 390px.
14. Testar mobile em 430px.
15. Testar tablet em 768px.
16. Testar desktop em 1024px.
17. Testar desktop em 1366px.
18. Navegar com TAB.
19. Confirmar foco visível.
20. Confirmar que não há scroll horizontal.
```

---

# 12. Definição de pronto geral

A estilização do cardápio estará pronta quando:

```text
A página /cardapio estiver visualmente coerente com a Home.
A paleta quente estiver aplicada.
O header usar a logo.
A busca estiver com visual de app.
As categorias estiverem claras.
Os cards estiverem com visual de padaria.
O botão Adicionar não for azul.
O carrinho estiver coerente com a identidade visual.
O mobile parecer aplicativo.
Acessibilidade básica estiver coberta.
SEO básico estiver aplicado.
Não houver scroll horizontal.
O build passar.
O cardápio continuar buscando dados reais do back-end.
Carrinho e checkout continuarem funcionando.
```

---

# 13. Observações finais

Este backlog não deve ser tratado como uma refatoração funcional do cardápio.

O foco é:

```text
Estilização.
Padronização visual.
UX mobile.
Acessibilidade.
Performance básica.
Consistência com a Home.
```

Mudanças maiores devem ficar para backlog futuro, por exemplo:

- novos endpoints;
- admin de imagens;
- categorias com destaque;
- filtros avançados;
- rota de encomendas integrada ao back-end;
- componentes compartilhados;
- design system público extraído para arquivos globais.
