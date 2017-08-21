import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {restClient} from '../../api/restClient';
import {VALIDATION_FAILURE, VALIDATION_REQUEST, VALIDATION_SUCCESS} from '../../types/ActionTypes';

const validationRequest = createEmptyAction(VALIDATION_REQUEST);
const validationSuccess = createPayloadAction(VALIDATION_SUCCESS);
const validationFailure = createPayloadAction(VALIDATION_FAILURE);

export const fetchValidations = () => {
  return (dispatch) => {
    dispatch(validationRequest());

    restClient.get('/validations')
      .then(response => response.data)
      .then(validations => dispatch(validationSuccess(validations)))
      .catch(error => dispatch(validationFailure(error)));
  };
};
