import type { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthTokenStorage } from './auth-token.storage';

export const authTokenInterceptor: HttpInterceptorFn = (request, next) => {
  const token = inject(AuthTokenStorage).getToken();

  let cloned = request.clone({ withCredentials: true });
  if (token) {
    cloned = cloned.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(cloned);
};
