import {toArray} from 'lodash';
import {createSelector} from 'reselect';
import {Maybe} from '../../helpers/Maybe';
import {ErrorResponse, Identifiable, uuid} from '../../types/Types';
import {DomainModel, NormalizedState, ObjectsById, RequestsHttp} from './domainModels';

export const getResultDomainModels =
  <T extends Identifiable>(state: NormalizedState<T>): uuid[] => state.result;

export const getEntitiesDomainModels =
  <T extends Identifiable>(state: NormalizedState<T>): ObjectsById<T> => state.entities || {};

export const getAllEntities =
  <T extends Identifiable>({entities, result}: NormalizedState<T>): T[] =>
    result.length ? toArray(entities) : [];

export const getDomainModel =
  <T extends Identifiable>({entities, result}: DomainModel<T>): DomainModel<T> =>
    ({result, entities});

export const getError =
  <T extends RequestsHttp>({error}: T): Maybe<ErrorResponse> =>
    Maybe.maybe(error);

export const getDomainModelById = <T extends Identifiable>(id: uuid) =>
  createSelector<NormalizedState<T>, ObjectsById<T>, Maybe<T>>(
    getEntitiesDomainModels,
    (entities: ObjectsById<T>) => Maybe.maybe<T>(entities[id]),
  );

export const getFirstDomainModel = <T extends Identifiable>({entities, result}: DomainModel<T>): Maybe<T> =>
  Maybe.maybe<T>(entities[result[0]]);
