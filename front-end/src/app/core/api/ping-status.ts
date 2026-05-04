export interface PingResponse {
  message: string;
  application: string;
  environment: 'local';
  checkedAt: string;
}

export type PingStatus =
  | {
      available: true;
      response: PingResponse;
    }
  | {
      available: false;
      errorMessage: string;
    };
