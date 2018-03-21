import {EmptyAction} from 'react-redux-typescript';
import {EndPoints} from '../../services/endPoints';
import {Action, ErrorResponse} from '../../types/Types';
import {
  ADD_SELECTION,
  DESELECT_SELECTION,
  RESET_SELECTION,
  SELECT_PERIOD,
  SELECT_SAVED_SELECTION,
  SET_SELECTION,
  UPDATE_SELECTION,
} from '../search/selection/selectionActions';
import {failureAction, requestAction, successAction} from './summaryApiActions';
import {SelectionSummary, SummaryState} from './summaryModels';

export const initialState: SummaryState = {
  isFetching: false,
  isSuccessfullyFetched: false,
  payload: {numMeters: 0, numCities: 0, numAddresses: 0},
};

type ActionTypes =
  | EmptyAction<string>
  | Action<SelectionSummary>
  | Action<ErrorResponse>;

const resetReducer = (state: SummaryState, action: ActionTypes): SummaryState => {
  switch (action.type) {
    case SELECT_SAVED_SELECTION:
    case ADD_SELECTION:
    case DESELECT_SELECTION:
    case UPDATE_SELECTION:
    case RESET_SELECTION:
    case SET_SELECTION:
    case SELECT_PERIOD:
      return {...initialState};
    default:
      return state;
  }
};

export const summary = (state: SummaryState = initialState, action: ActionTypes): SummaryState => {
  switch (action.type) {
    case requestAction(EndPoints.summaryMeters):
      return {
        ...state,
        isFetching: true,
      };
    case successAction(EndPoints.summaryMeters):
      return {
        ...state,
        isFetching: false,
        isSuccessfullyFetched: true,
        payload: (action as Action<SelectionSummary>).payload,
      };
    case failureAction(EndPoints.summaryMeters):
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
