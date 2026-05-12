import type { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/auth/auth.guard';
import { AdminConsolePageComponent } from './features/admin/pages/admin-console-page.component';
import { LoginPageComponent } from './features/auth/login/login-page.component';
import { CatalogAdminPageComponent } from './features/catalog/pages/catalog-admin-page.component';
import { PdvProductsPageComponent } from './features/pdv/pages/pdv-products-page.component';
import { StockPageComponent } from './features/stock/pages/stock-page.component';
import { UserManagementPageComponent } from './features/users/pages/user-management-page.component';
import { CheckoutPageComponent } from './features/site-publico/pages/checkout/checkout-page.component';
import { OrderConfirmationPageComponent } from './features/site-publico/pages/order-confirmation/order-confirmation-page.component';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/site-publico/pages/site-home/site-home.component').then((m) => m.SiteHomeComponent)
  },
  {
    path: 'cardapio',
    loadComponent: () =>
      import('./features/site-publico/pages/public-menu/public-menu.component').then((m) => m.PublicMenuComponent)
  },
  {
    path: 'encomendas',
    loadComponent: () =>
      import('./features/site-publico/pages/public-orders/public-orders.component').then((m) => m.PublicOrdersComponent)
  },
  {
    path: 'login',
    component: LoginPageComponent
  },
  {
    path: 'admin',
    component: AdminConsolePageComponent,
    canActivate: [authGuard]
  },
  {
    path: 'admin/sync',
    loadComponent: () =>
      import('./features/admin/pages/sync-dashboard/sync-dashboard.component').then((m) => m.SyncDashboardComponent),
    canActivate: [authGuard, roleGuard],
    data: {
      roles: ['ADMIN', 'GERENTE']
    }
  },
  {
    path: 'admin/whatsapp',
    loadComponent: () =>
      import('./features/whatsapp/pages/whatsapp-panel/whatsapp-panel.component').then((m) => m.WhatsAppPanelComponent),
    canActivate: [authGuard, roleGuard],
    data: {
      roles: ['ADMIN', 'GERENTE', 'ATENDENTE']
    }
  },
  {
    path: 'users',
    component: UserManagementPageComponent,
    canActivate: [authGuard, roleGuard],
    data: {
      roles: ['ADMIN']
    }
  },
  {
    path: 'catalog',
    component: CatalogAdminPageComponent,
    canActivate: [authGuard, roleGuard],
    data: {
      roles: ['ADMIN', 'GERENTE']
    }
  },
  {
    path: 'pdv',
    component: PdvProductsPageComponent,
    canActivate: [authGuard, roleGuard],
    data: {
      roles: ['GERENTE', 'CAIXA', 'ATENDENTE']
    }
  },
  {
    path: 'stock',
    component: StockPageComponent,
    canActivate: [authGuard, roleGuard],
    data: {
      roles: ['ADMIN', 'GERENTE']
    }
  },
  {
    path: 'kds',
    loadComponent: () => import('./features/kds/pages/kds-page.component').then((m) => m.KdsPageComponent),
    canActivate: [authGuard, roleGuard],
    data: {
      roles: ['ADMIN', 'GERENTE', 'COZINHA', 'EXPEDICAO']
    }
  },
  {
    path: 'checkout',
    component: CheckoutPageComponent
  },
  {
    path: 'pedido-confirmado/:id',
    component: OrderConfirmationPageComponent
  },
  {
    path: 'pagamento/:orderId',
    loadComponent: () =>
      import('./features/site-publico/pages/payment-page/payment-page.component').then((m) => m.PaymentPageComponent)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
