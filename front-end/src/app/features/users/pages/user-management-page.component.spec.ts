import type { ComponentFixture } from '@angular/core/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { UserAdminService } from '../data-access/user-admin.service';
import { UserManagementPageComponent } from './user-management-page.component';

describe('UserManagementPageComponent', () => {
  let fixture: ComponentFixture<UserManagementPageComponent>;
  let userAdminService: jasmine.SpyObj<UserAdminService>;

  beforeEach(async () => {
    userAdminService = jasmine.createSpyObj<UserAdminService>('UserAdminService', [
      'listUsers',
      'listRoles',
      'createUser'
    ]);
    userAdminService.listUsers.and.returnValue(
      of([
        {
          id: '11111111-1111-1111-1111-111111111111',
          name: 'Caixa',
          username: 'caixa',
          active: true,
          roles: ['CAIXA']
        }
      ])
    );
    userAdminService.listRoles.and.returnValue(
      of([
        { id: '22222222-2222-2222-2222-222222222222', name: 'ADMIN' },
        { id: '33333333-3333-3333-3333-333333333333', name: 'CAIXA' }
      ])
    );

    await TestBed.configureTestingModule({
      imports: [UserManagementPageComponent],
      providers: [provideRouter([]), { provide: UserAdminService, useValue: userAdminService }]
    }).compileComponents();

    fixture = TestBed.createComponent(UserManagementPageComponent);
  });

  it('requires mandatory fields before creating user', () => {
    fixture.componentInstance.submit();

    expect(userAdminService.createUser).not.toHaveBeenCalled();
  });

  it('renders users returned by API', () => {
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Caixa');
    expect(fixture.nativeElement.textContent).toContain('CAIXA');
  });

  it('renders mocked role options returned by API', () => {
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('ADMIN');
    expect(fixture.nativeElement.textContent).toContain('CAIXA');
  });

  it('keeps multiple role selection state', () => {
    fixture.componentInstance.toggleRole('ADMIN', true);
    fixture.componentInstance.toggleRole('CAIXA', true);
    fixture.componentInstance.toggleRole('ADMIN', false);

    expect(fixture.componentInstance.form.controls.roles.value).toEqual(['CAIXA']);
  });
});
