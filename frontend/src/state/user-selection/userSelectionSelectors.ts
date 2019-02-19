import {createSelector} from 'reselect';
import {DateRange, Period} from '../../components/dates/dateModels';
import {byNameAsc} from '../../helpers/comparators';
import {CurrentPeriod, toPeriodApiParameters} from '../../helpers/dateHelpers';
import {Maybe} from '../../helpers/Maybe';
import {getTranslationOrName} from '../../helpers/translations';
import {
  encodedUriParametersFrom, entityApiParametersCollectionStatFactory,
  EntityApiParametersFactory,
  entityApiParametersGatewaysFactory,
  entityApiParametersMetersFactory,
  toPaginationApiParameters,
  toThresholdParameter,
  toWildcardApiParameter,
} from '../../helpers/urlFactory';

import {EncodedUriParameters, uuid} from '../../types/Types';
import {ApiRequestSortingOptions, Pagination} from '../ui/pagination/paginationModels';
import {
  ParameterName,
  SelectedParameters,
  SelectionInterval,
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

export const getUserSelectionId = (state: UserSelectionState): uuid => state.userSelection.id;

const getCurrentPeriod = (state: UriLookupStatePaginated): CurrentPeriod => {
  const {start} = state;
  const {dateRange: {period, customDateRange}} = getSelectionParameters(state);
  return ({start, period, customDateRange: Maybe.maybe(customDateRange)});
};

const toSortParameters = (sort: ApiRequestSortingOptions[] | undefined): EncodedUriParameters[] =>
  sort
    ? sort.map(
    ({field, dir}: ApiRequestSortingOptions) =>
      `sort=${encodeURIComponent(field)}${dir ? ',' + dir : ''}`
    )
    : [];

const defaultPeriod: CurrentPeriod = {
  customDateRange: Maybe.nothing(),
  period: Period.latest,
};

const getPaginatedParameters = (toEntityParameters: EntityApiParametersFactory) =>
  createSelector<UriLookupStatePaginated,
    string,
    Pagination,
    ApiRequestSortingOptions[] | undefined,
    SelectedParameters,
    CurrentPeriod,
    EncodedUriParameters>(
    ({query}) => query!,
    ({pagination}) => pagination,
    ({sort}) => sort,
    getSelectionParameters,
    getCurrentPeriod,
    (query, pagination, sort, {dateRange, threshold, ...rest}, currentPeriod) => {
      const thresholdParameter = toThresholdParameter(threshold);
      const parametersToEncode = [
        ...toSortParameters(sort),
        ...toPeriodApiParameters(thresholdParameter.length ? currentPeriod : defaultPeriod),
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
        ...toPeriodApiParameters(thresholdParameter.length ? currentPeriod : defaultPeriod),
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

export const getPaginatedCollectionStatParameters = getPaginatedParameters(entityApiParametersCollectionStatFactory);

export const getPaginatedMeterParameters = getPaginatedParameters(entityApiParametersMetersFactory);

export const getPaginatedGatewayParameters = getPaginatedParameters(entityApiParametersGatewaysFactory);

export const getMeterParameters = getParameters(entityApiParametersMetersFactory);

export const allCurrentMeterParameters = encodedUriParametersFrom(toPeriodApiParameters({
  period: Period.latest,
  customDateRange: Maybe.nothing()
}));

export const getGatewayParameters = getParameters(entityApiParametersGatewaysFactory);

export const getCollectionStatParameters = getParameters(entityApiParametersCollectionStatFactory);

export const getSelectedPeriod =
  createSelector<UserSelection, SelectionInterval, {period: Period, customDateRange: Maybe<DateRange>}>(
    ({selectionParameters: {dateRange}}: UserSelection) => dateRange,
    ({period, customDateRange}: SelectionInterval) => ({
      period,
      customDateRange: Maybe.maybe(customDateRange),
    }),
  );

export const getUserSelection = (state: UserSelectionState): UserSelection => state.userSelection;
