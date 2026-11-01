import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AuthService, Role } from '../../core/auth.service';
import { AdminUser, AdminUserService } from './admin-user.service';

// US-24: admins see every user and block or unblock buyers and sellers
@Component({
  selector: 'app-admin-users',
  imports: [DatePipe],
  templateUrl: './admin-users.component.html',
})
export class AdminUsersComponent {
  private service = inject(AdminUserService);
  private auth = inject(AuthService);

  readonly tabs: { value: Role | ''; text: string }[] = [
    { value: '', text: 'All' },
    { value: 'BUYER', text: 'Buyers' },
    { value: 'SELLER', text: 'Sellers' },
    { value: 'ADMIN', text: 'Admins' },
  ];

  users = signal<AdminUser[]>([]);
  role = signal<Role | ''>('');
  loading = signal(true);
  error = signal('');
  busyId = signal<number | null>(null);

  blockedCount = computed(() => this.users().filter((u) => !u.active).length);

  constructor() {
    this.load();
  }

  show(role: Role | ''): void {
    this.role.set(role);
    this.load();
  }

  // admins and your own account can't be blocked (the backend checks this too)
  canToggle(user: AdminUser): boolean {
    return user.role !== 'ADMIN' && user.id !== this.auth.user()?.userId;
  }

  toggle(user: AdminUser): void {
    const action = user.active ? 'Block' : 'Unblock';
    if (!confirm(`${action} ${user.fullName}?`)) {
      return;
    }
    this.busyId.set(user.id);
    this.error.set('');
    this.service.setActive(user.id, !user.active).subscribe({
      next: (updated) => {
        this.users.update((list) => list.map((u) => (u.id === updated.id ? updated : u)));
        this.busyId.set(null);
      },
      error: (err) => {
        this.error.set(err.error?.message ?? `Could not ${action.toLowerCase()} this user`);
        this.busyId.set(null);
      },
    });
  }

  private load(): void {
    this.loading.set(true);
    this.error.set('');
    this.service.list(this.role()).subscribe({
      next: (list) => {
        this.users.set(list);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message ?? 'Could not load users');
        this.loading.set(false);
      },
    });
  }
}
