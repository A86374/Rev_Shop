import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Review {
  id: number;
  productId: number;
  buyerId: number;
  rating: number;
  comment: string | null;
  createdAt: string;
}

// your ReviewResponse.ProductReviews
export interface ProductReviews {
  productId: number;
  averageRating: number;
  count: number;
  reviews: Review[];
}

// switch to the environment file once Kiran's base app has one
const API = 'http://localhost:8080/api/reviews';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private http = inject(HttpClient);

  forProduct(productId: number): Observable<ProductReviews> {
    return this.http.get<ProductReviews>(`${API}/product/${productId}`);
  }

  add(productId: number, rating: number, comment: string): Observable<Review> {
    return this.http.post<Review>(API, { productId, rating, comment });
  }
}
