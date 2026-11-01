import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { AdminMonitorService, AdminPayment, PaymentStatus } from './admin-monitor.service';

@Component({
  selector: 'app-admin-payments',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './admin-payments.component.html',
})
export class AdminPaymentsComponent {
  private service = inject(AdminMonitorService);

  readonly badge: Record<PaymentStatus, string> = {
    PENDING: 'text-bg-warning',
    SUCCESS: 'text-bg-success',
    FAILED: 'text-bg-danger',
  };

  payments = signal<AdminPayment[]>([]);
  status = signal('');
  loading = signal(true);
  error = signal('');

  // money actually received, among the payments on screen
  received = computed(() =>
    this.payments()
      .filter((p) => p.status === 'SUCCESS')
      .reduce((sum, p) => sum + p.amount, 0),
  );

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
    this.service.payments(this.status()).subscribe({
      next: (list) => {
        this.payments.set(list);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message ?? 'Could not load payments');
        this.loading.set(false);
      },
    });
  }
}
