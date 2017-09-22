import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {DASHBOARD_FAILURE, DASHBOARD_REQUEST, DASHBOARD_SUCCESS} from '../../types/ActionTypes';
import {restClient} from '../../services/restClient';

const dashboardRequest = createEmptyAction(DASHBOARD_REQUEST);
const dashboardSuccess = createPayloadAction(DASHBOARD_SUCCESS);
const dashboardFailure = createPayloadAction(DASHBOARD_FAILURE);

export const fetchDashboard = () => {
  return (dispatch) => {
    dispatch(dashboardRequest());

    restClient.get('/dashboard/current')
      .then(response => response.data)
      .then(dashboards => dispatch(dashboardSuccess(dashboards)))
      .catch(error => dispatch(dashboardFailure(error)));
  };
};
