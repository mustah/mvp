import {createSelector} from 'reselect';
import {encodedUriParametersFrom} from '../../../services/urlFactory';
import {IdNamed, Period, uuid} from '../../../types/Types';
import {DomainModel} from '../../domain-models/geoData/geoDataModels';
import {SearchParameterState} from '../searchParameterReducer';
import {LookupState, parameterNames, SelectedParameters, SelectionState} from './selectionModels';

const getSelectedIds = (state: LookupState): SelectedParameters => state.selection.selected;

export const getSelection = (state: SearchParameterState): SelectionState => state.selection;

const getEntitiesSelector = (entityType: string): any =>
  createSelector<LookupState, DomainModel<IdNamed>, DomainModel<IdNamed>>(
    (state: LookupState) => state.repository[entityType].entities,
    (entities: DomainModel<IdNamed>) => entities,
  );

const getSelectedEntityIdsSelector = (entityType: string): any =>
  createSelector<LookupState, SelectedParameters, uuid[]>(
    getSelectedIds,
    (selectedParameters: SelectedParameters) => selectedParameters[entityType],
  );

const arrayDiff = <T>(superSet: T[], subSet: T[]): T[] => superSet.filter(a => !subSet.includes(a));

const deselectedIdsSelector = (entityType: string): any =>
  createSelector<LookupState, uuid[], SelectedParameters, uuid[]>(
    (state: LookupState) => state.repository[entityType].result,
    getSelectedIds,
    (result: uuid[], selected: SelectedParameters) => arrayDiff(result, selected[entityType]),
  );

const getDeselectedEntities = (entityType: string): any =>
  createSelector<LookupState, SelectionState, uuid[], DomainModel<IdNamed>, IdNamed[]>(
    deselectedIdsSelector(entityType),
    (state: LookupState) => state.repository[entityType].entities,
    (ids: uuid[], entities: DomainModel<IdNamed>) => ids.map(id => entities[id]),
  );

const getSelectedEntities = (entityType: string): any =>
  createSelector<LookupState, uuid[], DomainModel<IdNamed>, IdNamed[]>(
    getSelectedEntityIdsSelector(entityType),
    getEntitiesSelector(entityType),
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
