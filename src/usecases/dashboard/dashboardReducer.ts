import {AnyAction} from 'redux';
import * as actions from '../../types/ActionTypes';
import {Selectable} from '../../types/Types';
import {DashboardModel} from './models/DasboardModel';

export interface DashboardProps extends Selectable {
  title?: string;
  isFetching: boolean;
  records: DashboardModel[];
}

const initialState = {
  isSelected: false,
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
      };
    default:
      return state;
  }
};
