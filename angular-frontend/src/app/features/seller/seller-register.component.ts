import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

// switch to the environment file once Kiran's base app has one
const API = 'http://localhost:8080/api/auth/register';

@Component({
  selector: 'app-seller-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './seller-register.component.html',
})
export class SellerRegisterComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);

  saving = signal(false);
  error = signal('');

  // same rules as the backend, so most mistakes are caught before sending
  form = this.fb.nonNullable.group({
    businessName: ['', [Validators.required, Validators.maxLength(150)]],
    fullName: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  showError(name: string): boolean {
    const control = this.form.get(name);
    return !!control && control.invalid && (control.dirty || control.touched);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set('');

    this.http.post(API, { ...this.form.getRawValue(), role: 'SELLER' }).subscribe({
      next: () => this.router.navigate(['/login'], { queryParams: { registered: 'seller' } }),
      error: (err) => {
        this.error.set(err.error?.message ?? 'Registration failed. Try again.');
        this.saving.set(false);
      },
    });
  }
}
