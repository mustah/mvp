import {createSelector} from 'reselect';
import {IdNamed, uuid} from '../../../types/Types';
import {SearchParameterState, SelectedIds, SelectionEntity} from './selectionModels';
import {SelectionState} from './selectionReducer';

const getEntities = (state: SelectionState): SelectionEntity => state.entities;
const getSelected = (state: SelectionState): SelectedIds => state.selected;
const getResult = (state: SelectionState): SelectedIds => state.result;

const getEntitiesSelector = (entityType: string): any =>
  createSelector<SelectionState, SelectionEntity, IdNamed>(
    getEntities,
    (entities: SelectionEntity) => entities[entityType],
  );

const getSelectedEntityIdsSelector = (entityType: string): any =>
  createSelector<SelectionState, SelectedIds, uuid[]>(
    getSelected,
    (searchResult: SelectedIds) => searchResult[entityType],
  );

const arrayDiff = <T>(superSet: T[], subSet: T[]): T[] => superSet.filter(a => !subSet.includes(a));

const getDeselectedEntityIdsSelector = (entityType: string): any =>
  createSelector<SelectionState, SelectedIds, SelectedIds, uuid[]>(
    getResult,
    getSelected,
    (result: SelectedIds, selected: SelectedIds) => arrayDiff(result[entityType], selected[entityType]),
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

export const getDeselectedCities = getDeselectedEntities('cities');
export const getSelectedCities = getSelectedEntities('cities');
export const getDeselectedAddresses = getDeselectedEntities('addresses');
export const getSelectedAddresses = getSelectedEntities('addresses');
