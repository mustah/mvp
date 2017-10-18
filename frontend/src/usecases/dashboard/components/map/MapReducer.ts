import {AnyAction} from 'redux';
import {
  TOGGLE_CLUSTER_DIALOG, OPEN_CLUSTER_DIALOG, MAP_POSITION_REQUEST, MAP_POSITION_SUCCESS,
  MAP_POSITION_FAILURE,
} from './MapActions';
import {Marker} from 'leaflet';

export interface MapState {
  isClusterDialogOpen: boolean;
  markerPosition: any;
  selectedMarker?: Marker;
}

export const initialState: MapState = {
  isClusterDialogOpen: false,
  markerPosition: [
    {lat: 49.8397, lng: 24.0297},
    {lat: 49.8394, lng: 24.0294},
    {lat: 49.7394, lng: 24.0274},
    {lat: 47.7394, lng: 23.0274},
    {lat: 44.7394, lng: 23.0274},
    {lat: 52.2297, lng: 21.0122},
    {lat: 51.5074, lng: -0.0901},
  ],
};

export const map = (state: MapState = {isClusterDialogOpen : false, markerPosition: [
  {lat: 49.8397, lng: 24.0297},
  {lat: 49.8394, lng: 24.0294},
  {lat: 49.7394, lng: 24.0274},
  {lat: 47.7394, lng: 23.0274},
  {lat: 44.7394, lng: 23.0274},
  {lat: 52.2297, lng: 21.0122},
  {lat: 51.5074, lng: -0.0901},
]}, action: AnyAction): MapState => {
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
      let positions: Array<any> = [];
      payload.forEach((element) => { positions.push(element.position)});

      return {
        ...state,
        markerPosition: positions
      };
    case MAP_POSITION_FAILURE:
      return {
        ...state,
      };
    default:
      return state;
  }
};
