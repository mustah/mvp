import {createSelector} from 'reselect';
import {uuid} from '../../../types/Types';
import {NormalizedState, ObjectsById} from '../domainModels';
import {getEntitiesDomainModels, getResultDomainModels} from '../domainModelsSelectors';
import {Organisation} from './organisationModels';

export const getOrganisations =
  createSelector<NormalizedState<Organisation>, uuid[], ObjectsById<Organisation>, Organisation[]>(
    getResultDomainModels,
    getEntitiesDomainModels,
    (result: uuid[], entities: ObjectsById<Organisation>) => result.map((id) => entities[id]),
  );
