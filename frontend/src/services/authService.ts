import locationHelperBuilder from 'redux-auth-wrapper/history4/locationHelper';
import {connectedRouterRedirect} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../reducers/rootReducer';
import {routes} from '../app/routes';
import {Role} from '../state/domain-models/user/userModels';

const isAuthenticatedSelector = (state: RootState): boolean => state.auth.isAuthenticated;
const isNotAuthenticatedSelector = (state: RootState): boolean => !state.auth.isAuthenticated;
export const isAdminSelector = ({auth: {user}}: RootState): boolean =>
  (user!.roles.includes(Role.ADMIN) || user!.roles.includes(Role.SUPER_ADMIN));
const isAdminAuthenticatedSelector = (state: RootState) => isAuthenticatedSelector(state) && isAdminSelector(state);

export const userIsAuthenticated = connectedRouterRedirect({
  redirectPath: routes.login,
  authenticatedSelector: isAuthenticatedSelector,
  allowRedirectBack: false,
});

export const adminIsAuthenticated = connectedRouterRedirect({
  redirectPath: routes.home,
  authenticatedSelector: isAdminAuthenticatedSelector,
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
