import {createSelector} from 'reselect';
import {GeoDataState, IdNamedState} from './geoDataModels';

export const isFetchingAddresses = createSelector<GeoDataState, IdNamedState, boolean>(
  (state: GeoDataState) => state.addresses,
  (addresses: IdNamedState) => addresses.isFetching,
);

export const isFetchingCities = createSelector<GeoDataState, IdNamedState, boolean>(
  (state: GeoDataState) => state.cities,
  (cities: IdNamedState) => cities.isFetching,
);
