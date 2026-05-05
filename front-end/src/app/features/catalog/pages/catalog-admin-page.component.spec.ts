import type { ComponentFixture } from '@angular/core/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { mockCategory, mockProduct } from '../../../shared/models/domain.mocks';
import { CatalogAdminService } from '../data-access/catalog-admin.service';
import { CatalogAdminPageComponent } from './catalog-admin-page.component';

describe('CatalogAdminPageComponent', () => {
  let fixture: ComponentFixture<CatalogAdminPageComponent>;
  let catalogAdminService: jasmine.SpyObj<CatalogAdminService>;

  beforeEach(async () => {
    catalogAdminService = jasmine.createSpyObj<CatalogAdminService>('CatalogAdminService', [
      'listCategories',
      'createCategory',
      'updateCategory',
      'listProducts',
      'createProduct',
      'updateProduct',
      'updateProductAvailability',
      'findProductByBarcode'
    ]);
    catalogAdminService.listCategories.and.returnValue(of([mockCategory]));
    catalogAdminService.listProducts.and.returnValue(of([mockProduct]));
    catalogAdminService.createCategory.and.returnValue(of(mockCategory));
    catalogAdminService.updateCategory.and.returnValue(of(mockCategory));
    catalogAdminService.createProduct.and.returnValue(of(mockProduct));
    catalogAdminService.updateProduct.and.returnValue(of(mockProduct));
    catalogAdminService.updateProductAvailability.and.returnValue(
      of({
        id: 'dddddddd-dddd-dddd-dddd-dddddddddddd',
        productId: mockProduct.id,
        status: 'UNAVAILABLE',
        availableQuantity: null,
        channel: 'ALL',
        reason: 'Acabou na loja',
        updatedBy: null,
        createdAt: '2026-05-04T12:00:00Z',
        updatedAt: '2026-05-04T12:00:00Z',
        syncStatus: 'PENDING'
      })
    );
    catalogAdminService.findProductByBarcode.and.returnValue(of(mockProduct));

    await TestBed.configureTestingModule({
      imports: [CatalogAdminPageComponent],
      providers: [provideRouter([]), { provide: CatalogAdminService, useValue: catalogAdminService }]
    }).compileComponents();

    fixture = TestBed.createComponent(CatalogAdminPageComponent);
    fixture.detectChanges();
  });

  it('requires category name before submit', () => {
    fixture.componentInstance.categoryForm.controls.name.setValue('');

    fixture.componentInstance.submitCategory();

    expect(catalogAdminService.createCategory).not.toHaveBeenCalled();
  });

  it('submits category and shows API error when save fails', () => {
    catalogAdminService.createCategory.and.returnValue(throwError(() => new Error('fail')));
    fixture.componentInstance.categoryForm.controls.name.setValue('Bolos');

    fixture.componentInstance.submitCategory();

    expect(catalogAdminService.createCategory).toHaveBeenCalled();
    expect(fixture.componentInstance.categoryError).toBe('Nao foi possivel salvar a categoria.');
  });

  it('fills category form for editing and keeps channel checkbox state in payload', () => {
    fixture.componentInstance.editCategory({ ...mockCategory, showOnPdv: false });

    expect(fixture.componentInstance.categoryForm.controls.name.value).toBe(mockCategory.name);
    expect(fixture.componentInstance.categoryForm.controls.showOnPdv.value).toBeFalse();

    fixture.componentInstance.submitCategory();

    expect(catalogAdminService.updateCategory).toHaveBeenCalledWith(
      mockCategory.id,
      jasmine.objectContaining({ showOnPdv: false })
    );
  });

  it('requires product price category unit and sector before submit', () => {
    fixture.componentInstance.productForm.patchValue({
      categoryId: '',
      name: 'Pao frances',
      price: '',
      unitType: '' as never,
      preparationSector: '' as never
    });

    fixture.componentInstance.submitProduct();

    expect(catalogAdminService.createProduct).not.toHaveBeenCalled();
  });

  it('submits valid product form', () => {
    fixture.componentInstance.productForm.patchValue({
      categoryId: mockCategory.id,
      name: 'Pao frances',
      price: '1.20',
      unitType: 'UNIT',
      preparationSector: 'SEM_PREPARO'
    });

    fixture.componentInstance.submitProduct();

    expect(catalogAdminService.createProduct).toHaveBeenCalledWith(
      jasmine.objectContaining({
        categoryId: mockCategory.id,
        price: '1.20',
        unitType: 'UNIT',
        preparationSector: 'SEM_PREPARO'
      })
    );
  });

  it('fills product form for editing', () => {
    fixture.componentInstance.editProduct(mockProduct);

    expect(fixture.componentInstance.productForm.controls.name.value).toBe(mockProduct.name);
    expect(fixture.componentInstance.productForm.controls.categoryId.value).toBe(mockProduct.categoryId ?? '');
    expect(fixture.componentInstance.productForm.controls.unitType.value).toBe(mockProduct.unitType);
  });

  it('filters products by name and category with mocked data', () => {
    fixture.componentInstance.products = [
      mockProduct,
      {
        ...mockProduct,
        id: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        name: 'Cafe coado',
        categoryId: 'cccccccc-cccc-cccc-cccc-cccccccccccc'
      }
    ];
    fixture.componentInstance.filterForm.patchValue({ name: 'pao', categoryId: mockCategory.id });

    expect(fixture.componentInstance.filteredProducts).toEqual([mockProduct]);
  });

  it('calls barcode service when Enter is captured', () => {
    fixture.componentInstance.barcodeControl.setValue('789100000001');
    const event = new KeyboardEvent('keydown', { key: 'Enter' });
    spyOn(event, 'preventDefault');

    fixture.componentInstance.onBarcodeKeydown(event);

    expect(event.preventDefault).toHaveBeenCalled();
    expect(catalogAdminService.findProductByBarcode).toHaveBeenCalledWith('789100000001');
    expect(fixture.componentInstance.barcodeResult).toEqual(mockProduct);
  });

  it('availability toggle updates visual state and sends reason', () => {
    fixture.componentInstance.setAvailabilityReason(mockProduct.id, 'Acabou na loja');

    fixture.componentInstance.toggleAvailability(mockProduct, false);

    expect(catalogAdminService.updateProductAvailability).toHaveBeenCalledWith(mockProduct.id, {
      status: 'UNAVAILABLE',
      channel: 'ALL',
      reason: 'Acabou na loja'
    });
    expect(fixture.componentInstance.pendingSyncProductIds.has(mockProduct.id)).toBeTrue();
    expect(fixture.componentInstance.products[0].available).toBeFalse();
  });

  it('renders unavailable badge and pending sync indicator', () => {
    fixture.componentInstance.products = [{ ...mockProduct, available: false }];
    fixture.componentInstance.pendingSyncProductIds.add(mockProduct.id);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Indisponivel');
    expect(fixture.nativeElement.textContent).toContain('Pendente');
  });
});
