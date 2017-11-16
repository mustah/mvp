import {AnyAction} from 'redux';
import {initialDomain} from '../domainModelsReducer';
import {GATEWAY_FAILURE, GATEWAY_REQUEST, GATEWAY_SUCCESS} from './gatewayActions';
import {Gateway, GatewaysState} from './gatewayModels';

export const gateways = (state: GatewaysState = initialDomain<Gateway>(), action: AnyAction) => {
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
