import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type OrderStatus = 'PAYMENT_PENDING' | 'PLACED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';

export interface OrderItem {
  productId: number;
  productName: string;
  price: number;
  quantity: number;
  lineTotal: number;
}

export interface Order {
  id: number;
  checkoutRef: string;
  buyerId: number;
  sellerId: number;
  status: OrderStatus;
  paymentMethod: 'ONLINE' | 'COD';
  totalAmount: number;
  shippingAddress: string;
  refunded: boolean;
  createdAt: string;
  items: OrderItem[];
}

// shared by both order pages
export const STATUS_LABEL: Record<OrderStatus, string> = {
  PAYMENT_PENDING: 'Payment pending',
  PLACED: 'Placed',
  SHIPPED: 'Shipped',
  DELIVERED: 'Delivered',
  CANCELLED: 'Cancelled',
};

export const STATUS_BADGE: Record<OrderStatus, string> = {
  PAYMENT_PENDING: 'text-bg-warning',
  PLACED: 'text-bg-primary',
  SHIPPED: 'text-bg-info',
  DELIVERED: 'text-bg-success',
  CANCELLED: 'text-bg-secondary',
};

// switch to the environment file once Kiran's base app has one
const API = 'http://localhost:8080/api/orders';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private http = inject(HttpClient);

  myOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(API);
  }

  get(id: number): Observable<Order> {
    return this.http.get<Order>(`${API}/${id}`);
  }
}
