import {Maybe} from '../../helpers/Maybe';
import {ErrorResponse, Identifiable, uuid} from '../../types/Types';
import {ObjectsById} from '../domain-models/domainModels';
import {NormalizedPaginatedState} from './paginatedDomainModels';

export const getPageResult = <T extends Identifiable>({result}: NormalizedPaginatedState<T>, page: number): uuid[] =>
  result[page] && result[page].result ? result[page].result! : [];

export const getPageIsFetching = <T extends Identifiable>({result}: NormalizedPaginatedState<T>, page): boolean =>
  result[page] ? result[page].isFetching : false;

export const getPageError = <T extends Identifiable>({result}: NormalizedPaginatedState<T>, page): Maybe<ErrorResponse> =>
  result[page] ? Maybe.maybe(result[page].error) : Maybe.nothing();

export const getPaginatedEntities =
  <T extends Identifiable>({entities}: NormalizedPaginatedState<T>): ObjectsById<T> => entities;
