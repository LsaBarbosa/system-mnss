export const environment = {
  production: false,
  runtime: 'local',
  localApiBaseUrl: '/api',
  onlineApiBaseUrl: 'https://api.padarianovaalianca.com.br/api',
  sitePublico: {
    appName: 'site-publico',
    publicBaseUrl: '/',
    apiBaseUrl: '/api'
  },
  admin: {
    appName: 'admin',
    publicBaseUrl: '/admin',
    apiBaseUrl: '/api'
  }
} as const;
