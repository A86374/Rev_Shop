import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CartApiService, CartRow } from '../cart/cart-api.service';
import { CheckoutResult, CheckoutService } from './checkout.service';
import { PaymentPopupComponent } from '../payment/payment-popup.component';
import { PaymentResult } from '../payment/payment-api.service';

@Component({
  selector: 'app-checkout',
  imports: [ReactiveFormsModule, CurrencyPipe, RouterLink, PaymentPopupComponent],
  templateUrl: './checkout.component.html',
})
export class CheckoutComponent {
  private cart = inject(CartApiService);
  private checkoutApi = inject(CheckoutService);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  rows = signal<CartRow[]>([]);
  loading = signal(true);
  placing = signal(false);
  error = signal('');
  pendingPayment = signal<CheckoutResult | null>(null); // set for ONLINE: opens the popup

  total = computed(() =>
    this.rows().reduce((sum, r) => sum + (r.product ? r.product.price * r.line.quantity : 0), 0),
  );

  form = this.fb.nonNullable.group({
    shippingAddress: ['', [Validators.required, Validators.maxLength(500)]],
    paymentMethod: ['COD' as 'ONLINE' | 'COD', Validators.required],
  });

  constructor() {
    this.cart.load().subscribe({
      next: (rows) => {
        this.rows.set(rows);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message ?? 'Could not load your cart');
        this.loading.set(false);
      },
    });
  }

  showError(name: string): boolean {
    const control = this.form.get(name);
    return !!control && control.invalid && (control.dirty || control.touched);
  }

  placeOrder(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.placing.set(true);
    this.error.set('');

    this.checkoutApi.checkout(this.form.getRawValue()).subscribe({
      next: (res) => {
        this.cart.count.set(0);
        if (res.paymentMethod === 'ONLINE') {
          this.pendingPayment.set(res); // Abhinaya's popup takes over
        } else {
          this.router.navigate(['/orders'], { queryParams: { placed: res.checkoutRef } });
        }
      },
      error: (err) => {
        this.error.set(err.error?.message ?? 'Checkout failed. Please try again.');
        this.placing.set(false);
      },
    });
  }

  onPaymentFinished(result: PaymentResult): void {
    const ref = this.pendingPayment()?.checkoutRef;
    this.pendingPayment.set(null);
    this.router.navigate(['/orders'], {
      queryParams: result === 'SUCCESS' ? { placed: ref } : { cancelled: ref },
    });
  }
}
