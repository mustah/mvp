import {createSelector, OutputSelector} from 'reselect';
import {Period} from '../../components/dates/dateModels';
import {getTranslationOrName} from '../../helpers/translations';
import {
  encodedUriParametersForAllGateways,
  encodedUriParametersForAllMeters,
  encodedUriParametersForGateways,
  encodedUriParametersForMeters,
  PaginatedParametersCombiner,
  ParameterCallbacks,
} from '../../helpers/urlFactory';
import {EncodedUriParameters, uuid} from '../../types/Types';
import {DomainModel, SelectionEntity} from '../domain-models/domainModels';
import {Pagination} from '../ui/pagination/paginationModels';
import {
  LookupState,
  ParameterName,
  SelectedParameters,
  SelectionListItem,
  UserSelection,
  UserSelectionState
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

const deselectedIdsSelector = (entityType: string) =>
  createSelector<LookupState, DomainModel<SelectionEntity>, SelectedParameters, uuid[]>(
    getSelectionGroup(entityType),
    getSelectedIds,
    ({result}: DomainModel<SelectionEntity>, selected: SelectedParameters) => arrayDiff(
      result,
      selected[entityType],
    ),
  );

const getDeselectedEntities = (entityType: string) =>
  createSelector<LookupState, uuid[], DomainModel<SelectionEntity>, SelectionEntity[]>(
    deselectedIdsSelector(entityType),
    getSelectionGroup(entityType),
    (ids: uuid[], {entities}: DomainModel<SelectionEntity>) => ids.map((id) => entities[id]),
  );

const getSelectedEntities = (entityType: string) =>
  createSelector<LookupState, uuid[], DomainModel<SelectionEntity>, SelectionEntity[]>(
    getSelectedEntityIdsSelector(entityType),
    getSelectionGroup(entityType),
    (ids: uuid[], {entities}: DomainModel<SelectionEntity>) =>
      ids.map((id: uuid) => entities[id]).filter((item) => item),
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
          .sort(comparatorByNameAsc)
          .map(({id, name, ...extra}: SelectionEntity) =>
            ({id, name: getTranslationOrName(name, entityType), ...extra, selected: true}));

      const deselectedEntities: SelectionListItem[] =
        deselected
          .sort(comparatorByNameAsc)
          .map(({id, name, ...extra}: SelectionEntity) =>
            ({id, name: getTranslationOrName(name, entityType), ...extra, selected: false}));

      return [...selectedEntities, ...deselectedEntities];
    },
  );

const comparatorByNameAsc = (objA: SelectionEntity, objB: SelectionEntity) =>
  (objA.name > objB.name) ? 1 : ((objB.name > objA.name) ? -1 : 0);

const getSelectedParameters = (state: UserSelectionState): SelectedParameters =>
  state.userSelection.selectionParameters;

export const getCities = getList(ParameterName.cities);
export const getAddresses = getList(ParameterName.addresses);
export const getAlarms = getList(ParameterName.alarms);
export const getMeterStatuses = getList(ParameterName.meterStatuses);
export const getGatewayStatuses = getList(ParameterName.gatewayStatuses);

export interface UriLookupStatePaginated extends UserSelectionState {
  pagination: Pagination;
}

export const composePaginatedCombiner =
  (combiner: PaginatedParametersCombiner, callbacks?: ParameterCallbacks) =>
    createSelector<UriLookupStatePaginated, Pagination, SelectedParameters, EncodedUriParameters>(
      ({pagination}: UriLookupStatePaginated) => pagination,
      getSelectedParameters,
      (pagination: Pagination, selectedParameters: SelectedParameters) =>
        combiner(pagination, selectedParameters, callbacks),
    );

export const getPaginatedMeterParameters =
  composePaginatedCombiner(encodedUriParametersForMeters);

export const getPaginatedGatewayParameters =
  composePaginatedCombiner(encodedUriParametersForGateways);

export const getMeterParameters =
  createSelector<UserSelectionState, SelectedParameters, EncodedUriParameters>(
    getSelectedParameters,
    encodedUriParametersForAllMeters,
  );

export const getGatewayParameters =
  createSelector<UserSelectionState, SelectedParameters, EncodedUriParameters>(
    getSelectedParameters,
    encodedUriParametersForAllGateways,
  );

export const getSelectedPeriod = createSelector<UserSelection, SelectedParameters, Period>(
  (selection: UserSelection) => selection.selectionParameters,
  (selected: SelectedParameters) => selected.period,
);

export const getSelection = (state: UserSelectionState): UserSelection => state.userSelection;
