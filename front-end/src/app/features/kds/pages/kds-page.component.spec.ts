(window as unknown as Record<string, unknown>)['global'] = window;
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { KdsPageComponent } from './kds-page.component';
import { KdsService } from '../data-access/kds.service';
import { Observable, of, Subject } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('KdsPageComponent', () => {
  let component: KdsPageComponent;
  let fixture: ComponentFixture<KdsPageComponent>;
  let kdsServiceMock: {
    loadTickets: jasmine.Spy;
    getTicketsByStatus: jasmine.Spy;
    connectionStatus$: Observable<boolean>;
    readyOrders$: Subject<string>;
    startTicket: jasmine.Spy;
    readyTicket: jasmine.Spy;
    readyItem: jasmine.Spy;
    finishTicket: jasmine.Spy;
    finishOrder: jasmine.Spy;
  };

  beforeEach(async () => {
    kdsServiceMock = {
      loadTickets: jasmine.createSpy('loadTickets'),
      getTicketsByStatus: jasmine.createSpy('getTicketsByStatus').and.returnValue(of([])),
      connectionStatus$: of(true),
      readyOrders$: new Subject<string>(),
      startTicket: jasmine.createSpy('startTicket').and.returnValue(of({})),
      readyTicket: jasmine.createSpy('readyTicket').and.returnValue(of({})),
      readyItem: jasmine.createSpy('readyItem').and.returnValue(of({})),
      finishTicket: jasmine.createSpy('finishTicket').and.returnValue(of({})),
      finishOrder: jasmine.createSpy('finishOrder').and.returnValue(of({}))
    };

    await TestBed.configureTestingModule({
      imports: [KdsPageComponent],
      providers: [{ provide: KdsService, useValue: kdsServiceMock }, provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    fixture = TestBed.createComponent(KdsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load tickets on init', () => {
    expect(kdsServiceMock.loadTickets).toHaveBeenCalled();
  });

  it('should filter by sector', () => {
    component.onSectorChange('CHAPA');
    expect(component.selectedSector).toBe('CHAPA');
    expect(kdsServiceMock.loadTickets).toHaveBeenCalledWith('CHAPA');
  });

  it('should calculate wait time color', () => {
    expect(component.getWaitTimeColor(5)).toContain('success');
    expect(component.getWaitTimeColor(12)).toContain('warning');
    expect(component.getWaitTimeColor(20)).toContain('danger');
  });
});
