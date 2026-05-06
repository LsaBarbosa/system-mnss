export type RoleName = 'ADMIN' | 'GERENTE' | 'CAIXA' | 'ATENDENTE' | 'COZINHA' | 'ENTREGADOR' | 'EXPEDICAO' | 'CONSULTA';

export interface AuthUser {
  id: string;
  name: string;
  email?: string | null;
  username: string;
  active: boolean;
  roles: RoleName[];
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  expiresAt: string;
  user: AuthUser;
}

export interface RoleOption {
  id: string;
  name: RoleName;
  description?: string | null;
}

export interface CreateUserRequest {
  name: string;
  email?: string | null;
  username: string;
  password: string;
  active: boolean;
  roles: RoleName[];
}
