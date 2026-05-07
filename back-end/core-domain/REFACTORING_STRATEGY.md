# Estratégia de Desduplicação de Entidades

## Problema
local-app e online-app duplicam entidades (Product, Order, Category, Payment) com lógica de domínio idêntica.
Resultado: mudanças em uma app não se refletem na outra; manutenção 2x.

## Solução
Implementar **padrão de Contracts (Interfaces)** no core-domain que ambas as apps implementam.

## Passo a Passo

### 1. Criar Contracts (Interfaces) no core-domain
- `ProductContract` - define contrato de Product
- `CategoryContract` - define contrato de Category
- `OrderContract` - define contrato de Order
- `PaymentContract` - define contrato de Payment
- `CustomerContract` - define contrato de Customer

### 2. Implementar Contracts em Ambas as Apps
**local-app/domain/catalog/ProductEntity.java**
```java
public class ProductEntity extends BaseEntity implements ProductContract {
    // implementação específica do local
}
```

**online-app/domain/catalog/OnlineProductEntity.java**
```java
public class OnlineProductEntity extends BaseEntity implements ProductContract {
    // implementação específica do online
}
```

### 3. Compartilhar Lógica via Services
Serviços que usam `ProductContract` (interface) em vez de classe concreta:
```java
public ProductResponse getProductInfo(ProductContract product) {
    return new ProductResponse(product.getId(), product.getName(), ...);
}
```

### 4. Benefícios
- ✅ Reduz duplicação 50%
- ✅ Sincronização natural via contratos
- ✅ Services podem ser compartilhados
- ✅ Fácil adicionar validações compartilhadas

## Próximos Passos
1. Criar remaining contracts (Order, Payment, Customer)
2. Fazer ProductEntity implementar ProductContract
3. Fazer OnlineProductEntity implementar ProductContract
4. Refatorar Services para usar Contracts
5. Mover validações compartilhadas para services genéricos

## Entidades Prioritárias
1. Product ⭐⭐⭐ (usada em sync)
2. Order ⭐⭐⭐ (núcleo do domínio)
3. Payment ⭐⭐ (diferentes regras por app)
4. Customer ⭐⭐ (diferentes dados por app)
5. Category ⭐ (basicamente igual)
