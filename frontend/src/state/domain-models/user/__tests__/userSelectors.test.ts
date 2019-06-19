import {Role, User} from '../userModels';
import {getRoles} from '../userSelectors';

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

      expect(getRoles(user as User)).toEqual([Role.MVP_USER, Role.MVP_ADMIN, Role.SUPER_ADMIN]);
    });

    it('returns roles depending on highest ranked role', () => {
      const userAdmin: Partial<User> = {roles: [Role.MVP_USER, Role.MVP_ADMIN]};
      const userSuperAdmin: Partial<User> = {roles: [Role.SUPER_ADMIN, Role.MVP_USER]};

      expect(getRoles(userAdmin as User)).toEqual([Role.MVP_USER, Role.MVP_ADMIN]);
      expect(getRoles(userSuperAdmin as User)).toEqual([Role.MVP_USER, Role.MVP_ADMIN, Role.SUPER_ADMIN]);
    });
  });
});
