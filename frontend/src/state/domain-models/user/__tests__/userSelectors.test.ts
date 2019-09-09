import {Role, User} from '../userModels';
import {getRoles, isAdmin, isOtcUserOnly} from '../userSelectors';

describe('userSelectors', () => {

  describe('getRoles, returns the roles a user is allowed to create new users with', () => {

    it('returns the the roles for a given user', () => {
      const user: Partial<User> = {roles: [Role.MVP_USER]};

      expect(getRoles(user as User)).toEqual([Role.MVP_USER]);
    });

    it('returns the the roles for a user with roles [Role.MVP_ADMIN]', () => {
      const user: Partial<User> = {roles: [Role.MVP_ADMIN]};

      expect(getRoles(user as User)).toEqual([Role.MVP_USER, Role.MVP_ADMIN]);
    });

    it('returns the the roles for a super admin user', () => {
      const user: Partial<User> = {roles: [Role.SUPER_ADMIN]};

      expect(getRoles(user as User)).toEqual([
        Role.MVP_USER,
        Role.MVP_ADMIN,
        Role.SUPER_ADMIN,
        Role.OTC_OTD_ADMIN,
        Role.OTC_ADMIN,
        Role.OTC_USER,
      ]);
    });

    it('returns roles depending on highest ranked role', () => {
      const userAdmin: Partial<User> = {roles: [Role.MVP_USER, Role.MVP_ADMIN]};
      const userSuperAdmin: Partial<User> = {roles: [Role.SUPER_ADMIN, Role.MVP_USER]};

      expect(getRoles(userAdmin as User)).toEqual([Role.MVP_USER, Role.MVP_ADMIN]);
      expect(getRoles(userSuperAdmin as User)).toEqual([
        Role.MVP_USER,
        Role.MVP_ADMIN,
        Role.SUPER_ADMIN,
        Role.OTC_OTD_ADMIN,
        Role.OTC_ADMIN,
        Role.OTC_USER,
      ]);
    });

    it('gets otc admin roles', () => {
      const otcAdmin: Partial<User> = {roles: [Role.OTC_ADMIN]};

      expect(getRoles(otcAdmin as User)).toEqual([
        Role.OTC_ADMIN,
        Role.OTC_USER,
      ]);
    });

    it('gets otc user roles', () => {
      const otcAdmin: Partial<User> = {roles: [Role.OTC_USER]};

      expect(getRoles(otcAdmin as User)).toEqual([Role.OTC_USER]);
    });
  });

  describe('roles', () => {

    it('is admin', () => {
      expect(isAdmin([Role.OTC_ADMIN])).toBe(true);
      expect(isAdmin([Role.MVP_USER, Role.MVP_ADMIN])).toBe(true);
      expect(isAdmin([Role.MVP_USER, Role.SUPER_ADMIN])).toBe(true);
    });

    it('is not admin', () => {
      expect(isAdmin([Role.MVP_USER])).toBe(false);
    });

    it('is not just otc user', () => {
      expect(isOtcUserOnly([Role.MVP_USER])).toBe(false);
      expect(isOtcUserOnly([Role.OTC_ADMIN, Role.MVP_ADMIN])).toBe(false);
      expect(isOtcUserOnly([Role.OTC_ADMIN, Role.OTC_USER, Role.MVP_ADMIN])).toBe(false);
    });

    it('is just otc user', () => {
      expect(isOtcUserOnly([Role.OTC_ADMIN])).toBe(true);
      expect(isOtcUserOnly([Role.OTC_ADMIN, Role.OTC_USER])).toBe(true);
      expect(isOtcUserOnly([Role.OTC_OTD_ADMIN, Role.OTC_ADMIN, Role.OTC_USER])).toBe(true);
    });

  });
});
