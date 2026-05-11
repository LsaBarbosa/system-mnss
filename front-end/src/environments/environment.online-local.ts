export const environment = {
  production: false,
  runtime: 'online-local',
  localApiBaseUrl: '',
  onlineApiBaseUrl: 'http://localhost:8081/api',
  sitePublico: {
    appName: 'site-publico',
    publicBaseUrl: 'http://localhost:4201',
    apiBaseUrl: 'http://localhost:8081/api'
  },
  admin: {
    appName: 'admin',
    publicBaseUrl: 'http://localhost:4201',
    apiBaseUrl: 'http://localhost:8081/api'
  }
} as const;
