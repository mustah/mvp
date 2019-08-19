import locationHelperBuilder from 'redux-auth-wrapper/history4/locationHelper';
import {connectedRouterRedirect} from 'redux-auth-wrapper/history4/redirect';
import {routes} from '../app/routes';
import {RootState} from '../reducers/rootReducer';
import {Role} from '../state/domain-models/user/userModels';
import {isAdmin, isOnlyMvpUser, isOnlyOtcUser} from '../state/domain-models/user/userSelectors';

const isAuthenticatedSelector = (state: RootState): boolean => state.auth.isAuthenticated;
const isNotAuthenticatedSelector = (state: RootState): boolean => !state.auth.isAuthenticated;
const getUserRoles = (state: RootState): Role[] => state.auth.user!.roles;

export const isAdminAuthenticated = (state: RootState): boolean =>
  isAuthenticatedSelector(state) && isAdmin(getUserRoles(state));

export const isAuthenticatedOtcUserOnly = (state: RootState): boolean =>
  isAuthenticatedSelector(state) && isOnlyOtcUser(getUserRoles(state));

export const isAuthenticatedMvpUserOnly = (state: RootState): boolean =>
  isAuthenticatedSelector(state) && isOnlyMvpUser(getUserRoles(state));

const locationHelper = locationHelperBuilder({});

export const isAuthenticated = connectedRouterRedirect({
  redirectPath: (state, ownProps) =>
    isAuthenticatedOtcUserOnly(state)
      ? routes.admin
      : locationHelper.getRedirectQueryParam(ownProps) || routes.login,
  authenticatedSelector: isAuthenticatedMvpUserOnly,
  allowRedirectBack: false,
});

export const adminIsAuthenticated = connectedRouterRedirect({
  redirectPath: state => isAuthenticatedOtcUserOnly(state) ? routes.admin : routes.home,
  authenticatedSelector: isAdminAuthenticated,
  allowRedirectBack: false,
});

export const isNotAuthenticated = connectedRouterRedirect({
  redirectPath: (state, ownProps) =>
    isAuthenticatedOtcUserOnly(state)
      ? routes.admin
      : locationHelper.getRedirectQueryParam(ownProps) || routes.home,
  authenticatedSelector: isNotAuthenticatedSelector,
  allowRedirectBack: false,
});
