import { Component, DestroyRef, OnInit, computed, inject, input, output, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { PaymentApiService, PaymentResult } from './payment-api.service';

type PopupState = 'waiting' | 'working' | 'success' | 'failed';

@Component({
  selector: 'app-payment-popup',
  imports: [CurrencyPipe],
  templateUrl: './payment-popup.component.html',
})
export class PaymentPopupComponent implements OnInit {
  private api = inject(PaymentApiService);
  private destroyRef = inject(DestroyRef);

  paymentId = input.required<number>();
  amount = input.required<number>();
  durationSeconds = input(60);          // matches app.payment.expiry-minutes=1

  finished = output<PaymentResult>();   // SUCCESS or FAILED, sent when the buyer clicks Close

  state = signal<PopupState>('waiting');
  message = signal('');
  secondsLeft = signal(60);
  timeLeft = computed(() => {
    const s = this.secondsLeft();
    return `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`;
  });

  private timer?: ReturnType<typeof setInterval>;
  private result: PaymentResult = 'FAILED';

  ngOnInit(): void {
    this.secondsLeft.set(this.durationSeconds());
    this.timer = setInterval(() => {
      const next = this.secondsLeft() - 1;
      this.secondsLeft.set(Math.max(next, 0));
      if (next <= 0) {
        this.decide('CANCEL', 'Time is up. The payment was cancelled.');
      }
    }, 1000);
    this.destroyRef.onDestroy(() => this.stopTimer());
  }

  decide(action: 'PAY' | 'CANCEL', timeoutMessage?: string): void {
    if (this.state() !== 'waiting') {
      return;
    }
    this.stopTimer();
    this.state.set('working');
    this.api.confirm(this.paymentId(), action).subscribe({
      next: res => {
        this.result = res.status === 'SUCCESS' ? 'SUCCESS' : 'FAILED';
        if (this.result === 'SUCCESS') {
          this.state.set('success');
        } else {
          this.message.set(timeoutMessage ?? 'Payment cancelled.');
          this.state.set('failed');
        }
      },
      error: err => {
        // e.g. 409 "Payment expired" when the server already timed it out
        this.result = 'FAILED';
        this.message.set(timeoutMessage ?? err.error?.message ?? 'Payment could not be completed.');
        this.state.set('failed');
      },
    });
  }

  close(): void {
    this.finished.emit(this.result);
  }

  private stopTimer(): void {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = undefined;
    }
  }
}
