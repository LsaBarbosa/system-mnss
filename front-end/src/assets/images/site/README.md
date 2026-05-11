# Imagens da Home — Padaria Nova Aliança

## Objetivo

Organizar as imagens reais usadas na landing page pública da Padaria e Lanchonete Nova Aliança.

## Fotos Necessárias

| Uso | Arquivo Sugerido | Descrição |
|---|---|---|
| Hero principal | `hero-padaria.webp` | Foto forte com pães, lanches ou vitrine bem apresentada |
| Pães | `paes.webp` | Foto de pães frescos |
| Lanches | `lanches.webp` | Foto de misto quente, pão com queijo ou lanche similar |
| Salgados | `salgados.webp` | Foto de salgados bem iluminados |
| Bolos e tortas | `bolos-tortas.webp` | Foto de bolos, tortas ou doces |
| Fachada | `fachada.webp` | Foto externa da padaria para localização |
| Balcão | `balcao.webp` | Foto do balcão/vitrine organizada |

## Padrão Visual

### Qualidade e Apresentação

- **Preferir fotos reais da padaria** em vez de imagens genéricas.
- Usar boa iluminação natural ou lighting profissional.
- Evitar fundos bagunçados ou desordenados.
- Evitar produtos cortados ou parcialmente visíveis.
- Manter proporção consistente entre imagens.
- Usar bordas arredondadas no layout CSS para softer appearance.
- Manter tons quentes e aparência típica de padaria artesanal.

### Inspiração de Tom

- Cores quentes (#6b3f1d marrom, #f5a623 dourado, #e67e22 laranja, #fff7e8 creme).
- Produtos bem centrados e em destaque.
- Background neutro ou complementar.
- Foco no detalhe do produto (frescor, textura, apresentação).

## Performance

### Formato

- **Preferencial**: `.webp` (melhor compressão e qualidade).
- **Alternativa**: `.jpg` otimizado com quality 85-90%.
- **Evitar**: `.png` não otimizado, `.svg` para fotos.

### Tamanho de Arquivo

Antes de fazer upload, comprimir as imagens:

- **Hero principal**: idealmente até **250 KB** (máximo 350 KB).
- **Cards/seções**: idealmente até **120 KB** (máximo 180 KB).

#### Ferramentas recomendadas para compressão

- [TinyPNG](https://tinypng.com/) — fácil, web-based.
- [ImageMagick](https://imagemagick.org/) — CLI, batch processing.
- [FFmpeg](https://ffmpeg.org/) — para webp com qualidade controlada.

Comando exemplo com ffmpeg:

```bash
ffmpeg -i hero-padaria.jpg -c:v libwebp -q:v 80 hero-padaria.webp
```

### Proporções

- **Hero**: 4:3 (1200×900 ou 1600×1200) ou 16:10 (1600×1000).
- **Cards/seções**: 4:3 (400×300, 600×450) ou 1:1 (400×400).
- **Fachada**: 4:3 ou similar (para naturalidade).
- **Balcão**: 4:3 ou 16:9 (dependendo da composição).

## Implementação no Template

### Imagem Principal do Hero

Use `loading="eager"` **apenas** para imagens acima da dobra:

```html
<img
  src="assets/images/site/hero-padaria.webp"
  alt="Pães, lanches e bolos frescos da Padaria Nova Aliança"
  loading="eager"
  decoding="async"
/>
```

### Imagens Abaixo da Dobra

Use `loading="lazy"` para melhor performance:

```html
<img
  src="assets/images/site/paes.webp"
  alt="Pães frescos produzidos diariamente na Padaria Nova Aliança"
  loading="lazy"
  decoding="async"
/>
```

### Atributos Obrigatórios

```html
<img
  src="assets/images/site/exemplo.webp"
  alt="Descrição clara do conteúdo"
  loading="lazy"
  decoding="async"
  width="400"
  height="300"
/>
```

#### Explicação dos atributos

- **`src`**: Caminho relativo ao asset.
- **`alt`**: Texto alternativo descritivo (acessibilidade, SEO, fallback).
- **`loading`**: `eager` (hero), `lazy` (resto).
- **`decoding`**: `async` para não bloquear render.
- **`width` / `height`**: Evita layout shift enquanto imagem carrega.

## Alt Text — Guia

Exemplos de bons `alt` texts:

- ❌ "imagem"
- ❌ "hero.jpg"
- ✅ "Pães, lanches e bolos frescos da Padaria Nova Aliança"
- ✅ "Pães franceses quentes saindo do forno"
- ✅ "Misto quente apetitoso servido na Padaria Nova Aliança"
- ✅ "Salgados crocantes: coxinhas, pastéis e empadas"
- ✅ "Bolos e tortas artesanais da padaria"
- ✅ "Fachada da Padaria Nova Aliança em Parque Veneza, Magé"
- ✅ "Balcão organizado com vitrines de pães e salgados"

**Dica**: Alt text deve descrever o que a imagem mostra, não substituir o conteúdo texto.

## Checklist para Novo Upload

Antes de fazer upload de uma imagem, verificar:

- [ ] Foto é real da Padaria Nova Aliança.
- [ ] Imagem tem boa iluminação.
- [ ] Produtos estão bem centralizados e visíveis.
- [ ] Sem fundo bagunçado ou distrações.
- [ ] Formato: `.webp` ou `.jpg` otimizado.
- [ ] Tamanho: até 250 KB (hero) ou 120 KB (cards).
- [ ] Proporção: 4:3 ou 16:10 para hero, 4:3 ou 1:1 para cards.
- [ ] Alt text descritivo adicionado no HTML.
- [ ] `loading="lazy"` para imagens abaixo da dobra.
- [ ] `loading="eager"` apenas para hero.
- [ ] `decoding="async"` presente.

## Estrutura de Arquivo

```
front-end/src/assets/images/site/
├── README.md (este arquivo)
├── hero-padaria-placeholder.svg (MVP, será substituído)
├── hero-padaria.webp
├── paes.webp
├── lanches.webp
├── salgados.webp
├── bolos-tortas.webp
├── fachada.webp
└── balcao.webp
```

## Próximos Passos

1. Tirar fotos reais de boa qualidade da padaria.
2. Comprimir e otimizar em `.webp`.
3. Nomear conforme sugestão.
4. Fazer upload na pasta.
5. Atualizar references nos templates Angular.
6. Testar loading em mobile e desktop.
7. Monitorar performance com PageSpeed/Lighthouse.

## Referências

- [MDN: img element](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img)
- [WebP format](https://developers.google.com/speed/webp)
- [Web Performance Best Practices](https://web.dev/performance/)
- [Core Web Vitals](https://web.dev/vitals/)

---

**Última atualização**: 2026-05-11
**Responsável**: Equipe de Frontend
**Status**: MVP com placeholders, aguardando fotos reais
