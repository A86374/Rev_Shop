import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, forkJoin, map, of, switchMap, tap } from 'rxjs';
import { AuthService } from '../../core/auth.service';

export interface CartLine {
  id: number;
  userId: number;
  productId: number;
  quantity: number;
}

export interface CartProduct {
  id: number;
  name: string;
  price: number;
  mrp: number;
  stock: number;
  active: boolean;
}

export interface CartRow {
  line: CartLine;
  product: CartProduct | null; // null = product removed or deactivated
}

// switch to the environment file once Kiran's base app has one
const API = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class CartApiService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  count = signal(0); // number of items, for the navbar badge

  private get cartUrl(): string {
    return `${API}/cart/${this.auth.user()?.userId}`;
  }

  // your cart API only stores product ids, so each product's name and price is fetched too
  load(): Observable<CartRow[]> {
    return this.http.get<CartLine[]>(this.cartUrl).pipe(
      switchMap((lines) =>
        lines.length === 0
          ? of([] as CartRow[])
          : forkJoin(
              lines.map((line) =>
                this.http.get<CartProduct>(`${API}/products/${line.productId}`).pipe(
                  map((product): CartRow => ({ line, product })),
                  catchError(() => of<CartRow>({ line, product: null })),
                ),
              ),
            ),
      ),
      tap((rows) => this.count.set(rows.reduce((sum, r) => sum + r.line.quantity, 0))),
    );
  }

  add(productId: number, quantity = 1): Observable<CartLine> {
    return this.http
      .post<CartLine>(`${this.cartUrl}/items`, { productId, quantity })
      .pipe(tap(() => this.refreshCount()));
  }

  update(productId: number, quantity: number): Observable<CartLine> {
    return this.http.put<CartLine>(`${this.cartUrl}/items/${productId}`, { productId, quantity });
  }

  remove(productId: number): Observable<void> {
    return this.http.delete<void>(`${this.cartUrl}/items/${productId}`);
  }

  refreshCount(): void {
    if (!this.auth.user()) {
      this.count.set(0);
      return;
    }
    this.http.get<CartLine[]>(this.cartUrl).subscribe({
      next: (lines) => this.count.set(lines.reduce((sum, l) => sum + l.quantity, 0)),
      error: () => this.count.set(0),
    });
  }
}
