import {first, flatMap} from 'lodash';
import {createSelector} from 'reselect';
import {identity, isDefined} from '../../helpers/commonHelpers';
import {Maybe} from '../../helpers/Maybe';
import {orUnknown} from '../../helpers/translations';
import {ErrorResponse, Identifiable, uuid} from '../../types/Types';
import {EntityTypes, Pagination, PaginationState} from '../ui/pagination/paginationModels';
import {paginationPageSize} from '../ui/pagination/paginationReducer';
import {getPagination} from '../ui/pagination/paginationSelectors';
import {Meter, MetersState} from './meter/meterModels';
import {NormalizedPaginatedState} from './paginatedDomainModels';

export interface PageState<T extends Identifiable> {
  page: number;
  state: NormalizedPaginatedState<T>;
}

export const getPaginatedResult =
  <T extends Identifiable>({result}: NormalizedPaginatedState<T>, page: number): uuid[] =>
    result[page] && result[page].result || [];

export const getPageIsFetching =
  <T extends Identifiable>({result}: NormalizedPaginatedState<T>, page: number): boolean =>
    result[page] ? result[page].isFetching : false;

export const getPageError =
  createSelector<PageState<Identifiable>, PageState<Identifiable>, Maybe<ErrorResponse>>(
    identity,
    ({state: {result}, page}): Maybe<ErrorResponse> => Maybe.maybe(result[page]).flatMap(it => Maybe.maybe(it.error))
  );

export const getFirstPageError =
  createSelector<NormalizedPaginatedState<Identifiable>, NormalizedPaginatedState<Identifiable>, Maybe<ErrorResponse>>(
    identity,
    ({result}): Maybe<ErrorResponse> => Maybe.maybe(
      first(Object.keys(result).map(page => result[page]).filter(isDefined).map(it => it.error).filter(isDefined))
    )
  );

export const isMetersPageFetching = (meters: MetersState, pagination: PaginationState): boolean => {
  const entityType: EntityTypes = 'meters';
  const {page}: Pagination = getPagination({entityType, pagination});
  return getPageIsFetching(meters, page);
};

interface ArrayFillProps {
  page: number;
  fillSize: number;
}

export const fillWithNull = ({page, fillSize}: ArrayFillProps): any[] =>
  page > 0 ? new Array<any>(page * fillSize).fill(null, 0, page * fillSize) : [];

export const getAllMeters = createSelector<MetersState, MetersState, Meter[]>(
  identity,
  meters => {
    const pageNumbers: string[] = Object.keys(meters.result);
    const ids: uuid[] = pageNumbers.map(page => meters.result[page].result).filter(isDefined);
    const items = flatMap(ids)
      .map(id => meters.entities[id])
      .map(it => ({
        ...it,
        location: {
          ...it.location,
          city: orUnknown(it.location.city),
          address: orUnknown(it.location.address),
        },
        manufacturer: orUnknown(it.manufacturer),
      }));
    const currentPage = Number(pageNumbers[0]);
    return currentPage === 0
      ? items
      : [...fillWithNull({page: currentPage, fillSize: paginationPageSize}), ...items];
  }
);
