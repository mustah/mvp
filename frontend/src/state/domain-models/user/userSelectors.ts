import {every, some} from 'lodash';
import {createSelector} from 'reselect';
import {Role, User} from './userModels';

const isAdminPredicate = (role: Role): boolean =>
  role === Role.MVP_ADMIN ||
  role === Role.OTC_ADMIN ||
  role === Role.SUPER_ADMIN;

const isOtcPredicate = (role: Role): boolean =>
  role === Role.OTC_ADMIN ||
  role === Role.OTC_USER;

const isMvpPredicate = (role: Role): boolean =>
  role === Role.MVP_ADMIN ||
  role === Role.MVP_USER ||
  role === Role.SUPER_ADMIN;

export const isSuperAdmin = (user: User): boolean =>
  some(user.roles, role => role === Role.SUPER_ADMIN);

export const isMvpAdmin = (user: User): boolean =>
  some(user.roles, role => role === Role.MVP_ADMIN || role === Role.SUPER_ADMIN);

export const isMvpUserRole = (user: User): boolean =>
  some(user.roles, isMvpPredicate);

export const isOtcUserRole = (user: User): boolean =>
  some(user.roles, role => isOtcPredicate(role) || role === Role.SUPER_ADMIN);

export const isAdmin = (roles: Role[]) => some(roles, isAdminPredicate);
export const isMvpUser = (roles: Role[]) => some(roles, isMvpPredicate);
export const isOtcUserOnly = (roles: Role[]) => every(roles, isOtcPredicate);

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
