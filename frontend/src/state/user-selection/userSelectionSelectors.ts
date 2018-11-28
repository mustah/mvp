import {createSelector} from 'reselect';
import {DateRange, Period} from '../../components/dates/dateModels';
import {byNameAsc} from '../../helpers/comparators';
import {CurrentPeriod, toPeriodApiParameters} from '../../helpers/dateHelpers';
import {Maybe} from '../../helpers/Maybe';
import {getTranslationOrName} from '../../helpers/translations';
import {
  encodedUriParametersFrom,
  EntityApiParametersFactory,
  toEntityApiParametersGateways,
  toEntityApiParametersMeters,
  toPaginationApiParameters,
  toQueryApiParameters,
  toThresholdParameter,
} from '../../helpers/urlFactory';

import {EncodedUriParameters} from '../../types/Types';
import {Pagination} from '../ui/pagination/paginationModels';
import {
  ParameterName,
  SelectedParameters,
  SelectionInterval,
  SelectionItem,
  SelectionListItem,
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

const getCurrentPeriod = (state: UriLookupStatePaginated): CurrentPeriod => {
  const {start} = state;
  const {dateRange: {period, customDateRange}} = getSelectionParameters(state);
  return ({start, period, customDateRange: Maybe.maybe(customDateRange)});
};

const getPaginatedParameters = (toEntityParameters: EntityApiParametersFactory) =>
  createSelector<UriLookupStatePaginated, string, Pagination, SelectedParameters, CurrentPeriod, EncodedUriParameters>(
    ({query}) => query!,
    ({pagination}) => pagination,
    getSelectionParameters,
    getCurrentPeriod,
    (query, pagination, {dateRange, threshold, ...rest}, currentPeriod) =>
      encodedUriParametersFrom([
        ...toThresholdParameter(threshold),
        ...toEntityParameters(rest),
        ...toPeriodApiParameters(currentPeriod),
        ...toPaginationApiParameters(pagination),
        ...toQueryApiParameters(query),
      ]),
  );

const getParameters = (toEntityParameters: EntityApiParametersFactory) =>
  createSelector<UriLookupState, string, SelectedParameters, CurrentPeriod, EncodedUriParameters>(
    ({query}) => query!,
    getSelectionParameters,
    getCurrentPeriod,
    (query, {dateRange, threshold, ...rest}, currentPeriod) =>
      encodedUriParametersFrom([
        ...toThresholdParameter(threshold),
        ...toEntityParameters(rest),
        ...toPeriodApiParameters(currentPeriod),
        ...toQueryApiParameters(query),
      ]),
  );

export const getPaginatedMeterParameters = getPaginatedParameters(toEntityApiParametersMeters);

export const getPaginatedGatewayParameters = getPaginatedParameters(toEntityApiParametersGateways);

export const getMeterParameters = getParameters(toEntityApiParametersMeters);

export const getGatewayParameters = getParameters(toEntityApiParametersGateways);

export const getSelectedPeriod =
  createSelector<UserSelection, SelectionInterval, {period: Period, customDateRange: Maybe<DateRange>}>(
    ({selectionParameters: {dateRange}}: UserSelection) => dateRange,
    ({period, customDateRange}: SelectionInterval) => ({
      period,
      customDateRange: Maybe.maybe(customDateRange),
    }),
  );

export const getUserSelection = (state: UserSelectionState): UserSelection => state.userSelection;
