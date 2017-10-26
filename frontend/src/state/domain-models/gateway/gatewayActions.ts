import {normalize} from 'normalizr';
import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {filterToUri, restClient} from '../../../services/restClient';
import {gatewaySchema} from './gatewaySchema';

export const GATEWAY_REQUEST = 'GATEWAY_REQUEST';
export const GATEWAY_SUCCESS = 'GATEWAY_SUCCESS';
export const GATEWAY_FAILURE = 'GATEWAY_FAILURE';

const gatewayRequest = createEmptyAction(GATEWAY_REQUEST);
const gatewaySuccess = createPayloadAction(GATEWAY_SUCCESS);
const gatewayFailure = createPayloadAction(GATEWAY_FAILURE);

export const fetchGateways = (filter) => {
  return (dispatch) => {
    dispatch(gatewayRequest());

    const parameters = {
      ...filter,
    };

    restClient.get(filterToUri('/gateways', parameters))
      .then(response => {
        const gateways = response.data;
        return dispatch(gatewaySuccess({
          gateways: normalize(gateways, gatewaySchema),
        }));
      })
      .catch(error => dispatch(gatewayFailure(error)));
  };
};
