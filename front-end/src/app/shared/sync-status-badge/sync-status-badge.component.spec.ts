import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SyncStatusBadgeComponent } from './sync-status-badge.component';

describe('SyncStatusBadgeComponent', () => {
  let component: SyncStatusBadgeComponent;
  let fixture: ComponentFixture<SyncStatusBadgeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SyncStatusBadgeComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(SyncStatusBadgeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render correct label for SYNCED status', () => {
    component.status = 'SYNCED';
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.sync-badge__label')?.textContent).toContain('Sincronizado');
    expect(compiled.querySelector('.sync-badge')?.classList).toContain('success');
  });

  it('should render correct label for PENDING status', () => {
    component.status = 'PENDING';
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.sync-badge__label')?.textContent).toContain('Pendente');
    expect(compiled.querySelector('.sync-badge')?.classList).toContain('warning');
  });

  it('should render correct label for FAILED status', () => {
    component.status = 'FAILED';
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.sync-badge__label')?.textContent).toContain('Erro Sync');
    expect(compiled.querySelector('.sync-badge')?.classList).toContain('danger');
  });
});
