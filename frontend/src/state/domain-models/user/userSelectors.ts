import {createSelector} from 'reselect';
import {uuid} from '../../../types/Types';
import {NormalizedState, ObjectsById} from '../domainModels';
import {getEntitiesDomainModels, getResultDomainModels} from '../domainModelsSelectors';
import {Organisation, Role, roleList, User} from './userModels';

export const getOrganisations =
  createSelector<NormalizedState<Organisation>, uuid[], ObjectsById<Organisation>, Organisation[]>(
    getResultDomainModels,
    getEntitiesDomainModels,
    (result: uuid[], entities: ObjectsById<Organisation>) => result.map((id) => entities[id]),
  );

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
