import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { apiErrorInterceptor } from './api-error.interceptor';
import { ErrorMessageService } from './error-message.service';

describe('apiErrorInterceptor', () => {
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;
  let errorMessageService: ErrorMessageService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptors([apiErrorInterceptor])), provideHttpClientTesting()]
    });

    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
    errorMessageService = TestBed.inject(ErrorMessageService);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('converts standardized HTTP errors to displayable messages', () => {
    httpClient.get('/api/test').subscribe({
      next: () => fail('request should fail'),
      error: () => {
        expect(errorMessageService.currentMessage).toBe('Campos invalidos na requisicao.');
      }
    });

    const request = httpTestingController.expectOne('/api/test');
    request.flush(
      {
        code: 'VALIDATION_ERROR',
        message: 'Campos invalidos na requisicao.',
        status: 400,
        path: '/api/test',
        timestamp: '2026-05-03T12:00:00Z',
        validationErrors: [{ field: 'name', message: 'must not be blank' }]
      },
      {
        status: 400,
        statusText: 'Bad Request'
      }
    );
  });
});
