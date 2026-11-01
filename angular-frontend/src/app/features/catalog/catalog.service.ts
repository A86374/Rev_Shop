import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export type StockStatus = 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';

export interface ProductCard {
  id: number;
  name: string;
  price: number;
  mrp: number;
  discountPercent: number;
  categoryName: string;
  imageUrl: string | null;
  stockStatus: StockStatus;
  stockLabel: string;
}

// NEW: what GET /api/products/{id}/details returns
export interface ProductDetails {
  id: number;
  name: string;
  description: string | null;
  categoryId: number;
  categoryName: string;
  mrp: number;
  price: number;
  discountPercent: number;
  stock: number;
  stockStatus: StockStatus;
  stockLabel: string;
  sellerId: number;
  images: string[]; // photo URLs, the first one is the main photo
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Category {
  id: number;
  name: string;
}

export interface CatalogQuery {
  keyword: string;
  categoryId: number | null;
  minPrice: number | null;
  maxPrice: number | null;
  sort: string;
  page: number;
  size: number;
}

// switch to the environment file once Kiran's base app has one
const API = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private http = inject(HttpClient);

  search(q: CatalogQuery): Observable<PagedResponse<ProductCard>> {
    let params = new HttpParams().set('page', q.page).set('size', q.size).set('sort', q.sort);
    // only send the filters the buyer actually set
    if (q.keyword.trim()) {
      params = params.set('keyword', q.keyword.trim());
    }
    if (q.categoryId) {
      params = params.set('categoryId', q.categoryId);
    }
    if (q.minPrice != null) {
      params = params.set('minPrice', q.minPrice);
    }
    if (q.maxPrice != null) {
      params = params.set('maxPrice', q.maxPrice);
    }
    return this.http.get<PagedResponse<ProductCard>>(`${API}/products/search`, { params });
  }

  // NEW: the product page
  details(id: number): Observable<ProductDetails> {
    return this.http.get<ProductDetails>(`${API}/products/${id}/details`);
  }

  categories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${API}/categories`);
  }
}
