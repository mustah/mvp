import {IdNamed, uuid} from '../../../types/Types';
import {ObjectsById, NormalizedState} from '../domainModels';

export interface Organisation extends IdNamed {
  code: uuid;
  name: string;
}

export interface User {
  id: uuid;
  name: string;
  email: string;
  organisation: Organisation;
  password?: string;
  roles: Role[];
}

export type UserState = NormalizedState<User>;

export const enum Role {
  ADMIN = 'ADMIN',
  USER = 'USER',
  SUPER_ADMIN = 'SUPER_ADMIN',
}

export const filterUsersByUser = (users: ObjectsById<User>, currentUser: User): ObjectsById<User> => {
  if (currentUser.organisation.code === 'elvaco') {
    return users;
  }

  if (currentUser.roles.includes(Role.ADMIN)) {
    const filteredUsers = Object.keys(users).reduce((sum: User, id: string) => {
      if (currentUser.organisation.code === users[id].organisation.code) {
        sum[id] = users[id];
      }
      return sum;
    }, {});
    return filteredUsers;
  }

  return {[currentUser.id]: currentUser};
};
