import {IdNamed, uuid} from '../../types/Types';

export interface User {
  id: uuid;
  firstName: string;
  lastName: string;
  email: string;
  company: IdNamed;
}

export interface Unauthorized {
  timestamp: number;
  error: string;
  message: string;
  path: string;
  status: number;
}

export interface Authorized {
  user: User;
  token: string;
}

export interface AuthState {
  isAuthenticated: boolean;
  user?: User;
  token?: string;
  isLoading?: boolean;
  error?: Unauthorized;
}
