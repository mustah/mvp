import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {restClient} from '../../services/restClient';
import {DASHBOARD_FAILURE, DASHBOARD_REQUEST, DASHBOARD_SUCCESS} from '../../types/ActionTypes';
import {DashboardState} from './dashboardReducer';

const dashboardRequest = createEmptyAction(DASHBOARD_REQUEST);
const dashboardSuccess = createPayloadAction<string, DashboardState>(DASHBOARD_SUCCESS);
const dashboardFailure = createPayloadAction<string, DashboardState>(DASHBOARD_FAILURE);

export const fetchDashboard = () => {
  return (dispatch) => {
    dispatch(dashboardRequest());

    restClient.get('/dashboards/current')
      .then(response => response.data)
      .then(dashboards => dispatch(dashboardSuccess(dashboards)))
      .catch(error => dispatch(dashboardFailure(error)));
  };
};
