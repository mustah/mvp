import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {Marker} from 'leaflet';

export const TOGGLE_CLUSTER_DIALOG = 'TOGGLE_CLUSTER_DIALOG';
export const OPEN_CLUSTER_DIALOG = 'OPEN_CLUSTER_DIALOG';

export const toggleClusterDialog = createEmptyAction<string>(TOGGLE_CLUSTER_DIALOG);
export const openClusterDialog = (marker: Marker) => {
  return (dispatch) => dispatch(createPayloadAction(OPEN_CLUSTER_DIALOG) (marker));
}
