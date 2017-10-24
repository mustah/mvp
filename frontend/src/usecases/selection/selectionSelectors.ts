import {createSelector} from 'reselect';
import {IdNamed, uuid} from '../../types/Types';
import {SelectionOptionEntity, SelectionResult} from './models/selectionModels';
import {SelectionState} from './selectionReducer';

export const isFetching = (state: SelectionState): boolean => state.isFetching;

const getEntities = (state: SelectionState): SelectionOptionEntity => state.entities;
const getSelected = (state: SelectionState): SelectionResult => state.selected;
const getResult = (state: SelectionState): SelectionResult => state.result;

const getEntitiesSelector = (entityType: string): any =>
  createSelector<SelectionState, SelectionOptionEntity, IdNamed>(
    getEntities,
    (entities: SelectionOptionEntity) => entities[entityType],
  );

const getSelectedEntityIdsSelector = (entityType: string): any =>
  createSelector<SelectionState, SelectionResult, uuid[]>(
    getSelected,
    (searchResult: SelectionResult) => searchResult[entityType],
  );

const arrayDiff = <T>(superSet: T[], subSet: T[]): T[] => superSet.filter(a => !subSet.includes(a));

const getDeselectedEntityIdsSelector = (entityType: string): any =>
  createSelector<SelectionState, SelectionResult, SelectionResult, uuid[]>(
    getResult,
    getSelected,
    (result: SelectionResult, selected: SelectionResult) => arrayDiff(result[entityType], selected[entityType]),
  );

const getDeselectedEntities = (entityType: string): any =>
  createSelector<SelectionState, uuid[], SelectionOptionEntity, IdNamed[]>(
    getDeselectedEntityIdsSelector(entityType),
    getEntitiesSelector(entityType),
    (ids: uuid[], entities: SelectionOptionEntity) => ids.map((id) => entities[id]),
  );

const getSelectedEntities = (entityType: string): any =>
  createSelector<SelectionState, uuid[], SelectionOptionEntity, IdNamed[]>(
    getSelectedEntityIdsSelector(entityType),
    getEntitiesSelector(entityType),
    (ids: uuid[], entities: SelectionOptionEntity) => ids.map((id) => entities[id]),
  );

export const getDeselectedCities = getDeselectedEntities('cities');
export const getSelectedCities = getSelectedEntities('cities');
export const getDeselectedAddresses = getDeselectedEntities('addresses');
export const getSelectedAddresses = getSelectedEntities('addresses');
