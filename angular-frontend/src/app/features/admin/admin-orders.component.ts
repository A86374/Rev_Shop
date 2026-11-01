import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { AdminMonitorService } from './admin-monitor.service';
import { Order, OrderStatus, STATUS_BADGE, STATUS_LABEL } from '../orders/order.service';

@Component({
  selector: 'app-admin-orders',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './admin-orders.component.html',
})
export class AdminOrdersComponent {
  private service = inject(AdminMonitorService);

  readonly label = STATUS_LABEL;
  readonly badge = STATUS_BADGE;
  readonly statuses = Object.keys(STATUS_LABEL) as OrderStatus[];

  orders = signal<Order[]>([]);
  status = signal('');
  loading = signal(true);
  error = signal('');

  // totals of what is on screen
  total = computed(() => this.orders().reduce((sum, o) => sum + o.totalAmount, 0));
  refunds = computed(() => this.orders().filter((o) => o.refunded).length);

  constructor() {
    this.load();
  }

  filter(status: string): void {
    this.status.set(status);
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.error.set('');
    this.service.orders(this.status()).subscribe({
      next: (list) => {
        this.orders.set(list);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message ?? 'Could not load orders');
        this.loading.set(false);
      },
    });
  }
}
