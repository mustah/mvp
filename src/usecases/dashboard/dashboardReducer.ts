import {AnyAction} from 'redux';
import * as actions from '../../types/ActionTypes';
import {DashboardModel} from './models/DasboardModel';

export interface DashboardProps {
  title?: string;
  isFetching: boolean;
  records: DashboardModel[];
  error?: string;
}

const initialState = {
  isFetching: false,
  records: [],
};

export const dashboard = (state: DashboardProps = initialState, action: AnyAction): DashboardProps => {
  const {payload} = action;

  switch (action.type) {
    case actions.DASHBOARD_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    case actions.DASHBOARD_SUCCESS:
      return {
        ...state,
        isFetching: false,
        records: [...payload],
      };
    case actions.DASHBOARD_FAILURE:
      return {
        ...state,
        isFetching: false,
        error: payload,
      };
    default:
      return state;
  }
};
