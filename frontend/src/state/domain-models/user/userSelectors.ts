import {every, some} from 'lodash';
import {createSelector} from 'reselect';
import {Role, User} from './userModels';

export const isSuperAdmin = (user: User): boolean => user.roles.includes(Role.SUPER_ADMIN);

export const isMvpAdmin = (user: User): boolean => user.roles.includes(Role.MVP_ADMIN) || isSuperAdmin(user);

const isAdminPredicate = (role: Role) =>
  role === Role.MVP_ADMIN ||
  role === Role.OTC_ADMIN ||
  role === Role.SUPER_ADMIN;

const isOtcPredicate = (role: Role) =>
  role === Role.OTC_ADMIN ||
  role === Role.OTC_USER;

const isMvpPredicate = (role: Role) =>
  role === Role.MVP_ADMIN ||
  role === Role.MVP_USER ||
  role === Role.SUPER_ADMIN;

export const isAdmin = (roles: Role[]) => some(roles, isAdminPredicate);
export const isOnlyOtcUser = (roles: Role[]) => every(roles, isOtcPredicate);
export const isOnlyMvpUser = (roles: Role[]) => every(roles, isMvpPredicate);

const roleList: { [key in Role]: Role[] } = {
  [Role.MVP_USER]: [Role.MVP_USER],
  [Role.MVP_ADMIN]: [Role.MVP_USER, Role.MVP_ADMIN],
  [Role.SUPER_ADMIN]: [Role.MVP_USER, Role.MVP_ADMIN, Role.SUPER_ADMIN, Role.OTC_ADMIN, Role.OTC_USER],
  [Role.OTC_ADMIN]: [Role.OTC_ADMIN, Role.OTC_USER],
  [Role.OTC_USER]: [Role.OTC_USER],
};

export const getRoles = createSelector<User, Role[], Role[]>(
  ({roles}: User) => roles,
  (roles: Role[]) => {
    if (roles.includes(Role.SUPER_ADMIN)) {
      return roleList[Role.SUPER_ADMIN];
    } else if (roles.includes(Role.MVP_ADMIN)) {
      return roleList[Role.MVP_ADMIN];
    } else if (roles.includes(Role.OTC_ADMIN)) {
      return roleList[Role.OTC_ADMIN];
    } else if (roles.includes(Role.OTC_USER)) {
      return roleList[Role.OTC_USER];
    } else {
      return roleList[Role.MVP_USER];
    }
  },
);
