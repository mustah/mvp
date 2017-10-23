import {createSelector} from 'reselect';
import {IdNamed, uuid} from '../../types/Types';
import {SearchOptionEntity, SearchResult} from './models/searchModels';
import {SearchState} from './searchReducer';

export const isFetching = (state: SearchState): boolean => state.isFetching;

const getEntities = (state: SearchState): SearchOptionEntity => state.entities;
const getSelected = (state: SearchState): SearchResult => state.selected;
const getResult = (state: SearchState): SearchResult => state.result;

const getEntitiesSelector = (entityType: string): any =>
  createSelector<SearchState, SearchOptionEntity, IdNamed>(
    getEntities,
    (entities: SearchOptionEntity) => entities[entityType],
  );

const getSelectedEntityIdsSelector = (entityType: string): any =>
  createSelector<SearchState, SearchResult, uuid[]>(
    getSelected,
    (searchResult: SearchResult) => searchResult[entityType],
  );

const arrayDiff = <T>(superSet: T[], subSet: T[]): T[] => superSet.filter(a => !subSet.includes(a));

const getDeselectedEntityIdsSelector = (entityType: string): any =>
  createSelector<SearchState, SearchResult, SearchResult, uuid[]>(
    getResult,
    getSelected,
    (result: SearchResult, selected: SearchResult) => arrayDiff(result[entityType], selected[entityType]),
  );

const getDeselectedEntities = (entityType: string): any =>
  createSelector<SearchState, uuid[], SearchOptionEntity, IdNamed[]>(
    getDeselectedEntityIdsSelector(entityType),
    getEntitiesSelector(entityType),
    (ids: uuid[], entities: SearchOptionEntity) => ids.map((id) => entities[id]),
  );

const getSelectedEntities = (entityType: string): any =>
  createSelector<SearchState, uuid[], SearchOptionEntity, IdNamed[]>(
    getSelectedEntityIdsSelector(entityType),
    getEntitiesSelector(entityType),
    (ids: uuid[], entities: SearchOptionEntity) => ids.map((id) => entities[id]),
  );

export const getDeselectedCities = getDeselectedEntities('cities');
export const getSelectedCities = getSelectedEntities('cities');
export const getDeselectedAddresses = getDeselectedEntities('addresses');
export const getSelectedAddresses = getSelectedEntities('addresses');
