import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/type-helpers';
import {User} from '../../state/domain-models/user/userModels';
import {Action} from '../../types/Types';
import {authSetUser, loginFailure, loginRequest, loginSuccess, logoutUser} from './authActions';
import {Authorized, AuthState, Unauthorized} from './authModels';

export const initialAuthState: AuthState = {isAuthenticated: false};

type ActionTypes =
  | Action<Authorized>
  | Action<Unauthorized>
  | Action<User>
  | EmptyAction<string>;

const success = (state: AuthState, {payload}: Action<Authorized>): AuthState => ({
  ...state,
  isLoading: false,
  isAuthenticated: true,
  user: {...payload.user},
  token: payload.token,
  error: undefined,
});

const failure = (state: AuthState, {payload}: Action<Unauthorized>): AuthState => ({
  ...state,
  isLoading: false,
  isAuthenticated: false,
  error: payload,
});

const setUserInfo = (state: AuthState, {payload}: Action<User>): AuthState => ({
  ...state,
  user: payload,
});

const logout = (_: AuthState, {payload}: Action<Unauthorized>): AuthState => ({
  ...initialAuthState,
  error: payload,
});

export const auth = (state: AuthState = initialAuthState, action: ActionTypes): AuthState => {
  switch (action.type) {
    case getType(loginRequest):
      return {
        ...state,
        isLoading: true,
        isAuthenticated: false,
      };
    case getType(loginSuccess):
      return success(state, action as Action<Authorized>);
    case getType(loginFailure):
      return failure(state, action as Action<Unauthorized>);
    case getType(logoutUser):
      return logout(state, action as Action<Unauthorized>);
    case getType(authSetUser):
      return setUserInfo(state, action as Action<User>);
    default:
      return state;
  }
};
