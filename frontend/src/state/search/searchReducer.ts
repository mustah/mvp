import {Location} from 'history';
import {ActionType, getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/type-helpers';
import {isOnSearchPage} from '../../app/routes';
import {Action} from '../../types/Types';
import {logoutUser} from '../../usecases/auth/authActions';
import {locationChange} from '../location/locationActions';
import {
  addParameterToSelection,
  deselectSelection,
  RESET_SELECTION,
  selectSavedSelectionAction,
  setThresholdAction,
} from '../user-selection/userSelectionActions';
import {search as searchAction} from './searchActions';
import {Query, QueryParameter} from './searchModels';

export interface SearchState {
  validation: Query;
}

export const initialState: SearchState = {
  validation: {}
};

type Actions =
  | Action<QueryParameter>
  | ActionType<typeof locationChange>
  | EmptyAction<string>;

export const search = (state: SearchState = initialState, action: Actions): SearchState => {
  switch (action.type) {
    case getType(searchAction):
      return {...state, ...(action as Action<QueryParameter>).payload};
    case getType(locationChange):
      return isOnSearchPage((action as Action<Location>).payload)
        ? state
        : {validation: {}};
    case getType(setThresholdAction):
    case getType(addParameterToSelection):
    case getType(deselectSelection):
    case getType(selectSavedSelectionAction):
    case RESET_SELECTION:
    case getType(logoutUser):
      return initialState;
    default:
      return state;
  }
};
