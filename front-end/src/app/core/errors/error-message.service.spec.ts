import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ErrorMessageService } from './error-message.service';

describe('ErrorMessageService', () => {
  let service: ErrorMessageService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ErrorMessageService);
  });

  it('uses the standardized API error message when available', () => {
    const message = service.showHttpError(
      new HttpErrorResponse({
        status: 409,
        statusText: 'Conflict',
        url: '/api/catalog/categories',
        error: {
          code: 'CATALOG_DUPLICATE',
          message: 'Categoria ja cadastrada.',
          status: 409,
          path: '/api/catalog/categories',
          timestamp: '2026-05-03T12:00:00Z',
          validationErrors: []
        }
      })
    );

    expect(message).toBe('Categoria ja cadastrada.');
    expect(service.currentMessage).toBe('Categoria ja cadastrada.');
  });

  it('falls back to a connection message for network failures', () => {
    const message = service.showHttpError(new HttpErrorResponse({ status: 0 }));

    expect(message).toBe('Nao foi possivel conectar ao servidor.');
  });
});
