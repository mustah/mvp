import {EmptyAction} from 'react-redux-typescript';
import {Action} from '../../types/Types';
import {LOGIN_FAILURE, LOGIN_REQUEST, LOGIN_SUCCESS, LOGOUT_REQUEST, LOGOUT_SUCCESS} from './authActions';
import {Authorized, AuthState, Unauthorized} from './authModels';

const initialAuthState: AuthState = {isAuthenticated: false};

type ActionTypes =
  & Action<Authorized>
  & Action<Unauthorized>
  & EmptyAction<string>;

export const auth = (state: AuthState = initialAuthState, action: ActionTypes): AuthState => {
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
        error: {...(action as Action<Unauthorized>).payload},
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
