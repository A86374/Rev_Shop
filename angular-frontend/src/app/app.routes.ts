import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { LoginComponent } from './features/auth/login.component';
import { RegisterComponent } from './features/auth/register.component';
import { ProfileComponent } from './features/profile/profile.component';
import { authGuard, roleGuard } from './core/auth.guard';
import { PaymentDemoComponent } from './features/payment/payment-demo.component';

import { MyOrdersComponent } from './features/orders/my-orders.component';
import { OrderDetailsComponent } from './features/orders/order-details.component';

// Everyone adds their routes under their own name, so pulls don't clash.
// Keep the '**' line at the very bottom.
export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },

  // ---- Nikitha: catalogue, cart, checkout ----

  // ---- Abhinaya: orders, admin users, payment ----
  { path: 'orders', component: MyOrdersComponent },
  { path: 'orders/:id', component: OrderDetailsComponent },
  { path: 'payment-demo', component: PaymentDemoComponent },
  // ---- Hasitha: seller pages ----

  { path: '**', redirectTo: '' },
];
