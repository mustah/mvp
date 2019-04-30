import {createSelector} from 'reselect';
import {Maybe} from '../../helpers/Maybe';
import {ErrorResponse, Identifiable, uuid} from '../../types/Types';
import {ObjectsById} from '../domain-models/domainModels';
import {EntityTypes, Pagination, PaginationState} from '../ui/pagination/paginationModels';
import {getPagination} from '../ui/pagination/paginationSelectors';
import {NormalizedPaginatedState} from './paginatedDomainModels';

export const getPageResult =
  <T extends Identifiable>({result}: NormalizedPaginatedState<T>, page: number): uuid[] =>
    result[page] && result[page].result ? result[page].result! : [];

export const getPageIsFetching =
  <T extends Identifiable>({result}: NormalizedPaginatedState<T>, page): boolean =>
    result[page] ? result[page].isFetching : false;

export const getPageError =
  <T extends Identifiable>({result}: NormalizedPaginatedState<T>, page): Maybe<ErrorResponse> =>
    result[page] ? Maybe.maybe(result[page].error) : Maybe.nothing();

export const getPaginatedEntities =
  <T extends Identifiable>({entities}: NormalizedPaginatedState<T>): ObjectsById<T> => entities;

export const getPaginatedDomainModelById = <T extends Identifiable>(id: uuid) =>
  createSelector<NormalizedPaginatedState<T>, ObjectsById<T>, Maybe<T>>(
    getPaginatedEntities,
    ((entities: ObjectsById<T>) => Maybe.maybe<T>(entities[id])),
  );

export const isMetersPageFetching =
  <T extends Identifiable>(meters: NormalizedPaginatedState<T>, pagination: PaginationState): boolean => {
    const entityType: EntityTypes = 'meters';
    const {page}: Pagination = getPagination({entityType, pagination});
    return getPageIsFetching(meters, page);
  };
