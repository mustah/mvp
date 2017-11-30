import {EmptyAction} from 'react-redux-typescript';
import {Action, ErrorResponse} from '../../types/Types';
import {DASHBOARD_FAILURE, DASHBOARD_REQUEST, DASHBOARD_SUCCESS} from './dashboardActions';
import {DashboardModel} from './dashboardModels';

export interface DashboardState {
  isFetching: boolean;
  record?: DashboardModel;
  error?: ErrorResponse;
}

export const initialState: DashboardState = {
  isFetching: false,
};

const success = (state: DashboardState = initialState, action: Action<DashboardModel>): DashboardState => {
  const {payload} = action;
  return {
    ...state,
    isFetching: false,
    record: {
      ...payload,
      widgets: [...payload.widgets],
    },
  };
};

const failure = (state: DashboardState = initialState, action: Action<ErrorResponse>): DashboardState => {
  const {payload} = action;
  return {
    ...state,
    isFetching: false,
    error: {...payload},
  };
};

type ActionTypes = Action<DashboardModel> | Action<ErrorResponse> | EmptyAction<string>;

export const dashboard = (state: DashboardState = initialState, action: ActionTypes): DashboardState => {
  switch (action.type) {
    case DASHBOARD_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    case DASHBOARD_SUCCESS:
      return success(state, action as Action<DashboardModel>);
    case DASHBOARD_FAILURE:
      return failure(state, action as Action<ErrorResponse>);
    default:
      return state;
  }
};
