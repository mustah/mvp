import {ActionType, getType} from 'typesafe-actions';
import {resetSelection, selectSavedSelectionAction} from '../../state/user-selection/userSelectionActions';
import {Dictionary} from '../../types/Types';
import {logoutUser} from '../auth/authActions';
import {onCenterMap} from './mapActions';
import {MapZoomSettings} from './mapModels';

export const defaultZoomLevel = 7;

export type MapState = Dictionary<MapZoomSettings>;

export const initialState: MapState = {};

type ActionTypes = ActionType<typeof onCenterMap
  | typeof logoutUser
  | typeof selectSavedSelectionAction
  | typeof resetSelection>;

export const map = (
  state: MapState = initialState,
  action: ActionTypes,
): MapState => {
  switch (action.type) {
    case getType(onCenterMap):
      const {center, id, zoom} = action.payload;
      return {...state, [id]: {center, zoom}};
    case getType(selectSavedSelectionAction):
    case getType(resetSelection):
    case getType(logoutUser):
      return initialState;
    default:
      return state;
  }
};
