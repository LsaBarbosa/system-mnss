import type { ComponentFixture } from '@angular/core/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { mockCashMovement, mockCashRegister, mockCategory, mockProduct } from '../../../shared/models/domain.mocks';
import { CashRegisterService } from '../data-access/cash-register.service';
import { PdvCatalogService } from '../data-access/pdv-catalog.service';
import { PdvProductsPageComponent } from './pdv-products-page.component';

describe('PdvProductsPageComponent', () => {
  let fixture: ComponentFixture<PdvProductsPageComponent>;
  let pdvCatalogService: jasmine.SpyObj<PdvCatalogService>;
  let cashRegisterService: jasmine.SpyObj<CashRegisterService>;

  beforeEach(async () => {
    pdvCatalogService = jasmine.createSpyObj<PdvCatalogService>('PdvCatalogService', ['listProducts']);
    cashRegisterService = jasmine.createSpyObj<CashRegisterService>('CashRegisterService', [
      'open',
      'current',
      'createMovement',
      'close',
      'summary'
    ]);
    pdvCatalogService.listProducts.and.returnValue(
      of([
        {
          category: mockCategory,
          products: [mockProduct]
        }
      ])
    );
    cashRegisterService.current.and.returnValue(of({ open: true, cashRegister: mockCashRegister }));
    cashRegisterService.summary.and.returnValue(of(cashSummary()));
    cashRegisterService.open.and.returnValue(of(mockCashRegister));
    cashRegisterService.createMovement.and.returnValue(of(mockCashMovement));
    cashRegisterService.close.and.returnValue(of(cashSummary()));

    await TestBed.configureTestingModule({
      imports: [PdvProductsPageComponent],
      providers: [
        provideRouter([]),
        { provide: PdvCatalogService, useValue: pdvCatalogService },
        { provide: CashRegisterService, useValue: cashRegisterService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PdvProductsPageComponent);
    fixture.detectChanges();
  });

  it('renders products grouped by category', () => {
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain(mockCategory.name);
    expect(fixture.nativeElement.textContent).toContain(mockProduct.name);
  });

  it('renders open cash status in PDV header', () => {
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Caixa aberto');
    expect(fixture.nativeElement.textContent).toContain('Esperado 45.00');
  });

  it('requires initial amount before opening cash register', () => {
    fixture.componentInstance.currentCash = { open: false, cashRegister: null };
    fixture.componentInstance.openForm.controls.openingAmount.setValue('');

    fixture.componentInstance.openCash();

    expect(cashRegisterService.open).not.toHaveBeenCalled();
  });

  it('opens cash register and releases PDV', () => {
    fixture.componentInstance.currentCash = { open: false, cashRegister: null };
    fixture.componentInstance.openForm.patchValue({ openingAmount: '30.00', notes: 'Troco' });

    fixture.componentInstance.openCash();

    expect(cashRegisterService.open).toHaveBeenCalledWith({ openingAmount: '30.00', notes: 'Troco' });
    expect(fixture.componentInstance.canSell).toBeTrue();
  });

  it('validates cash movement amount and reason', () => {
    fixture.componentInstance.movementForm.patchValue({ type: 'CASH_OUT', amount: '', description: '' });

    fixture.componentInstance.registerMovement();

    expect(cashRegisterService.createMovement).not.toHaveBeenCalled();
  });

  it('submits cash supply to current register', () => {
    fixture.componentInstance.movementForm.patchValue({
      type: 'CASH_IN',
      amount: '15.00',
      description: 'Suprimento'
    });

    fixture.componentInstance.registerMovement();

    expect(cashRegisterService.createMovement).toHaveBeenCalledWith(mockCashRegister.id, {
      type: 'CASH_IN',
      amount: '15.00',
      description: 'Suprimento'
    });
  });

  it('calculates visual difference and requires notes when divergent', () => {
    fixture.componentInstance.closeForm.patchValue({ closingAmount: '44.00', notes: '' });

    fixture.componentInstance.closeCash();

    expect(fixture.componentInstance.visualDifference).toBe('-1.00');
    expect(fixture.componentInstance.cashError).toBe('Informe a justificativa da divergencia.');
    expect(cashRegisterService.close).not.toHaveBeenCalled();
  });

  it('closes cash register with counted amount', () => {
    fixture.componentInstance.closeForm.patchValue({ closingAmount: '45.00', notes: '' });

    fixture.componentInstance.closeCash();

    expect(cashRegisterService.close).toHaveBeenCalledWith(mockCashRegister.id, {
      closingAmount: '45.00',
      notes: null
    });
  });

  function cashSummary() {
    return {
      cashRegister: mockCashRegister,
      totalsByPaymentMethod: {
        CASH: '0.00',
        PIX: '0.00',
        CREDIT_CARD: '0.00',
        DEBIT_CARD: '0.00',
        ONLINE_PIX: '0.00',
        ONLINE_CREDIT_CARD: '0.00',
        ONLINE_DEBIT_CARD: '0.00',
        MEAL_VOUCHER: '0.00',
        MIXED: '0.00'
      },
      saleTotal: '0.00',
      refundTotal: '0.00',
      cashInTotal: '15.00',
      cashOutTotal: '0.00',
      adjustmentTotal: '0.00',
      expectedAmount: '45.00',
      closingAmount: null,
      differenceAmount: '0.00',
      movements: [mockCashMovement]
    };
  }
});
