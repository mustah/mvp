import {AnyAction} from 'redux';
import {widgetFactory} from '../../services/widgetService';
import {DASHBOARD_FAILURE, DASHBOARD_REQUEST, DASHBOARD_SUCCESS} from '../../types/ActionTypes';
import {DashboardModel} from './models/dashboardModels';

export interface DashboardState {
  isFetching: boolean;
  record?: DashboardModel;
  error?: string;
}

export const initialState: DashboardState = {
  isFetching: false,
};

export const dashboard = (state: DashboardState = initialState, action: AnyAction): DashboardState => {
  const {payload} = action;

  switch (action.type) {
    case DASHBOARD_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    case DASHBOARD_SUCCESS:
      const dashboard = payload;
      dashboard.systemOverview.widgets = payload.systemOverview.widgets.map(widgetFactory);
      return {
        ...state,
        isFetching: false,
        record: dashboard,
      };
    case DASHBOARD_FAILURE:
      return {
        ...state,
        isFetching: false,
        error: payload,
      };
    default:
      return state;
  }
};
