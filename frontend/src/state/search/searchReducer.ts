import {ActionType, getType} from 'typesafe-actions';
import {isOnSearchPage} from '../../app/routes';
import {logoutUser} from '../../usecases/auth/authActions';
import {locationChange} from '../location/locationActions';
import {
  addParameterToSelection,
  deselectSelection,
  resetSelection,
  selectSavedSelectionAction,
  setThreshold,
} from '../user-selection/userSelectionActions';
import {search as searchAction} from './searchActions';
import {Query} from './searchModels';

export interface SearchState {
  validation: Query;
}

export const initialState: SearchState = {
  validation: {}
};

type Actions = ActionType<typeof locationChange
  | typeof searchAction
  | typeof setThreshold
  | typeof addParameterToSelection
  | typeof deselectSelection
  | typeof selectSavedSelectionAction
  | typeof logoutUser
  | typeof resetSelection>;

export const search = (state: SearchState = initialState, action: Actions): SearchState => {
  switch (action.type) {
    case getType(searchAction):
      return {...state, ...action.payload};
    case getType(locationChange):
      return isOnSearchPage(action.payload.location)
        ? state
        : {validation: {}};
    case getType(setThreshold):
    case getType(addParameterToSelection):
    case getType(deselectSelection):
    case getType(selectSavedSelectionAction):
    case getType(logoutUser):
    case getType(resetSelection):
      return initialState;
    default:
      return state;
  }
};
