import type { ApplicationConfig } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { authTokenInterceptor } from './core/auth/auth-token.interceptor';
import { apiErrorInterceptor } from './core/errors/api-error.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [provideRouter(routes), provideHttpClient(withInterceptors([authTokenInterceptor, apiErrorInterceptor]))]
};
