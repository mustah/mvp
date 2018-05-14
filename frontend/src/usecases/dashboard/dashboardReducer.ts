import {EmptyAction} from 'react-redux-typescript';
import {Action, ErrorResponse} from '../../types/Types';
import {LOGOUT_USER} from '../auth/authActions';
import {DASHBOARD_FAILURE, DASHBOARD_REQUEST, DASHBOARD_SUCCESS} from './dashboardActions';
import {DashboardModel} from './dashboardModels';

export interface DashboardState {
  isFetching: boolean;
  record?: DashboardModel;
  error?: ErrorResponse;
}

export const initialDashboardState: DashboardState = {
  isFetching: false,
};

const success = (state: DashboardState, {payload}: Action<DashboardModel>): DashboardState =>
  ({
    ...state,
    isFetching: false,
    record: {
      ...payload,
      widgets: [...payload.widgets],
    },
  });

const failure = (state: DashboardState, {payload}: Action<ErrorResponse>): DashboardState =>
  ({
    ...state,
    isFetching: false,
    error: {...payload},
  });

type ActionTypes = Action<DashboardModel> | Action<ErrorResponse> | EmptyAction<string>;

export const dashboard = (
  state: DashboardState = initialDashboardState,
  action: ActionTypes,
): DashboardState => {
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
    case LOGOUT_USER:
      return {...initialDashboardState};
    default:
      return state;
  }
};
