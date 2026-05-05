import { CommonModule } from '@angular/common';
import type { OnInit } from '@angular/core';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import type {
  AvailabilityStatus,
  CategoryModel,
  PreparationSector,
  ProductModel,
  UnitType,
  Uuid
} from '../../../shared/models/domain.models';
import { ErrorBannerComponent } from '../../../shared/error-banner/error-banner.component';
import { SyncStatusBadgeComponent } from '../../../shared/sync-status-badge/sync-status-badge.component';
import { CatalogAdminService } from '../data-access/catalog-admin.service';
import type { CategoryPayload, ProductPayload } from '../data-access/catalog-admin.service';

@Component({
  selector: 'mnss-catalog-admin-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ErrorBannerComponent, SyncStatusBadgeComponent],
  templateUrl: './catalog-admin-page.component.html',
  styleUrl: './catalog-admin-page.component.scss'
})
export class CatalogAdminPageComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly catalogAdminService = inject(CatalogAdminService);

  categories: CategoryModel[] = [];
  products: ProductModel[] = [];
  availabilityReasons: Record<Uuid, string> = {};
  pendingSyncProductIds = new Set<Uuid>();
  failedSyncProductIds = new Set<Uuid>();
  barcodeResult: ProductModel | null = null;
  categoryError: string | null = null;
  productError: string | null = null;
  barcodeError: string | null = null;
  editingCategoryId: Uuid | null = null;
  editingProductId: Uuid | null = null;

  readonly unitTypes: UnitType[] = ['UNIT', 'KG', 'GRAM', 'LITER', 'ML', 'SLICE', 'PORTION', 'PACKAGE'];
  readonly preparationSectors: PreparationSector[] = [
    'BALCAO',
    'CHAPA',
    'BEBIDAS',
    'CONFEITARIA',
    'EXPEDICAO',
    'DELIVERY',
    'SEM_PREPARO'
  ];

  readonly categoryForm = this.formBuilder.nonNullable.group({
    name: ['', Validators.required],
    description: [''],
    displayOrder: [0],
    active: [true],
    showOnline: [true],
    showOnPdv: [true],
    showOnWhatsapp: [true]
  });

  readonly productForm = this.formBuilder.nonNullable.group({
    categoryId: ['', Validators.required],
    name: ['', Validators.required],
    description: [''],
    price: ['', [Validators.required, Validators.min(0)]],
    promotionalPrice: [''],
    costPrice: [''],
    sku: [''],
    barcode: [''],
    unitType: ['UNIT' as UnitType, Validators.required],
    preparationSector: ['SEM_PREPARO' as PreparationSector, Validators.required],
    preparationTimeMinutes: [null as number | null],
    active: [true],
    available: [true],
    sellOnPdv: [true],
    sellOnline: [true],
    sellOnWhatsapp: [true]
  });

  readonly filterForm = this.formBuilder.nonNullable.group({
    name: [''],
    categoryId: ['']
  });

  readonly barcodeControl = this.formBuilder.nonNullable.control('');

  ngOnInit(): void {
    this.loadCatalog();
  }

  get filteredProducts(): ProductModel[] {
    const name = this.filterForm.controls.name.value.trim().toLowerCase();
    const categoryId = this.filterForm.controls.categoryId.value;
    return this.products.filter((product) => {
      const matchesName = !name || product.name.toLowerCase().includes(name);
      const matchesCategory = !categoryId || product.categoryId === categoryId;
      return matchesName && matchesCategory;
    });
  }

  submitCategory(): void {
    this.categoryError = null;
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      return;
    }

    const payload = this.categoryPayload();
    const request$ = this.editingCategoryId
      ? this.catalogAdminService.updateCategory(this.editingCategoryId, payload)
      : this.catalogAdminService.createCategory(payload);

    request$.subscribe({
      next: (category) => {
        this.upsertCategory(category);
        this.resetCategoryForm();
      },
      error: () => {
        this.categoryError = 'Nao foi possivel salvar a categoria.';
      }
    });
  }

  editCategory(category: CategoryModel): void {
    this.editingCategoryId = category.id;
    this.categoryForm.setValue({
      name: category.name,
      description: category.description ?? '',
      displayOrder: category.displayOrder,
      active: category.active,
      showOnline: category.showOnline,
      showOnPdv: category.showOnPdv,
      showOnWhatsapp: category.showOnWhatsapp
    });
  }

  submitProduct(): void {
    this.productError = null;
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }

    const payload = this.productPayload();
    const request$ = this.editingProductId
      ? this.catalogAdminService.updateProduct(this.editingProductId, payload)
      : this.catalogAdminService.createProduct(payload);

    request$.subscribe({
      next: (product) => {
        this.upsertProduct(product);
        this.resetProductForm();
      },
      error: () => {
        this.productError = 'Nao foi possivel salvar o produto.';
      }
    });
  }

  editProduct(product: ProductModel): void {
    this.editingProductId = product.id;
    this.productForm.setValue({
      categoryId: product.categoryId ?? '',
      name: product.name,
      description: product.description ?? '',
      price: String(product.price),
      promotionalPrice: product.promotionalPrice ? String(product.promotionalPrice) : '',
      costPrice: product.costPrice ? String(product.costPrice) : '',
      sku: product.sku ?? '',
      barcode: product.barcode ?? '',
      unitType: product.unitType,
      preparationSector: product.preparationSector,
      preparationTimeMinutes: product.preparationTimeMinutes,
      active: product.active,
      available: product.available,
      sellOnPdv: product.sellOnPdv,
      sellOnline: product.sellOnline,
      sellOnWhatsapp: product.sellOnWhatsapp
    });
  }

  onBarcodeKeydown(event: KeyboardEvent): void {
    if (event.key !== 'Enter') {
      return;
    }
    event.preventDefault();
    const barcode = this.barcodeControl.value.trim();
    if (!barcode) {
      return;
    }
    this.barcodeError = null;
    this.catalogAdminService.findProductByBarcode(barcode).subscribe({
      next: (product) => {
        this.barcodeResult = product;
      },
      error: () => {
        this.barcodeResult = null;
        this.barcodeError = 'Produto nao encontrado para venda.';
      }
    });
  }

  setAvailabilityReason(productId: Uuid, reason: string): void {
    this.availabilityReasons = { ...this.availabilityReasons, [productId]: reason };
  }

  toggleAvailability(product: ProductModel, available: boolean): void {
    const status: AvailabilityStatus = available ? 'AVAILABLE' : 'UNAVAILABLE';
    const reason = this.availabilityReasons[product.id]?.trim() ?? '';
    if (status === 'UNAVAILABLE' && !reason) {
      this.productError = 'Informe o motivo da indisponibilidade.';
      return;
    }

    this.productError = null;
    this.catalogAdminService
      .updateProductAvailability(product.id, {
        status,
        channel: 'ALL',
        reason: status === 'UNAVAILABLE' ? reason : null
      })
      .subscribe({
        next: (availability) => {
          this.products = this.products.map((candidate) =>
            candidate.id === product.id
              ? {
                  ...candidate,
                  available: availability.status !== 'UNAVAILABLE'
                }
              : candidate
          );
          this.pendingSyncProductIds.delete(product.id);
          this.failedSyncProductIds.delete(product.id);
          if (availability.syncStatus === 'PENDING') {
            this.pendingSyncProductIds.add(product.id);
          }
          if (availability.syncStatus === 'FAILED') {
            this.failedSyncProductIds.add(product.id);
          }
        },
        error: () => {
          this.productError = 'Nao foi possivel atualizar a disponibilidade.';
        }
      });
  }

  getSyncStatus(productId: Uuid): any {
    if (this.failedSyncProductIds.has(productId)) {
      return 'FAILED';
    }
    return this.pendingSyncProductIds.has(productId) ? 'PENDING' : 'SYNCED';
  }

  private loadCatalog(): void {
    this.catalogAdminService.listCategories().subscribe((categories) => {
      this.categories = categories;
    });
    this.catalogAdminService.listProducts().subscribe((products) => {
      this.products = products;
    });
  }

  private categoryPayload(): CategoryPayload {
    return this.categoryForm.getRawValue();
  }

  private productPayload(): ProductPayload {
    const formValue = this.productForm.getRawValue();
    return {
      ...formValue,
      categoryId: formValue.categoryId,
      promotionalPrice: this.emptyToNull(formValue.promotionalPrice),
      costPrice: this.emptyToNull(formValue.costPrice),
      sku: this.emptyToNull(formValue.sku),
      barcode: this.emptyToNull(formValue.barcode),
      description: this.emptyToNull(formValue.description)
    };
  }

  private emptyToNull(value: string): string | null {
    const normalized = value.trim();
    return normalized ? normalized : null;
  }

  private upsertCategory(category: CategoryModel): void {
    this.categories = [...this.categories.filter((candidate) => candidate.id !== category.id), category].sort(
      (left, right) => left.displayOrder - right.displayOrder || left.name.localeCompare(right.name)
    );
  }

  private upsertProduct(product: ProductModel): void {
    this.products = [...this.products.filter((candidate) => candidate.id !== product.id), product].sort((left, right) =>
      left.name.localeCompare(right.name)
    );
  }

  private resetCategoryForm(): void {
    this.editingCategoryId = null;
    this.categoryForm.reset({
      name: '',
      description: '',
      displayOrder: 0,
      active: true,
      showOnline: true,
      showOnPdv: true,
      showOnWhatsapp: true
    });
  }

  private resetProductForm(): void {
    this.editingProductId = null;
    this.productForm.reset({
      categoryId: '',
      name: '',
      description: '',
      price: '',
      promotionalPrice: '',
      costPrice: '',
      sku: '',
      barcode: '',
      unitType: 'UNIT',
      preparationSector: 'SEM_PREPARO',
      preparationTimeMinutes: null,
      active: true,
      available: true,
      sellOnPdv: true,
      sellOnline: true,
      sellOnWhatsapp: true
    });
  }
}
