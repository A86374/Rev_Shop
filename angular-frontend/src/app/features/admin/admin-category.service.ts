import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AdminCategory {
  id: number;
  name: string;
  description: string | null;
}

export interface CategoryPayload {
  name: string;
  description: string;
}

// switch to the environment file once Kiran's base app has one
const API = 'http://localhost:8080/api/categories';

// your CategoryController: GET is open to everyone, the rest is ADMIN only (gateway)
@Injectable({ providedIn: 'root' })
export class AdminCategoryService {
  private http = inject(HttpClient);

  list(): Observable<AdminCategory[]> {
    return this.http.get<AdminCategory[]>(API);
  }

  add(category: CategoryPayload): Observable<AdminCategory> {
    return this.http.post<AdminCategory>(API, category);
  }

  update(id: number, category: CategoryPayload): Observable<AdminCategory> {
    return this.http.put<AdminCategory>(`${API}/${id}`, category);
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/${id}`);
  }
}
