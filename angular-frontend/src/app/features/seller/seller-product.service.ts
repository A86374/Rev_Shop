import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Category {
  id: number;
  name: string;
}

export interface SellerProduct {
  id: number;
  name: string;
  description: string | null;
  category: Category;
  mrp: number;
  price: number;
  stock: number;
  lowStockThreshold: number;
  sellerId: number;
  active: boolean;
}

export interface ProductPayload {
  name: string;
  description: string;
  categoryId: number;
  mrp: number;
  price: number;
  stock: number;
  lowStockThreshold: number;
}

export interface ProductPhoto {
  id: number;
  url: string;
}

// switch to the environment file once Kiran's base app has one
const API = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class SellerProductService {
  private http = inject(HttpClient);

  categories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${API}/categories`);
  }

  myProducts(): Observable<SellerProduct[]> {
    return this.http.get<SellerProduct[]>(`${API}/seller/products`);
  }

  add(product: ProductPayload): Observable<SellerProduct> {
    return this.http.post<SellerProduct>(`${API}/seller/products`, product);
  }

  update(id: number, product: ProductPayload): Observable<SellerProduct> {
    return this.http.put<SellerProduct>(`${API}/seller/products/${id}`, product);
  }

  setActive(id: number, active: boolean): Observable<SellerProduct> {
    return this.http.patch<SellerProduct>(`${API}/seller/products/${id}/status`, { active });
  }

  photos(id: number): Observable<ProductPhoto[]> {
    return this.http.get<ProductPhoto[]>(`${API}/products/${id}/images`);
  }

  uploadPhoto(id: number, file: File): Observable<ProductPhoto> {
    const form = new FormData();
    form.append('file', file); // the name must match @RequestParam("file")
    return this.http.post<ProductPhoto>(`${API}/seller/products/${id}/images`, form);
  }

  deletePhoto(id: number, photoId: number): Observable<void> {
    return this.http.delete<void>(`${API}/seller/products/${id}/images/${photoId}`);
  }
}
