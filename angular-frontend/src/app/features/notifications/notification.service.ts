import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AppNotification {
  id: number;
  type: string;
  message: string;
  read: boolean;
  createdAt: string;
}

// switch to the environment file once Kiran's base app has one
const API = 'http://localhost:8080/api/notifications';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private http = inject(HttpClient);

  list(): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(API);
  }

  unreadCount(): Observable<{ unread: number }> {
    return this.http.get<{ unread: number }>(`${API}/unread-count`);
  }

  markRead(id: number): Observable<AppNotification> {
    return this.http.patch<AppNotification>(`${API}/${id}/read`, {});
  }

  markAllRead(): Observable<{ unread: number }> {
    return this.http.patch<{ unread: number }>(`${API}/read-all`, {});
  }
}
