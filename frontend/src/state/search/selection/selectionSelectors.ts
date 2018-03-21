import {createSelector, OutputSelector} from 'reselect';
import {Period} from '../../../components/dates/dateModels';
import {getTranslationOrName} from '../../../helpers/translations';
import {
  encodedUriParametersForAllMeters,
  encodedUriParametersForGateways,
  encodedUriParametersForMeters,
} from '../../../helpers/urlFactory';
import {IdNamed, uuid} from '../../../types/Types';
import {PaginatedDomainModelsState} from '../../domain-models-paginated/paginatedDomainModels';
import {DomainModel, SelectionEntity} from '../../domain-models/domainModels';
import {Pagination, PaginationLookupState} from '../../ui/pagination/paginationModels';
import {getPagination} from '../../ui/pagination/paginationSelectors';
import {SearchParameterState} from '../searchParameterReducer';
import {
  LookupState,
  ParameterName,
  SelectedParameters,
  SelectionListItem,
  SelectionState,
} from './selectionModels';
import {initialState} from './selectionReducer';

const getSelectedIds = (state: LookupState): SelectedParameters => state.selection.selected;

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
            ({id, name: getTranslationOrName({id, name}, entityType), ...extra, selected: true}));

      const deselectedEntities: SelectionListItem[] =
        deselected
          .sort(comparatorByNameAsc)
          .map(({id, name, ...extra}: SelectionEntity) =>
            ({id, name: getTranslationOrName({id, name}, entityType), ...extra, selected: false}));

      return [...selectedEntities, ...deselectedEntities];
    },
  );

const comparatorByNameAsc = (objA: SelectionEntity, objB: SelectionEntity) =>
  (objA.name > objB.name) ? 1 : ((objB.name > objA.name) ? -1 : 0);

const getSelectedParameters = (state: SearchParameterState): SelectedParameters => state.selection.selected;

export const getCities = getList(ParameterName.cities);
export const getAddresses = getList(ParameterName.addresses);
export const getAlarms = getList(ParameterName.alarms);
export const getMeterStatuses = getList(ParameterName.meterStatuses);
export const getGatewayStatuses = getList(ParameterName.gatewayStatuses);

export type UriLookupStatePaginated =
  SearchParameterState
  & PaginationLookupState<PaginatedDomainModelsState>;

export const getEncodedUriParametersForMeters =
  createSelector<UriLookupStatePaginated, Pagination, SelectedParameters, string>(
    getPagination,
    getSelectedParameters,
    encodedUriParametersForMeters,
  );

export const getEncodedUriParametersForAllMeters =
  createSelector<SearchParameterState, SelectedParameters, string>(
    getSelectedParameters,
    encodedUriParametersForAllMeters,
  );

export const getEncodedUriParametersForGateways =
  createSelector<SearchParameterState, SelectedParameters, string>(
    getSelectedParameters,
    encodedUriParametersForGateways,
  );

export const getSelectedPeriod = createSelector<SelectionState, SelectedParameters, Period>(
  (selection: SelectionState) => selection.selected,
  (selected: SelectedParameters) => selected.period! || initialState.selected.period,
);

export const getSavedSelections = createSelector<SearchParameterState, SelectionState[], IdNamed[]>(
  (state: SearchParameterState) => state.saved,
  (selectionState: SelectionState[]) => selectionState.map(({id, name}) => ({id, name})),
);

export const getSelection = (state: SearchParameterState): SelectionState => state.selection;
