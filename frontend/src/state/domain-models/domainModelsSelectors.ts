import {Maybe} from '../../helpers/Maybe';
import {ErrorResponse, HasId, uuid} from '../../types/Types';
import {DomainModel, NormalizedState, ObjectsById} from './domainModels';

export const getResultDomainModels = <T extends HasId>(state: NormalizedState<T>): uuid[] => state.result;
export const getEntitiesDomainModels = <T extends HasId>(state: NormalizedState<T>): ObjectsById<T> => state.entities;
export const getDomainModel = <T extends HasId>({entities, result}: NormalizedState<T>): DomainModel<T> =>
  ({result, entities});

export const getError = <T extends HasId>({error}: NormalizedState<T>): Maybe<ErrorResponse> => Maybe.maybe(error);
