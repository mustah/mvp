import {AnyAction} from 'redux';
import {
  TOGGLE_CLUSTER_DIALOG, OPEN_CLUSTER_DIALOG, MAP_POSITION_REQUEST, MAP_POSITION_SUCCESS,
  MAP_POSITION_FAILURE,
} from './MapActions';
import {Marker} from 'leaflet';

export interface MapState {
  isClusterDialogOpen: boolean;

  // TODO type
  moids?: any;
  selectedMarker?: Marker;
}

export const initialState: MapState = {
  isClusterDialogOpen: false,
};

export const map = (state: MapState = {isClusterDialogOpen : false}, action: AnyAction): MapState => {
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
        moids: payload,
      };
    case MAP_POSITION_FAILURE:
      return {
        ...state,
      };
    default:
      return state;
  }
};
