import {first, flatMap} from 'lodash';
import {createSelector} from 'reselect';
import {identity, isDefined} from '../../helpers/commonHelpers';
import {Maybe} from '../../helpers/Maybe';
import {orUnknown} from '../../helpers/translations';
import {ErrorResponse, Identifiable, IdNamed, toIdNamed, uuid} from '../../types/Types';
import {CollectionStat, CollectionStatFacilityState} from '../domain-models/collection-stat/collectionStatModels';
import {Pagination, PaginationState} from '../ui/pagination/paginationModels';
import {paginationPageSize} from '../ui/pagination/paginationReducer';
import {BatchReference, BatchReferencesState} from './batch-references/batchReferenceModels';
import {Device, DevicesState} from './devices/deviceModels';
import {Meter, MetersState} from './meter/meterModels';
import {NormalizedPaginatedState} from './paginatedDomainModels';

export interface PageState<T extends Identifiable> {
  page: number;
  state: NormalizedPaginatedState<T>;
}

export const getPageIsFetching =
  <T extends Identifiable>({result}: NormalizedPaginatedState<T>, page: number = 0): boolean =>
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
  const {page}: Pagination = pagination.meters;
  return getPageIsFetching(meters, page);
};

interface ArrayFillProps {
  page: number;
  fillSize: number;
}

export const fillWithNull = ({page, fillSize}: ArrayFillProps): any[] =>
  page > 0 ? new Array<any>(page * fillSize).fill(null, 0, page * fillSize) : [];

const itemsCombiner = <T extends Identifiable>(
  state: NormalizedPaginatedState<T>,
  mapper: (value: T, index: number, array: T[]) => T = identity
): T[] => {
  const pageNumbers: string[] = Object.keys(state.result);
  const ids: uuid[] = pageNumbers.map(page => state.result[page].result).filter(isDefined);
  const items = flatMap(ids)
    .map(id => state.entities[id])
    .map(mapper);

  const page = Number(pageNumbers[0]);
  return page === 0
    ? items
    : [...fillWithNull({page, fillSize: paginationPageSize}), ...items];
};

const meterMapper = (meter: Meter): Meter => ({
  ...meter,
  location: {
    ...meter.location,
    city: orUnknown(meter.location.city),
    address: orUnknown(meter.location.address),
  },
  manufacturer: orUnknown(meter.manufacturer),
});

export const getAllMeters = createSelector<MetersState, MetersState, Meter[]>(
  identity,
  state => itemsCombiner(state, meterMapper)
);

export const getCollectionStats =
  createSelector<CollectionStatFacilityState, CollectionStatFacilityState, CollectionStat[]>(
    identity,
    itemsCombiner
  );

export const getBatchReferences =
  createSelector<BatchReferencesState, BatchReferencesState, BatchReference[]>(
    identity,
    itemsCombiner
  );

export const getDevices =
  createSelector<DevicesState, DevicesState, Device[]>(
    identity,
    itemsCombiner
  );

export const getSelectableDevices =
  createSelector<DevicesState, DevicesState, IdNamed[]>(
    identity,
    state => itemsCombiner(state).map(it => toIdNamed(it.id as string))
  );
