import {createSelector} from 'reselect';
import {IdNamed, uuid} from '../../types/Types';
import {SearchOption, SearchOptionResult} from './models/searchModels';
import {SearchState} from './searchReducer';

export const isFetching = (state: SearchState): boolean => state.isFetching;

export const getEntities = (state: SearchState): SearchOption => state.entities;

export const getResult = (state: SearchState): SearchOptionResult => state.result;

export const getCities = createSelector<SearchState, SearchOption, SearchOptionResult, IdNamed[]>(
  getEntities,
  getResult,
  (entities: SearchOption, result: SearchOptionResult) => result.cities.map((id: uuid) => entities.cities[id]),
);

export const getAddresses = createSelector<SearchState, SearchOption, SearchOptionResult, IdNamed[]>(
  getEntities,
  getResult,
  (entities: SearchOption, result: SearchOptionResult) => result.addresses.map((id: uuid) => entities.addresses[id]),
);
