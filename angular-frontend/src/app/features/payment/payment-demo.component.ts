import { Component, signal } from '@angular/core';
import { PaymentPopupComponent } from './payment-popup.component';
import { PaymentResult } from './payment-api.service';

// TEMPORARY test page for the popup.
// On Friday, Nikitha's checkout page opens the popup itself; then delete this file and its route.
@Component({
  selector: 'app-payment-demo',
  imports: [PaymentPopupComponent],
  template: `
    <div class="container py-4" style="max-width: 480px">
      <h2 class="h4">Payment popup test</h2>
      <p class="text-muted small">Create an ONLINE payment in Postman first, then enter its id here.</p>

      <label class="form-label" for="pid">Payment id</label>
      <input id="pid" class="form-control mb-2" type="number" #pid value="1">

      <label class="form-label" for="amt">Amount (₹)</label>
      <input id="amt" class="form-control mb-3" type="number" #amt value="1499">

      <button type="button" class="btn btn-primary" (click)="open(+pid.value, +amt.value)">Open popup</button>

      @if (lastResult()) {
        <p class="mt-3 mb-0">Last result: <strong>{{ lastResult() }}</strong></p>
      }
    </div>

    @if (showPopup()) {
      <app-payment-popup [paymentId]="paymentId()" [amount]="amount()" (finished)="onFinished($event)" />
    }
  `,
})
export class PaymentDemoComponent {
  showPopup = signal(false);
  paymentId = signal(0);
  amount = signal(0);
  lastResult = signal<PaymentResult | ''>('');

  open(id: number, amount: number): void {
    this.paymentId.set(id);
    this.amount.set(amount);
    this.showPopup.set(true);
  }

  onFinished(result: PaymentResult): void {
    this.lastResult.set(result);
    this.showPopup.set(false);
  }
}
