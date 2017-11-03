import {createSelector} from 'reselect';
import {IdNamed, uuid} from '../../../types/Types';
import {DomainModel, GeoDataState, IdNamedState} from './geoDataModels';

export const getGeoDataEntitiesBy = (entityType: string) =>
  (state: GeoDataState): DomainModel<IdNamed> => state[entityType].entities;

export const getGeoDataResultBy = (entityType: string) =>
  (state: GeoDataState): uuid[] => state[entityType].result;

export const isFetchingAddresses = createSelector<GeoDataState, IdNamedState, boolean>(
  (state: GeoDataState) => state.addresses,
  (addresses: IdNamedState) => addresses.isFetching,
);

export const isFetchingCities = createSelector<GeoDataState, IdNamedState, boolean>(
  (state: GeoDataState) => state.cities,
  (cities: IdNamedState) => cities.isFetching,
);
