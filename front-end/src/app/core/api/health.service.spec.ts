import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HealthService } from './health.service';

describe('HealthService', () => {
  let service: HealthService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(HealthService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('requests local health endpoint', () => {
    service.getLocalHealth().subscribe((response) => {
      expect(response.status).toBe('UP');
      expect(response.environment).toBe('local');
    });

    const request = httpTestingController.expectOne('/api/health');
    expect(request.request.method).toBe('GET');
    request.flush({
      status: 'UP',
      environment: 'local',
      offlineCriticalOperation: true,
      message: 'ready',
      checkedAt: '2026-05-03T12:00:00Z'
    });
  });
});
