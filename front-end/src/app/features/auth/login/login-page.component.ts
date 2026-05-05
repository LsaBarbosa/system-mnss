import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { ErrorBannerComponent } from '../../../shared/error-banner/error-banner.component';

@Component({
  selector: 'mnss-login-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ErrorBannerComponent],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.scss'
})
export class LoginPageComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  errorMessage = '';
  submitting = false;

  readonly form = this.formBuilder.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  submit(): void {
    this.errorMessage = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.submitting = false;
        void this.router.navigateByUrl('/admin');
      },
      error: () => {
        this.submitting = false;
        this.errorMessage = 'Credenciais invalidas ou usuario inativo.';
      }
    });
  }
}
