import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {restClient} from '../../services/restClient';
import {
  COLLECTION_FAILURE,
  COLLECTION_REQUEST,
  COLLECTION_SUCCESS,
  GATEWAY_FAILURE,
  GATEWAY_REQUEST,
  GATEWAY_SUCCESS,
} from '../../types/ActionTypes';

const collectionRequest = createEmptyAction(COLLECTION_REQUEST);
const collectionSuccess = createPayloadAction(COLLECTION_SUCCESS);
const collectionFailure = createPayloadAction(COLLECTION_FAILURE);

const gatewayRequest = createEmptyAction(GATEWAY_REQUEST);
const gatewaySuccess = createPayloadAction(GATEWAY_SUCCESS);
const gatewayFailure = createPayloadAction(GATEWAY_FAILURE);

export const fetchCollections = () => {
  return (dispatch) => {
    dispatch(collectionRequest());

    restClient.get('/collections')
      .then(response => response.data)
      .then(collections => dispatch(collectionSuccess(collections)))
      .catch(error => dispatch(collectionFailure(error)));
  };
};

export const fetchGateways = () => {
  return (dispatch) => {
    dispatch(gatewayRequest());

    restClient.get('/gateways')
      .then(response => response.data)
      .then(gateways => dispatch(gatewaySuccess(gateways)))
      .catch(error => dispatch(gatewayFailure(error)));
  };
};
