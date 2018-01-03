import {IdNamed, uuid} from '../../../types/Types';
import {DomainModel, NormalizedState} from '../domainModels';

interface Company extends IdNamed {
  code: uuid;
  name: string;
}

export interface User {
  id: uuid;
  name: string;
  email: string;
  company: Company;
  roles: Role[];
}

export type UserState = NormalizedState<User>;

export const enum Role {
  'ADMIN' = 'ADMIN',
  'USER' = 'USER',
}

export const filterUsersByUser = (users: DomainModel<User>, currentUser: User): DomainModel<User> => {
  if (currentUser.company.code === 'elvaco') {
    return users;
  }

  if (currentUser.roles.includes(Role.ADMIN)) {
    const filteredUsers = Object.keys(users).reduce((sum: User, id: string) => {
      if (currentUser.company.code === users[id].company.code) {
        sum[id] = users[id];
      }
      return sum;
    }, {});
    return filteredUsers;
  }

  return {[currentUser.id]: currentUser};
};
