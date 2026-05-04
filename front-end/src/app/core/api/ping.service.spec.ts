import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PingService } from './ping.service';

describe('PingService', () => {
  let service: PingService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(PingService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('calls /api/ping and maps success', () => {
    service.checkLocalApi().subscribe((status) => {
      expect(status.available).toBeTrue();
      if (status.available) {
        expect(status.response.message).toBe('pong');
        expect(status.response.environment).toBe('local');
      }
    });

    const request = httpTestingController.expectOne('/api/ping');
    expect(request.request.method).toBe('GET');
    request.flush({
      message: 'pong',
      application: 'mnss-local-api',
      environment: 'local',
      checkedAt: '2026-05-03T12:00:00Z'
    });
  });

  it('maps request errors to unavailable status', () => {
    service.checkLocalApi().subscribe((status) => {
      expect(status.available).toBeFalse();
      if (!status.available) {
        expect(status.errorMessage).toBe('API local indisponivel');
      }
    });

    const request = httpTestingController.expectOne('/api/ping');
    request.flush('unavailable', {
      status: 503,
      statusText: 'Service Unavailable'
    });
  });
});
