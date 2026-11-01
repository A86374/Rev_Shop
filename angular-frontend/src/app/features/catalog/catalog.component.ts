import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CatalogService, Category, PagedResponse, ProductCard } from './catalog.service';

@Component({
  selector: 'app-catalog',
  imports: [ReactiveFormsModule, RouterLink, CurrencyPipe],
  templateUrl: './catalog.component.html',
})
export class CatalogComponent {
  private service = inject(CatalogService);
  private fb = inject(FormBuilder);

  readonly pageSize = 12;

  categories = signal<Category[]>([]);
  result = signal<PagedResponse<ProductCard> | null>(null);
  page = signal(0);
  loading = signal(false);
  error = signal('');

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
