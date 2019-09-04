import {createSelector} from 'reselect';
import {Period} from '../../components/dates/dateModels';
import {identity} from '../../helpers/commonHelpers';
import {byNameAsc} from '../../helpers/comparators';
import {CurrentPeriod, toPeriodApiParameters} from '../../helpers/dateHelpers';
import {Maybe} from '../../helpers/Maybe';
import {getTranslationOrName} from '../../helpers/translations';
import {
  encodedUriParametersFrom,
  EntityApiParametersFactory,
  makeApiParameters,
  makeCollectionPeriodParametersOf,
  makeMeterApiParameters,
  toPaginationApiParameters,
  toThresholdParameter,
  toWildcardApiParameter,
} from '../../helpers/urlFactory';
import {RootState} from '../../reducers/rootReducer';

import {EncodedUriParameters, uuid} from '../../types/Types';
import {Pagination, SortOption} from '../ui/pagination/paginationModels';
import {
  ParameterName,
  SelectedParameters,
  SelectionItem,
  SelectionListItem,
  ThresholdQuery,
  UriLookupState,
  UriLookupStatePaginated,
  UserSelection,
  UserSelectionState
} from './userSelectionModels';

const getSelectionParameters = (state: UserSelectionState): SelectedParameters =>
  state.userSelection.selectionParameters;

const sortAndTranslateItems =
  (
    items: SelectionItem[] = [],
    entityType: ParameterName,
    selected: boolean,
  ): SelectionListItem[] =>
    items.map(({id, name, ...extra}: SelectionItem) =>
      ({id, name: getTranslationOrName(name, entityType), ...extra, selected}))
      .sort(byNameAsc);

const selectedEntitiesCombiner = (entityType: ParameterName) =>
  (selected: SelectionItem[]): SelectionListItem[] =>
    [...sortAndTranslateItems(selected, entityType, true)];

const selectionCombiner = (entityType: ParameterName) =>
  (selectedParameters: SelectedParameters): SelectionListItem[] =>
    selectedEntitiesCombiner(entityType)(selectedParameters[entityType]);

const getSelectedItems = (entityType: ParameterName) =>
  createSelector<UserSelectionState, SelectedParameters, SelectionListItem[]>(
    getSelectionParameters,
    selectionCombiner(entityType),
  );

export const getSelectedAlarms = getSelectedItems(ParameterName.alarms);
export const getSelectedMedia = getSelectedItems(ParameterName.media);
export const getSelectedOrganisations = getSelectedItems(ParameterName.organisations);
export const getSelectedReported = getSelectedItems(ParameterName.reported);
export const getSelectedCities = getSelectedItems(ParameterName.cities);
export const getSelectedAddresses = getSelectedItems(ParameterName.addresses);
export const getSelectedFacilities = getSelectedItems(ParameterName.facilities);
export const getSelectedSecondaryAddresses = getSelectedItems(ParameterName.secondaryAddresses);
export const getSelectedGatewaySerials = getSelectedItems(ParameterName.gatewaySerials);

export const getThreshold = (state: UserSelectionState): ThresholdQuery | undefined =>
  getSelectionParameters(state).threshold;

export const getUserSelection = (state: UserSelectionState): UserSelection => state.userSelection;

export const getUserSelectionId = (state: UserSelectionState): uuid => getUserSelection(state).id;

const getCurrentPeriod = (state: UriLookupStatePaginated): CurrentPeriod => {
  const {start} = state;
  const {dateRange} = getSelectionParameters(state);
  if (dateRange) {
    const {period, customDateRange} = dateRange;
    return ({start, period, customDateRange: Maybe.maybe(customDateRange)});
  } else {
    return ({period: Period.now, customDateRange: Maybe.nothing()});
  }
};

const toSortParameters = (sort: SortOption[] | undefined): EncodedUriParameters[] =>
  sort
    ? sort.map(
    ({field, dir}: SortOption) =>
      `sort=${encodeURIComponent(field)}${dir ? ',' + dir : ''}`
    )
    : [];

const toLimitParameter = (limit?: number): EncodedUriParameters[] =>
  Maybe.maybe(limit).map(it => [`limit=${it}`]).orElse([]);

const defaultPeriod: CurrentPeriod = {
  customDateRange: Maybe.nothing(),
  period: Period.now,
};

