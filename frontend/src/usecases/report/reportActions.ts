import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {restClient} from '../../services/restClient';

export const REPORTS_REQUEST = 'REPORTS_REQUEST';
export const REPORTS_SUCCESS = 'REPORTS_SUCCESS';
export const REPORTS_FAILURE = 'REPORTS_FAILURE';

const reportsRequest = createEmptyAction(REPORTS_REQUEST);
const reportsSuccess = createPayloadAction(REPORTS_SUCCESS);
const reportsFailure = createPayloadAction(REPORTS_FAILURE);

export const fetchReports = () => {
  return async (dispatch) => {
    dispatch(reportsRequest());
    try {
      const {data: reports} = await restClient.get('/reports');
      dispatch(reportsSuccess(reports));
    } catch (error) {
      const {response: {data}} = error;
      dispatch(reportsFailure(data));
    }
  };
};
