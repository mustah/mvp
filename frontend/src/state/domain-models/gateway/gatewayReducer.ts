import {AnyAction} from 'redux';
import {GATEWAY_FAILURE, GATEWAY_REQUEST, GATEWAY_SUCCESS} from './gatewayActions';
import {GatewaysState} from './gatewayModels';

const initialState: GatewaysState = {
  isFetching: false,
  total: 0,
  result: [],
  entities: {},
};

export const gateways = (state: GatewaysState = initialState, action: AnyAction) => {
  const {payload} = action;
  switch (action.type) {
    case GATEWAY_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    case GATEWAY_SUCCESS:
      const {gateways: {result, entities}} = payload;
      return {
        isFetching: false,
        total: result.length,
        result,
        entities: entities.gateways,
      };
    case GATEWAY_FAILURE:
      return {
        ...state,
        isFetching: false,
        error: {...payload},
      };
    default:
      return state;
  }
};
