import type { ComponentFixture } from '@angular/core/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { mockCashMovement, mockCashRegister, mockCategory, mockProduct } from '../../../shared/models/domain.mocks';
import { CashRegisterService } from '../data-access/cash-register.service';
import { PdvCatalogService } from '../data-access/pdv-catalog.service';
import { PdvSaleService } from '../data-access/pdv-sale.service';
import type { PdvSale } from '../domain/pdv-sale.models';
import { HardwareService } from '../data-access/hardware.service';
import { AuthService } from '../../../core/auth/auth.service';
import { KdsService } from '../../kds/data-access/kds.service';
import { PdvProductsPageComponent } from './pdv-products-page.component';

describe('PdvProductsPageComponent', () => {
  let fixture: ComponentFixture<PdvProductsPageComponent>;
  let pdvCatalogService: jasmine.SpyObj<PdvCatalogService>;
  let cashRegisterService: jasmine.SpyObj<CashRegisterService>;
  let pdvSaleService: jasmine.SpyObj<PdvSaleService>;
  let hardwareService: jasmine.SpyObj<HardwareService>;
  let authService: jasmine.SpyObj<AuthService>;
  let kdsService: jasmine.SpyObj<KdsService>;

  beforeEach(async () => {
    pdvCatalogService = jasmine.createSpyObj<PdvCatalogService>('PdvCatalogService', [
      'listProducts',
      'findProductByBarcode'
    ]);
    cashRegisterService = jasmine.createSpyObj<CashRegisterService>('CashRegisterService', [
      'open',
      'current',
      'createMovement',
      'close',
      'summary'
    ]);
    pdvSaleService = jasmine.createSpyObj<PdvSaleService>('PdvSaleService', [
      'listSales',
      'createSale',
      'addItem',
      'updateItem',
      'removeItem',
      'pay',
      'finish',
      'applyDiscount',
      'cancelSale',
      'getSale'
    ]);
    hardwareService = jasmine.createSpyObj<HardwareService>('HardwareService', ['openDrawer']);
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['isAuthenticated', 'hasAnyRole'], {
      currentUser: { id: 'u1', name: 'User', username: 'user', roles: ['ADMIN'], active: true }
    });
    kdsService = jasmine.createSpyObj<KdsService>('KdsService', ['loadTickets'], {
      readyOrders$: of(),
      tickets$: of([])
    });
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
    pdvCatalogService.findProductByBarcode.and.returnValue(of(mockProduct));
    pdvSaleService.createSale.and.returnValue(of(sale([])));
    pdvSaleService.listSales.and.returnValue(of([sale([saleItem()])]));
    pdvSaleService.addItem.and.returnValue(of(sale([saleItem()])));
    pdvSaleService.updateItem.and.returnValue(of(sale([{ ...saleItem(), quantity: '2.000', totalPrice: '2.40' }])));
    pdvSaleService.removeItem.and.returnValue(of(sale([])));
    pdvSaleService.pay.and.returnValue(
      of({
        id: 'payment-id',
        orderId: '44444444-4444-4444-4444-444444444444',
        method: 'PIX',
        status: 'PAID',
        amount: '1.20',
        recordedAmount: '1.20',
        remainingAmount: '0.00',
        changeAmount: '0.00',
        transactionId: null,
        gateway: null,
        paidAt: '2026-05-04T12:06:00Z',
        canceledAt: null,
        orderStatus: 'PAID',
        orderPaymentStatus: 'PAID',
        createdAt: '2026-05-04T12:06:00Z',
        updatedAt: '2026-05-04T12:06:00Z'
      })
    );
    pdvSaleService.finish.and.returnValue(of(sale([])));
    pdvSaleService.applyDiscount.and.returnValue(of(sale([])));
    pdvSaleService.cancelSale.and.returnValue(of(sale([])));
    pdvSaleService.getSale.and.returnValue(of(sale([])));
    hardwareService.openDrawer.and.returnValue(of(undefined));

    await TestBed.configureTestingModule({
      imports: [PdvProductsPageComponent],
      providers: [
        provideRouter([]),
        { provide: PdvCatalogService, useValue: pdvCatalogService },
        { provide: CashRegisterService, useValue: cashRegisterService },
        { provide: PdvSaleService, useValue: pdvSaleService },
        { provide: HardwareService, useValue: hardwareService },
        { provide: AuthService, useValue: authService },
        { provide: KdsService, useValue: kdsService }
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

  it('loads sale history for PDV orders', () => {
    expect(pdvSaleService.listSales).toHaveBeenCalled();
    expect(fixture.componentInstance.salesHistory.length).toBe(1);
    expect(fixture.nativeElement.textContent).toContain('Historico de vendas');
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

  it('starts sale and initializes cart', () => {
    fixture.componentInstance.startSale();

    expect(pdvSaleService.createSale).toHaveBeenCalled();
    expect(fixture.componentInstance.currentSale?.items).toEqual([]);
  });

  it('clicking product adds item and updates subtotal', () => {
    fixture.componentInstance.currentSale = sale([]);

    fixture.componentInstance.addProduct(mockProduct);

    expect(pdvSaleService.addItem).toHaveBeenCalledWith('44444444-4444-4444-4444-444444444444', {
      productId: mockProduct.id,
      quantity: '1.000'
    });
    expect(fixture.componentInstance.currentSale?.subtotal).toBe('1.20');
  });

  it('updates quantity and visual total from returned payload', () => {
    const item = saleItem();
    fixture.componentInstance.currentSale = sale([item]);
    const currentSaleId = fixture.componentInstance.currentSale.id;

    fixture.componentInstance.increaseItem(item);

    expect(pdvSaleService.updateItem).toHaveBeenCalledWith(currentSaleId, item.id, {
      quantity: '2.000'
    });
    expect(fixture.componentInstance.currentSale?.items[0].totalPrice).toBe('2.40');
  });

  it('removes item from cart', () => {
    const item = saleItem();
    fixture.componentInstance.currentSale = sale([item]);
    const currentSaleId = fixture.componentInstance.currentSale.id;

    fixture.componentInstance.removeItem(item);

    expect(pdvSaleService.removeItem).toHaveBeenCalledWith(currentSaleId, item.id);
    expect(fixture.componentInstance.currentSale?.items).toEqual([]);
  });

  it('registers payment and finalizes sale when remaining amount is zero', () => {
    fixture.componentInstance.currentSale = sale([saleItem()]);
    fixture.componentInstance.paymentForm.patchValue({ method: 'PIX', amount: '1.20', transactionId: 'tx-1' });

    fixture.componentInstance.registerPayment();

    expect(pdvSaleService.pay).toHaveBeenCalledWith('44444444-4444-4444-4444-444444444444', {
      method: 'PIX',
      amount: '1.20',
      transactionId: 'tx-1',
      gateway: null
    });
    expect(fixture.componentInstance.saleSuccess).toContain('Pagamento concluído');
  });

  it('finishes sale and triggers hardware', () => {
    const s = sale([saleItem()]);
    s.remainingAmount = '0.00';
    s.payments = [{ id: '1', method: 'CASH', amount: '1.20', createdAt: '2026-05-04T12:06:00Z' }];
    fixture.componentInstance.currentSale = s;
    pdvSaleService.finish.and.returnValue(of(s));

    fixture.componentInstance.finishSale();

    expect(pdvSaleService.finish).toHaveBeenCalled();
    expect(hardwareService.openDrawer).toHaveBeenCalled();
    expect(fixture.componentInstance.currentSale).toBeNull();
  });

  it('applies discount', () => {
    fixture.componentInstance.currentSale = sale([saleItem()]);
    fixture.componentInstance.discountForm.patchValue({ type: 'VALUE', amount: '0.10' });

    fixture.componentInstance.applyDiscount();

    expect(pdvSaleService.applyDiscount).toHaveBeenCalledWith('44444444-4444-4444-4444-444444444444', {
      amount: '0.10'
    });
    expect(fixture.componentInstance.saleSuccess).toBe('Desconto aplicado.');
  });

  it('cancels sale', () => {
    fixture.componentInstance.saleToCancel = '44444444-4444-4444-4444-444444444444';
    fixture.componentInstance.cancelForm.patchValue({ reason: 'Teste cancelamento' });

    fixture.componentInstance.confirmCancelSale();

    expect(pdvSaleService.cancelSale).toHaveBeenCalledWith('44444444-4444-4444-4444-444444444444', {
      reason: 'Teste cancelamento'
    });
    expect(fixture.componentInstance.saleSuccess).toContain('Venda cancelada');
  });

  it('does not register payment for empty cart', () => {
    fixture.componentInstance.currentSale = sale([]);
    fixture.componentInstance.paymentForm.patchValue({ method: 'CASH', amount: '0.00', transactionId: '' });

    fixture.componentInstance.registerPayment();

    expect(pdvSaleService.pay).not.toHaveBeenCalled();
    expect(fixture.componentInstance.saleError).toBe('Adicione itens antes de registrar pagamento.');
  });

  it('filters products by search form', () => {
    fixture.componentInstance.productFilterForm.patchValue({ name: 'pao', categoryId: mockCategory.id });

    fixture.componentInstance.searchProducts();

    expect(pdvCatalogService.listProducts).toHaveBeenCalledWith({ name: 'pao', categoryId: mockCategory.id });
  });

  it('scanner by Enter finds barcode and adds item', () => {
    fixture.componentInstance.currentSale = sale([]);
    fixture.componentInstance.barcodeControl.setValue('789100');
    const event = new KeyboardEvent('keydown', { key: 'Enter' });
    spyOn(event, 'preventDefault');

    fixture.componentInstance.onBarcodeKeydown(event);

    expect(event.preventDefault).toHaveBeenCalled();
    expect(pdvCatalogService.findProductByBarcode).toHaveBeenCalledWith('789100');
    expect(pdvSaleService.addItem).toHaveBeenCalled();
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

  function sale(items: PdvSale['items']): PdvSale {
    const subtotal = items
      .reduce((total: number, item: PdvSale['items'][number]) => total + Number(item.totalPrice), 0)
      .toFixed(2);
    return {
      id: '44444444-4444-4444-4444-444444444444',
      orderNumber: 12,
      origin: 'PDV',
      status: 'CREATED',
      paymentStatus: 'PENDING',
      deliveryType: 'LOCAL_CONSUMPTION',
      subtotal,
      discountAmount: '0.00',
      deliveryFee: '0.00',
      totalAmount: subtotal,
      items,
      payments: [],
      remainingAmount: '1.20',
      syncStatus: null,
      createdAt: '2026-05-01T10:00:00Z',
      updatedAt: '2026-05-04T12:05:00Z'
    };
  }

  function saleItem(): PdvSale['items'][number] {
    return {
      id: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
      orderId: '44444444-4444-4444-4444-444444444444',
      productId: mockProduct.id,
      productNameSnapshot: mockProduct.name,
      quantity: '1.000',
      unitPrice: '1.20',
      totalPrice: '1.20',
      observation: null,
      status: 'CREATED',
      preparationSector: 'SEM_PREPARO',
      createdAt: '2026-05-04T12:00:00Z',
      updatedAt: '2026-05-04T12:05:00Z'
    };
  }
});
