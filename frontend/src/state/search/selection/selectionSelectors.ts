import {createSelector} from 'reselect';
import {encodedUriParametersFrom} from '../../../services/urlFactory';
import {IdNamed, Period, uuid} from '../../../types/Types';
import {getResultDomainModels} from '../../domain-models/domainModelsSelectors';
import {DomainModel, GeoDataState} from '../../domain-models/geoData/geoDataModels';
import {getGeoDataEntitiesBy, getGeoDataResultBy} from '../../domain-models/geoData/geoDataSelectors';
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

const getGeoData = (state: LookupState): GeoDataState => state.geoData;

const entitiesSelector = (entityType: string): any =>
  createSelector<LookupState, GeoDataState, DomainModel<IdNamed>>(
    getGeoData,
    getGeoDataEntitiesBy(entityType),
  );

const resultSelector = (entityType: string): any =>
  createSelector<LookupState, GeoDataState, uuid[]>(
    getGeoData,
    getGeoDataResultBy(entityType),
  );

const getSelectedEntityIdsSelector = (entityType: string): any =>
  createSelector<LookupState, SelectedParameters, uuid[]>(
    getSelectedIds,
    (selectedParameters: SelectedParameters) => selectedParameters[entityType],
  );

const arrayDiff = <T>(superSet: T[], subSet: T[]): T[] => superSet.filter(a => !subSet.includes(a));

const deselectedIdsSelector = (entityType: string): any =>
  createSelector<LookupState, GeoDataState, uuid[], SelectedParameters, uuid[]>(
    resultSelector(entityType),
    getSelectedIds,
    (result: uuid[], selected: SelectedParameters) => arrayDiff(result, selected[entityType]),
  );

const getDeselectedEntities = (entityType: string): any =>
  createSelector<LookupState, SelectionState, uuid[], DomainModel<IdNamed>, IdNamed[]>(
    deselectedIdsSelector(entityType),
    entitiesSelector(entityType),
    (ids: uuid[], entities: DomainModel<IdNamed>) => ids.map(id => entities[id]),
  );

const getSelectedEntities = (entityType: string): any =>
  createSelector<LookupState, uuid[], DomainModel<IdNamed>, IdNamed[]>(
    getSelectedEntityIdsSelector(entityType),
    entitiesSelector(entityType),
    (ids: uuid[], entities: DomainModel<IdNamed>) => ids.map((id: uuid) => entities[id]).filter((item) => item),
  );

export const getCitiesSelection = entitiesSelector(parameterNames.cities);

// TODO: Handle typing, that getAddresses return Address[] and not IdNamed[]
const getList = (entityType: string): any =>
  createSelector<LookupState, IdNamed[], IdNamed[], SelectionListItem[] | null[]>(
    getSelectedEntities(entityType),
    getDeselectedEntities(entityType),
    (selected: IdNamed[], deselected: IdNamed[]) => {
      const selectedEntities = selected.sort(entitySort).map((unit: IdNamed) => ({...unit, selected: true}));
      const deselectedEntities = deselected.sort(entitySort).map((unit: IdNamed) => ({...unit, selected: false}));
      return [...selectedEntities, ...deselectedEntities];
    },
  );
const entitySort = (objA: IdNamed, objB: IdNamed) => (objA.name > objB.name) ? 1 : ((objB.name > objA.name) ? -1 : 0);

const getSelectedParameters = (state: SearchParameterState): SelectedParameters => state.selection.selected;

export const getCities = getList(parameterNames.cities);
export const getAddresses = getList(parameterNames.addresses);

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
    const meters = new Set<uuid>();

    metersList.map((meterId: uuid) => {
        const {city, address} = metersLookup[meterId];
        cities.add(city.id);
        addresses.add(address.id);
        meters.add(meterId);
      },
    );
    return ({
      cities: cities.size,
      addresses: addresses.size,
      meters: meters.size,
    });
  },
);
