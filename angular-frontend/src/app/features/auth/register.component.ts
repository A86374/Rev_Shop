import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  saving = signal(false);
  error = signal('');

  // same rules as the backend (RegisterRequest), so most mistakes are caught before sending
  form = this.fb.nonNullable.group({
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

    // register returns a token too, so the buyer is logged in straight away
    this.auth.register(this.form.getRawValue()).subscribe({
      next: user => this.router.navigateByUrl(this.auth.homeFor(user.role)),
      error: err => {
        this.error.set(err.error?.message ?? 'Registration failed. Please try again.');
        this.saving.set(false);
      },
    });
  }
}
