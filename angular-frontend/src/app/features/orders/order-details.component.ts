import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Order, OrderService, STATUS_BADGE, STATUS_LABEL } from './order.service';

@Component({
  selector: 'app-order-details',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './order-details.component.html',
})
export class OrderDetailsComponent {
  private service = inject(OrderService);
  private route = inject(ActivatedRoute);

  readonly label = STATUS_LABEL;
  readonly badge = STATUS_BADGE;

  order = signal<Order | null>(null);
  error = signal('');
  confirming = signal(false); // first click asks "are you sure?"
  cancelling = signal(false);

  canCancel = computed(() => {
    const status = this.order()?.status;
    return status === 'PAYMENT_PENDING' || status === 'PLACED';
  });

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.service.get(id).subscribe({
      next: (order) => this.order.set(order),
      error: (err) => this.error.set(err.error?.message ?? 'Could not load this order'),
    });
  }

  cancel(): void {
    const order = this.order();
    if (!order) {
      return;
    }
    this.cancelling.set(true);
    this.error.set('');
    this.service.cancel(order.id).subscribe({
      next: (updated) => {
        this.order.set(updated);
        this.confirming.set(false);
        this.cancelling.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message ?? 'Could not cancel this order');
        this.cancelling.set(false);
      },
    });
  }
}
