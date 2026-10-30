import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

const GATEWAY = 'http://localhost:8080/';

// Adds "Authorization: Bearer <token>" to every call to our gateway.
// A 401 means the token is missing or expired, so the user is sent to the login page.
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token;
  const toGateway = req.url.startsWith(GATEWAY);

  const request = token && toGateway
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(request).pipe(
    catchError((err: HttpErrorResponse) => {
      // a wrong password on the login page is also a 401: that one must stay on the page
      if (err.status === 401 && toGateway && !req.url.includes('/api/auth/')) {
        auth.logout();
      }
      return throwError(() => err);
    }),
  );
};
