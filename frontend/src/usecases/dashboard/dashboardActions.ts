import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {Dispatch} from 'redux';
import {restClient} from '../../services/restClient';
import {DASHBOARD_FAILURE, DASHBOARD_REQUEST, DASHBOARD_SUCCESS} from '../../types/ActionTypes';
import {DashboardState} from './dashboardReducer';

const dashboardRequest = createEmptyAction(DASHBOARD_REQUEST);
const dashboardSuccess = createPayloadAction<string, DashboardState>(DASHBOARD_SUCCESS);
const dashboardFailure = createPayloadAction<string, DashboardState>(DASHBOARD_FAILURE);

export const fetchDashboard = () =>
  async (dispatch: Dispatch<any>) => {
    try {
      dispatch(dashboardRequest());
      const {data: dashboards} = await restClient.get('/dashboards/current');
      dispatch(dashboardSuccess(dashboards));
    }catch (error) {
      const {response: {data}} = error;
      dispatch(dashboardFailure(data));
    }
  };
