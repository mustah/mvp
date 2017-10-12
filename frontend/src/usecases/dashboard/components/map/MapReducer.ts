import {AnyAction} from 'redux';
import {TOGGLE_CLUSTER_DIALOG} from './MapActions';

export interface MapState {
  isClusterDialogOpen: boolean;
}

export const initialState: MapState = {
  isClusterDialogOpen: false,
};

export const map = (state: MapState = {isClusterDialogOpen : false}, action: AnyAction): MapState => {
  switch (action.type) {
    case TOGGLE_CLUSTER_DIALOG:
      return {
        ...state,
        isClusterDialogOpen: !state.isClusterDialogOpen,
      };
    default:
      return state;
  }
};
