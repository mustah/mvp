import {combineReducers} from 'redux';
import {GatewaysState} from './gateway/gatewayModels';
import {gateways} from './gateway/gatewayReducer';
import {GeoDataState, IdNamedState} from './geoData/geoDataModels';
import {alarms, geoData} from './geoData/geoDataReducer';
import {MetersState} from './meter/meterModels';
import {meters} from './meter/meterReducer';

export interface DomainModelsState {
  gateways: GatewaysState;
  meters: MetersState;
  geoData: GeoDataState;
  alarms: IdNamedState;
}

export const domainModels = combineReducers<DomainModelsState>({
  gateways,
  meters,
  geoData,
  alarms,
});
