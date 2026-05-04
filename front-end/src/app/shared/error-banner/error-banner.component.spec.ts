import type { ComponentFixture } from '@angular/core/testing';
import { TestBed } from '@angular/core/testing';
import { ErrorMessageService } from '../../core/errors/error-message.service';
import { ErrorBannerComponent } from './error-banner.component';

describe('ErrorBannerComponent', () => {
  let fixture: ComponentFixture<ErrorBannerComponent>;
  let errorMessageService: ErrorMessageService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ErrorBannerComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(ErrorBannerComponent);
    errorMessageService = TestBed.inject(ErrorMessageService);
  });

  it('renders and clears the current error message', () => {
    errorMessageService.showMessage('Erro padronizado');
    fixture.detectChanges();

    const nativeElement = fixture.nativeElement as HTMLElement;
    expect(nativeElement.textContent).toContain('Erro padronizado');

    nativeElement.querySelector('button')?.dispatchEvent(new Event('click'));
    fixture.detectChanges();

    expect(errorMessageService.currentMessage).toBeNull();
  });
});
