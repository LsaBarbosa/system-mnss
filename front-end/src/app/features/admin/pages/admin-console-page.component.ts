import { CommonModule } from '@angular/common';
import type { OnInit } from '@angular/core';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HealthService } from '../../../core/api/health.service';
import type { HealthStatus } from '../../../core/api/health-status';
import { AuthService } from '../../../core/auth/auth.service';
import { HasRoleDirective } from '../../../shared/auth/has-role.directive';
import { ErrorBannerComponent } from '../../../shared/error-banner/error-banner.component';

type ApiState = 'checking' | 'online' | 'unstable' | 'offline';

interface OperationTile {
  label: string;
  status: string;
  tone: 'ready' | 'pending' | 'warning';
}

@Component({
  selector: 'mnss-admin-console-page',
  standalone: true,
  imports: [CommonModule, RouterLink, ErrorBannerComponent, HasRoleDirective],
  templateUrl: './admin-console-page.component.html',
  styleUrl: './admin-console-page.component.scss'
})
export class AdminConsolePageComponent implements OnInit {
  apiState: ApiState = 'checking';
  healthStatus?: HealthStatus;

  readonly navigation = [
    { label: 'PDV', route: '/pdv' },
    { label: 'Caixa', route: '/' },
    { label: 'KDS', route: '/' },
    { label: 'Catalogo', route: '/catalog' },
    { label: 'Estoque', route: '/stock' },
    { label: 'Usuarios', route: '/users' },
    { label: 'Sync', route: '/' }
  ];

  readonly operationTiles: OperationTile[] = [
    { label: 'PDV local', status: 'Base criada', tone: 'ready' },
    { label: 'Infra tecnica', status: 'Health ativo', tone: 'ready' },
    { label: 'KDS', status: 'Contrato pendente', tone: 'pending' },
    { label: 'Sincronizacao', status: 'Outbox previsto', tone: 'warning' }
  ];

  constructor(
    private readonly healthService: HealthService,
    readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.authService.loadMe().subscribe();
    this.healthService.getLocalHealth().subscribe({
      next: (healthStatus) => {
        this.healthStatus = healthStatus;
        this.apiState = healthStatus.status === 'UP' ? 'online' : 'unstable';
      },
      error: () => {
        this.apiState = 'offline';
      }
    });
  }

  get apiLabel(): string {
    return {
      checking: 'Verificando',
      online: 'Online',
      unstable: 'Instavel',
      offline: 'Offline'
    }[this.apiState];
  }

  get healthComponents(): Array<{ name: string; status: string }> {
    const components = this.healthStatus?.components ?? {};
    return Object.keys(components).map((name) => ({ name, status: components[name] }));
  }
}
