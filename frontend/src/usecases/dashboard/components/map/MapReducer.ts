import {AnyAction} from 'redux';
import {TOGGLE_CLUSTER_DIALOG} from './MapActions';
import {Marker} from 'react-leaflet';

export interface MapState {
  isClusterDialogOpen: boolean;
  markerPositions: any;
  selectedMarker?: Marker;
}

export const initialState: MapState = {
  isClusterDialogOpen: false,
  markerPositions: [
    {lat: 49.8397, lng: 24.0297},
    {lat: 49.8394, lng: 24.0294},
    {lat: 49.7394, lng: 24.0274},
    {lat: 47.7394, lng: 23.0274},
    {lat: 44.7394, lng: 23.0274},
    {lat: 52.2297, lng: 21.0122},
    {lat: 51.5074, lng: -0.0901},
  ],
};

export const map = (state: MapState = {isClusterDialogOpen : false, markerPositions: [
  {lat: 49.8397, lng: 24.0297},
  {lat: 49.8394, lng: 24.0294},
  {lat: 49.7394, lng: 24.0274},
  {lat: 47.7394, lng: 23.0274},
  {lat: 44.7394, lng: 23.0274},
  {lat: 52.2297, lng: 21.0122},
  {lat: 51.5074, lng: -0.0901},
]}, action: AnyAction): MapState => {
  switch (action.type) {
    case TOGGLE_CLUSTER_DIALOG:
      return {
        ...state,
        isClusterDialogOpen: !state.isClusterDialogOpen,
      };
    default:
      return state;
  }
};
