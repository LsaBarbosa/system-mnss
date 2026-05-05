import type { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/auth/auth.guard';
import { AdminConsolePageComponent } from './features/admin/pages/admin-console-page.component';
import { LoginPageComponent } from './features/auth/login/login-page.component';
import { CatalogAdminPageComponent } from './features/catalog/pages/catalog-admin-page.component';
import { PdvProductsPageComponent } from './features/pdv/pages/pdv-products-page.component';
import { StockPageComponent } from './features/stock/pages/stock-page.component';
import { UserManagementPageComponent } from './features/users/pages/user-management-page.component';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginPageComponent
  },
  {
    path: '',
    component: AdminConsolePageComponent,
    canActivate: [authGuard]
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
      roles: ['GERENTE']
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
    loadComponent: () => import('./features/kds/pages/kds-page.component').then(m => m.KdsPageComponent),
    canActivate: [authGuard, roleGuard],
    data: {
      roles: ['ADMIN', 'GERENTE', 'COZINHA']
    }
  },
  {
    path: '**',
    redirectTo: ''
  }
];
