import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { ORDER_BADGE, ORDER_LABEL, SellerOrder, SellerOrderService } from './seller-order.service';

@Component({
  selector: 'app-seller-orders',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './seller-orders.component.html',
})
export class SellerOrdersComponent {
  private service = inject(SellerOrderService);

  readonly label = ORDER_LABEL;
  readonly badge = ORDER_BADGE;
  readonly tabs = [
    { value: '', text: 'All' },
    { value: 'PLACED', text: 'To ship' },
    { value: 'SHIPPED', text: 'Shipped' },
    { value: 'DELIVERED', text: 'Delivered' },
    { value: 'CANCELLED', text: 'Cancelled' },
  ];

  // the dashboard links here with ?status=PLACED
  status = signal(inject(ActivatedRoute).snapshot.queryParamMap.get('status') ?? '');
  orders = signal<SellerOrder[]>([]);
  loading = signal(true);
  error = signal('');
  busyId = signal<number | null>(null);

  constructor() {
    this.load();
  }

  show(status: string): void {
    this.status.set(status);
    this.load();
  }

  // PLACED -> SHIPPED -> DELIVERED
  advance(order: SellerOrder): void {
    const next = order.status === 'PLACED' ? 'SHIPPED' : 'DELIVERED';
    this.busyId.set(order.id);
    this.error.set('');
    this.service.updateStatus(order.id, next).subscribe({
      next: (updated) => {
        // on a filtered tab the order moves to another tab, so it leaves this list
        this.orders.update((list) =>
          this.status() && updated.status !== this.status()
            ? list.filter((o) => o.id !== updated.id)
            : list.map((o) => (o.id === updated.id ? updated : o)),
        );
        this.busyId.set(null);
      },
      error: (err) => {
        this.error.set(err.error?.message ?? 'Could not update this order');
        this.busyId.set(null);
      },
    });
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
        this.error.set(err.error?.message ?? 'Could not load your orders');
        this.loading.set(false);
      },
    });
  }
}
