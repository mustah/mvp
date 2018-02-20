import locationHelperBuilder from 'redux-auth-wrapper/history4/locationHelper';
import {connectedRouterRedirect} from 'redux-auth-wrapper/history4/redirect';
import {routes} from '../app/routes';
import {RootState} from '../reducers/rootReducer';
import {Role} from '../state/domain-models/user/userModels';

const isAuthenticatedSelector = (state: RootState): boolean => state.auth.isAuthenticated;
const isNotAuthenticatedSelector = (state: RootState): boolean => !state.auth.isAuthenticated;

export const isAdminSelector = ({auth: {user}}: RootState): boolean =>
  (user!.roles.includes(Role.ADMIN) || user!.roles.includes(Role.SUPER_ADMIN));
export const isSuperAdminSelector = ({auth: {user}}: RootState) => user!.roles.includes(Role.SUPER_ADMIN);

const isAdminAuthenticatedSelector = (state: RootState) => isAuthenticatedSelector(state) && isAdminSelector(state);
const isSuperAdminAuthenticatedSelector = (state: RootState) =>
  isAuthenticatedSelector(state) && isSuperAdminSelector(state);

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

export const superAdminIsAuthenticated = connectedRouterRedirect({
  redirectPath: routes.admin,
  authenticatedSelector: isSuperAdminAuthenticatedSelector,
  allowRedirectBack: false,
});

export const userIsNotAuthenticated = connectedRouterRedirect({
  redirectPath: (state, ownProps) => locationHelperBuilder({}).getRedirectQueryParam(ownProps) || routes.home,
  authenticatedSelector: isNotAuthenticatedSelector,
  allowRedirectBack: false,
});

export const makeToken = (username: string, password: string): string =>
  btoa(`${username}:${password}`);
