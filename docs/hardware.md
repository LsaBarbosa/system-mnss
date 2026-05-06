# Hardware — Sistema Nova Aliança

## 1. Objetivo

Este documento descreve a estrutura física recomendada para o Sistema Nova Aliança, contemplando:

- Servidor local
- PDV
- KDS
- Impressoras
- Gaveta de dinheiro
- Leitor de código de barras
- Rede local
- Internet
- Nobreak
- Contingência

## 2. Decisão principal

Como o sistema será híbrido local + online, a padaria precisará de uma estrutura local mínima para operar mesmo sem internet.

```text
Servidor local
+
PDV
+
KDS
+
Rede interna estável
+
Nobreak
+
Internet principal e backup
```

## 3. Topologia recomendada

```text
Internet principal
      │
Modem da operadora
      │
Roteador principal ───────── Internet backup 4G/5G
      │
Switch Gigabit
      │
├── Servidor local
├── PDV caixa
├── Impressora caixa
├── Impressora cozinha
├── Access Point Wi-Fi
└── KDS monitor/mini PC ou tablet via Wi-Fi
```

## 4. Servidor local

O servidor local é responsável por rodar:

- API local
- PostgreSQL local
- RabbitMQ local
- Redis local
- Nginx local
- Sync Worker local
- Front-ends locais, se necessário

## 5. Configuração mínima do servidor local

```text
Tipo: Mini PC ou desktop compacto
Processador: Intel i5 ou Ryzen 5
Memória RAM: 16 GB
Armazenamento: SSD 512 GB
Rede: Gigabit Ethernet
Sistema: Ubuntu Server
Energia: Nobreak obrigatório
```

## 6. Configuração ideal do servidor local

```text
Tipo: Mini PC ou desktop compacto
Processador: Intel i5/i7 ou Ryzen 5/7
Memória RAM: 32 GB
Armazenamento principal: SSD NVMe 1 TB
Armazenamento secundário: SSD/HD para backup local
Rede: Gigabit Ethernet
Sistema: Ubuntu Server
Energia: Nobreak obrigatório
```

## 7. PDV

O PDV será usado no caixa.

### Hardware mínimo

```text
Computador ou mini PC
Monitor 15" ou superior
Teclado
Mouse
Impressora térmica
Gaveta de dinheiro
Leitor de código de barras
Maquininha
Nobreak
Rede cabeada
```

### Configuração do computador PDV

```text
Processador: Intel i3/i5 ou Ryzen 3/5
Memória RAM: 8 GB mínimo
SSD: 240 GB mínimo
Sistema: Windows ou Linux
Rede: Ethernet
Navegador atualizado
```

### Configuração ideal

```text
Processador: Intel i5 ou Ryzen 5
Memória RAM: 16 GB
SSD: 480 GB ou superior
Monitor touchscreen opcional
Rede cabeada
```

## 8. Impressora térmica

Recomendação:

```text
Tipo: Térmica 80mm
Conexão: Ethernet preferencialmente
Compatibilidade: ESC/POS
Corte automático: recomendado
```

### Uso

- Cupom simples
- Comprovante de venda
- Fechamento de caixa
- Sangria
- Suprimento
- Pedido de cozinha, se necessário

### Decisão

Preferir impressora Ethernet para permitir uso em rede.

Evitar impressora Wi-Fi para operação crítica.

## 9. Gaveta de dinheiro

A gaveta normalmente é ligada na impressora.

```text
PDV finaliza venda em dinheiro
↓
Sistema envia comando para impressora
↓
Impressora aciona gaveta
```

Recomendação:

```text
Gaveta metálica
Conexão RJ11/RJ12
Separadores para notas e moedas
```

## 10. Leitor de código de barras

Recomendação:

```text
Leitor 2D USB
```

Motivo:

- Lê código de barras comum.
- Lê QR Code.
- Tem baixo custo.
- É mais estável que Bluetooth.

## 11. Maquininha ou pinpad

### Fase inicial

Usar maquininha separada.

Fluxo:

```text
Operador registra venda
↓
Digita valor na maquininha
↓
Cliente paga
↓
Operador confirma no sistema
```

### Fase futura

Integrar pinpad.

Fluxo:

```text
Sistema envia valor para pinpad
↓
Cliente paga
↓
Sistema recebe confirmação automática
↓
Venda é finalizada
```

## 12. KDS

O KDS pode usar tablet ou monitor com mini PC.

## 12.1 Opção econômica

```text
Tablet Android 10" ou 11"
Wi-Fi estável
Suporte de parede
Carregador fixo
```

Vantagens:

- Menor custo
- Fácil instalação
- Tela touch

Desvantagens:

- Tela menor
- Depende do Wi-Fi
- Menos robusto para ambiente de cozinha

