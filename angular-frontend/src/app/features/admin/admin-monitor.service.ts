import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order } from '../orders/order.service';

export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED';

// your AdminPaymentResponse
export interface AdminPayment {
  id: number;
  checkoutRef: string;
  buyerId: number;
  amount: number;
  method: 'ONLINE' | 'COD';
  status: PaymentStatus;
  createdAt: string;
  expiresAt: string | null;
}

// switch to the environment file once Kiran's base app has one
const API = 'http://localhost:8080/api/admin';

@Injectable({ providedIn: 'root' })
export class AdminMonitorService {
  private http = inject(HttpClient);

  orders(status?: string): Observable<Order[]> {
    return this.http.get<Order[]>(`${API}/orders`, { params: this.statusParam(status) });
  }

  payments(status?: string): Observable<AdminPayment[]> {
    return this.http.get<AdminPayment[]>(`${API}/payments`, { params: this.statusParam(status) });
  }

  private statusParam(status?: string): HttpParams {
    return status ? new HttpParams().set('status', status) : new HttpParams();
  }
}
