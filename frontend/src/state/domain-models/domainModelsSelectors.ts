import {createSelector} from 'reselect';
import {Maybe} from '../../helpers/Maybe';
import {ErrorResponse, Identifiable, uuid} from '../../types/Types';
import {DomainModel, NormalizedState, ObjectsById} from './domainModels';

export const getResultDomainModels =
  <T extends Identifiable>(state: NormalizedState<T>): uuid[] => state.result;

export const getEntitiesDomainModels =
  <T extends Identifiable>(state: NormalizedState<T>): ObjectsById<T> => state.entities || {};

export const getDomainModel =
  <T extends Identifiable>({entities, result}: NormalizedState<T>): DomainModel<T> =>
    ({result, entities});

export const getError =
  <T extends Identifiable>({error}: NormalizedState<T>): Maybe<ErrorResponse> => Maybe.maybe(error);

export const getDomainModelById = <T extends Identifiable>(id: uuid) =>
  createSelector<NormalizedState<T>, ObjectsById<T>, Maybe<T>>(
    getEntitiesDomainModels,
    ((entities: ObjectsById<T>) => Maybe.maybe<T>(entities[id])),
  );
