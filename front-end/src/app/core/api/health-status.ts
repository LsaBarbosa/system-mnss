export interface HealthStatus {
  status: 'UP' | 'DOWN' | 'OUT_OF_SERVICE' | 'UNKNOWN';
  environment: 'local' | 'online';
  offlineCriticalOperation: boolean;
  message: string;
  checkedAt: string;
  components: Record<string, string>;
}
