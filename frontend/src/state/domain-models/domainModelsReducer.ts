import {AnyAction, combineReducers} from 'redux';
import {parameterNames} from '../search/selection/selectionModels';
import {DOMAIN_MODELS_FAILURE, DOMAIN_MODELS_REQUEST, DOMAIN_MODELS_SUCCESS} from './domainModelsActions';
import {SelectionEntityState} from './domainModelsModels';
import {GatewaysState} from './gateway/gatewayModels';
import {gateways} from './gateway/gatewayReducer';
import {MetersState} from './meter/meterModels';
import {meters} from './meter/meterReducer';

export const initialState: SelectionEntityState = {
  result: [],
  entities: {},
  isFetching: false,
  total: 0,
};

const domainModelReducerFor = (entity: string, state: SelectionEntityState, action: AnyAction): any => {
  const {payload} = action;

  switch (action.type) {
    case DOMAIN_MODELS_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    case DOMAIN_MODELS_SUCCESS:
      return {
        ...state,
        isFetching: false,
        entities: payload.entities[entity],
        result: payload.result[entity],
        total: payload.result[entity].length,
      };
    case DOMAIN_MODELS_FAILURE:
      return {
        ...state,
        isFetching: false,
        error: {...payload},
      };
    default:
      return state;
  }
};

export const addresses = (state: SelectionEntityState = initialState, action: AnyAction): SelectionEntityState =>
  domainModelReducerFor(parameterNames.addresses, state, action);

export const cities = (state: SelectionEntityState = initialState, action: AnyAction): SelectionEntityState =>
  domainModelReducerFor(parameterNames.cities, state, action);

export const alarms = (state: SelectionEntityState = initialState, action: AnyAction): SelectionEntityState =>
  domainModelReducerFor(parameterNames.alarms, state, action);

export interface DomainModelsState {
  gateways: GatewaysState;
  meters: MetersState;
  addresses: SelectionEntityState;
  cities: SelectionEntityState;
  alarms: SelectionEntityState;
}

export const domainModels = combineReducers<DomainModelsState>({
  gateways,
  meters,
  addresses,
  cities,
  alarms,
});
