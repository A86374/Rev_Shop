import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { WishlistItem, WishlistService } from './wishlist.service';
import { CartApiService } from '../cart/cart-api.service';

@Component({
  selector: 'app-wishlist',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './wishlist.component.html',
})
export class WishlistComponent {
  private wishlist = inject(WishlistService);
  private cart = inject(CartApiService);

  items = signal<WishlistItem[]>([]);
  loading = signal(true);
  error = signal('');
  busyId = signal<number | null>(null);
  message = signal('');

  constructor() {
    this.wishlist.list().subscribe({
      next: (items) => {
        this.items.set(items);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message ?? 'Could not load your wishlist');
        this.loading.set(false);
      },
    });
  }

  // add to the cart first, then take it off the wishlist
  moveToCart(item: WishlistItem): void {
    this.busyId.set(item.productId);
    this.error.set('');
    this.cart
      .add(item.productId)
      .pipe(switchMap(() => this.wishlist.remove(item.productId)))
      .subscribe({
        next: () => {
          this.drop(item);
          this.flash(`${item.name} moved to your cart`);
        },
        error: (err) => {
          this.busyId.set(null);
          this.error.set(err.error?.message ?? 'Could not move it to your cart');
        },
      });
  }

  remove(item: WishlistItem): void {
    this.busyId.set(item.productId);
    this.error.set('');
    this.wishlist.remove(item.productId).subscribe({
      next: () => this.drop(item),
      error: (err) => {
        this.busyId.set(null);
        this.error.set(err.error?.message ?? 'Could not remove it');
      },
    });
  }

  private drop(item: WishlistItem): void {
    this.items.update((list) => list.filter((i) => i.productId !== item.productId));
    this.busyId.set(null);
  }

  private flash(text: string): void {
    this.message.set(text);
    setTimeout(() => this.message.set(''), 2500);
  }
}
