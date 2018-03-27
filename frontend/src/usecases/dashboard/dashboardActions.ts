import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {makeUrl} from '../../helpers/urlFactory';
import {EndPoints} from '../../services/endPoints';
import {InvalidToken, restClient} from '../../services/restClient';
import {EncodedUriParameters, ErrorResponse} from '../../types/Types';
import {logout} from '../auth/authActions';
import {DashboardModel} from './dashboardModels';

export const DASHBOARD_REQUEST = 'DASHBOARD_REQUEST';
export const DASHBOARD_SUCCESS = 'DASHBOARD_SUCCESS';
export const DASHBOARD_FAILURE = 'DASHBOARD_FAILURE';

export const dashboardRequest = createEmptyAction(DASHBOARD_REQUEST);
export const dashboardSuccess = createPayloadAction<string, DashboardModel>(DASHBOARD_SUCCESS);
export const dashboardFailure = createPayloadAction<string, ErrorResponse>(DASHBOARD_FAILURE);

export const fetchDashboard =
  (parameters: EncodedUriParameters) =>
    async (dispatch) => {
      try {
        dispatch(dashboardRequest());
        const {data: dashboard} = await restClient.get(makeUrl(EndPoints.dashboard, parameters));
        dispatch(dashboardSuccess(dashboard));
      } catch (error) {
        if (error instanceof InvalidToken) {
          await dispatch(logout(error));
        } else {
          const {response: {data}} = error;
          dispatch(dashboardFailure(data));
        }
      }
    };
