import {HasId, uuid} from '../../types/Types';
import {ObjectsById} from '../domain-models/domainModels';
import {NormalizedPaginatedState} from './paginatedDomainModels';

export const getPageResult = <T extends HasId>({result}: NormalizedPaginatedState<T>, page: number): uuid[] =>
  result[page] && result[page].result ? result[page].result! : [];

export const getPageIsFetching = <T extends HasId>({result}: NormalizedPaginatedState<T>, page): boolean =>
  result[page] ? result[page].isFetching : true;

export const getPaginatedEntities =
  <T extends HasId>({entities}: NormalizedPaginatedState<T>): ObjectsById<T> => entities;
