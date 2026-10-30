import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Role } from '../../core/auth.service';

export interface Profile {
  id: number;
  fullName: string;
  email: string;
  phone: string;
  role: Role;
  businessName: string | null;
  createdAt: string;
}

export interface Address {
  id: number;
  fullName: string;
  phone: string;
  line1: string;
  line2: string | null;
  city: string;
  state: string;
  pincode: string;
  defaultAddress: boolean;
  text: string;          // the whole address on one line
}

export interface AddressPayload {
  fullName: string;
  phone: string;
  line1: string;
  line2: string;
  city: string;
  state: string;
  pincode: string;
}

// switch to an environment file before deploying
const API = 'http://localhost:8080/api/users/me';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private http = inject(HttpClient);

  get(): Observable<Profile> {
    return this.http.get<Profile>(API);
  }

  update(changes: { fullName: string; phone: string; businessName: string }): Observable<Profile> {
    return this.http.put<Profile>(API, changes);
  }

  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    return this.http.patch<void>(`${API}/password`, { currentPassword, newPassword });
  }

  addresses(): Observable<Address[]> {
    return this.http.get<Address[]>(`${API}/addresses`);
  }

  addAddress(address: AddressPayload): Observable<Address> {
    return this.http.post<Address>(`${API}/addresses`, address);
  }

  updateAddress(id: number, address: AddressPayload): Observable<Address> {
    return this.http.put<Address>(`${API}/addresses/${id}`, address);
  }

  makeDefault(id: number): Observable<Address> {
    return this.http.patch<Address>(`${API}/addresses/${id}/default`, {});
  }

  deleteAddress(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/addresses/${id}`);
  }
}
