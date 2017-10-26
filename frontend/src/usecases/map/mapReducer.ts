import {Marker} from 'leaflet';
import {AnyAction} from 'redux';
import {
  MAP_POSITION_FAILURE,
  MAP_POSITION_REQUEST,
  MAP_POSITION_SUCCESS,
  OPEN_CLUSTER_DIALOG,
  TOGGLE_CLUSTER_DIALOG,
} from './mapActions';

export interface MapState {
  isClusterDialogOpen: boolean;

  // TODO type
  moids: any[];
  selectedMarker?: Marker;
}

export const initialState: MapState = {
  isClusterDialogOpen: false,
  moids: [],
};

export const map = (state: MapState = initialState, action: AnyAction): MapState => {
  const {payload} = action;

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
    case MAP_POSITION_REQUEST:
      return {
        ...state,
      };
    case MAP_POSITION_SUCCESS:
      return {
        ...state,
        moids: [...payload],
      };
    case MAP_POSITION_FAILURE:
      return {
        ...state,
      };
    default:
      return state;
  }
};
