import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export type SellerOrderStatus =
  'PAYMENT_PENDING' | 'PLACED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';

// the same JSON as Abhinaya's OrderResponse
export interface SellerOrder {
  id: number;
  checkoutRef: string;
  buyerId: number;
  status: SellerOrderStatus;
  paymentMethod: 'ONLINE' | 'COD';
  totalAmount: number;
  shippingAddress: string;
  createdAt: string;
  items: {
    productId: number;
    productName: string;
    price: number;
    quantity: number;
    lineTotal: number;
  }[];
}

export interface TopProduct {
  productId: number;
  productName: string;
  unitsSold: number;
  revenue: number;
}

export interface SellerDashboard {
  toShip: number;
  shipped: number;
  delivered: number;
  cancelled: number;
  awaitingPayment: number;
  revenue: number;
  pendingRevenue: number;
  itemsSold: number;
  topProducts: TopProduct[];
  recentOrders: SellerOrder[];
}

export const ORDER_LABEL: Record<SellerOrderStatus, string> = {
  PAYMENT_PENDING: 'Awaiting payment',
  PLACED: 'To ship',
  SHIPPED: 'Shipped',
  DELIVERED: 'Delivered',
  CANCELLED: 'Cancelled',
};

export const ORDER_BADGE: Record<SellerOrderStatus, string> = {
  PAYMENT_PENDING: 'text-bg-warning',
  PLACED: 'text-bg-primary',
  SHIPPED: 'text-bg-info',
  DELIVERED: 'text-bg-success',
  CANCELLED: 'text-bg-secondary',
};

// switch to the environment file once Kiran's base app has one
const API = 'http://localhost:8080/api/seller';

@Injectable({ providedIn: 'root' })
export class SellerOrderService {
  private http = inject(HttpClient);

  orders(status?: string): Observable<SellerOrder[]> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<SellerOrder[]>(`${API}/orders`, { params });
  }

  updateStatus(id: number, status: 'SHIPPED' | 'DELIVERED'): Observable<SellerOrder> {
    return this.http.patch<SellerOrder>(`${API}/orders/${id}/status`, { status });
  }

  dashboard(): Observable<SellerDashboard> {
    return this.http.get<SellerDashboard>(`${API}/dashboard`);
  }
}
