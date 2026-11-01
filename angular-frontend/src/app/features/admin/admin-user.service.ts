import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Role } from '../../core/auth.service';

// user-service AdminUserResponse (never includes the password)
export interface AdminUser {
  id: number;
  fullName: string;
  email: string;
  phone: string;
  role: Role;
  businessName: string | null;
  active: boolean;
  createdAt: string;
}

const API = 'http://localhost:8080/api/admin/users';

@Injectable({ providedIn: 'root' })
export class AdminUserService {
  private http = inject(HttpClient);

  // empty role = everyone
  list(role?: Role | ''): Observable<AdminUser[]> {
    const params = role ? new HttpParams().set('role', role) : new HttpParams();
    return this.http.get<AdminUser[]>(API, { params });
  }

  // active false blocks the user, true unblocks
  setActive(id: number, active: boolean): Observable<AdminUser> {
    return this.http.patch<AdminUser>(`${API}/${id}/status`, { active });
  }
}
