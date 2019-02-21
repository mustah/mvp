import {EmptyAction} from 'typesafe-actions/dist/types';
import {GeoPosition} from '../../state/domain-models/location/locationModels';
import {Action, uuid} from '../../types/Types';
import {LOGOUT_USER} from '../auth/authActions';
import {CENTER_MAP, CLOSE_CLUSTER_DIALOG, OPEN_CLUSTER_DIALOG} from './mapActions';

export interface MapState {
  isClusterDialogOpen: boolean;
  selectedMarker?: uuid;
  viewCenter?: GeoPosition;
}

export const initialState: MapState = {
  isClusterDialogOpen: false,
};

export const map = (
  state: MapState = initialState,
  action: Action<uuid> | Action<GeoPosition> | EmptyAction<string>,
): MapState => {
  switch (action.type) {
    case CLOSE_CLUSTER_DIALOG:
      return {
        ...state,
        isClusterDialogOpen: false,
        selectedMarker: undefined,
      };
    case OPEN_CLUSTER_DIALOG:
      return {
        ...state,
        isClusterDialogOpen: true,
        selectedMarker: (action as Action<uuid>).payload,
      };
    case CENTER_MAP:
      return {
        ...state,
        viewCenter: (action as Action<GeoPosition>).payload,
      };
    case LOGOUT_USER:
      return initialState;
    default:
      return state;
  }
};
