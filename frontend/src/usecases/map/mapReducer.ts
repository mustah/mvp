import {Action} from '../../types/Types';
import {CLOSE_CLUSTER_DIALOG, OPEN_CLUSTER_DIALOG} from './mapActions';
import {ExtendedMarker} from './mapModels';
import {EmptyAction} from 'react-redux-typescript';

export interface MapState {
  isClusterDialogOpen: boolean;
  selectedMarker?: ExtendedMarker;
}

export const initialState: MapState = {
  isClusterDialogOpen: false,
};

export const map = (state: MapState = initialState, action: Action<ExtendedMarker> | EmptyAction<string>): MapState => {
  switch (action.type) {
    case CLOSE_CLUSTER_DIALOG:
      return {
        ...state,
        isClusterDialogOpen: false,
      };
    case OPEN_CLUSTER_DIALOG:
      return {
        ...state,
        isClusterDialogOpen: true,
        selectedMarker: (action as Action<ExtendedMarker>).payload,
      };
    default:
      return state;
  }
};
