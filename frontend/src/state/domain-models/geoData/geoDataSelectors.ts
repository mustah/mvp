import {SelectionEntityState} from '../../search/selection/selectionModels';

export const isFetchingGeoData = (state: SelectionEntityState): boolean => state.isFetching;
