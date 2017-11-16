import {AnyAction} from 'redux';
import {
  OPEN_CLUSTER_DIALOG,
  TOGGLE_CLUSTER_DIALOG,
} from './mapActions';
import {IdentifiedMarker} from './mapModels';

export interface MapState {
  isClusterDialogOpen: boolean;
  selectedMarker?: IdentifiedMarker;
}

export const initialState: MapState = {
  isClusterDialogOpen: false,
  selectedMarker: {
    options: null,
  },
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
