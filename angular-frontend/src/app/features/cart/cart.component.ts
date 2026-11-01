import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CartApiService, CartRow } from './cart-api.service';

@Component({
  selector: 'app-cart',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './cart.component.html',
})
export class CartComponent {
  private cart = inject(CartApiService);

  rows = signal<CartRow[]>([]);
  loading = signal(true);
  error = signal('');
  busyId = signal<number | null>(null);

  total = computed(() =>
    this.rows().reduce((sum, r) => sum + (r.product ? r.product.price * r.line.quantity : 0), 0),
  );

  // checkout stays disabled while something in the cart can't be bought
  blocked = computed(() => this.rows().some((r) => !this.available(r)));

  constructor() {
    this.reload();
  }

  available(r: CartRow): boolean {
    return !!r.product && r.product.active && r.line.quantity <= r.product.stock;
  }

  change(r: CartRow, delta: number): void {
    const quantity = r.line.quantity + delta;
    if (quantity < 1) {
      return;
    }
    this.busyId.set(r.line.productId);
    this.error.set('');
    this.cart.update(r.line.productId, quantity).subscribe({
      next: () => this.reload(),
      error: (err) => {
        this.error.set(err.error?.message ?? 'Could not change the quantity');
        this.busyId.set(null);
      },
    });
  }

  remove(r: CartRow): void {
    this.busyId.set(r.line.productId);
    this.error.set('');
    this.cart.remove(r.line.productId).subscribe({
      next: () => this.reload(),
      error: (err) => {
        this.error.set(err.error?.message ?? 'Could not remove this item');
        this.busyId.set(null);
      },
    });
  }

  private reload(): void {
    this.cart.load().subscribe({
      next: (rows) => {
        this.rows.set(rows);
        this.loading.set(false);
        this.busyId.set(null);
      },
      error: (err) => {
        this.error.set(err.error?.message ?? 'Could not load your cart');
        this.loading.set(false);
        this.busyId.set(null);
      },
    });
  }
}