const determineActivePeriod = (
  hasWildcard: boolean,
  selectionPeriod: CurrentPeriod,
  threshold?: ThresholdQuery
): CurrentPeriod => {
  if (hasWildcard && selectionPeriod) {
    return selectionPeriod;
  }

  if (threshold && threshold.dateRange) {
    if (threshold.dateRange.customDateRange || threshold.dateRange.period === Period.custom) {
      return {period: Period.custom, customDateRange: Maybe.maybe(threshold.dateRange.customDateRange)};
    } else {
      return {period: threshold.dateRange.period, customDateRange: Maybe.nothing()};
    }
  }

  return defaultPeriod;
};

interface Parameters {
  limit: number;
  query: string;
}

const getPaginatedParameters = (toEntityParameters: EntityApiParametersFactory) =>
  createSelector<UriLookupStatePaginated,
    Parameters,
    Pagination,
    SortOption[] | undefined,
    SelectedParameters,
    CurrentPeriod,
    EncodedUriParameters>(
    ({limit, query}) => ({query: query!, limit: limit!}),
    ({pagination}) => pagination,
    ({sort}) => sort,
    getSelectionParameters,
    getCurrentPeriod,
    ({limit, query}, pagination, sort, {dateRange, threshold, ...rest}, currentPeriod) => {
      const thresholdParameter = toThresholdParameter(threshold);
      const parametersToEncode = [
        ...toSortParameters(sort),
        ...toLimitParameter(limit),
        ...toPeriodApiParameters(determineActivePeriod(query !== undefined, currentPeriod, threshold)),
        ...toPaginationApiParameters(pagination),
      ];
      return query
        ? encodedUriParametersFrom([
          ...parametersToEncode,
          ...toWildcardApiParameter(query)
        ])
        : encodedUriParametersFrom([
          ...thresholdParameter,
          ...toEntityParameters(rest),
          ...parametersToEncode,
        ]);
    },
  );

const getParameters = (toEntityParameters: EntityApiParametersFactory) =>
  createSelector<UriLookupState, string, SelectedParameters, CurrentPeriod, EncodedUriParameters>(
    ({query}) => query!,
    getSelectionParameters,
    getCurrentPeriod,
    (query, {dateRange, threshold, ...rest}, currentPeriod) => {
      const thresholdParameter = toThresholdParameter(threshold);
      const parametersToEncode = [
        ...toPeriodApiParameters(determineActivePeriod(query !== undefined, currentPeriod, threshold)),
      ];
      return query
        ? encodedUriParametersFrom([
          ...toWildcardApiParameter(query),
          ...parametersToEncode,
        ])
        : encodedUriParametersFrom([
          ...thresholdParameter,
          ...toEntityParameters(rest),
          ...parametersToEncode,
        ]);
    },
  );

export const allCurrentMeterParameters = encodedUriParametersFrom(toPeriodApiParameters(defaultPeriod));

export const getPaginatedMeterParameters = getPaginatedParameters(makeMeterApiParameters);

export const getMeterParameters = getParameters(makeMeterApiParameters);

export const getPaginatedApiParameters = getPaginatedParameters(makeApiParameters);

export const getApiParameters = getParameters(makeApiParameters);

export const getBatchReferencesParameters = createSelector<RootState, RootState, EncodedUriParameters>(
  identity,
  ({
    paginatedDomainModels: {batchReferences: {sort}},
    ui: {pagination: {batchReferences: pagination}},
  }: RootState) =>
    encodedUriParametersFrom([
      ...toPaginationApiParameters(pagination),
      ...toSortParameters(sort),
    ])
);

export const getCollectionStatsParameters = createSelector<RootState, RootState, EncodedUriParameters>(
  identity,
  ({
    collection: {timePeriod},
    paginatedDomainModels: {collectionStatFacilities: {sort}},
    search: {validation: {query}},
    ui: {pagination: {collectionStatFacilities: pagination}},
    userSelection: {userSelection}
  }: RootState) =>
    `${makeCollectionPeriodParametersOf(timePeriod)}&${getPaginatedApiParameters({
      sort,
      pagination,
      userSelection,
      query,
    })}`
);

export const getCollectionStatsExcelExportParameters = createSelector<RootState, RootState, EncodedUriParameters>(
  identity,
  ({
    collection: {timePeriod},
    paginatedDomainModels: {collectionStatFacilities: {sort}},
    search: {validation: {query}},
    ui: {pagination: {collectionStatFacilities: pagination}},
    userSelection: {userSelection}
  }: RootState) =>
    `${makeCollectionPeriodParametersOf(timePeriod)}&${getPaginatedApiParameters({
      limit: pagination.totalElements,
      pagination,
      query,
      userSelection,
      sort,
    })}`
);
