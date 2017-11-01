import {Marker} from 'leaflet';
import {AnyAction} from 'redux';
import {
  OPEN_CLUSTER_DIALOG,
  TOGGLE_CLUSTER_DIALOG,
} from './mapActions';

export interface MapState {
  isClusterDialogOpen: boolean;
  selectedMarker?: Marker;
}

export const initialState: MapState = {
  isClusterDialogOpen: false,
};

export const map = (state: MapState = initialState, action: AnyAction): MapState => {

  switch (action.type) {
    case TOGGLE_CLUSTER_DIALOG:
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
