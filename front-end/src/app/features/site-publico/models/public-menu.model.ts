export interface StoreInfoResponse {
  name: string;
  address: string;
  hours: string;
  phone: string;
  description: string;
}

export interface PublicProductResponse {
  id: string;
  name: string;
  description: string | null;
  price: number;
  promotionalPrice: number | null;
  imageUrl: string | null;
}

export interface PublicCategoryResponse {
  id: string;
  name: string;
  description: string | null;
  imageUrl: string | null;
}

export interface PublicMenuResponse {
  category: PublicCategoryResponse;
  products: PublicProductResponse[];
}
