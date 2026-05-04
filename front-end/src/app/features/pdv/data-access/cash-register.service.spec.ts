import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { mockCashMovement, mockCashRegister } from '../../../shared/models/domain.mocks';
import { CashRegisterService } from './cash-register.service';

describe('CashRegisterService', () => {
  let service: CashRegisterService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(CashRegisterService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('opens cash register', () => {
    service.open({ openingAmount: '30.00', notes: 'Troco inicial' }).subscribe((cashRegister) => {
      expect(cashRegister).toEqual(mockCashRegister);
    });

    const request = httpTestingController.expectOne('/api/cash-register/open');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ openingAmount: '30.00', notes: 'Troco inicial' });
    request.flush(mockCashRegister);
  });

  it('loads current cash register', () => {
    service.current().subscribe((currentCash) => {
      expect(currentCash.open).toBeTrue();
      expect(currentCash.cashRegister).toEqual(mockCashRegister);
    });

    const request = httpTestingController.expectOne('/api/cash-register/current');
    expect(request.request.method).toBe('GET');
    request.flush({ open: true, cashRegister: mockCashRegister });
  });

  it('creates cash movement on correct endpoint', () => {
    service
      .createMovement(mockCashRegister.id, {
        type: 'CASH_OUT',
        amount: '5.00',
        description: 'Sangria'
      })
      .subscribe((movement) => {
        expect(movement.type).toBe('CASH_OUT');
      });

    const request = httpTestingController.expectOne(`/api/cash-register/${mockCashRegister.id}/movement`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      type: 'CASH_OUT',
      amount: '5.00',
      description: 'Sangria'
    });
    request.flush({ ...mockCashMovement, type: 'CASH_OUT' });
  });

  it('closes cash register and loads summary', () => {
    service.close(mockCashRegister.id, { closingAmount: '45.00', notes: null }).subscribe((summary) => {
      expect(summary.expectedAmount).toBe('45.00');
    });

    const closeRequest = httpTestingController.expectOne(`/api/cash-register/${mockCashRegister.id}/close`);
    expect(closeRequest.request.method).toBe('POST');
    closeRequest.flush(summary());

    service.summary(mockCashRegister.id).subscribe((cashSummary) => {
      expect(cashSummary.movements).toEqual([mockCashMovement]);
    });

    const summaryRequest = httpTestingController.expectOne(`/api/cash-register/${mockCashRegister.id}/summary`);
    expect(summaryRequest.request.method).toBe('GET');
    summaryRequest.flush(summary());
  });

  function summary() {
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
