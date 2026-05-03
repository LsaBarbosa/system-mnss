import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { HealthService } from './core/api/health.service';
import { HealthStatus } from './core/api/health-status';

type ApiState = 'checking' | 'available' | 'unavailable';

interface OperationTile {
  label: string;
  status: string;
  tone: 'ready' | 'pending' | 'warning';
}

@Component({
  selector: 'mnss-root',
  standalone: true,
  imports: [CommonModule],
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
