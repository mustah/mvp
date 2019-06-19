import locationHelperBuilder from 'redux-auth-wrapper/history4/locationHelper';
import {connectedRouterRedirect} from 'redux-auth-wrapper/history4/redirect';
import {routes} from '../app/routes';
import {RootState} from '../reducers/rootReducer';
import {Role} from '../state/domain-models/user/userModels';

const isAuthenticatedSelector = (state: RootState): boolean => state.auth.isAuthenticated;
const isNotAuthenticatedSelector = (state: RootState): boolean => !state.auth.isAuthenticated;

const isMvpAdminSelector = (state: RootState): boolean =>
  state.auth.user!.roles.includes(Role.MVP_ADMIN) || isSuperAdminSelector(state);

const isSuperAdminSelector = ({auth: {user}}: RootState): boolean =>
  user!.roles.includes(Role.SUPER_ADMIN);

const isMvpAdminAuthenticatedSelector = (state: RootState): boolean =>
  isAuthenticatedSelector(state) && isMvpAdminSelector(state);

export const userIsAuthenticated = connectedRouterRedirect({
  redirectPath: routes.login,
  authenticatedSelector: isAuthenticatedSelector,
  allowRedirectBack: false,
});

export const mvpAdminIsAuthenticated = connectedRouterRedirect({
  redirectPath: routes.home,
  authenticatedSelector: isMvpAdminAuthenticatedSelector,
  allowRedirectBack: false,
});

export const userIsNotAuthenticated = connectedRouterRedirect({
  redirectPath: (
    _,
    ownProps,
  ) => locationHelperBuilder({}).getRedirectQueryParam(ownProps) || routes.home,
  authenticatedSelector: isNotAuthenticatedSelector,
  allowRedirectBack: false,
});

export const makeToken = (username: string, password: string): string =>
  btoa(`${username}:${password}`);
