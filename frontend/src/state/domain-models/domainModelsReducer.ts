import {EmptyAction} from 'react-redux-typescript';
import {combineReducers} from 'redux';
import {Action, ErrorResponse, uuid} from '../../types/Types';
import {ParameterName} from '../search/selection/selectionModels';
import {DomainModelsState, EndPoints, Normalized, NormalizedState, SelectionEntity} from './domainModels';
import {DOMAIN_MODELS_FAILURE, DOMAIN_MODELS_REQUEST, DOMAIN_MODELS_SUCCESS} from './domainModelsActions';
import {Gateway} from './gateway/gatewayModels';
import {Meter} from './meter/meterModels';

export const initialDomain = <T>(): NormalizedState<T> => ({
  result: [],
  entities: {},
  isFetching: false,
  total: 0,
});

const addDomainModelFor =
  <T>(entity: string, state: NormalizedState<T>, action: Action<Normalized<T>>): NormalizedState<T> => {
    const {payload} = action;
    const result: uuid[] = Array.isArray(payload.result) ? payload.result : payload.result[entity];
    const entities: any = payload.entities[entity];
    return {
      ...state,
      isFetching: false,
      entities,
      result,
      total: result.length,
    };
  };

type ActionTypes<T> =
  | EmptyAction<string>
  | Action<Normalized<T>>
  | Action<ErrorResponse>;

const reducerFor = <T>(entity: string, endPoint: EndPoints) =>
  (state: NormalizedState<T> = initialDomain<T>(), action: ActionTypes<T>): NormalizedState<T> => {
    switch (action.type) {
      case DOMAIN_MODELS_REQUEST.concat(endPoint):
        return {
          ...state,
          isFetching: true,
        };
      case DOMAIN_MODELS_SUCCESS.concat(endPoint):
        return addDomainModelFor<T>(entity, state, action as Action<Normalized<T>>);
      case DOMAIN_MODELS_FAILURE.concat(endPoint):
        return {
          ...state,
          isFetching: false,
          error: {...(action as Action<ErrorResponse>).payload},
        };
      default:
        return state;
    }
  };

export const addresses = reducerFor<SelectionEntity>(ParameterName.addresses, EndPoints.selections);
export const cities = reducerFor<SelectionEntity>(ParameterName.cities, EndPoints.selections);
export const alarms = reducerFor<SelectionEntity>(ParameterName.alarms, EndPoints.selections);
export const manufacturers = reducerFor<SelectionEntity>(ParameterName.manufacturers, EndPoints.selections);
export const productModels = reducerFor<SelectionEntity>(ParameterName.productModels, EndPoints.selections);
export const meterStatuses = reducerFor<SelectionEntity>(ParameterName.meterStatuses, EndPoints.selections);
export const gatewayStatuses = reducerFor<SelectionEntity>(ParameterName.gatewayStatuses, EndPoints.selections);
export const gateways = reducerFor<Gateway>('gateways', EndPoints.gateways);
export const meters = reducerFor<Meter>('meters', EndPoints.meters);

export const domainModels = combineReducers<DomainModelsState>({
  addresses,
  cities,
  alarms,
  manufacturers,
  productModels,
  meterStatuses,
  gatewayStatuses,
  gateways,
  meters,
});
