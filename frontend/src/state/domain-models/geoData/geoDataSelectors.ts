import {createSelector} from 'reselect';
import {IdNamed, uuid} from '../../../types/Types';
import {DomainModel, GeoDataState, IdNamedState} from './geoDataModels';

export const getCitiesEntities = (state: GeoDataState): DomainModel<IdNamed> => state.cities.entities;
export const getAddressesEntities = (state: GeoDataState): DomainModel<IdNamed> => state.addresses.entities;

export const getCitiesResult = (state: GeoDataState): uuid[] => state.cities.result;
export const getAddressesResult = (state: GeoDataState): uuid[] => state.addresses.result;

export const isFetchingAddresses = createSelector<GeoDataState, IdNamedState, boolean>(
  (state: GeoDataState) => state.addresses,
  (addresses: IdNamedState) => addresses.isFetching,
);

export const isFetchingCities = createSelector<GeoDataState, IdNamedState, boolean>(
  (state: GeoDataState) => state.cities,
  (cities: IdNamedState) => cities.isFetching,
);
