import locationHelperBuilder from 'redux-auth-wrapper/history4/locationHelper';
import {connectedRouterRedirect} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../reducers/index';
import {routes} from '../usecases/app/routes';
import {AuthState} from '../usecases/auth/authReducer';
import {storageService} from './StorageService';

const isAuthenticatedSelector = (state: RootState): boolean => state.auth.isAuthenticated;
const isNotAuthenticatedSelector = (state: RootState): boolean => !state.auth.isAuthenticated;

export const userIsAuthenticated = connectedRouterRedirect({
  redirectPath: routes.login,
  authenticatedSelector: isAuthenticatedSelector,
  allowRedirectBack: false,
});

export const userIsNotAuthenticated = connectedRouterRedirect({
  redirectPath: (state, ownProps) => locationHelperBuilder({}).getRedirectQueryParam(ownProps) || routes.home,
  authenticatedSelector: isNotAuthenticatedSelector,
  allowRedirectBack: false,
});

export const makeToken = (username: string, password: string): string => {
  return btoa(`${username}:${password}`);
};

export const mvpAuthKey = 'mvpAuthKey';

export const saveAuthState = (state: AuthState): void => {
  if (state.isAuthenticated && state.token) {
    try {
      storageService.setItem(mvpAuthKey, JSON.stringify(state));
    } catch (error) {
      // ignore write errors
    }
  }
};

export const initialAuthState: AuthState = {isAuthenticated: false};

export const loadAuthState = (): AuthState => {
  const item = storageService.getItem(mvpAuthKey);
  try {
    return (item && JSON.parse(item)) || {...initialAuthState};
  } catch (error) {
    return {...initialAuthState};
  }
};
