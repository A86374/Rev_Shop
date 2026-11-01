import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Order, OrderService, STATUS_BADGE, STATUS_LABEL } from './order.service';

@Component({
  selector: 'app-my-orders',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './my-orders.component.html',
})
export class MyOrdersComponent {
  private service = inject(OrderService);

  readonly label = STATUS_LABEL;
  readonly badge = STATUS_BADGE;

  orders = signal<Order[]>([]);
  loading = signal(true);
  error = signal('');

  constructor() {
    this.service.myOrders().subscribe({
      next: (list) => {
        this.orders.set(list);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message ?? 'Could not load your orders');
        this.loading.set(false);
      },
    });
  }

  itemCount(order: Order): number {
    return order.items.reduce((sum, item) => sum + item.quantity, 0);
  }
}
