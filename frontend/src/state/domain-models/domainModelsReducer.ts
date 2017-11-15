import {combineReducers} from 'redux';
import {GatewaysState} from './gateway/gatewayModels';
import {gateways} from './gateway/gatewayReducer';
import {AddressState, IdNamedState} from './geoData/geoDataModels';
import {addresses, alarms, cities} from './geoData/geoDataReducer';
import {MetersState} from './meter/meterModels';
import {meters} from './meter/meterReducer';

export interface DomainModelsState {
  gateways: GatewaysState;
  meters: MetersState;
  addresses: AddressState;
  cities: IdNamedState;
  alarms: IdNamedState;
}

export const domainModels = combineReducers<DomainModelsState>({
  gateways,
  meters,
  addresses,
  cities,
  alarms,
});
