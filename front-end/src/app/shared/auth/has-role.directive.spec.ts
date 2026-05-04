import { Component } from '@angular/core';
import type { ComponentFixture } from '@angular/core/testing';
import { TestBed } from '@angular/core/testing';
import { BehaviorSubject } from 'rxjs';
import type { AuthUser, RoleName } from '../../core/auth/auth.models';
import { AuthService } from '../../core/auth/auth.service';
import { HasRoleDirective } from './has-role.directive';

@Component({
  standalone: true,
  imports: [HasRoleDirective],
  template: '<button type="button" *mnssHasRole="[\'GERENTE\']">Critico</button>'
})
class HostComponent {}

describe('HasRoleDirective', () => {
  let fixture: ComponentFixture<HostComponent>;
  let userSubject: BehaviorSubject<AuthUser | null>;

  beforeEach(async () => {
    userSubject = new BehaviorSubject<AuthUser | null>(null);

    await TestBed.configureTestingModule({
      imports: [HostComponent],
      providers: [
        {
          provide: AuthService,
          useValue: {
            currentUser$: userSubject.asObservable(),
            hasAnyRole: (roles: readonly RoleName[]) => {
              const user = userSubject.value;
              return !!user && (user.roles.includes('ADMIN') || roles.some((role) => user.roles.includes(role)));
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HostComponent);
  });

  it('hides critical action without required profile', () => {
    userSubject.next({
      id: '11111111-1111-1111-1111-111111111111',
      name: 'Caixa',
      username: 'caixa',
      active: true,
      roles: ['CAIXA']
    });

    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).not.toContain('Critico');
  });

  it('shows critical action for admin', () => {
    userSubject.next({
      id: '11111111-1111-1111-1111-111111111111',
      name: 'Admin',
      username: 'admin',
      active: true,
      roles: ['ADMIN']
    });

    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Critico');
  });
});
