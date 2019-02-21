import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/types';
import {GeoPosition} from '../../state/domain-models/location/locationModels';
import {Action, uuid} from '../../types/Types';
import {logoutUser} from '../auth/authActions';
import {centerMap, closeClusterDialog, openDialog} from './mapActions';

export interface MapState {
  isClusterDialogOpen: boolean;
  selectedMarker?: uuid;
  viewCenter?: GeoPosition;
}

export const initialState: MapState = {
  isClusterDialogOpen: false,
};

type ActionTypes = Action<uuid | GeoPosition> | EmptyAction<string>;

export const map = (
  state: MapState = initialState,
  action: ActionTypes,
): MapState => {
  switch (action.type) {
    case getType(closeClusterDialog):
      return {
        ...state,
        isClusterDialogOpen: false,
        selectedMarker: undefined,
      };
    case getType(openDialog):
      return {
        ...state,
        isClusterDialogOpen: true,
        selectedMarker: (action as Action<uuid>).payload,
      };
    case getType(centerMap):
      return {
        ...state,
        viewCenter: (action as Action<GeoPosition>).payload,
      };
    case getType(logoutUser):
      return initialState;
    default:
      return state;
  }
};
