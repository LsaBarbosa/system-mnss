import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AppComponent } from './app.component';
import { HealthService } from './core/api/health.service';

describe('AppComponent', () => {
  let fixture: ComponentFixture<AppComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        {
          provide: HealthService,
          useValue: {
            getLocalHealth: () =>
              of({
                status: 'UP',
                environment: 'local',
                offlineCriticalOperation: true,
                message: 'ready',
                checkedAt: '2026-05-03T12:00:00Z'
              })
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
  });

  it('renders initial operation console', () => {
    fixture.detectChanges();

    const nativeElement = fixture.nativeElement as HTMLElement;
    expect(nativeElement.textContent).toContain('Console inicial');
    expect(nativeElement.textContent).toContain('Online local');
  });
});
