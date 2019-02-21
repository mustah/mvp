import {EmptyAction} from 'typesafe-actions/dist/types';
import {User} from '../../state/domain-models/user/userModels';
import {Action} from '../../types/Types';
import {AUTH_SET_USER_INFO, LOGIN_FAILURE, LOGIN_REQUEST, LOGIN_SUCCESS, LOGOUT_USER} from './authActions';
import {Authorized, AuthState, Unauthorized} from './authModels';

export const initialAuthState: AuthState = {isAuthenticated: false};

type ActionTypes =
  | Action<Authorized>
  | Action<Unauthorized>
  | Action<User>
  | EmptyAction<string>;

const loginSuccess = (state: AuthState, {payload}: Action<Authorized>): AuthState => ({
  ...state,
  isLoading: false,
  isAuthenticated: true,
  user: {...payload.user},
  token: payload.token,
  error: undefined,
});

const loginFailure = (state: AuthState, {payload}: Action<Unauthorized>): AuthState => ({
  ...state,
  isLoading: false,
  isAuthenticated: false,
  error: payload,
});

const setUserInfo = (state: AuthState, {payload}: Action<User>): AuthState => ({
  ...state,
  user: payload,
});

const logoutUser = (state: AuthState, {payload}: Action<Unauthorized>): AuthState => ({
  ...initialAuthState,
  error: payload,
});

export const auth = (state: AuthState = initialAuthState, action: ActionTypes): AuthState => {
  switch (action.type) {
    case LOGIN_REQUEST:
      return {
        ...state,
        isLoading: true,
        isAuthenticated: false,
      };
    case LOGIN_SUCCESS:
      return loginSuccess(state, action as Action<Authorized>);
    case LOGIN_FAILURE:
      return loginFailure(state, action as Action<Unauthorized>);
    case LOGOUT_USER:
      return logoutUser(state, action as Action<Unauthorized>);
    case AUTH_SET_USER_INFO:
      return setUserInfo(state, action as Action<User>);
    default:
      return state;
  }
};
