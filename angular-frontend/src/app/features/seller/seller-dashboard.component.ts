import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import {
  ORDER_BADGE,
  ORDER_LABEL,
  SellerDashboard,
  SellerOrderService,
} from './seller-order.service';
import { SellerProduct, SellerProductService } from './seller-product.service';

@Component({
  selector: 'app-seller-dashboard',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './seller-dashboard.component.html',
})
export class SellerDashboardComponent {
  readonly label = ORDER_LABEL;
  readonly badge = ORDER_BADGE;

  data = signal<SellerDashboard | null>(null);
  lowStock = signal<SellerProduct[]>([]);
  error = signal('');

  constructor() {
    inject(SellerOrderService)
      .dashboard()
      .subscribe({
        next: (d) => this.data.set(d),
        error: (err) => this.error.set(err.error?.message ?? 'Could not load your dashboard'),
      });

    // low stock comes from your own product list (product-service)
    inject(SellerProductService)
      .myProducts()
      .subscribe({
        next: (list) =>
          this.lowStock.set(list.filter((p) => p.active && p.stock <= p.lowStockThreshold)),
        error: () => this.lowStock.set([]),
      });
  }
}
