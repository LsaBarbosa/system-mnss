import type { ComponentFixture } from '@angular/core/testing';
import { TestBed } from '@angular/core/testing';
import { mockAuditLog, mockAvailabilityAuditLog } from '../models/domain.mocks';
import { AuditLogTableComponent } from './audit-log-table.component';

describe('AuditLogTableComponent', () => {
  let fixture: ComponentFixture<AuditLogTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuditLogTableComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(AuditLogTableComponent);
  });

  it('renders mocked audit logs', () => {
    fixture.componentInstance.logs = [mockAuditLog];
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('PRODUCT_PRICE_CHANGED');
    expect(fixture.nativeElement.textContent).toContain('Product');
    expect(fixture.nativeElement.textContent).toContain(mockAuditLog.actorUserId);
  });

  it('renders mocked availability history', () => {
    fixture.componentInstance.logs = [mockAvailabilityAuditLog];
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('PRODUCT_AVAILABILITY_CHANGED');
    expect(fixture.nativeElement.textContent).toContain('ProductAvailability');
  });
});
