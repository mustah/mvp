import {createSelector} from 'reselect';
import {getTranslationOrName} from '../../../services/translationService';
import {encodedUriParametersForGateways, encodedUriParametersForMeters} from '../../../services/urlFactory';
import {IdNamed, Period, uuid} from '../../../types/Types';
import {DomainModel, Normalized, SelectionEntity} from '../../domain-models/domainModels';
import {getResultDomainModels} from '../../domain-models/domainModelsSelectors';
import {Meter, MetersState} from '../../domain-models/meter/meterModels';
import {getMeterEntities} from '../../domain-models/meter/meterSelectors';
import {SearchParameterState} from '../searchParameterReducer';
import {
  LookupState,
  ParameterName,
  SelectedParameters,
  SelectionListItem,
  SelectionState,
  SelectionSummary,
} from './selectionModels';
import {initialState} from './selectionReducer';

const getSelectedIds = (state: LookupState): SelectedParameters => state.selection.selected;

const getSelectionGroup = (entityType: string) =>
  (state: LookupState): Normalized<SelectionEntity> => state.domainModels[entityType];

const getSelectedEntityIdsSelector = (entityType: string): any =>
  createSelector<LookupState, SelectedParameters, uuid[]>(
    getSelectedIds,
    (selectedParameters: SelectedParameters) => selectedParameters[entityType],
  );

const arrayDiff = <T>(superSet: T[], subSet: T[]): T[] => superSet.filter(a => !subSet.includes(a));

const deselectedIdsSelector = (entityType: string): any =>
  createSelector<LookupState, Normalized<SelectionEntity>, SelectedParameters, uuid[]>(
    getSelectionGroup(entityType),
    getSelectedIds,
    ({result}: Normalized<SelectionEntity>, selected: SelectedParameters) => arrayDiff(result, selected[entityType]),
  );

const getDeselectedEntities = (entityType: string): any =>
  createSelector<LookupState, uuid[], Normalized<SelectionEntity>, SelectionEntity[]>(
    deselectedIdsSelector(entityType),
    getSelectionGroup(entityType),
    (ids: uuid[], {entities}: Normalized<SelectionEntity>) => ids.map(id => entities[id]),
  );

const getSelectedEntities = (entityType: string): any =>
  createSelector<LookupState, uuid[], Normalized<SelectionEntity>, SelectionEntity[]>(
    getSelectedEntityIdsSelector(entityType),
    getSelectionGroup(entityType),
    (ids: uuid[], {entities}: Normalized<SelectionEntity>) =>
      ids.map((id: uuid) => entities[id]).filter((item) => item),
  );

export const getCitiesSelection = getSelectionGroup(ParameterName.cities);

const getList = (entityType: ParameterName): any =>
  createSelector<LookupState, SelectionEntity[], SelectionEntity[], SelectionListItem[] | null[]>(
    getSelectedEntities(entityType),
    getDeselectedEntities(entityType),
    (selected: SelectionEntity[], deselected: SelectionEntity[]) => {
      const selectedEntities = selected.sort(entitySort).map(({id, name, ...extra}: SelectionEntity) =>
        ({id, name: getTranslationOrName({id, name}, entityType), ...extra, selected: true}));
      const deselectedEntities = deselected.sort(entitySort).map(({id, name, ...extra}: SelectionEntity) =>
        ({id, name: getTranslationOrName({id, name}, entityType), ...extra, selected: false}));
      return [...selectedEntities, ...deselectedEntities];
    },
  );
const entitySort = (objA: SelectionEntity, objB: SelectionEntity) =>
  (objA.name > objB.name) ? 1 : ((objB.name > objA.name) ? -1 : 0);

const getSelectedParameters = (state: SearchParameterState): SelectedParameters => state.selection.selected;

export const getCities = getList(ParameterName.cities);
export const getAddresses = getList(ParameterName.addresses);
export const getAlarms = getList(ParameterName.alarms);
export const getManufacturers = getList(ParameterName.manufacturers);
export const getProductModels = getList(ParameterName.productModels);
export const getMeterStatuses = getList(ParameterName.meterStatuses);
export const getGatewayStatuses = getList(ParameterName.gatewayStatuses);

export const getEncodedUriParametersForMeters = createSelector<SearchParameterState, SelectedParameters, string>(
  getSelectedParameters,
  encodedUriParametersForMeters,
);

export const getEncodedUriParametersForGateways = createSelector<SearchParameterState, SelectedParameters, string>(
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

export const getSelectionSummary = createSelector<MetersState, uuid[], DomainModel<Meter>, SelectionSummary>(
  getResultDomainModels,
  getMeterEntities,
  (metersList: uuid[], metersLookup: DomainModel<Meter>) => {
    const cities = new Set<uuid>();
    const addresses = new Set<uuid>();

    metersList.map((meterId: uuid) => {
        const {city, address} = metersLookup[meterId];
        cities.add(city.id);
        addresses.add(address.id);
      },
    );
    return ({
      cities: cities.size,
      addresses: addresses.size,
      meters: metersList.length,
    });
  },
);
