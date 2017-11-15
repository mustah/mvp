import {SelectionEntityState} from '../domainModels';

export const isFetchingGeoData = (state: SelectionEntityState): boolean => state.isFetching;
