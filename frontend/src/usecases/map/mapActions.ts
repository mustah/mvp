import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {ExtendedMarker} from './mapModels';

export const TOGGLE_CLUSTER_DIALOG = 'TOGGLE_CLUSTER_DIALOG';
export const OPEN_CLUSTER_DIALOG = 'OPEN_CLUSTER_DIALOG';

export const toggleClusterDialog = createEmptyAction<string>(TOGGLE_CLUSTER_DIALOG);
export const openDialog = createPayloadAction<string, ExtendedMarker>(OPEN_CLUSTER_DIALOG);

export const openClusterDialog = (marker: ExtendedMarker) =>
  (dispatch) => dispatch(openDialog(marker));
