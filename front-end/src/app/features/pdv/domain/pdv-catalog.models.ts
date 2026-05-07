import type { CategoryModel, ProductModel, Uuid } from '../../../shared/models/domain.models';

export interface PdvProductGroup {
  category: CategoryModel;
  products: ProductModel[];
}

export interface PdvProductFilters {
  name?: string | null;
  categoryId?: Uuid | null;
}
