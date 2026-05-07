import { environment } from './environment.local';

describe('local environment', () => {
  it('uses nginx-relative local API settings', () => {
    expect(environment.production).toBeFalse();
    expect(environment.runtime).toBe('local');
    expect(environment.localApiBaseUrl).toBe('/api');
    expect(environment.sitePublico.apiBaseUrl).toBe('/api');
    expect(environment.admin.apiBaseUrl).toBe('/api');
  });

  it('localApiBaseUrl does not contain localhost', () => {
    expect(environment.localApiBaseUrl).not.toContain('localhost');
  });

  it('does not have double /api prefix', () => {
    expect(environment.localApiBaseUrl).not.toContain('/api/api');
  });

  it('PDV services use localApiBaseUrl for local runtime', () => {
    expect(environment.runtime).toBe('local');
    expect(environment.localApiBaseUrl).toBe('/api');
    // runtime local => PDV usa localApiBaseUrl, nunca onlineApiBaseUrl
    expect(environment.onlineApiBaseUrl).toContain('https://');
  });
});
