import {combineReducers} from 'redux';
import {GatewaysState} from './gateway/gatewayModels';
import {gateways} from './gateway/gatewayReducer';

export interface DomainModelsState {
  gateways: GatewaysState;
}

export const domainModels = combineReducers<DomainModelsState>({
  gateways,
});
