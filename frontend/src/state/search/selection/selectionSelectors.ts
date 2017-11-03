import {createSelector} from 'reselect';
import {encodedUriParametersFrom} from '../../../services/urlFactory';
import {IdNamed, Period, uuid} from '../../../types/Types';
import {DomainModel, GeoDataState} from '../../domain-models/geoData/geoDataModels';
import {getGeoDataEntitiesBy, getGeoDataResultBy} from '../../domain-models/geoData/geoDataSelectors';
import {SearchParameterState} from '../searchParameterReducer';
import {LookupState, parameterNames, SelectedParameters, SelectionState} from './selectionModels';

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
    (ids: uuid[], entities: DomainModel<IdNamed>) => ids.map((id: uuid) => entities[id]),
  );

export const getDeselectedCities = getDeselectedEntities(parameterNames.cities);
export const getDeselectedAddresses = getDeselectedEntities(parameterNames.addresses);

export const getSelectedCities = getSelectedEntities(parameterNames.cities);
export const getSelectedAddresses = getSelectedEntities(parameterNames.addresses);

export const getEncodedUriParameters = createSelector<SearchParameterState, SelectedParameters, string>(
  (searchParameters: SearchParameterState) => searchParameters.selection.selected,
  encodedUriParametersFrom,
);

export const getSelectedPeriod = createSelector<SelectionState, SelectedParameters, Period>(
  (selection: SelectionState) => selection.selected,
  (selected: SelectedParameters) => selected.period! || Period.now,
);

export const getSelection = (state: SearchParameterState): SelectionState => state.selection;
