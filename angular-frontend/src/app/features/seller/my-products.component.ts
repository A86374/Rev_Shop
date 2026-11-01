import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { SellerProduct, SellerProductService } from './seller-product.service';

@Component({
  selector: 'app-my-products',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './my-products.component.html',
})
export class MyProductsComponent {
  private service = inject(SellerProductService);

  products = signal<SellerProduct[]>([]);
  loading = signal(true);
  error = signal('');
  busyId = signal<number | null>(null);

  constructor() {
    this.service.myProducts().subscribe({
      next: (list) => {
        this.products.set(list);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message ?? 'Could not load your products');
        this.loading.set(false);
      },
    });
  }

  toggle(product: SellerProduct): void {
    this.busyId.set(product.id);
    this.error.set('');
    this.service.setActive(product.id, !product.active).subscribe({
      next: (updated) => {
        this.products.update((list) => list.map((p) => (p.id === updated.id ? updated : p)));
        this.busyId.set(null);
      },
      error: (err) => {
        this.error.set(err.error?.message ?? 'Could not update this product');
        this.busyId.set(null);
      },
    });
  }

  isLow(product: SellerProduct): boolean {
    return product.stock > 0 && product.stock <= product.lowStockThreshold;
  }
}
