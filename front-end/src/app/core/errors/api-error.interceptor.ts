import type { HttpInterceptorFn } from '@angular/common/http';
import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { ErrorMessageService } from './error-message.service';

export const apiErrorInterceptor: HttpInterceptorFn = (request, next) => {
  const errorMessageService = inject(ErrorMessageService);
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(request).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        if (error.status === 401) {
          authService.logout();
          if (!router.url.includes('/login')) {
            router.navigate(['/login']);
          }
          errorMessageService.showMessage('Sessão expirada. Faça login novamente.');
        } else if (error.status === 403) {
          errorMessageService.showMessage('Permissão insuficiente para esta ação.');
        } else {
          errorMessageService.showHttpError(error);
        }
      }

      return throwError(() => error);
    })
  );
};
