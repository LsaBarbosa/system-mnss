import { CommonModule } from '@angular/common';
import type { OnInit } from '@angular/core';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ErrorBannerComponent } from '../../../shared/error-banner/error-banner.component';
import type { CashMovementType } from '../../../shared/models/domain.models';
import { CashRegisterService } from '../data-access/cash-register.service';
import type { CashRegisterSummaryResponse, CurrentCashRegisterResponse } from '../data-access/cash-register.service';
import type { PdvProductGroup } from '../data-access/pdv-catalog.service';
import { PdvCatalogService } from '../data-access/pdv-catalog.service';

@Component({
  selector: 'mnss-pdv-products-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ErrorBannerComponent],
  templateUrl: './pdv-products-page.component.html',
  styleUrl: './pdv-products-page.component.scss'
})
export class PdvProductsPageComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly pdvCatalogService = inject(PdvCatalogService);
  private readonly cashRegisterService = inject(CashRegisterService);

  groups: PdvProductGroup[] = [];
  currentCash: CurrentCashRegisterResponse = { open: false, cashRegister: null };
  cashSummary: CashRegisterSummaryResponse | null = null;
  cashError: string | null = null;
  cashSuccess: string | null = null;

  readonly openForm = this.formBuilder.nonNullable.group({
    openingAmount: ['', [Validators.required, Validators.min(0)]],
    notes: ['']
  });

  readonly movementForm = this.formBuilder.nonNullable.group({
    type: ['CASH_OUT' as CashMovementType, Validators.required],
    amount: ['', [Validators.required, Validators.min(0.01)]],
    description: ['', Validators.required]
  });

  readonly closeForm = this.formBuilder.nonNullable.group({
    closingAmount: ['', [Validators.required, Validators.min(0)]],
    notes: ['']
  });

  ngOnInit(): void {
    this.loadCash();
    this.pdvCatalogService.listProducts().subscribe((groups) => {
      this.groups = groups;
    });
  }

  get cashStatusLabel(): string {
    return this.currentCash.open ? 'Caixa aberto' : 'Caixa fechado';
  }

  get canSell(): boolean {
    return this.currentCash.open;
  }

  get visualDifference(): string {
    const counted = Number(this.closeForm.controls.closingAmount.value || 0);
    const expected = Number(this.cashSummary?.expectedAmount ?? this.currentCash.cashRegister?.openingAmount ?? 0);
    return (counted - expected).toFixed(2);
  }

  openCash(): void {
    this.clearCashMessages();
    if (this.openForm.invalid) {
      this.openForm.markAllAsTouched();
      return;
    }

    const formValue = this.openForm.getRawValue();
    this.cashRegisterService
      .open({
        openingAmount: formValue.openingAmount,
        notes: this.emptyToNull(formValue.notes)
      })
      .subscribe({
        next: (cashRegister) => {
          this.currentCash = { open: true, cashRegister };
          this.cashSuccess = 'Caixa aberto.';
          this.openForm.reset({ openingAmount: '', notes: '' });
          this.loadSummary(cashRegister.id);
        },
        error: () => {
          this.cashError = 'Nao foi possivel abrir o caixa.';
        }
      });
  }

  registerMovement(): void {
    this.clearCashMessages();
    const cashRegisterId = this.currentCash.cashRegister?.id;
    if (!cashRegisterId) {
      this.cashError = 'Abra o caixa antes de registrar movimentacoes.';
      return;
    }
    if (this.movementForm.invalid) {
      this.movementForm.markAllAsTouched();
      return;
    }

    const formValue = this.movementForm.getRawValue();
    this.cashRegisterService
      .createMovement(cashRegisterId, {
        type: formValue.type,
        amount: formValue.amount,
        description: formValue.description.trim()
      })
      .subscribe({
        next: () => {
          this.cashSuccess = formValue.type === 'CASH_OUT' ? 'Sangria registrada.' : 'Suprimento registrado.';
          this.movementForm.reset({ type: 'CASH_OUT', amount: '', description: '' });
          this.loadSummary(cashRegisterId);
        },
        error: () => {
          this.cashError = 'Nao foi possivel registrar a movimentacao.';
        }
      });
  }

  closeCash(): void {
    this.clearCashMessages();
    const cashRegisterId = this.currentCash.cashRegister?.id;
    if (!cashRegisterId) {
      this.cashError = 'Nao ha caixa aberto para fechar.';
      return;
    }
    if (this.closeForm.invalid) {
      this.closeForm.markAllAsTouched();
      return;
    }
    if (this.visualDifference !== '0.00' && !this.closeForm.controls.notes.value.trim()) {
      this.cashError = 'Informe a justificativa da divergencia.';
      return;
    }

    const formValue = this.closeForm.getRawValue();
    this.cashRegisterService
      .close(cashRegisterId, {
        closingAmount: formValue.closingAmount,
        notes: this.emptyToNull(formValue.notes)
      })
      .subscribe({
        next: (summary) => {
          this.cashSummary = summary;
          this.currentCash = { open: false, cashRegister: summary.cashRegister };
          this.cashSuccess = 'Caixa fechado.';
          this.closeForm.reset({ closingAmount: '', notes: '' });
        },
        error: () => {
          this.cashError = 'Nao foi possivel fechar o caixa.';
        }
      });
  }

  private loadCash(): void {
    this.cashRegisterService.current().subscribe({
      next: (currentCash) => {
        this.currentCash = currentCash;
        if (currentCash.cashRegister) {
          this.loadSummary(currentCash.cashRegister.id);
        }
      },
      error: () => {
        this.cashError = 'Nao foi possivel consultar o caixa atual.';
      }
    });
  }

  private loadSummary(cashRegisterId: string): void {
    this.cashRegisterService.summary(cashRegisterId).subscribe({
      next: (summary) => {
        this.cashSummary = summary;
      },
      error: () => {
        this.cashSummary = null;
      }
    });
  }

  private clearCashMessages(): void {
    this.cashError = null;
    this.cashSuccess = null;
  }

  private emptyToNull(value: string): string | null {
    const normalized = value.trim();
    return normalized ? normalized : null;
  }
}
