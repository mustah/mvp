import {AnyAction} from 'redux';
import {LOGIN_FAILURE, LOGIN_REQUEST, LOGIN_SUCCESS, LOGOUT_REQUEST, LOGOUT_SUCCESS} from './authActions';

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  company: string;
}

export interface UnauthorizedDTO {
  timestamp: number;
  error: string;
  message: string;
  path: string;
  status: number;
}

export interface Authenticated {
  isAuthenticated: boolean;
}

export interface AuthState extends Authenticated {
  user?: User;
  token?: string;
  isLoading?: boolean;
  error?: UnauthorizedDTO;
}

const initialState: AuthState = {
  isAuthenticated: false,
};

export const auth = (state: AuthState = initialState, action: AnyAction): AuthState => {
  const {payload} = action;

  switch (action.type) {
    case LOGIN_REQUEST:
      return {
        ...state,
        isLoading: true,
        isAuthenticated: false,
      };
    case LOGIN_SUCCESS:
      return {
        ...state,
        isLoading: false,
        isAuthenticated: true,
        user: {...payload.user},
        token: payload.token,
        error: undefined,
      };
    case LOGIN_FAILURE:
      return {
        ...state,
        isLoading: false,
        isAuthenticated: false,
        error: {...payload},
      };
    case LOGOUT_REQUEST:
      return {
        ...state,
        isLoading: true,
      };
    case LOGOUT_SUCCESS:
      return {
        ...state,
        isLoading: false,
        isAuthenticated: false,
        token: undefined,
        user: undefined,
      };
    default:
      return state;
  }
};
