import {normalize} from 'normalizr';
import {ObjectsById} from '../../domainModels';
import {filterUsersByUser, Role, User} from '../userModels';
import {userSchema} from '../userSchema';

describe('userModel', () => {

  describe('filterUsersByUser', () => {
    const regularUsers: User[] = [
      {
        id: 'johnny@wayne-industries.se',
        name: 'Johnny Persbrandt',
        email: 'johnny@wayne-industries.se',
        organisation: {id: 'wayne-industries', name: 'wayne-industries', code: 'wayne-industries'},
        roles: [Role.USER],
      },
    ];

    const regularAdmins = [
      {
        id: 'svante@wayne-industries.se',
        name: 'Svante Sturesson',
        email: 'svante@wayne-industries.se',
        organisation: {id: 'wayne-industries', name: 'wayne-industries', code: 'wayne-industries'},
        roles: [Role.USER, Role.ADMIN],
      },
      {
        id: 'bengan@wayne-industries.se',
        name: 'Bagare Bength',
        email: 'bengan@wayne-industries.se',
        organisation: {id: 'wayne-industries', name: 'wayne-industries', code: 'wayne-industries'},
        roles: [Role.USER, Role.ADMIN],
      },
    ];

    const elvacoUsers = [
      {
        id: 'andnil@elvaco.se',
        name: 'Anders Nilsson',
        email: 'andnil@elvaco.se',
        organisation: {id: 'elvaco', name: 'elvaco', code: 'elvaco'},
        roles: [Role.USER],
      },
    ];
    const elvacoAdmins = [
      {
        id: 'evanil@elvaco.se',
        name: 'Eva Nilsson',
        email: 'evanil@elvaco.se',
        organisation: {id: 'elvaco', name: 'elvaco', code: 'elvaco'},
        roles: [Role.USER, Role.ADMIN],
      },
    ];

    const usersFromDifferentLayers: ObjectsById<User> = normalize([
      ...regularAdmins,
      ...regularUsers,
      ...elvacoAdmins,
      ...elvacoUsers,
    ], userSchema).entities.users;

    const allUsers = Object.keys(usersFromDifferentLayers).length;

    const usersVisibleByUser = (user: User): number => {
      const filteredUsers = filterUsersByUser(usersFromDifferentLayers, user);
      return Object.keys(filteredUsers).length;
    };

    it('allows Elvaco user to see users from all companies', () => {
      const elvacoUser: User = usersFromDifferentLayers['evanil@elvaco.se'];
      expect(usersVisibleByUser(elvacoUser)).toEqual(allUsers);
    });

    it('allows admin users to see both users and other admins from the same organisation', () => {
      const regularAdmin: User = usersFromDifferentLayers['svante@wayne-industries.se'];
      const filteredUsers = usersVisibleByUser(regularAdmin);
      expect(filteredUsers).toEqual(regularAdmins.length + regularUsers.length);
      expect(filteredUsers).toBeLessThan(allUsers);
    });

    it('disallows regular users to see any other users than themselves', () => {
      const regularUser: User = usersFromDifferentLayers['johnny@wayne-industries.se'];
      expect(usersVisibleByUser(regularUser)).toEqual(1);
    });
  });
});
