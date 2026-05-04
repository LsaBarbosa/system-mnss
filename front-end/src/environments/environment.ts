export const environment = {
  production: false,
  runtime: 'development',
  localApiBaseUrl: '/api',
  onlineApiBaseUrl: '/api',
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
