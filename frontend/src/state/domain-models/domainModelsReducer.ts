import {gateways, GatewaysState} from './gateway/gatewayReducer';
import {combineReducers} from 'redux';

export interface DomainModels {
  gateways: GatewaysState;
}

export const domainModels = combineReducers<DomainModels>({
  gateways,
});