## 12.2 Opção robusta

```text
Mini PC
Monitor 24"
Suporte de parede
Mouse sem fio ou touch
Rede cabeada
```

Vantagens:

- Mais estável
- Tela maior
- Melhor para muitos pedidos
- Pode usar cabo de rede

Desvantagens:

- Custo maior
- Instalação mais trabalhosa

## 13. Impressora de cozinha

Mesmo usando KDS, recomenda-se avaliar uma impressora de cozinha como contingência.

Uso:

- KDS indisponível
- Tablet descarregado
- Problema na tela
- Equipe prefere papel em alguns setores

Recomendação:

```text
Impressora térmica Ethernet
Instalada em local protegido
Papel 80mm
```

## 14. Rede local

A rede é crítica para PDV e KDS.

### Equipamentos

```text
Roteador principal
Switch Gigabit
Access Point Wi-Fi
Cabos de rede
Nobreak para modem/roteador/switch
```

### Regras

- Servidor local deve usar cabo.
- PDV deve usar cabo.
- Impressoras devem usar cabo ou USB.
- KDS robusto deve usar cabo.
- Tablets podem usar Wi-Fi.
- Modem da operadora não deve ser o único roteador.
- Equipamentos de rede devem ficar no nobreak.

## 15. Internet

### Internet principal

Recomendado:

```text
Fibra ótica
Boa estabilidade
Upload razoável
IP fixo não é obrigatório
```

### Internet backup

Recomendado:

```text
Roteador 4G/5G
Chip de dados
Failover manual ou automático
```

## 16. Nobreak

Nobreak é obrigatório para:

- Servidor local
- Roteador
- Switch
- PDV

### Nobreaks recomendados

```text
1 nobreak para servidor + rede
1 nobreak para PDV + impressora
```

Se possível:

```text
1 nobreak adicional para KDS robusto
```

## 17. Distribuição física

### Caixa

```text
Monitor PDV
Teclado/mouse ou touch
Impressora térmica
Gaveta de dinheiro
Leitor de código de barras
Maquininha
Nobreak
Rede cabeada
```

### Cozinha/chapa

```text
KDS
Suporte fixo
Fonte protegida
Opcional: impressora de cozinha
```

### Escritório/local protegido

```text
Servidor local
Roteador
Switch
Nobreak
Backup local
```

## 18. Ambiente da cozinha

Cuidados:

- Proteger contra gordura.
- Evitar proximidade com chapa/fritura.
- Fixar cabos.
- Evitar tomada exposta.
- Usar suporte de parede.
- Manter limpeza fácil.
- Evitar equipamentos frágeis em área quente.

## 19. Kit mínimo para MVP

```text
1 servidor local
1 computador PDV
1 monitor PDV
1 impressora térmica
1 gaveta de dinheiro
1 leitor código de barras
1 tablet KDS
1 roteador bom
1 switch Gigabit
1 nobreak para servidor/rede
1 nobreak para caixa
1 internet backup 4G/5G
```

## 20. Kit intermediário

```text
1 servidor local
1 PDV completo
1 KDS na chapa
1 KDS na expedição
1 impressora caixa
1 impressora cozinha
1 roteador
1 switch Gigabit
1 access point
2 nobreaks
1 internet backup
```

## 21. Kit robusto

```text
1 servidor local mais forte
2 PDVs
2 ou 3 KDS
2 impressoras no caixa
1 impressora cozinha
1 roteador empresarial
1 switch Gigabit
1 ou 2 access points
3 nobreaks
Internet principal
Internet backup
Backup local em segundo disco
```

## 22. Plano de contingência

### Se a internet cair

Continua funcionando:

- PDV
- Caixa
- KDS local
- Impressão
- Vendas presenciais

Afetado:

- Site
- WhatsApp
- Pedidos online novos
- Pagamentos online
- Sincronização

### Se o KDS cair

Alternativas:

- Impressão de cozinha
- Tablet reserva
- Visualização no PDV/Admin

### Se a impressora cair

Alternativas:

- KDS continua recebendo
- Comprovante digital
- Impressora reserva futura

### Se o servidor local cair

Impacto alto.

Mitigações:

- Nobreak
- Backup diário
- Imagem Docker
- Documentação de restauração
- Equipamento reserva futuramente

## 23. Recomendação final

Para iniciar o projeto com segurança:

```text
Servidor local dedicado
PDV cabeado
Impressora térmica Ethernet
KDS em tablet ou monitor
Roteador próprio
Switch Gigabit
Nobreak
Internet backup
```

A rede e energia são tão importantes quanto o software.  
Sem rede local estável, PDV e KDS podem apresentar falhas mesmo com o sistema bem desenvolvido.
