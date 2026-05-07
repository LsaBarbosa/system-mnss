# PWA (Progressive Web App) Setup

## O que foi implementado

1. **Service Worker** (`src/service-worker.ts`)
   - Network-first para APIs (sync com online-app)
   - Cache-first para assets (imagens, CSS, JS)
   - Offline fallback response

2. **Service Worker Registration** (`src/register-service-worker.ts`)
   - Registra o SW ao carregar a app
   - Monitora atualizações
   - Notifica usuário quando nova versão está disponível

## Status de Integração

✅ **COMPLETO** - Todos os passos de integração foram implementados:

### 1. ✅ Integración no Angular App (main.ts)
Service Worker é registrado após inicialização da app em `src/main.ts`

### 2. ✅ Configuração Angular (angular.json)
`serviceWorker: "ngsw-config.json"` adicionado nas build options

### 3. ✅ Arquivo ngsw-config.json
Criado com estratégias:
- **app**: prefetch (app shell cacheado)
- **assets**: lazy loading (imagens, fonts)
- **api**: performance strategy (cache de 5 minutos)

### 4. ✅ Arquivo manifest.json
Criado em `src/assets/manifest.json` com:
- Metadata da app (nome, descrição, display standalone)
- Icons (192x192, 512x512, maskable variants)
- Screenshots para install prompt
- App shortcuts (PDV, KDS)

### 5. ✅ Index.html atualizado
- Link to manifest.json
- Theme-color meta tag (#1976d2)
- Description meta tag
- Favicon reference

## Próximo Passo: Gerar Icons PWA

Para ativar o install prompt do browser, precisamos gerar icons em diversos tamanhos:

### Icons Necessários (coloque em `src/assets/`)

1. **icon-192x192.png** - Icon padrão (192x192px)
2. **icon-512x512.png** - Icon grande (512x512px)
3. **icon-192x192-maskable.png** - Icon adaptável para notch (192x192px)
4. **icon-512x512-maskable.png** - Icon adaptável para notch (512x512px)
5. **icon-96x96.png** - Icon para shortcuts (96x96px)
6. **screenshot-540x720.png** - Screenshot mobile (540x720px)
7. **screenshot-1080x1440.png** - Screenshot mobile grande (1080x1440px)

### Gerar Icons (Tool Recommendations)

- **ImageMagick**: `convert logo.png -resize 192x192 icon-192x192.png`
- **Online**: https://www.favicon-generator.org/ ou https://www.pwabuilder.com/
- **Design Tool**: Figma, Illustrator com export para múltiplos tamanhos

⚠️ **Nota**: O browser só mostra install prompt quando:
1. HTTPS está ativado (ou localhost dev)
2. Service Worker está registrado
3. manifest.json é referenciado
4. Icons estão presentes

## Benefícios Offline
- ✅ PDV funciona offline (cache de dados)
- ✅ KDS acessa dados cacheados
- ✅ APIs retornam dados cacheados quando offline
- ✅ App fica funcional mesmo sem internet

## Teste
1. Build: `npm run build`
2. Serve: `npx http-server dist/novo-alianca -p 8080`
3. Abrir DevTools → Application → Service Workers
4. Desativar internet e testar funcionalidade offline
