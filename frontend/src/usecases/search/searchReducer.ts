import {Location} from 'history';
import {EmptyAction} from 'react-redux-typescript';
import {LOCATION_CHANGE} from 'react-router-redux';
import {routes} from '../../app/routes';
import {
  ADD_PARAMETER_TO_SELECTION,
  DESELECT_SELECTION,
  RESET_SELECTION,
  SELECT_SAVED_SELECTION,
} from '../../state/user-selection/userSelectionActions';
import {Action} from '../../types/Types';
import {LOGOUT_USER} from '../auth/authActions';
import {SEARCH, SEARCH_SELECTION_TREE} from './searchActions';
import {Query, QueryParameter} from './searchModels';

export interface SearchState {
  collection: Query;
  validation: Query;
  selectionTree: Query;
}

export const initialState = {
  collection: {},
  validation: {},
  selectionTree: {},
};

type Actions =
  | Action<QueryParameter>
  | Action<Location>
  | EmptyAction<string>;

const resetValidationQuery = (state: SearchState, {pathname}: Location): SearchState => {
  if (pathname === routes.selection) {
    // the selection page reuses the validation rendering view,
    // resetting the search term shows potentially hidden items in the selection view
    return {...state, validation: {}};
  } else {
    return state;
  }
};

export const search = (state: SearchState = initialState, action: Actions): SearchState => {
  switch (action.type) {
    case SEARCH:
    case SEARCH_SELECTION_TREE:
      return {...state, ...(action as Action<QueryParameter>).payload};
    case LOCATION_CHANGE:
      return resetValidationQuery(state, (action as Action<Location>).payload);
    case SELECT_SAVED_SELECTION:
    case ADD_PARAMETER_TO_SELECTION:
    case DESELECT_SELECTION:
    case RESET_SELECTION:
    case LOGOUT_USER:
      return {...initialState};
    default:
      return state;
  }
};
