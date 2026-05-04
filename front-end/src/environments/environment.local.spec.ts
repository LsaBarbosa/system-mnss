import { environment } from './environment.local';

describe('local environment', () => {
  it('uses nginx-relative local API settings', () => {
    expect(environment.production).toBeFalse();
    expect(environment.runtime).toBe('local');
    expect(environment.localApiBaseUrl).toBe('/api');
    expect(environment.sitePublico.apiBaseUrl).toBe('/api');
    expect(environment.admin.apiBaseUrl).toBe('/api');
  });
});
