import {
  OPEN_CLUSTER_DIALOG,
  CLOSE_CLUSTER_DIALOG,
} from './mapActions';
import {ExtendedMarker} from './mapModels';
import {Action} from '../../types/Types';

export interface MapState {
  isClusterDialogOpen: boolean;
  selectedMarker?: ExtendedMarker;
}

export const initialState: MapState = {
  isClusterDialogOpen: false,
};

export const map = (state: MapState = initialState, action: Action<ExtendedMarker>): MapState => {
  switch (action.type) {
    case CLOSE_CLUSTER_DIALOG:
      return {
        ...state,
        isClusterDialogOpen: false,
      };
    case OPEN_CLUSTER_DIALOG:
      return {
        ...state,
        isClusterDialogOpen: true,
        selectedMarker: action.payload,
      };
    default:
      return state;
  }
};
