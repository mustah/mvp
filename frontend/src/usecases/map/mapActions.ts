import {Marker} from 'leaflet';
import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';

export const TOGGLE_CLUSTER_DIALOG = 'TOGGLE_CLUSTER_DIALOG';
export const OPEN_CLUSTER_DIALOG = 'OPEN_CLUSTER_DIALOG';
export const MAP_POSITION_REQUEST = 'MAP_POSITION_REQUEST';
export const MAP_POSITION_SUCCESS = 'MAP_POSITION_SUCCESS';
export const MAP_POSITION_FAILURE = 'MAP_POSITION_FAILURE';

export const toggleClusterDialog = createEmptyAction<string>(TOGGLE_CLUSTER_DIALOG);
export const openDialog = createPayloadAction<string, Marker>(OPEN_CLUSTER_DIALOG);

export const openClusterDialog = (marker: Marker) => (dispatch) => dispatch(openDialog(marker));
