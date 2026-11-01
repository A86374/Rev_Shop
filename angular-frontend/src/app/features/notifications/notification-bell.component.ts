import { Component, DestroyRef, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AppNotification, NotificationService } from './notification.service';

@Component({
  selector: 'app-notification-bell',
  imports: [DatePipe],
  templateUrl: './notification-bell.component.html',
})
export class NotificationBellComponent {
  private service = inject(NotificationService);

  unread = signal(0);
  open = signal(false);
  loading = signal(false);
  items = signal<AppNotification[]>([]);

  constructor() {
    this.refreshCount();
    const timer = setInterval(() => this.refreshCount(), 30_000); // check every 30 seconds
    inject(DestroyRef).onDestroy(() => clearInterval(timer));
  }

  refreshCount(): void {
    this.service.unreadCount().subscribe({
      next: (res) => this.unread.set(res.unread),
      error: () => this.unread.set(0),
    });
  }

  toggle(): void {
    this.open.update((isOpen) => !isOpen);
    if (!this.open()) {
      return;
    }
    this.loading.set(true);
    this.service.list().subscribe({
      next: (list) => {
        this.items.set(list.slice(0, 10)); // newest 10
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  markRead(n: AppNotification): void {
    if (n.read) {
      return;
    }
    this.service.markRead(n.id).subscribe(() => {
      this.items.update((list) => list.map((x) => (x.id === n.id ? { ...x, read: true } : x)));
      this.unread.update((count) => Math.max(count - 1, 0));
    });
  }

  markAllRead(): void {
    this.service.markAllRead().subscribe(() => {
      this.items.update((list) => list.map((x) => ({ ...x, read: true })));
      this.unread.set(0);
    });
  }
}
