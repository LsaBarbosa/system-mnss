import type { ComponentFixture } from '@angular/core/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { LoginPageComponent } from './login-page.component';

describe('LoginPageComponent', () => {
  let fixture: ComponentFixture<LoginPageComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['login']);
    router = jasmine.createSpyObj<Router>('Router', ['navigateByUrl']);

    await TestBed.configureTestingModule({
      imports: [LoginPageComponent],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginPageComponent);
  });

  it('blocks submit while form is invalid', () => {
    fixture.componentInstance.submit();

    expect(authService.login).not.toHaveBeenCalled();
  });

  it('navigates after successful login', () => {
    authService.login.and.returnValue(
      of({
        token: 'token',
        expiresAt: '2026-05-04T12:00:00Z',
        user: {
          id: '11111111-1111-1111-1111-111111111111',
          name: 'Admin',
          username: 'admin',
          active: true,
          roles: ['ADMIN']
        }
      })
    );
    fixture.componentInstance.form.setValue({ username: 'admin', password: 'secret' });

    fixture.componentInstance.submit();

    expect(authService.login).toHaveBeenCalledWith({ username: 'admin', password: 'secret' });
    expect(router.navigateByUrl).toHaveBeenCalledWith('/admin');
  });

  it('shows message when login fails', () => {
    authService.login.and.returnValue(throwError(() => new Error('invalid')));
    fixture.componentInstance.form.setValue({ username: 'admin', password: 'wrong' });

    fixture.componentInstance.submit();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Credenciais invalidas ou usuario inativo.');
  });
});
