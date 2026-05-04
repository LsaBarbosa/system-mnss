import { environment } from './environment.online';

describe('online environment', () => {
  it('loads online API settings for site-publico and admin', () => {
    expect(environment.production).toBeTrue();
    expect(environment.runtime).toBe('online');
    expect(environment.onlineApiBaseUrl).toBe('https://api.padarianovaalianca.com.br/api');

    expect(environment.sitePublico.appName).toBe('site-publico');
    expect(environment.sitePublico.publicBaseUrl).toBe('https://padarianovaalianca.com.br');
    expect(environment.sitePublico.apiBaseUrl).toBe(environment.onlineApiBaseUrl);

    expect(environment.admin.appName).toBe('admin');
    expect(environment.admin.publicBaseUrl).toBe('https://admin.padarianovaalianca.com.br');
    expect(environment.admin.apiBaseUrl).toBe(environment.onlineApiBaseUrl);
  });

  it('does not use localhost in production URLs', () => {
    expect(environment.onlineApiBaseUrl).not.toContain('localhost');
    expect(environment.sitePublico.publicBaseUrl).not.toContain('localhost');
    expect(environment.sitePublico.apiBaseUrl).not.toContain('localhost');
    expect(environment.admin.publicBaseUrl).not.toContain('localhost');
    expect(environment.admin.apiBaseUrl).not.toContain('localhost');
  });
});
