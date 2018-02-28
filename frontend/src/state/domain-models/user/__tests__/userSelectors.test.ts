import {Role, User} from '../userModels';
import {getRoles} from '../userSelectors';

describe('userSelectors', () => {
  describe('getRoles, returns the roles a user is allowed to create new users with', () => {
    it('returns the the roles for a user with roles [Role.USER]', () => {
      const user: Partial<User> = {
        roles: [Role.USER],
      };

      expect(getRoles(user as User)).toEqual([Role.USER]);
    });
    it('returns the the roles for a user with roles [Role.ADMIN]', () => {
      const user: Partial<User> = {
        roles: [Role.ADMIN],
      };

      expect(getRoles(user as User)).toEqual([Role.USER, Role.ADMIN]);
    });
    it('returns the the roles for a user with roles [Role.SUPER_ADMIN]', () => {
      const user: Partial<User> = {
        roles: [Role.SUPER_ADMIN],
      };

      expect(getRoles(user as User)).toEqual([Role.USER, Role.ADMIN, Role.SUPER_ADMIN]);
    });
    it('returns roles depending on highest ranked role', () => {
      const userAdmin: Partial<User> = {
        roles: [Role.USER, Role.ADMIN],
      };
      const userSuperAdmin: Partial<User> = {
        roles: [Role.SUPER_ADMIN, Role.USER],
      };

      expect(getRoles(userAdmin as User)).toEqual([Role.USER, Role.ADMIN]);
      expect(getRoles(userSuperAdmin as User)).toEqual([Role.USER, Role.ADMIN, Role.SUPER_ADMIN]);
    });
  });
});
