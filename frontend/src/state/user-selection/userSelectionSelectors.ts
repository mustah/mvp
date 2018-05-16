import {createSelector, OutputSelector} from 'reselect';
import {DateRange, Period} from '../../components/dates/dateModels';
import {CurrentPeriod, toPeriodApiParameters} from '../../helpers/dateHelpers';
import {Maybe} from '../../helpers/Maybe';
import {getTranslationOrName} from '../../helpers/translations';
import {
  encodedUriParametersFrom,
  toEntityApiParametersGateways,
  toEntityApiParametersMeters,
  toPaginationApiParameters,
} from '../../helpers/urlFactory';

import {EncodedUriParameters, uuid} from '../../types/Types';
import {DomainModel, SelectionEntity} from '../domain-models/domainModels';
import {Pagination} from '../ui/pagination/paginationModels';
import {
  LookupState,
  ParameterName,
  SelectedParameters,
  SelectionInterval,
  SelectionListItem,
  UserSelection,
  UserSelectionState,
} from './userSelectionModels';

const getSelectedIds = (state: LookupState): SelectedParameters =>
  state.userSelection.userSelection.selectionParameters;

const getSelectionGroup = (entityType: string) =>
  (state: LookupState): DomainModel<SelectionEntity> => state.domainModels[entityType];

const getSelectedEntityIdsSelector = (entityType: string) =>
  createSelector<LookupState, SelectedParameters, uuid[]>(
    getSelectedIds,
    (selectedParameters: SelectedParameters) => selectedParameters[entityType],
  );

const arrayDiff = <T>(
  superSet: T[],
  subSet: T[],
): T[] => superSet.filter((a) => !subSet.includes(a));

const getDiffCombiner = (entityType: string) => (
  {result}: DomainModel<SelectionEntity>,
  selected: SelectedParameters,
) => arrayDiff(
  result,
  selected[entityType] || '',
);

const deselectedIdsSelector = (entityType: string) =>
  createSelector<LookupState, DomainModel<SelectionEntity>, SelectedParameters, uuid[]>(
    getSelectionGroup(entityType),
    getSelectedIds,
    getDiffCombiner(entityType),
  );

const getDeselectedEntities = (entityType: string) =>
  createSelector<LookupState, uuid[], DomainModel<SelectionEntity>, SelectionEntity[]>(
    deselectedIdsSelector(entityType),
    getSelectionGroup(entityType),
    (ids: uuid[], {entities}: DomainModel<SelectionEntity>) => ids.map((id) => entities[id]),
  );

const getCombiner = (ids: uuid[] = [], {entities}: DomainModel<SelectionEntity>): any => {
  return ids
    .map((id: uuid) => entities[id])
    .filter((item) => item);
};

const getSelectedEntities = (entityType: string) =>
  createSelector<LookupState, uuid[], DomainModel<SelectionEntity>, SelectionEntity[]>(
    getSelectedEntityIdsSelector(entityType),
    getSelectionGroup(entityType),
    getCombiner,
  );

export const getCitiesSelection = getSelectionGroup(ParameterName.cities);

type ListResultCombiner = (
  selected: SelectionEntity[],
  deselected: SelectionEntity[],
) => SelectionListItem[];

type ListSelector = OutputSelector<LookupState, SelectionListItem[], ListResultCombiner>;

const getList = (entityType: ParameterName): ListSelector =>
  createSelector<LookupState, SelectionEntity[], SelectionEntity[], SelectionListItem[]>(
    getSelectedEntities(entityType),
    getDeselectedEntities(entityType),
    (selected: SelectionEntity[], deselected: SelectionEntity[]): SelectionListItem[] => {
      const selectedEntities: SelectionListItem[] =
        selected
          .map(({id, name, ...extra}: SelectionEntity) =>
            ({id, name: getTranslationOrName(name, entityType), ...extra, selected: true}))
          .sort(comparatorByNameAsc);

      const deselectedEntities: SelectionListItem[] =
        deselected
          .map(({id, name, ...extra}: SelectionEntity) =>
            ({id, name: getTranslationOrName(name, entityType), ...extra, selected: false}))
          .sort(comparatorByNameAsc);

      return [...selectedEntities, ...deselectedEntities];
    },
  );

const comparatorByNameAsc = (objA: SelectionEntity, objB: SelectionEntity) =>
  (objA.name > objB.name) ? 1 : ((objB.name > objA.name) ? -1 : 0);

export const getAddresses = getList(ParameterName.addresses);
export const getCities = getList(ParameterName.cities);
export const getGatewayStatuses = getList(ParameterName.gatewayStatuses);
export const getMedia = getList(ParameterName.media);
export const getMeterStatuses = getList(ParameterName.meterStatuses);

export interface UriLookupState extends UserSelectionState {
  now: Date;
}

export interface UriLookupStatePaginated extends UriLookupState {
  pagination: Pagination;
}

const getSelectedParameters = (state: UserSelectionState): SelectedParameters =>
  state.userSelection.selectionParameters;

const getCurrentPeriod = (state: UriLookupStatePaginated): CurrentPeriod => {
  const {now} = state;
  const {dateRange: {period, customDateRange}} = getSelectedParameters(state);
  return ({now, period, customDateRange: Maybe.maybe(customDateRange)});
};

export const getPaginatedMeterParameters =
  createSelector<UriLookupStatePaginated, Pagination, SelectedParameters, CurrentPeriod, EncodedUriParameters>(
    ({pagination}) => pagination,
    getSelectedParameters,
    getCurrentPeriod,
    (pagination, {dateRange, ...rest}, currentPeriod) => {
      return encodedUriParametersFrom([
        ...toEntityApiParametersMeters(rest),
        ...toPeriodApiParameters(currentPeriod),
        ...toPaginationApiParameters(pagination),
      ]);
    },
  );

export const getPaginatedGatewayParameters =
  createSelector<UriLookupStatePaginated, Pagination, SelectedParameters, CurrentPeriod, EncodedUriParameters>(
    ({pagination}) => pagination,
    getSelectedParameters,
    getCurrentPeriod,
    (pagination, {dateRange, ...rest}, currentPeriod) => {
      return encodedUriParametersFrom([
        ...toEntityApiParametersGateways(rest),
        ...toPeriodApiParameters(currentPeriod),
        ...toPaginationApiParameters(pagination),
      ]);
    },
  );

export const getMeterParameters =
  createSelector<UriLookupState, SelectedParameters, CurrentPeriod, EncodedUriParameters>(
    getSelectedParameters,
    getCurrentPeriod,
    ({dateRange, ...rest}, currentPeriod) => {
      return encodedUriParametersFrom([
        ...toEntityApiParametersMeters(rest),
        ...toPeriodApiParameters(currentPeriod),
      ]);
    },
  );

export const getGatewayParameters =
  createSelector<UriLookupState, SelectedParameters, CurrentPeriod, EncodedUriParameters>(
    getSelectedParameters,
    getCurrentPeriod,
    ({dateRange, ...rest}, currentPeriod) => {
      return encodedUriParametersFrom([
        ...toEntityApiParametersGateways(rest),
        ...toPeriodApiParameters(currentPeriod),
      ]);
    },
  );

export const getSelectedPeriod =
  createSelector<UserSelection, SelectionInterval, {period: Period, customDateRange: Maybe<DateRange>}>(
    ({selectionParameters: {dateRange}}: UserSelection) => dateRange,
    ({period, customDateRange}: SelectionInterval) => ({
      period,
      customDateRange: Maybe.maybe(customDateRange),
    }),
  );

export const getSelection = (state: UserSelectionState): UserSelection => state.userSelection;
