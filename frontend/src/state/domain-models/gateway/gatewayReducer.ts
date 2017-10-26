import {AnyAction} from 'redux';
import {GATEWAY_REQUEST, GATEWAY_SUCCESS} from './gatewayActions';

interface Gateway {
  [key: string]: {
    id: string;
    facility: string;
    address: string;
    city: string;
    productModel: string;
    telephoneNo: string;
    ip: string;
    port: string;
    status: string;
    position: string;
  };
}

export interface Gateways {
  result: string[];
  entities: {
    gateways: Gateway;
  };
}

export interface GatewaysState {
  isFetching: boolean;
  total: number;
  gateways: Gateways;
}

const initialState: GatewaysState = {
  isFetching: false,
  total: 0,
  gateways: {result: [], entities: {gateways: {}}},
};

export const gateways = (state: GatewaysState = initialState, action: AnyAction) => {
  switch (action.type) {
    case GATEWAY_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    case GATEWAY_SUCCESS: // TODO: should add fetched data to existing gateways.
      const {gateways, total} = action.payload;
      return {
        isFetching: false,
        total,
        gateways, // TODO: Does this need to use spread?
      };
    default:
      return state;
  }
};
