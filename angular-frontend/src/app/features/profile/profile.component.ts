import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth.service';
import { Profile, ProfileService } from './profile.service';
import { AddressBookComponent } from './address-book.component';

@Component({
  selector: 'app-profile',
  imports: [DatePipe, ReactiveFormsModule, AddressBookComponent],
  templateUrl: './profile.component.html',
})
export class ProfileComponent {
  private service = inject(ProfileService);
  private fb = inject(FormBuilder);
  protected auth = inject(AuthService);

  profile = signal<Profile | null>(null);
  error = signal('');
  saving = signal(false);
  saved = signal(false);

  passwordSaving = signal(false);
  passwordError = signal('');
  passwordChanged = signal(false);

  // same rules as UpdateProfileRequest
  form = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.maxLength(100)]],
    phone: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
    businessName: ['', Validators.maxLength(150)],
  });

  passwordForm = this.fb.nonNullable.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', Validators.required],
  });

  constructor() {
    this.service.get().subscribe({
      next: p => {
        this.profile.set(p);
        this.form.setValue({ fullName: p.fullName, phone: p.phone, businessName: p.businessName ?? '' });
        // a seller can't remove the business name (US-02)
        if (p.role === 'SELLER') {
          this.form.controls.businessName.addValidators(Validators.required);
          this.form.controls.businessName.updateValueAndValidity();
        }
      },
      error: err => this.error.set(err.error?.message ?? 'Could not load your profile'),
    });
  }

  showError(name: 'fullName' | 'phone' | 'businessName'): boolean {
    const control = this.form.controls[name];
    return control.invalid && (control.dirty || control.touched);
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.saved.set(false);
    this.error.set('');
    this.service.update(this.form.getRawValue()).subscribe({
      next: p => {
        this.profile.set(p);
        this.auth.updateUser({ fullName: p.fullName });   // the navbar shows the new name
        this.form.markAsPristine();
        this.saving.set(false);
        this.saved.set(true);
      },
      error: err => {
        this.error.set(err.error?.message ?? 'Could not save your profile');
        this.saving.set(false);
      },
    });
  }

  changePassword(): void {
    const { currentPassword, newPassword, confirmPassword } = this.passwordForm.getRawValue();
    this.passwordChanged.set(false);
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }
    if (newPassword !== confirmPassword) {
      this.passwordError.set('The two new passwords don’t match');
      return;
    }
    this.passwordSaving.set(true);
    this.passwordError.set('');
    this.service.changePassword(currentPassword, newPassword).subscribe({
      next: () => {
        this.passwordForm.reset();
        this.passwordSaving.set(false);
        this.passwordChanged.set(true);
      },
      error: err => {
        // 400 "Current password is wrong" or "must be different"
        this.passwordError.set(err.error?.message ?? 'Could not change your password');
        this.passwordSaving.set(false);
      },
    });
  }
}
