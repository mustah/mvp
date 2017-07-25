import {AnyAction} from 'redux';
import {DASHBOARD_FAILURE, DASHBOARD_REQUEST, DASHBOARD_SUCCESS} from '../../types/ActionTypes';
import {DashboardModel} from './models/DasboardModel';

export interface DashboardState {
  title?: string;
  isFetching: boolean;
  records: DashboardModel[];
  error?: string;
}

const initialState = {
  isFetching: false,
  records: [],
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
      return {
        ...state,
        isFetching: false,
        records: [...payload],
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
