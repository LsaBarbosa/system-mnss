export interface HealthStatus {
  status: 'UP' | 'DOWN';
  environment: 'local' | 'online';
  offlineCriticalOperation: boolean;
  message: string;
  checkedAt: string;
}
