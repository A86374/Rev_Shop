import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CheckoutRequest {
  paymentMethod: 'ONLINE' | 'COD';
  shippingAddress: string;
}

export interface CheckoutResult {
  checkoutRef: string;
  paymentId: number;
  paymentMethod: 'ONLINE' | 'COD';
  totalAmount: number;
  paymentExpiresAt: string | null;
  orders: { id: number; sellerId: number; status: string; totalAmount: number }[];
}

// switch to the environment file once Kiran's base app has one
const API = 'http://localhost:8080/api/orders/checkout';

@Injectable({ providedIn: 'root' })
export class CheckoutService {
  private http = inject(HttpClient);

  checkout(request: CheckoutRequest): Observable<CheckoutResult> {
    return this.http.post<CheckoutResult>(API, request);
  }
}
