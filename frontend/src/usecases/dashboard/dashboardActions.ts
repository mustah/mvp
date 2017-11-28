import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {Dispatch} from 'redux';
import {restClient} from '../../services/restClient';
import {DashboardState} from './dashboardReducer';

export const DASHBOARD_REQUEST = 'DASHBOARD_REQUEST';
export const DASHBOARD_SUCCESS = 'DASHBOARD_SUCCESS';
export const DASHBOARD_FAILURE = 'DASHBOARD_FAILURE';

const dashboardRequest = createEmptyAction(DASHBOARD_REQUEST);
const dashboardSuccess = createPayloadAction<string, DashboardState>(DASHBOARD_SUCCESS);
const dashboardFailure = createPayloadAction<string, DashboardState>(DASHBOARD_FAILURE);

export const fetchDashboard = () =>
  async (dispatch: Dispatch<any>) => {
    try {
      dispatch(dashboardRequest());
      const {data: dashboards} = await restClient.get('/dashboards/current');
      dispatch(dashboardSuccess(dashboards));
    } catch (error) {
      const {response: {data}} = error;
      dispatch(dashboardFailure(data));
    }
  };
