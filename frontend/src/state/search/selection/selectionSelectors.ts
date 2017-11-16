import {createSelector} from 'reselect';
import {encodedUriParametersFrom} from '../../../services/urlFactory';
import {IdNamed, Period, uuid} from '../../../types/Types';
import {Normalized, SelectionEntity} from '../../domain-models/domainModels';
import {getResultDomainModels} from '../../domain-models/domainModelsSelectors';
import {Meter, MetersState} from '../../domain-models/meter/meterModels';
import {getMeterEntities} from '../../domain-models/meter/meterSelectors';
import {SearchParameterState} from '../searchParameterReducer';
import {
  LookupState,
  parameterNames,
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

export const getCitiesSelection = getSelectionGroup(parameterNames.cities);

const getList = (entityType: string): any =>
  createSelector<LookupState, SelectionEntity[], SelectionEntity[], SelectionListItem[] | null[]>(
    getSelectedEntities(entityType),
    getDeselectedEntities(entityType),
    (selected: SelectionEntity[], deselected: SelectionEntity[]) => {
      const selectedEntities = selected.sort(entitySort).map((unit: IdNamed) => ({...unit, selected: true}));
      const deselectedEntities = deselected.sort(entitySort).map((unit: IdNamed) => ({...unit, selected: false}));
      return [...selectedEntities, ...deselectedEntities];
    },
  );
const entitySort = (objA: SelectionEntity, objB: SelectionEntity) =>
  (objA.name > objB.name) ? 1 : ((objB.name > objA.name) ? -1 : 0);

const getSelectedParameters = (state: SearchParameterState): SelectedParameters => state.selection.selected;

export const getCities = getList(parameterNames.cities);
export const getAddresses = getList(parameterNames.addresses);
export const getAlarms = getList(parameterNames.alarms);

export const getEncodedUriParameters = createSelector<SearchParameterState, SelectedParameters, string>(
  getSelectedParameters,
  encodedUriParametersFrom,
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

export const getSelectionSummary = createSelector<MetersState, uuid[], {[key: string]: Meter}, SelectionSummary>(
  getResultDomainModels,
  getMeterEntities,
  (metersList: uuid[], metersLookup: {[key: string]: Meter}) => {
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
