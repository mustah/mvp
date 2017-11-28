import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {ExtendedMarker} from './mapModels';

export const CLOSE_CLUSTER_DIALOG = 'CLOSE_CLUSTER_DIALOG';
export const OPEN_CLUSTER_DIALOG = 'OPEN_CLUSTER_DIALOG';

export const closeClusterDialog = createEmptyAction<string>(CLOSE_CLUSTER_DIALOG);
export const openDialog = createPayloadAction<string, ExtendedMarker>(OPEN_CLUSTER_DIALOG);

export const openClusterDialog = (marker: ExtendedMarker) =>
  (dispatch) => dispatch(openDialog(marker));
