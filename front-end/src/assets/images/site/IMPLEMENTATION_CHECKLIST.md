# Checklist de Implementação de Imagens — HOME-12

## Status MVP

- [x] Documentação criada (README.md)
- [x] Padrão visual definido
- [x] Padrão de performance definido
- [x] Padrão de alt text definido
- [x] Padrão de lazy loading definido
- [x] Imagem placeholder (SVG) em uso
- [ ] Fotos reais tiradas
- [ ] Fotos reais otimizadas e comprimidas
- [ ] Fotos reais uploaded para assets
- [ ] Template atualizado com URLs de produção
- [ ] Performance testada em mobile
- [ ] Performance testada em desktop (Lighthouse)

## Imagens Planejadas vs. Implementadas

### Hero Principal

- [ ] **Arquivo**: `hero-padaria.webp`
- [ ] **Localização no código**: `src/app/features/site-publico/pages/site-home/site-home.component.ts`
- [ ] **Propriedade**: `readonly heroImageUrl = 'assets/images/site/hero-padaria.webp';`
- [ ] **Atributos HTML**: `loading="eager"`, `decoding="async"`, `alt` descritivo
- [ ] **Tamanho**: até 250 KB
- [ ] **Proporção**: 4:3 ou 16:10
- **Status**: ⏳ Placeholder SVG em uso

### Seção de Pães (Featured Products)

- [ ] **Arquivo**: `paes.webp`
- [ ] **Localização no código**: Não implementado ainda (seria em featured products ou seção nova)
- [ ] **Atributos HTML**: `loading="lazy"`, `decoding="async"`, `alt` descritivo
- [ ] **Tamanho**: até 120 KB
- [ ] **Proporção**: 4:3 ou 1:1
- **Status**: ⏳ Não implementado ainda (pode ser futuro)

### Seção de Lanches

- [ ] **Arquivo**: `lanches.webp`
- [ ] **Localização no código**: Não implementado ainda
- [ ] **Atributos HTML**: `loading="lazy"`, `decoding="async"`, `alt` descritivo
- [ ] **Tamanho**: até 120 KB
- [ ] **Proporção**: 4:3 ou 1:1
- **Status**: ⏳ Não implementado ainda (pode ser futuro)

### Seção de Salgados

- [ ] **Arquivo**: `salgados.webp`
- [ ] **Localização no código**: Não implementado ainda
- [ ] **Atributos HTML**: `loading="lazy"`, `decoding="async"`, `alt` descritivo
- [ ] **Tamanho**: até 120 KB
- [ ] **Proporção**: 4:3 ou 1:1
- **Status**: ⏳ Não implementado ainda (pode ser futuro)

### Seção de Bolos e Tortas

- [ ] **Arquivo**: `bolos-tortas.webp`
- [ ] **Localização no código**: Não implementado ainda
- [ ] **Atributos HTML**: `loading="lazy"`, `decoding="async"`, `alt` descritivo
- [ ] **Tamanho**: até 120 KB
- [ ] **Proporção**: 4:3 ou 1:1
- **Status**: ⏳ Não implementado ainda (pode ser futuro)

### Seção de Localização (Fachada)

- [ ] **Arquivo**: `fachada.webp`
- [ ] **Localização no código**: Não implementado ainda
- [ ] **Atributos HTML**: `loading="lazy"`, `decoding="async"`, `alt` descritivo
- [ ] **Tamanho**: até 120 KB
- [ ] **Proporção**: 4:3
- **Status**: ⏳ Não implementado ainda (pode ser futuro)

### Seção de Balcão/Vitrine

- [ ] **Arquivo**: `balcao.webp`
- [ ] **Localização no código**: Não implementado ainda
- [ ] **Atributos HTML**: `loading="lazy"`, `decoding="async"`, `alt` descritivo
- [ ] **Tamanho**: até 120 KB
- [ ] **Proporção**: 4:3 ou 16:9
- **Status**: ⏳ Não implementado ainda (pode ser futuro)

## Passos para Completar MVP+

Quando as fotos reais chegarem:

### Fase 1: Obtenção e Otimização

1. **Tirar fotos reais**
   - [ ] Foto do hero (vitrine, pães, lanches)
   - [ ] Foto da fachada
   - [ ] Foto do balcão
   - [ ] Demais fotos de produtos

2. **Otimizar imagens**
   - [ ] Converter para `.webp` com ffmpeg
   - [ ] Comprimir até tamanho especificado
   - [ ] Validar proporções
   - [ ] Testar qualidade visual

3. **Upload de arquivos**
   - [ ] Fazer upload em `front-end/src/assets/images/site/`
   - [ ] Nomear conforme padrão sugerido
   - [ ] Validar permission e integridade

### Fase 2: Implementação no Template

1. **Hero Principal**
   - [ ] Atualizar `heroImageUrl` no componente
   - [ ] Testar rendering e loading
   - [ ] Testar em mobile
   - [ ] Testar em desktop

2. **Imagens Futuras** (conforme prioridade)
   - [ ] Criação de seções/componentes para exibir fotos
   - [ ] Atualização de templates
   - [ ] Testes de performance

### Fase 3: Validação

1. **Performance**
   - [ ] Executar Lighthouse no desktop
   - [ ] Executar Lighthouse no mobile
   - [ ] Comparar com metrics de baseline
   - [ ] Validar Core Web Vitals

2. **Acessibilidade**
   - [ ] Validar alt texts em screen reader
   - [ ] Validar contraste
   - [ ] Validar navegação

3. **Responsividade**
   - [ ] Testar em mobile (320px+)
   - [ ] Testar em tablet (768px)
   - [ ] Testar em desktop (1920px+)

## Links de Referência

- Documentação: `/front-end/src/assets/images/site/README.md`
- Home Component: `/front-end/src/app/features/site-publico/pages/site-home/site-home.component.ts`

## Notas

- MVP usa placeholder SVG para hero.
- Home já está otimizada para aceitar imagens reais.
- Nenhuma mudança quebra o build atual.
- Quando fotos reais chegarem, apenas UPDATE de URLs necessário.

---

**Última atualização**: 2026-05-11
**Criado por**: HOME-12 (Imagens e conteúdo visual)
