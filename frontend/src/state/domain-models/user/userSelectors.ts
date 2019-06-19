import {createSelector} from 'reselect';
import {Role, User} from './userModels';

export const isSuperAdmin = (user: User): boolean => user.roles.includes(Role.SUPER_ADMIN);

export const isMvpAdmin = (user: User): boolean => user.roles.includes(Role.MVP_ADMIN) || isSuperAdmin(user);

const roleList: { [key in Role]: Role[] } = {
  [Role.MVP_USER]: [Role.MVP_USER],
  [Role.MVP_ADMIN]: [Role.MVP_USER, Role.MVP_ADMIN],
  [Role.SUPER_ADMIN]: [Role.MVP_USER, Role.MVP_ADMIN, Role.SUPER_ADMIN],
};

export const getRoles = createSelector<User, Role[], Role[]>(
  ({roles}: User) => roles,
  (roles: Role[]) => {
    if (roles.includes(Role.SUPER_ADMIN)) {
      return roleList[Role.SUPER_ADMIN];
    } else if (roles.includes(Role.MVP_ADMIN)) {
      return roleList[Role.MVP_ADMIN];
    } else {
      return roleList[Role.MVP_USER];
    }
  },
);
