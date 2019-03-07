import {EmptyAction} from 'typesafe-actions/dist/types';
import {EndPoints} from '../../services/endPoints';
import {failureAction, requestAction, successAction} from '../../state/api/apiActions';
import {resetReducer} from '../../reducers/resetReducer';
import {Action, ErrorResponse} from '../../types/Types';
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
    default:
      return resetReducer<DashboardState>(state, action, initialState);
  }
};
