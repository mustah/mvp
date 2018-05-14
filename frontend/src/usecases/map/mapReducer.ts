import {EmptyAction} from 'react-redux-typescript';
import {Action, uuid} from '../../types/Types';
import {LOGOUT_USER} from '../auth/authActions';
import {CLOSE_CLUSTER_DIALOG, OPEN_CLUSTER_DIALOG} from './mapActions';

export interface MapState {
  isClusterDialogOpen: boolean;
  selectedMarker?: uuid;
}

export const initialState: MapState = {
  isClusterDialogOpen: false,
};

export const map = (
  state: MapState = initialState,
  action: Action<uuid> | EmptyAction<string>,
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
    case LOGOUT_USER:
      return {...initialState};
    default:
      return state;
  }
};
