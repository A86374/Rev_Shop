import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable } from 'rxjs';
import { CatalogService, ProductDetails } from './catalog.service';
import { CartApiService } from '../cart/cart-api.service';
import { WishlistService } from '../wishlist/wishlist.service';
import { AuthService } from '../../core/auth.service';
// Abhinaya adds her reviews import here

@Component({
  selector: 'app-product-details',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './product-details.component.html',
})
export class ProductDetailsComponent {
  private service = inject(CatalogService);
  private cart = inject(CartApiService);
  private auth = inject(AuthService);
  private router = inject(Router);
  protected wishlist = inject(WishlistService);

  product = signal<ProductDetails | null>(null);
  photo = signal<string | null>(null); // the big photo
  quantity = signal(1);
  loading = signal(true);
  busy = signal(false);
  error = signal('');
  message = signal('');

  // visitors and buyers can shop; sellers and admins only look
  canBuy = computed(() => {
    const user = this.auth.user();
    return !user || user.role === 'BUYER';
  });

  constructor() {
    // /products/1 -> /products/2 reuses this page, so it listens to the id
    inject(ActivatedRoute)
      .paramMap.pipe(takeUntilDestroyed())
      .subscribe((params) => this.load(Number(params.get('id'))));

    if (this.auth.user()?.role === 'BUYER') {
      this.wishlist.list().subscribe({ error: () => undefined }); // fills the heart
    }
  }

  changeQuantity(step: number): void {
    const p = this.product();
    if (p) {
      this.quantity.set(Math.min(Math.max(1, this.quantity() + step), Math.max(1, p.stock)));
    }
  }

  addToCart(): void {
    const p = this.product();
    if (!p || !this.loggedIn(p)) {
      return;
    }
    this.busy.set(true);
    this.error.set('');
    this.cart.add(p.id, this.quantity()).subscribe({
      next: () => {
        this.busy.set(false);
        this.flash(`${p.name} added to your cart`);
      },
      error: (err) => {
        this.busy.set(false);
        this.error.set(err.error?.message ?? 'Could not add to cart');
      },
    });
  }

  toggleWishlist(): void {
    const p = this.product();
    if (!p || !this.loggedIn(p)) {
      return;
    }
    const saved = this.wishlist.has(p.id);
    const call: Observable<unknown> = saved ? this.wishlist.remove(p.id) : this.wishlist.add(p.id);
    this.error.set('');
    call.subscribe({
      next: () => this.flash(saved ? 'Removed from your wishlist' : 'Saved to your wishlist'),
      error: (err) => this.error.set(err.error?.message ?? 'Could not update your wishlist'),
    });
  }

  private load(id: number): void {
    this.loading.set(true);
    this.error.set('');
    this.product.set(null);
    this.quantity.set(1);
    this.service.details(id).subscribe({
      next: (p) => {
        this.product.set(p);
        this.photo.set(p.images[0] ?? null);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(
          err.status === 404
            ? 'This product is not available.'
            : (err.error?.message ?? 'Could not load this product'),
        );
        this.loading.set(false);
      },
    });
  }

  // visitors go to the login page and come back here afterwards
  private loggedIn(p: ProductDetails): boolean {
    if (this.auth.user()) {
      return true;
    }
    this.router.navigate(['/login'], { queryParams: { returnUrl: `/products/${p.id}` } });
    return false;
  }

  private flash(text: string): void {
    this.message.set(text);
    setTimeout(() => this.message.set(''), 2500);
  }
}
