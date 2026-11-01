import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { LoginComponent } from './features/auth/login.component';
import { RegisterComponent } from './features/auth/register.component';
import { ProfileComponent } from './features/profile/profile.component';
import { authGuard, roleGuard } from './core/auth.guard';
import { PaymentDemoComponent } from './features/payment/payment-demo.component';

import { MyOrdersComponent } from './features/orders/my-orders.component';
import { OrderDetailsComponent } from './features/orders/order-details.component';

// add this import at the top of Kiran's app.routes.ts
import { SellerRegisterComponent } from './features/seller/seller-register.component';
// add these imports at the top of app.routes.ts
import { SellerOrdersComponent } from './features/seller/seller-orders.component';
import { SellerDashboardComponent } from './features/seller/seller-dashboard.component';

// add these imports at the top of Kiran's app.routes.ts
import { MyProductsComponent } from './features/seller/my-products.component';
import { ProductFormComponent } from './features/seller/product-form.component';

// add this import at the top of app.routes.ts
import { AdminCategoriesComponent } from './features/admin/admin-categories.component';
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
  // add this entry inside the routes array
  { path: 'register/seller', component: SellerRegisterComponent },

  { path: 'seller/products', component: MyProductsComponent },
  { path: 'seller/products/new', component: ProductFormComponent },
  { path: 'seller/products/:id/edit', component: ProductFormComponent },

  // add this entry under "---- Hasitha: seller pages ----"
  {
    path: 'admin/categories',
    component: AdminCategoriesComponent,
    canActivate: [roleGuard('ADMIN')],
  },

  // add these entries under "---- Hasitha: seller pages ----"
  { path: 'seller/orders', component: SellerOrdersComponent, canActivate: [roleGuard('SELLER')] },
  {
    path: 'seller/dashboard',
    component: SellerDashboardComponent,
    canActivate: [roleGuard('SELLER')],
  },

  { path: '**', redirectTo: '' },
];
