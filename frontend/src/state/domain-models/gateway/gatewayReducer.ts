import {AnyAction} from 'redux';
import {GATEWAY_REQUEST, GATEWAY_SUCCESS} from './gatewayActions';
import {GatewaysState} from './gatewayModels';

const initialState: GatewaysState = {
  isFetching: false,
  total: 0,
  result: [],
  entities: {gateways: {}},
};

export const gateways = (state: GatewaysState = initialState, action: AnyAction) => {
  switch (action.type) {
    case GATEWAY_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    case GATEWAY_SUCCESS:
      const {gateways} = action.payload;
      return {
        isFetching: false,
        total: gateways.result.length, // TODO: a work around since we don't use pagination form db.json.
        // Got total from that before
        ...gateways,
      };
    default:
      return state;
  }
};
