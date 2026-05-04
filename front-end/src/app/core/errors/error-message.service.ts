import type { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import type { ApiError } from './api-error';

@Injectable({ providedIn: 'root' })
export class ErrorMessageService {
  private readonly messageSubject = new BehaviorSubject<string | null>(null);

  readonly message$ = this.messageSubject.asObservable();

  get currentMessage(): string | null {
    return this.messageSubject.value;
  }

  showMessage(message: string): void {
    this.messageSubject.next(message);
  }

  showHttpError(error: HttpErrorResponse): string {
    const message = this.toDisplayMessage(error);
    this.showMessage(message);
    return message;
  }

  clear(): void {
    this.messageSubject.next(null);
  }

  private toDisplayMessage(error: HttpErrorResponse): string {
    if (this.isApiError(error.error)) {
      return error.error.message;
    }

    if (error.status === 0) {
      return 'Nao foi possivel conectar ao servidor.';
    }

    return 'Erro inesperado ao processar a requisicao.';
  }

  private isApiError(value: unknown): value is ApiError {
    if (!value || typeof value !== 'object') {
      return false;
    }

    const candidate = value as Partial<ApiError>;
    return (
      typeof candidate.code === 'string' &&
      typeof candidate.message === 'string' &&
      typeof candidate.status === 'number' &&
      typeof candidate.path === 'string' &&
      typeof candidate.timestamp === 'string' &&
      Array.isArray(candidate.validationErrors)
    );
  }
}
