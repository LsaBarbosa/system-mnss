import { CommonModule } from '@angular/common';
import type { OnInit } from '@angular/core';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import type { ProductModel, StockMovementType, Uuid } from '../../../shared/models/domain.models';
import { ErrorBannerComponent } from '../../../shared/error-banner/error-banner.component';
import { CatalogAdminService } from '../../catalog/data-access/catalog-admin.service';
import { StockService } from '../data-access/stock.service';
import type { StockBalanceModel, StockMovementPayload, StockMovementResponse } from '../data-access/stock.service';

interface MovementTypeOption {
  type: StockMovementType;
  label: string;
}

@Component({
  selector: 'mnss-stock-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ErrorBannerComponent],
  templateUrl: './stock-page.component.html',
  styleUrl: './stock-page.component.scss'
})
export class StockPageComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly stockService = inject(StockService);
  private readonly catalogAdminService = inject(CatalogAdminService);

  products: ProductModel[] = [];
  balances: StockBalanceModel[] = [];
  movements: StockMovementResponse[] = [];
  stockError: string | null = null;
  saving = false;

  readonly movementTypes: MovementTypeOption[] = [
    { type: 'IN', label: 'Entrada de fornecedor' },
    { type: 'ADJUSTMENT', label: 'Ajuste positivo' },
    { type: 'LOSS', label: 'Perda' }
  ];

  readonly stockForm = this.formBuilder.nonNullable.group({
    productId: ['', Validators.required],
    type: ['IN' as StockMovementType, Validators.required],
    quantity: ['', [Validators.required, Validators.min(0.001)]],
    reason: ['']
  });

  ngOnInit(): void {
    this.loadStock();
  }

  submitMovement(): void {
    this.stockError = null;
    if (this.stockForm.invalid || this.reasonRequired) {
      this.stockForm.markAllAsTouched();
      if (this.reasonRequired) {
        this.stockError = 'Informe o motivo da movimentacao.';
      }
      return;
    }

    this.saving = true;
    this.stockService.createMovement(this.movementPayload()).subscribe({
      next: () => {
        this.resetForm();
        this.loadStock();
        this.saving = false;
      },
      error: () => {
        this.stockError = 'Nao foi possivel registrar a movimentacao.';
        this.saving = false;
      }
    });
  }

  get reasonRequired(): boolean {
    const type = this.stockForm.controls.type.value;
    const reason = this.stockForm.controls.reason.value.trim();
    return (type === 'LOSS' || type === 'ADJUSTMENT') && !reason;
  }

  productName(productId: Uuid): string {
    return this.products.find((product) => product.id === productId)?.name ?? productId;
  }

  private loadStock(): void {
    this.catalogAdminService.listProducts().subscribe({
      next: (products) => {
        this.products = products;
      },
      error: () => {
        this.stockError = 'Nao foi possivel carregar os produtos.';
      }
    });
    this.stockService.listBalances().subscribe({
      next: (balances) => {
        this.balances = balances;
      },
      error: () => {
        this.stockError = 'Nao foi possivel carregar os saldos.';
      }
    });
    this.stockService.listMovements().subscribe({
      next: (movements) => {
        this.movements = movements;
      },
      error: () => {
        this.stockError = 'Nao foi possivel carregar o historico de estoque.';
      }
    });
  }

  private movementPayload(): StockMovementPayload {
    const formValue = this.stockForm.getRawValue();
    return {
      productId: formValue.productId,
      type: formValue.type,
      quantity: formValue.quantity,
      reason: this.emptyToNull(formValue.reason)
    };
  }

  private resetForm(): void {
    this.stockForm.reset({
      productId: '',
      type: 'IN',
      quantity: '',
      reason: ''
    });
  }

  private emptyToNull(value: string): string | null {
    const normalized = value.trim();
    return normalized ? normalized : null;
  }
}
