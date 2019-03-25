import {Location} from 'history';
import {LOCATION_CHANGE} from 'react-router-redux';
import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/types';
import {isOnSearchPage} from '../../app/routes';
import {
  ADD_PARAMETER_TO_SELECTION,
  DESELECT_SELECTION,
  RESET_SELECTION,
  SELECT_SAVED_SELECTION,
  setThresholdAction,
} from '../user-selection/userSelectionActions';
import {Action} from '../../types/Types';
import {logoutUser} from '../../usecases/auth/authActions';
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
  | Action<Location>
  | EmptyAction<string>;

export const search = (state: SearchState = initialState, action: Actions): SearchState => {
  switch (action.type) {
    case getType(searchAction):
      return {...state, ...(action as Action<QueryParameter>).payload};
    case LOCATION_CHANGE:
      return isOnSearchPage((action as Action<Location>).payload)
        ? state
        : {validation: {}};
    case getType(setThresholdAction):
    case SELECT_SAVED_SELECTION:
    case ADD_PARAMETER_TO_SELECTION:
    case DESELECT_SELECTION:
    case RESET_SELECTION:
    case getType(logoutUser):
      return initialState;
    default:
      return state;
  }
};
