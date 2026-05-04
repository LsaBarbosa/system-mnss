import type { ComponentFixture } from '@angular/core/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { mockProduct, mockStockMovement } from '../../../shared/models/domain.mocks';
import { CatalogAdminService } from '../../catalog/data-access/catalog-admin.service';
import { StockService } from '../data-access/stock.service';
import { StockPageComponent } from './stock-page.component';

describe('StockPageComponent', () => {
  let fixture: ComponentFixture<StockPageComponent>;
  let stockService: jasmine.SpyObj<StockService>;
  let catalogAdminService: jasmine.SpyObj<CatalogAdminService>;

  beforeEach(async () => {
    stockService = jasmine.createSpyObj<StockService>('StockService', [
      'listMovements',
      'createMovement',
      'listBalances'
    ]);
    catalogAdminService = jasmine.createSpyObj<CatalogAdminService>('CatalogAdminService', ['listProducts']);

    catalogAdminService.listProducts.and.returnValue(of([mockProduct]));
    stockService.listMovements.and.returnValue(of([{ ...mockStockMovement, productName: mockProduct.name }]));
    stockService.listBalances.and.returnValue(
      of([{ productId: mockProduct.id, productName: mockProduct.name, quantity: '10.000' }])
    );
    stockService.createMovement.and.returnValue(of({ ...mockStockMovement, productName: mockProduct.name }));

    await TestBed.configureTestingModule({
      imports: [StockPageComponent],
      providers: [
        provideRouter([]),
        { provide: StockService, useValue: stockService },
        { provide: CatalogAdminService, useValue: catalogAdminService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(StockPageComponent);
    fixture.detectChanges();
  });

  it('requires product and quantity before submit', () => {
    fixture.componentInstance.stockForm.patchValue({
      productId: '',
      quantity: ''
    });

    fixture.componentInstance.submitMovement();

    expect(stockService.createMovement).not.toHaveBeenCalled();
  });

  it('requires reason for loss movement', () => {
    fixture.componentInstance.stockForm.patchValue({
      productId: mockProduct.id,
      type: 'LOSS',
      quantity: '1.000',
      reason: ''
    });

    fixture.componentInstance.submitMovement();

    expect(stockService.createMovement).not.toHaveBeenCalled();
    expect(fixture.componentInstance.stockError).toBe('Informe o motivo da movimentacao.');
  });

  it('submits supplier entry movement', () => {
    fixture.componentInstance.stockForm.patchValue({
      productId: mockProduct.id,
      type: 'IN',
      quantity: '10.000',
      reason: ''
    });

    fixture.componentInstance.submitMovement();

    expect(stockService.createMovement).toHaveBeenCalledWith({
      productId: mockProduct.id,
      type: 'IN',
      quantity: '10.000',
      reason: null
    });
  });

  it('renders balance by product', () => {
    expect(fixture.nativeElement.textContent).toContain(mockProduct.name);
    expect(fixture.nativeElement.textContent).toContain('10.000');
  });
});
