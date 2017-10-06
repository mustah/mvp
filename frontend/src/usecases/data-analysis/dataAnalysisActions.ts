import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {restClient} from '../../services/restClient';
import {DATA_ANALYSIS_FAILURE, DATA_ANALYSIS_REQUEST, DATA_ANALYSIS_SUCCESS} from '../../types/ActionTypes';

const dataAnalysisRequest = createEmptyAction(DATA_ANALYSIS_REQUEST);
const dataAnalysisSuccess = createPayloadAction(DATA_ANALYSIS_SUCCESS);
const dataAnalysisFailure = createPayloadAction(DATA_ANALYSIS_FAILURE);

export const fetchDataAnalysis = () => {
  return (dispatch) => {
    dispatch(dataAnalysisRequest());

    restClient.get('/validations')
      .then(response => response.data)
      .then(dataAnalysis => dispatch(dataAnalysisSuccess(dataAnalysis)))
      .catch(error => dispatch(dataAnalysisFailure(error)));
  };
};
