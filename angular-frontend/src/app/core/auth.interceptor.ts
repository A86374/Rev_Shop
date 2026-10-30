import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject, isDevMode } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

const GATEWAY = 'http://localhost:8080/';

// Every service file calls 'http://localhost:8080/api/...'. That works with ng serve on a laptop.
// The deployed app is served by Nginx, which forwards /api to the gateway from the same address,
// so in a production build the URL becomes '/api/...'. Nobody has to change their service files.
function forDeployment(url: string): string {
  return !isDevMode() && url.startsWith(GATEWAY) ? url.substring(GATEWAY.length - 1) : url;
}

// Adds "Authorization: Bearer <token>" to every call to our gateway.
// A 401 means the token is missing or expired, so the user is sent to the login page.
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token;
  const toGateway = req.url.startsWith(GATEWAY);

  let request = toGateway ? req.clone({ url: forDeployment(req.url) }) : req;
  if (token && toGateway) {
    request = request.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

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
