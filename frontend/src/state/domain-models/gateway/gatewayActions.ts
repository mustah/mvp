import {normalize} from 'normalizr';
import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {Dispatch} from 'redux';
import {restClient} from '../../../services/restClient';
import {makeUrl} from '../../../services/urlFactory';
import {gatewaySchema} from './gatewaySchema';

export const GATEWAY_REQUEST = 'GATEWAY_REQUEST';
export const GATEWAY_SUCCESS = 'GATEWAY_SUCCESS';
export const GATEWAY_FAILURE = 'GATEWAY_FAILURE';

const gatewayRequest = createEmptyAction(GATEWAY_REQUEST);
const gatewaySuccess = createPayloadAction(GATEWAY_SUCCESS);
const gatewayFailure = createPayloadAction(GATEWAY_FAILURE);

export const fetchGateways = (encodedUriParameters: string) =>
  async (dispatch: Dispatch<any>) => {
    try {
      dispatch(gatewayRequest());
      const {data: gateways} = await restClient.get(makeUrl('/gateways', encodedUriParameters));
      dispatch(gatewaySuccess({gateways: normalize(gateways, gatewaySchema)}));
    } catch (error) {
      dispatch(gatewayFailure(error));
    }
  };
