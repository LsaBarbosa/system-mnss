import { CommonModule } from '@angular/common';
import type { OnInit } from '@angular/core';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import type { AuthUser, RoleName, RoleOption } from '../../../core/auth/auth.models';
import { ErrorBannerComponent } from '../../../shared/error-banner/error-banner.component';
import { UserAdminService } from '../data-access/user-admin.service';

@Component({
  selector: 'mnss-user-management-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ErrorBannerComponent],
  templateUrl: './user-management-page.component.html',
  styleUrl: './user-management-page.component.scss'
})
export class UserManagementPageComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly userAdminService = inject(UserAdminService);

  users: AuthUser[] = [];
  roles: RoleOption[] = [];
  saving = false;

  readonly form = this.formBuilder.nonNullable.group({
    name: ['', Validators.required],
    email: [''],
    username: ['', Validators.required],
    password: ['', Validators.required],
    active: [true],
    roles: [[] as RoleName[], Validators.required]
  });

  ngOnInit(): void {
    this.loadUsers();
    this.userAdminService.listRoles().subscribe((roles) => {
      this.roles = roles;
    });
  }

  submit(): void {
    if (this.form.invalid || this.form.controls.roles.value.length === 0) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.userAdminService.createUser(this.form.getRawValue()).subscribe({
      next: (user) => {
        this.users = [...this.users, user].sort((left, right) => left.username.localeCompare(right.username));
        this.saving = false;
        this.form.reset({
          name: '',
          email: '',
          username: '',
          password: '',
          active: true,
          roles: []
        });
      },
      error: () => {
        this.saving = false;
      }
    });
  }

  toggleRole(role: RoleName, checked: boolean): void {
    const roles = new Set(this.form.controls.roles.value);
    if (checked) {
      roles.add(role);
    } else {
      roles.delete(role);
    }
    this.form.controls.roles.setValue([...roles]);
    this.form.controls.roles.markAsTouched();
  }

  hasRole(role: RoleName): boolean {
    return this.form.controls.roles.value.includes(role);
  }

  private loadUsers(): void {
    this.userAdminService.listUsers().subscribe((users) => {
      this.users = users;
    });
  }
}
