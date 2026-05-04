import type { HttpInterceptorFn } from '@angular/common/http';
import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ErrorMessageService } from './error-message.service';

export const apiErrorInterceptor: HttpInterceptorFn = (request, next) => {
  const errorMessageService = inject(ErrorMessageService);

  return next(request).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        errorMessageService.showHttpError(error);
      }

      return throwError(() => error);
    })
  );
};
