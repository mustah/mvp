import {createAction, createStandardAction} from 'typesafe-actions';
import {GeoPosition} from '../../state/domain-models/location/locationModels';
import {uuid} from '../../types/Types';
import {Marker} from './mapModels';

export const closeClusterDialog = createAction('CLOSE_CLUSTER_DIALOG');
export const openDialog = createStandardAction('OPEN_CLUSTER_DIALOG')<uuid>();
export const centerMap = createStandardAction('CENTER_MAP')<GeoPosition>();

export const openClusterDialog = ({options: {mapMarkerItem}}: Marker) =>
  (dispatch) => dispatch(openDialog(mapMarkerItem));
