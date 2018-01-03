import {User} from '../../state/domain-models/user/userModels';

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
