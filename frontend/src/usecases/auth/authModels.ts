import {uuid} from '../../types/Types';

export interface User {
  id: uuid;
  firstName: string;
  lastName: string;
  email: string;
  company: string;
}

export interface Unauthorized {
  timestamp: number;
  error: string;
  message: string;
  path: string;
  status: number;
}

export interface Authenticated {
  isAuthenticated: boolean;
}

export interface Authorized {
  user: User;
  token: string;
}

export interface AuthState extends Authenticated {
  user?: User;
  token?: string;
  isLoading?: boolean;
  error?: Unauthorized;
}
