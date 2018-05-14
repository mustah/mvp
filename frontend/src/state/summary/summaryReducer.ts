import {EmptyAction} from 'react-redux-typescript';
import {EndPoints} from '../../services/endPoints';
import {Action, ErrorResponse} from '../../types/Types';
import {LOGOUT_USER} from '../../usecases/auth/authActions';
import {failureAction, requestAction, successAction} from '../common/apiActions';
import {resetReducer} from '../domain-models/domainModelsReducer';
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
    case LOGOUT_USER:
      return {...initialState};
    default:
      return resetReducer(state, action, {...initialState});
  }
};
