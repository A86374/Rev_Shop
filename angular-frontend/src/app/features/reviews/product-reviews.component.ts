import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProductReviews, ReviewService } from './review.service';
import { AuthService } from '../../core/auth.service';

// used on Nikitha's product page: <app-product-reviews [productId]="p.id" />
@Component({
  selector: 'app-product-reviews',
  imports: [DatePipe, ReactiveFormsModule, RouterLink],
  templateUrl: './product-reviews.component.html',
})
export class ProductReviewsComponent {
  private service = inject(ReviewService);
  private fb = inject(FormBuilder);
  protected auth = inject(AuthService);

  productId = input.required<number>();

  data = signal<ProductReviews | null>(null);
  saving = signal(false);
  error = signal('');
  thanks = signal(false);

  isBuyer = computed(() => this.auth.user()?.role === 'BUYER');

  form = this.fb.nonNullable.group({
    rating: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
    comment: ['', Validators.maxLength(1000)],
  });

  constructor() {
    // runs again when the buyer opens another product
    effect(() => {
      const id = this.productId();
      untracked(() => this.load(id));
    });
  }

  stars(rating: number): string {
    const full = Math.round(rating);
    return '★'.repeat(full) + '☆'.repeat(5 - full);
  }

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    this.saving.set(true);
    this.error.set('');
    const { rating, comment } = this.form.getRawValue();
    this.service.add(this.productId(), Number(rating), comment.trim()).subscribe({
      next: () => {
        this.saving.set(false);
        this.thanks.set(true);
        this.form.reset({ rating: 5, comment: '' });
        this.load(this.productId());
      },
      error: (err) => {
        // 403: not delivered to you yet, 409: you already reviewed it
        this.error.set(err.error?.message ?? 'Could not save your review');
        this.saving.set(false);
      },
    });
  }

  private load(id: number): void {
    this.thanks.set(false);
    this.error.set('');
    this.service.forProduct(id).subscribe({
      next: (data) => this.data.set(data),
      error: () => this.data.set(null),
    });
  }
}
