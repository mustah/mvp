import {combineReducers} from 'redux';
import {GatewaysState} from './gateway/gatewayModels';
import {gateways} from './gateway/gatewayReducer';
import {GeoDataState} from './geoData/geoDataModels';
import {geoData} from './geoData/geoDataReducer';
import {MetersState} from './meter/meterModels';
import {meters} from './meter/meterReducer';

export interface DomainModelsState {
  gateways: GatewaysState;
  meters: MetersState;
  geoData: GeoDataState;
}

export const domainModels = combineReducers<DomainModelsState>({
  gateways,
  meters,
  geoData,
});
