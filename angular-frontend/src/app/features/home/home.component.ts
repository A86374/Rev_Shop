import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  template: `
    <div class="container py-5 text-center" style="max-width: 640px;">
      <h1 class="display-5 fw-bold">RevShop</h1>
      <p class="lead text-muted mb-4">Buy from many sellers in one place. Pay online or cash on delivery.</p>

      <div class="d-flex flex-wrap justify-content-center gap-2">
        <a routerLink="/products" class="btn btn-primary btn-lg">Browse products</a>
        @if (auth.user(); as u) {
          <a [routerLink]="auth.homeFor(u.role)" class="btn btn-outline-secondary btn-lg">Go to my area</a>
        } @else {
          <a routerLink="/register" class="btn btn-outline-secondary btn-lg">Create an account</a>
        }
      </div>

      @if (!auth.user()) {
        <p class="small text-muted mt-4 mb-0">Want to sell? <a routerLink="/register/seller">Register as a seller</a></p>
      }
    </div>
  `,
})
export class HomeComponent {
  protected readonly auth = inject(AuthService);
}
