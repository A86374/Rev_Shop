import { Component, inject, signal } from '@angular/core';
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

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.service.get(id).subscribe({
      next: (order) => this.order.set(order),
      error: (err) => this.error.set(err.error?.message ?? 'Could not load this order'),
    });
  }
}
