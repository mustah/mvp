import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {Marker, MapMarkerItem} from './mapModels';

export const CLOSE_CLUSTER_DIALOG = 'CLOSE_CLUSTER_DIALOG';
export const OPEN_CLUSTER_DIALOG = 'OPEN_CLUSTER_DIALOG';

export const closeClusterDialog = createEmptyAction<string>(CLOSE_CLUSTER_DIALOG);
export const openDialog = createPayloadAction<string, MapMarkerItem>(OPEN_CLUSTER_DIALOG);

export const openClusterDialog = ({options: {mapMarkerItem}}: Marker) =>
  (dispatch) => dispatch(openDialog(mapMarkerItem));
