import {EmptyAction} from 'react-redux-typescript';
import {EndPoints} from '../../services/endPoints';
import {failureAction, requestAction, successAction} from '../../state/common/apiActions';
import {resetReducer} from '../../state/domain-models/domainModelsReducer';
import {Action, ErrorResponse} from '../../types/Types';
import {LOGOUT_USER} from '../auth/authActions';
import {DashboardModel} from './dashboardModels';

export interface DashboardState {
  isFetching: boolean;
  isSuccessfullyFetched: boolean;
  record?: DashboardModel;
  error?: ErrorResponse;
}

export const initialState: DashboardState = {
  isFetching: false,
  isSuccessfullyFetched: false,
};

const success = (state: DashboardState, {payload}: Action<DashboardModel>): DashboardState =>
  ({
    ...state,
    isFetching: false,
    isSuccessfullyFetched: true,
    record: {
      ...payload,
      widgets: [...payload.widgets],
    },
  });

const failure = (state: DashboardState, {payload}: Action<ErrorResponse>): DashboardState =>
  ({
    ...state,
    isFetching: false,
    isSuccessfullyFetched: false,
    error: {...payload},
  });

type ActionTypes = Action<DashboardModel> | Action<ErrorResponse> | EmptyAction<string>;

export const dashboard = (
  state: DashboardState = initialState,
  action: ActionTypes,
): DashboardState => {
  switch (action.type) {
    case requestAction(EndPoints.dashboard):
      return {...state, isFetching: true};
    case successAction(EndPoints.dashboard):
      return success(state, action as Action<DashboardModel>);
    case failureAction(EndPoints.dashboard):
      return failure(state, action as Action<ErrorResponse>);
    case LOGOUT_USER:
      return {...initialState};
    default:
      return resetReducer<DashboardState>(state, action, {...initialState});
  }
};
