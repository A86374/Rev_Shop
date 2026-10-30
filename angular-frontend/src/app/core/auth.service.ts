import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

export type Role = 'BUYER' | 'SELLER' | 'ADMIN';

// the logged-in user; every page reads it from auth.user()
export interface AuthUser {
  userId: number;
  fullName: string;
  email: string;
  role: Role;
}

// what /api/auth/login and /api/auth/register return
export interface AuthResponse extends AuthUser {
  token: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  phone: string;
  password: string;
}

// switch to an environment file before deploying
const API = 'http://localhost:8080/api/auth';
const TOKEN_KEY = 'revshop_token';
const USER_KEY = 'revshop_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  // restored from the browser on refresh, so a page reload keeps you logged in
  readonly user = signal<AuthUser | null>(this.restore());
  readonly isLoggedIn = computed(() => this.user() !== null);

  get token(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY);
    } catch {
      return null;
    }
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API}/login`, { email, password }).pipe(
      tap(res => this.save(res)),
    );
  }

  // buyers only; sellers use Hasitha's /register/seller page
  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API}/register`, { ...request, role: 'BUYER' }).pipe(
      tap(res => this.save(res)),
    );
  }

  logout(): void {
    this.clearStorage();
    this.user.set(null);
    this.router.navigate(['/login']);
  }

  // where each role lands after login
  homeFor(role: Role): string {
    switch (role) {
      case 'SELLER':
        return '/seller/products';
      case 'ADMIN':
        return '/admin/users';
      default:
        return '/products';
    }
  }

  private save(res: AuthResponse): void {
    const user: AuthUser = { userId: res.userId, fullName: res.fullName, email: res.email, role: res.role };
    try {
      localStorage.setItem(TOKEN_KEY, res.token);
      localStorage.setItem(USER_KEY, JSON.stringify(user));
    } catch {
      // private window: you stay logged in until the tab closes
    }
    this.user.set(user);
  }

  private restore(): AuthUser | null {
    try {
      const token = localStorage.getItem(TOKEN_KEY);
      const raw = localStorage.getItem(USER_KEY);
      if (!token || !raw || this.isExpired(token)) {
        this.clearStorage();
        return null;
      }
      return JSON.parse(raw) as AuthUser;
    } catch {
      return null;
    }
  }

  // reads "exp" from the JWT; an expired token logs you out on the next refresh
  private isExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
      return typeof payload.exp === 'number' && payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }

  private clearStorage(): void {
    try {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    } catch {
      // nothing stored
    }
  }
}
