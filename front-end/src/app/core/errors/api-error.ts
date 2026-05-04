export interface ApiFieldError {
  field: string;
  message: string;
}

export interface ApiError {
  code: string;
  message: string;
  status: number;
  path: string;
  timestamp: string;
  validationErrors: ApiFieldError[];
}
