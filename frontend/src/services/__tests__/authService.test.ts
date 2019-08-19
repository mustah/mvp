import {RootState} from '../../reducers/rootReducer';
import {Role} from '../../state/domain-models/user/userModels';
import {isAdminAuthenticated, isAuthenticatedMvpUser, isAuthenticatedOtcUserOnly} from '../authService';

describe('authService', () => {

  describe('is authenticated', () => {

    it('does not authenticate admin users that are not authorized', () => {
      expect(isAdminAuthenticated(notAuthenticatedUser([Role.SUPER_ADMIN]) as RootState)).toBe(false);
    });

    it('does not authenticate non-admin users', () => {
      expect(isAdminAuthenticated(authenticatedUser([Role.MVP_USER]) as RootState)).toBe(false);
    });

    it('authenticates admin users', () => {
      expect(isAdminAuthenticated(authenticatedUser([Role.SUPER_ADMIN]) as RootState)).toBe(true);
      expect(isAdminAuthenticated(authenticatedUser([Role.OTC_ADMIN]) as RootState)).toBe(true);
      expect(isAdminAuthenticated(authenticatedUser([Role.MVP_ADMIN]) as RootState)).toBe(true);
    });

    it('does authenticates when user has other role than otc', () => {
      expect(isAuthenticatedOtcUserOnly(authenticatedUser([Role.MVP_ADMIN]) as RootState)).toBe(false);
      expect(isAuthenticatedOtcUserOnly(authenticatedUser([Role.MVP_ADMIN, Role.OTC_ADMIN]) as RootState)).toBe(false);
    });

    it('authenticates user with otc roles only', () => {
      expect(isAuthenticatedOtcUserOnly(authenticatedUser([Role.OTC_ADMIN]) as RootState)).toBe(true);
      expect(isAuthenticatedOtcUserOnly(authenticatedUser([Role.OTC_USER, Role.OTC_ADMIN]) as RootState)).toBe(true);
    });

    it('does not authenticate when user has not mvp role', () => {
      expect(isAuthenticatedMvpUser(authenticatedUser([Role.OTC_ADMIN]) as RootState)).toBe(false);
      expect(isAuthenticatedMvpUser(authenticatedUser([Role.OTC_USER, Role.OTC_ADMIN]) as RootState)).toBe(false);
    });

    it('authenticates user with mvp roles', () => {
      expect(isAuthenticatedMvpUser(authenticatedUser([Role.MVP_ADMIN]) as RootState)).toBe(true);
      expect(isAuthenticatedMvpUser(authenticatedUser([Role.SUPER_ADMIN]) as RootState)).toBe(true);
      expect(isAuthenticatedMvpUser(authenticatedUser([Role.MVP_USER, Role.MVP_ADMIN]) as RootState)).toBe(true);
      expect(isAuthenticatedMvpUser(authenticatedUser([Role.OTC_ADMIN, Role.MVP_ADMIN]) as RootState)).toBe(true);
      expect(isAuthenticatedMvpUser(authenticatedUser([Role.OTC_USER, Role.MVP_USER]) as RootState)).toBe(true);
    });

    const authenticatedUser = (roles: Role[]) => ({
      auth: {isAuthenticated: true, user: {roles}}
    });

    const notAuthenticatedUser = (roles: Role[]) => ({
      auth: {isAuthenticated: false, user: {roles}}
    });
  });
});
