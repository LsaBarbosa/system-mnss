import type { ComponentFixture } from '@angular/core/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { BehaviorSubject, of } from 'rxjs';
import { HealthService } from '../../../core/api/health.service';
import type { AuthUser, RoleName } from '../../../core/auth/auth.models';
import { AuthService } from '../../../core/auth/auth.service';
import { AdminConsolePageComponent } from './admin-console-page.component';

describe('AdminConsolePageComponent', () => {
  let fixture: ComponentFixture<AdminConsolePageComponent>;
  let userSubject: BehaviorSubject<AuthUser | null>;

  beforeEach(async () => {
    userSubject = new BehaviorSubject<AuthUser | null>({
      id: '11111111-1111-1111-1111-111111111111',
      name: 'Admin',
      username: 'admin',
      active: true,
      roles: ['ADMIN']
    });

    await TestBed.configureTestingModule({
      imports: [AdminConsolePageComponent],
      providers: [
        provideRouter([]),
        {
          provide: HealthService,
          useValue: {
            getLocalHealth: () =>
              of({
                status: 'UP',
                environment: 'local',
                offlineCriticalOperation: true,
                message: 'ready',
                checkedAt: '2026-05-03T12:00:00Z',
                components: {
                  db: 'UP',
                  redis: 'UP',
                  rabbit: 'UP'
                }
              })
          }
        },
        {
          provide: AuthService,
          useValue: {
            currentUser$: userSubject.asObservable(),
            loadMe: () => of(userSubject.value),
            hasAnyRole: (roles: readonly RoleName[]) => {
              const user = userSubject.value;
              return !!user && (user.roles.includes('ADMIN') || roles.some((role) => user.roles.includes(role)));
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminConsolePageComponent);
  });

  it('renders protected operation console', () => {
    fixture.detectChanges();

    const nativeElement = fixture.nativeElement as HTMLElement;
    expect(nativeElement.textContent).toContain('Console inicial');
    expect(nativeElement.textContent).toContain('Online');
    expect(nativeElement.textContent).toContain('redis');
  });
});
