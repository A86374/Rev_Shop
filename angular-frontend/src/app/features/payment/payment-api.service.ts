import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type PaymentResult = 'SUCCESS' | 'FAILED';

export interface PaymentResponse {
  id: number;
  checkoutRef: string;
  buyerId: number;
  amount: number;
  method: 'ONLINE' | 'COD';
  status: 'PENDING' | 'SUCCESS' | 'FAILED';
  expiresAt: string | null;
}

// switch to the environment file once Kiran's base app has one
const API = 'http://localhost:8080/api/payments';

@Injectable({ providedIn: 'root' })
export class PaymentApiService {
  private http = inject(HttpClient);

  confirm(paymentId: number, action: 'PAY' | 'CANCEL'): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${API}/${paymentId}/confirm`, { action });
  }
}
