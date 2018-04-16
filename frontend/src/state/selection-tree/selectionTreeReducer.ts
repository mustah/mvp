import {EmptyAction} from 'react-redux-typescript';
import {EndPoints} from '../../services/endPoints';
import {Action, ErrorResponse} from '../../types/Types';
import {
  ADD_PARAMETER_TO_SELECTION,
  DESELECT_SELECTION,
  RESET_SELECTION,
  SELECT_PERIOD,
  SELECT_SAVED_SELECTION,
  SET_SELECTION,
  SET_CURRENT_SELECTION,
} from '../search/selection/selectionActions';
import {failureAction, requestAction, successAction} from '../summary/summaryApiActions';
import {NormalizedSelectionTree, SelectionTreeState} from './selectionTreeModels';

export const initialState: SelectionTreeState = {
  isFetching: false,
  isSuccessfullyFetched: false,
  entities: {
    cities: {},
    addresses: {},
    meters: {},
  },
  result: {
    cities: [],
  },
};

type ActionTypes =
  | EmptyAction<string>
  | Action<NormalizedSelectionTree>
  | Action<ErrorResponse>;

const resetReducer = (state: SelectionTreeState, action: ActionTypes): SelectionTreeState => {
  switch (action.type) {
    case SELECT_SAVED_SELECTION:
    case ADD_PARAMETER_TO_SELECTION:
    case DESELECT_SELECTION:
    case SET_CURRENT_SELECTION:
    case RESET_SELECTION:
    case SET_SELECTION:
    case SELECT_PERIOD:
      return {...initialState};
    default:
      return state;
  }
};

export const selectionTree = (state: SelectionTreeState = initialState, action: ActionTypes): SelectionTreeState => {
  switch (action.type) {
    case requestAction(EndPoints.selectionTree):
      return {
        ...state,
        isFetching: true,
      };
    case successAction(EndPoints.selectionTree):
      return {
        ...state,
        isFetching: false,
        isSuccessfullyFetched: true,
        ...(action as Action<NormalizedSelectionTree>).payload,
      };
    case failureAction(EndPoints.selectionTree):
      return {
        ...state,
        isFetching: false,
        isSuccessfullyFetched: false,
        error: (action as Action<ErrorResponse>).payload,
      };
    default:
      return resetReducer(state, action);
  }
};
