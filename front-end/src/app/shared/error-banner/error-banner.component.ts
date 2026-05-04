import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ErrorMessageService } from '../../core/errors/error-message.service';

@Component({
  selector: 'mnss-error-banner',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './error-banner.component.html',
  styleUrl: './error-banner.component.scss'
})
export class ErrorBannerComponent {
  private readonly errorMessageService = inject(ErrorMessageService);

  readonly message$ = this.errorMessageService.message$;

  clear(): void {
    this.errorMessageService.clear();
  }
}
