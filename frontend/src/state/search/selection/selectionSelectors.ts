import {createSelector} from 'reselect';
import {encodedUriParametersFrom} from '../../../services/urlFactory';
import {IdNamed, Period, uuid} from '../../../types/Types';
import {SearchParameterState} from '../searchParameterReducer';
import {parameterNames, SelectedParameters, SelectionEntity} from './selectionModels';
import {SelectionState} from './selectionReducer';

const getEntities = (state: SelectionState): SelectionEntity => state.entities;
const getSelected = (state: SelectionState): SelectedParameters => state.selected;
const getResult = (state: SelectionState): SelectedParameters => state.result;

const getEntitiesSelector = (entityType: string): any =>
  createSelector<SelectionState, SelectionEntity, IdNamed>(
    getEntities,
    (entities: SelectionEntity) => entities[entityType],
  );

const getSelectedEntityIdsSelector = (entityType: string): any =>
  createSelector<SelectionState, SelectedParameters, uuid[]>(
    getSelected,
    (searchResult: SelectedParameters) => searchResult[entityType],
  );

const arrayDiff = <T>(superSet: T[], subSet: T[]): T[] => superSet.filter(a => !subSet.includes(a));

const getDeselectedEntityIdsSelector = (entityType: string): any =>
  createSelector<SelectionState, SelectedParameters, SelectedParameters, uuid[]>(
    getResult,
    getSelected,
    (result: SelectedParameters, selected: SelectedParameters) => arrayDiff(result[entityType], selected[entityType]),
  );

const getDeselectedEntities = (entityType: string): any =>
  createSelector<SelectionState, uuid[], SelectionEntity, IdNamed[]>(
    getDeselectedEntityIdsSelector(entityType),
    getEntitiesSelector(entityType),
    (ids: uuid[], entities: SelectionEntity) => ids.map((id) => entities[id]),
  );

const getSelectedEntities = (entityType: string): any =>
  createSelector<SelectionState, uuid[], SelectionEntity, IdNamed[]>(
    getSelectedEntityIdsSelector(entityType),
    getEntitiesSelector(entityType),
    (ids: uuid[], entities: SelectionEntity) => ids.map((id) => entities[id]),
  );

export const getSelection = (state: SearchParameterState): SelectionState => state.selection;

export const isFetching = createSelector<SearchParameterState, SelectionState, boolean>(
  getSelection,
  selection => selection.isFetching,
);

export const getDeselectedCities = getDeselectedEntities(parameterNames.cities);
export const getSelectedCities = getSelectedEntities(parameterNames.cities);
export const getDeselectedAddresses = getDeselectedEntities(parameterNames.addresses);
export const getSelectedAddresses = getSelectedEntities(parameterNames.addresses);

export const getEncodedUriParameters = createSelector<SearchParameterState, SelectedParameters, string>(
  (searchParameters: SearchParameterState) => searchParameters.selection.selected,
  encodedUriParametersFrom,
);

export const getSelectedPeriod = createSelector<SelectionState, SelectedParameters, Period>(
  getSelected,
  (selected: SelectedParameters) => selected.period! || Period.now,
);
