import locationHelperBuilder from 'redux-auth-wrapper/history4/locationHelper';
import {connectedRouterRedirect} from 'redux-auth-wrapper/history4/redirect';
import {routes} from '../app/routes';
import {RootState} from '../reducers/rootReducer';
import {Role, User} from '../state/domain-models/user/userModels';
import {isAdmin, isMvpUser, isOtcUserOnly, isOtcUserRole} from '../state/domain-models/user/userSelectors';

const isAuthenticatedSelector = (state: RootState): boolean => state.auth.isAuthenticated;
const isNotAuthenticatedSelector = (state: RootState): boolean => !state.auth.isAuthenticated;
const getUser = (state: RootState): User => state.auth.user!;
const getUserRoles = (state: RootState): Role[] => getUser(state).roles;

export const isAdminAuthenticated = (state: RootState): boolean =>
  isAuthenticatedSelector(state) && isAdmin(getUserRoles(state));

export const isAuthenticatedOtcUser = (state: RootState): boolean =>
  isAuthenticatedSelector(state) && isOtcUserRole(getUser(state));

export const isAuthenticatedOtcUserOnly = (state: RootState): boolean =>
  isAuthenticatedSelector(state) && isOtcUserOnly(getUserRoles(state));

export const isAuthenticatedMvpUser = (state: RootState): boolean =>
  isAuthenticatedSelector(state) && isMvpUser(getUserRoles(state));

const locationHelper = locationHelperBuilder({});

export const isAuthenticated = connectedRouterRedirect({
  redirectPath: (state, ownProps) =>
    isAuthenticatedOtcUserOnly(state)
      ? routes.otc
      : locationHelper.getRedirectQueryParam(ownProps) || routes.login,
  authenticatedSelector: isAuthenticatedMvpUser,
  allowRedirectBack: false,
});

export const adminIsAuthenticated = connectedRouterRedirect({
  redirectPath: state => isAuthenticatedOtcUserOnly(state) ? routes.admin : routes.home,
  authenticatedSelector: isAdminAuthenticated,
  allowRedirectBack: false,
});

export const otcIsAuthenticated = connectedRouterRedirect({
  redirectPath: (state, ownProps) => isAuthenticatedOtcUserOnly(state)
    ? routes.otc
    : locationHelper.getRedirectQueryParam(ownProps) || routes.login,
  authenticatedSelector: isAuthenticatedOtcUser,
  allowRedirectBack: false,
});

export const isNotAuthenticated = connectedRouterRedirect({
  redirectPath: (state, ownProps) =>
    isAuthenticatedOtcUserOnly(state)
      ? routes.otc
      : locationHelper.getRedirectQueryParam(ownProps) || routes.home,
  authenticatedSelector: isNotAuthenticatedSelector,
  allowRedirectBack: false,
});
