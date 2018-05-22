import {createSelector} from 'reselect';
import {Maybe} from '../../../helpers/Maybe';
import {uuid} from '../../../types/Types';
import {NormalizedState, ObjectsById} from '../domainModels';
import {getEntitiesDomainModels, getResultDomainModels} from '../domainModelsSelectors';
import {Organisation} from './organisationModels';

export const getOrganisation = ({entities}: NormalizedState<Organisation>, id: uuid): Maybe<Organisation> =>
  Maybe.maybe(entities[id]);

export const getOrganisations =
  createSelector<NormalizedState<Organisation>, uuid[], ObjectsById<Organisation>, Organisation[]>(
    getResultDomainModels,
    getEntitiesDomainModels,
    (result: uuid[], entities: ObjectsById<Organisation>) => result.map((id) => entities[id]),
  );
