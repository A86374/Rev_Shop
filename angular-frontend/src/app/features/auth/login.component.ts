import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  saving = signal(false);
  error = signal('');

  // Hasitha's seller signup sends ?registered=seller after a successful signup
  readonly justRegistered = this.route.snapshot.queryParamMap.get('registered');

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
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

    const { email, password } = this.form.getRawValue();
    this.auth.login(email, password).subscribe({
      next: user => {
        // back to the page that sent them here, or their role's home
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        this.router.navigateByUrl(returnUrl ?? this.auth.homeFor(user.role));
      },
      error: err => {
        // 401 wrong email/password, 403 blocked account (Abhinaya's block feature)
        this.error.set(err.error?.message ?? 'Login failed. Please try again.');
        this.saving.set(false);
      },
    });
  }
}
