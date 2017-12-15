import {EmptyAction} from 'react-redux-typescript';
import {Action} from '../../types/Types';
import {CLOSE_CLUSTER_DIALOG, OPEN_CLUSTER_DIALOG} from './mapActions';
import {MapMarkerItem} from './mapModels';

export interface MapState {
  isClusterDialogOpen: boolean;
  selectedMarker?: MapMarkerItem;
}

export const initialState: MapState = {
  isClusterDialogOpen: false,
};

export const map = (state: MapState = initialState, action: Action<MapMarkerItem> | EmptyAction<string>): MapState => {
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
        selectedMarker: (action as Action<MapMarkerItem>).payload,
      };
    default:
      return state;
  }
};
