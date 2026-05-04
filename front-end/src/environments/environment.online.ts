export const environment = {
  production: true,
  runtime: 'online',
  localApiBaseUrl: '',
  onlineApiBaseUrl: 'https://api.padarianovaalianca.com.br/api',
  sitePublico: {
    appName: 'site-publico',
    publicBaseUrl: 'https://padarianovaalianca.com.br',
    apiBaseUrl: 'https://api.padarianovaalianca.com.br/api'
  },
  admin: {
    appName: 'admin',
    publicBaseUrl: 'https://admin.padarianovaalianca.com.br',
    apiBaseUrl: 'https://api.padarianovaalianca.com.br/api'
  }
} as const;
