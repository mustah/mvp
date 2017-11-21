import {AnyAction, combineReducers} from 'redux';
import {parameterNames} from '../search/selection/selectionModels';
import {DomainModelsState, EndPoints, NormalizedState, SelectionEntity} from './domainModels';
import {DOMAIN_MODELS_FAILURE, DOMAIN_MODELS_REQUEST, DOMAIN_MODELS_SUCCESS} from './domainModelsActions';
import {Gateway} from './gateway/gatewayModels';
import {Meter} from './meter/meterModels';

export const initialDomain = <T>(): NormalizedState<T> => ({
  result: [],
  entities: {},
  isFetching: false,
  total: 0,
});

const domainModelReducerFor = <T>(entity: string, endPoint: EndPoints) =>
  (state: NormalizedState<T> = initialDomain<T>(), action: AnyAction): any => {
    const {payload} = action;

    switch (action.type) {
      case DOMAIN_MODELS_REQUEST.concat(endPoint):
        return {
          ...state,
          isFetching: true,
        };
      case DOMAIN_MODELS_SUCCESS.concat(endPoint):
        const result = Array.isArray(payload.result) ? payload.result : payload.result[entity];
        return {
          ...state,
          isFetching: false,
          entities: payload.entities[entity],
          result,
          total: result.length,
        };
      case DOMAIN_MODELS_FAILURE.concat(endPoint):
        return {
          ...state,
          isFetching: false,
          error: {...payload},
        };
      default:
        return state;
    }
  };

export const addresses = domainModelReducerFor<SelectionEntity>(parameterNames.addresses, EndPoints.selections);
export const cities = domainModelReducerFor<SelectionEntity>(parameterNames.cities, EndPoints.selections);
export const alarms = domainModelReducerFor<SelectionEntity>(parameterNames.alarms, EndPoints.selections);
export const manufacturers = domainModelReducerFor<SelectionEntity>(parameterNames.manufacturers, EndPoints.selections);
export const productModels = domainModelReducerFor<SelectionEntity>(parameterNames.productModels, EndPoints.selections);
export const meterStatuses = domainModelReducerFor<SelectionEntity>(parameterNames.meterStatuses, EndPoints.selections);
export const gateways = domainModelReducerFor<Gateway>('gateways', EndPoints.gateways);
export const meters = domainModelReducerFor<Meter>('meters', EndPoints.meters);

export const domainModels = combineReducers<DomainModelsState>({
  addresses,
  cities,
  alarms,
  manufacturers,
  productModels,
  meterStatuses,
  gateways,
  meters,
});
