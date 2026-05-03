import { CommonModule } from '@angular/common';
import type { OnInit } from '@angular/core';
import { Component } from '@angular/core';
import { HealthService } from './core/api/health.service';
import type { HealthStatus } from './core/api/health-status';
import { ErrorBannerComponent } from './shared/error-banner/error-banner.component';

type ApiState = 'checking' | 'available' | 'unavailable';

interface OperationTile {
  label: string;
  status: string;
  tone: 'ready' | 'pending' | 'warning';
}

@Component({
  selector: 'mnss-root',
  standalone: true,
  imports: [CommonModule, ErrorBannerComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  apiState: ApiState = 'checking';
  healthStatus?: HealthStatus;

  readonly navigation = ['PDV', 'Caixa', 'KDS', 'Catalogo', 'Sync'];

  readonly operationTiles: OperationTile[] = [
    { label: 'PDV local', status: 'Base criada', tone: 'ready' },
    { label: 'Caixa', status: 'Contrato pendente', tone: 'pending' },
    { label: 'KDS', status: 'Contrato pendente', tone: 'pending' },
    { label: 'Sincronizacao', status: 'Outbox previsto', tone: 'warning' }
  ];

  constructor(private readonly healthService: HealthService) {}

  ngOnInit(): void {
    this.healthService.getLocalHealth().subscribe({
      next: (healthStatus) => {
        this.healthStatus = healthStatus;
        this.apiState = 'available';
      },
      error: () => {
        this.apiState = 'unavailable';
      }
    });
  }

  get apiLabel(): string {
    return {
      checking: 'Verificando',
      available: 'Online local',
      unavailable: 'Indisponivel'
    }[this.apiState];
  }
}
