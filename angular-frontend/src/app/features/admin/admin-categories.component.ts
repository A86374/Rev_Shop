import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminCategory, AdminCategoryService } from './admin-category.service';

@Component({
  selector: 'app-admin-categories',
  imports: [ReactiveFormsModule],
  templateUrl: './admin-categories.component.html',
})
export class AdminCategoriesComponent {
  private service = inject(AdminCategoryService);
  private fb = inject(FormBuilder);

  categories = signal<AdminCategory[]>([]);
  loading = signal(true);
  error = signal('');
  saving = signal(false);
  editingId = signal<number | null>(null);

  // same limits as your CategoryRequest
  addForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: ['', Validators.maxLength(500)],
  });

  editForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: ['', Validators.maxLength(500)],
  });

  constructor() {
    this.service.list().subscribe({
      next: (list) => {
        this.categories.set(list);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message ?? 'Could not load categories');
        this.loading.set(false);
      },
    });
  }

  add(): void {
    if (this.addForm.invalid) {
      this.addForm.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set('');
    this.service.add(this.addForm.getRawValue()).subscribe({
      next: (created) => {
        this.categories.update((list) => [...list, created]);
        this.addForm.reset();
        this.saving.set(false);
      },
      error: (err) => this.fail(err, 'Could not add the category'), // 409: already exists
    });
  }

  startEdit(category: AdminCategory): void {
    this.editingId.set(category.id);
    this.editForm.setValue({ name: category.name, description: category.description ?? '' });
  }

  saveEdit(category: AdminCategory): void {
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set('');
    this.service.update(category.id, this.editForm.getRawValue()).subscribe({
      next: (updated) => {
        this.categories.update((list) => list.map((c) => (c.id === updated.id ? updated : c)));
        this.editingId.set(null);
        this.saving.set(false);
      },
      error: (err) => this.fail(err, 'Could not save the category'),
    });
  }

  remove(category: AdminCategory): void {
    if (!confirm(`Delete "${category.name}"?`)) {
      return;
    }
    this.saving.set(true);
    this.error.set('');
    this.service.remove(category.id).subscribe({
      next: () => {
        this.categories.update((list) => list.filter((c) => c.id !== category.id));
        this.saving.set(false);
      },
      error: (err) => this.fail(err, 'Could not delete the category'), // 409: it still has products
    });
  }

  private fail(err: { error?: { message?: string } }, fallback: string): void {
    this.error.set(err.error?.message ?? fallback);
    this.saving.set(false);
  }
}
