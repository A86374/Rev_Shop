import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

// what your WishlistItemResponse returns
export interface WishlistItem {
  productId: number;
  name: string | null; // null when the product was removed or deactivated
  price: number | null;
  available: boolean; // active and in stock
  addedAt: string;
}

// switch to the environment file once Kiran's base app has one
const API = 'http://localhost:8080/api/wishlist';

@Injectable({ providedIn: 'root' })
export class WishlistService {
  private http = inject(HttpClient);

  // ids of the saved products, so the product page can fill its heart
  private ids = signal<ReadonlySet<number>>(new Set());

  has(productId: number): boolean {
    return this.ids().has(productId);
  }

  list(): Observable<WishlistItem[]> {
    return this.http
      .get<WishlistItem[]>(API)
      .pipe(tap((items) => this.ids.set(new Set(items.map((i) => i.productId)))));
  }

  add(productId: number): Observable<WishlistItem> {
    return this.http
      .post<WishlistItem>(API, { productId })
      .pipe(tap(() => this.ids.update((ids) => new Set(ids).add(productId))));
  }

  remove(productId: number): Observable<void> {
    return this.http.delete<void>(`${API}/${productId}`).pipe(
      tap(() =>
        this.ids.update((ids) => {
          const next = new Set(ids);
          next.delete(productId);
          return next;
        }),
      ),
    );
  }
}
