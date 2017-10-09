import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {restClient} from '../../services/restClient';

export const REPORTS_REQUEST = 'REPORTS_REQUEST';
export const REPORTS_SUCCESS = 'REPORTS_SUCCESS';
export const REPORTS_FAILURE = 'REPORTS_FAILURE';

const reportsRequest = createEmptyAction(REPORTS_REQUEST);
const reportsSuccess = createPayloadAction(REPORTS_SUCCESS);
const reportsFailure = createPayloadAction(REPORTS_FAILURE);

export const fetchReports = () => {
  return (dispatch) => {
    dispatch(reportsRequest());

    restClient.get('/reports')
      .then(response => response.data)
      .then(reports => dispatch(reportsSuccess(reports)))
      .catch(error => dispatch(reportsFailure(error)));
  };
};
