import {combineReducers} from 'redux';
import {GatewaysState} from './gateway/gatewayModels';
import {gateways} from './gateway/gatewayReducer';
import {MetersState} from './meter/meterModels';
import {meters} from './meter/meterReducer';

export interface DomainModelsState {
  gateways: GatewaysState;
  meters: MetersState;
}

export const domainModels = combineReducers<DomainModelsState>({
  gateways,
  meters,
});
