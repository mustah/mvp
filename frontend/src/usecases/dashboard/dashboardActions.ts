import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {IndicatorType} from '../../components/indicators/indicatorWidgetModels';
import {ErrorResponse, Status} from '../../types/Types';
import {DashboardModel} from './dashboardModels';

export const DASHBOARD_REQUEST = 'DASHBOARD_REQUEST';
export const DASHBOARD_SUCCESS = 'DASHBOARD_SUCCESS';
export const DASHBOARD_FAILURE = 'DASHBOARD_FAILURE';

export const dashboardRequest = createEmptyAction(DASHBOARD_REQUEST);
export const dashboardSuccess = createPayloadAction<string, DashboardModel>(DASHBOARD_SUCCESS);
export const dashboardFailure = createPayloadAction<string, ErrorResponse>(DASHBOARD_FAILURE);

export const fetchDashboard = () =>
  (dispatch) =>
    dispatch(dashboardSuccess({
      id: 'dashboard-id-123',
      widgets: [
        {
          type: IndicatorType.collection,
          total: 1697,
          status: Status.warning,
          pending: 22,
        },
        {
          type: IndicatorType.measurementQuality,
          total: 1709,
          status: Status.critical,
          pending: 6,
        },
      ],

    }));
