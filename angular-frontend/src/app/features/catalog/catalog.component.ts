import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CatalogService, Category, PagedResponse, ProductCard } from './catalog.service';
import { CartApiService } from '../cart/cart-api.service';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-catalog',
  imports: [ReactiveFormsModule, RouterLink, CurrencyPipe],
  templateUrl: './catalog.component.html',
})
export class CatalogComponent {
  private service = inject(CatalogService);
  private cart = inject(CartApiService);
  private auth = inject(AuthService);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  readonly pageSize = 12;

  categories = signal<Category[]>([]);
  result = signal<PagedResponse<ProductCard> | null>(null);
  page = signal(0);
  loading = signal(false);
  error = signal('');
  addingId = signal<number | null>(null);
  added = signal(''); // short "added to cart" message

  filters = this.fb.group({
    keyword: [''],
    categoryId: [''],
    minPrice: [null as number | null],
    maxPrice: [null as number | null],
    sort: ['newest'],
  });

  constructor() {
    this.service.categories().subscribe({
      next: (list) => this.categories.set(list),
      error: () => this.categories.set([]),
    });
    this.load();
  }

  apply(): void {
    this.page.set(0);
    this.load();
  }

  reset(): void {
    this.filters.reset({
      keyword: '',
      categoryId: '',
      minPrice: null,
      maxPrice: null,
      sort: 'newest',
    });
    this.apply();
  }

  goTo(page: number): void {
    this.page.set(page);
    this.load();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  addToCart(p: ProductCard): void {
    if (!this.auth.user()) {
      this.router.navigate(['/login']);
      return;
    }
    this.addingId.set(p.id);
    this.error.set('');
    this.cart.add(p.id).subscribe({
      next: () => {
        this.addingId.set(null);
        this.added.set(`${p.name} added to your cart`);
        setTimeout(() => this.added.set(''), 2500);
      },
      error: (err) => {
        this.addingId.set(null);
        this.error.set(err.error?.message ?? 'Could not add to cart');
      },
    });
  }

  private load(): void {
    const f = this.filters.getRawValue();
    this.loading.set(true);
    this.error.set('');

    this.service
      .search({
        keyword: f.keyword ?? '',
        categoryId: f.categoryId ? Number(f.categoryId) : null,
        minPrice: f.minPrice,
        maxPrice: f.maxPrice,
        sort: f.sort ?? 'newest',
        page: this.page(),
        size: this.pageSize,
      })
      .subscribe({
        next: (res) => {
          this.result.set(res);
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set(err.error?.message ?? 'Could not load products');
          this.loading.set(false);
        },
      });
  }
}
