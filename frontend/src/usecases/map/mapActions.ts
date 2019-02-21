import {GeoPosition} from '../../state/domain-models/location/locationModels';
import {emptyActionOf, payloadActionOf, uuid} from '../../types/Types';
import {Marker} from './mapModels';

export const CLOSE_CLUSTER_DIALOG = 'CLOSE_CLUSTER_DIALOG';
export const OPEN_CLUSTER_DIALOG = 'OPEN_CLUSTER_DIALOG';
export const CENTER_MAP = 'CENTER_MAP';

export const closeClusterDialog = emptyActionOf(CLOSE_CLUSTER_DIALOG);
export const openDialog = payloadActionOf<uuid>(OPEN_CLUSTER_DIALOG);
export const centerMap = payloadActionOf<GeoPosition>(CENTER_MAP);

export const openClusterDialog = ({options: {mapMarkerItem}}: Marker) =>
  (dispatch) => dispatch(openDialog(mapMarkerItem));
