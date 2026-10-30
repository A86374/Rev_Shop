import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Address, ProfileService } from './profile.service';

@Component({
  selector: 'app-address-book',
  imports: [ReactiveFormsModule],
  templateUrl: './address-book.component.html',
})
export class AddressBookComponent {
  private service = inject(ProfileService);
  private fb = inject(FormBuilder);

  readonly max = 5;   // same limit as the API

  addresses = signal<Address[]>([]);
  formOpen = signal(false);
  editingId = signal<number | null>(null);   // null = adding a new one
  saving = signal(false);
  busyId = signal<number | null>(null);
  error = signal('');

  // same rules as AddressRequest
  form = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.maxLength(100)]],
    phone: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
    line1: ['', [Validators.required, Validators.maxLength(200)]],
    line2: ['', Validators.maxLength(200)],
    city: ['', [Validators.required, Validators.maxLength(80)]],
    state: ['', [Validators.required, Validators.maxLength(80)]],
    pincode: ['', [Validators.required, Validators.pattern(/^[1-9]\d{5}$/)]],
  });

  constructor() {
    this.load();
  }

  showError(name: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[name];
    return control.invalid && (control.dirty || control.touched);
  }

  openNew(): void {
    this.editingId.set(null);
    this.form.reset();
    this.formOpen.set(true);
  }

  openEdit(a: Address): void {
    this.editingId.set(a.id);
    this.form.setValue({
      fullName: a.fullName, phone: a.phone, line1: a.line1, line2: a.line2 ?? '',
      city: a.city, state: a.state, pincode: a.pincode,
    });
    this.formOpen.set(true);
  }

  cancel(): void {
    this.formOpen.set(false);
    this.editingId.set(null);
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const id = this.editingId();
    const call = id === null
      ? this.service.addAddress(this.form.getRawValue())
      : this.service.updateAddress(id, this.form.getRawValue());
    this.saving.set(true);
    this.error.set('');
    call.subscribe({
      next: () => {
        this.saving.set(false);
        this.cancel();
        this.load();
      },
      error: err => {
        this.error.set(err.error?.message ?? 'Could not save the address');
        this.saving.set(false);
      },
    });
  }

  makeDefault(a: Address): void {
    this.busyId.set(a.id);
    this.service.makeDefault(a.id).subscribe({
      next: () => this.load(),
      error: err => {
        this.error.set(err.error?.message ?? 'Could not change the default');
        this.busyId.set(null);
      },
    });
  }

  remove(a: Address): void {
    if (!confirm(`Delete the address in ${a.city}?`)) {
      return;
    }
    this.busyId.set(a.id);
    this.service.deleteAddress(a.id).subscribe({
      next: () => this.load(),
      error: err => {
        this.error.set(err.error?.message ?? 'Could not delete the address');
        this.busyId.set(null);
      },
    });
  }

  // reload after every change: the API decides which one is the default
  private load(): void {
    this.service.addresses().subscribe({
      next: list => {
        this.addresses.set(list);
        this.busyId.set(null);
      },
      error: err => this.error.set(err.error?.message ?? 'Could not load your addresses'),
    });
  }
}
