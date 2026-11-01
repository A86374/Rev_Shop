import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { LoginComponent } from './features/auth/login.component';
import { RegisterComponent } from './features/auth/register.component';
import { ProfileComponent } from './features/profile/profile.component';
import { authGuard, roleGuard } from './core/auth.guard';

import { MyOrdersComponent } from './features/orders/my-orders.component';
import { OrderDetailsComponent } from './features/orders/order-details.component';
import { AdminOrdersComponent } from './features/admin/admin-orders.component';
import { AdminPaymentsComponent } from './features/admin/admin-payments.component';
import { AdminUsersComponent } from './features/admin/admin-users.component';

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

import { CatalogComponent } from './features/catalog/catalog.component';
import { CartComponent } from './features/cart/cart.component';
import { CheckoutComponent } from './features/checkout/checkout.component';
import { ProductDetailsComponent } from './features/catalog/product-details.component';
import { WishlistComponent } from './features/wishlist/wishlist.component';
// Everyone adds their routes under their own name, so pulls don't clash.
// Keep the '**' line at the very bottom.
export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },

  // ---- Nikitha: catalogue, cart, checkout ----
  { path: 'products', component: CatalogComponent },
  { path: 'cart', component: CartComponent, canActivate: [roleGuard('BUYER')] },
  { path: 'checkout', component: CheckoutComponent, canActivate: [roleGuard('BUYER')] },
  { path: 'products/:id', component: ProductDetailsComponent },
  { path: 'wishlist', component: WishlistComponent, canActivate: [roleGuard('BUYER')] },

  // ---- Abhinaya: orders, admin users, payment ----
  { path: 'orders', component: MyOrdersComponent, canActivate: [roleGuard('BUYER')] },
  { path: 'orders/:id', component: OrderDetailsComponent, canActivate: [roleGuard('BUYER')] },
  { path: 'admin/users', component: AdminUsersComponent, canActivate: [roleGuard('ADMIN')] },
  { path: 'admin/orders', component: AdminOrdersComponent, canActivate: [roleGuard('ADMIN')] },
  { path: 'admin/payments', component: AdminPaymentsComponent, canActivate: [roleGuard('ADMIN')] },
  // ---- Hasitha: seller pages ----
  // add this entry inside the routes array
  { path: 'register/seller', component: SellerRegisterComponent },

  { path: 'seller/products', component: MyProductsComponent, canActivate: [roleGuard('SELLER')] },
  { path: 'seller/products/new', component: ProductFormComponent, canActivate: [roleGuard('SELLER')] },
  { path: 'seller/products/:id/edit', component: ProductFormComponent, canActivate: [roleGuard('SELLER')] },

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
