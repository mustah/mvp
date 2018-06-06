import {createSelector} from 'reselect';
import {Role, roleList, User} from './userModels';

export const isSuperAdmin = (user: User): boolean => user.roles.includes(Role.SUPER_ADMIN);

export const isAdmin = (user: User): boolean => user.roles.includes(Role.ADMIN) || isSuperAdmin(user);

export const getRoles = createSelector<User, Role[], Role[]>(
  ({roles}: User) => roles,
  (roles: Role[]) => {
    if (roles.includes(Role.SUPER_ADMIN)) {
      return roleList[Role.SUPER_ADMIN];
    } else if (roles.includes(Role.ADMIN)) {
      return roleList[Role.ADMIN];
    } else {
      return roleList[Role.USER];
    }
  },
);
