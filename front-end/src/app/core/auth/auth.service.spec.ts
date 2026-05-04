import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { AuthTokenStorage } from './auth-token.storage';

describe('AuthService', () => {
  let service: AuthService;
  let httpTestingController: HttpTestingController;
  let storedToken: string | null;

  beforeEach(() => {
    storedToken = null;
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: AuthTokenStorage,
          useValue: {
            getToken: () => storedToken,
            setToken: (token: string) => {
              storedToken = token;
            },
            clear: () => {
              storedToken = null;
            }
          }
        }
      ]
    });
    service = TestBed.inject(AuthService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('stores token and authenticated user after login', () => {
    service.login({ username: 'admin', password: 'secret' }).subscribe((response) => {
      expect(response.token).toBe('token');
      expect(service.currentUser?.username).toBe('admin');
    });

    const request = httpTestingController.expectOne('/api/auth/login');
    expect(request.request.method).toBe('POST');
    request.flush({
      token: 'token',
      expiresAt: '2026-05-04T12:00:00Z',
      user: {
        id: '11111111-1111-1111-1111-111111111111',
        name: 'Admin',
        username: 'admin',
        active: true,
        roles: ['ADMIN']
      }
    });

    expect(storedToken).toBe('token');
  });
});
