import {AnyAction} from 'redux';
import {Meter, MetersState} from './meterModels';
import {METER_FAILURE, METER_REQUEST, METER_SUCCESS} from './meterActions';
import {initialDomain} from '../domainModelsReducer';

export const meters = (state: MetersState = initialDomain<Meter>(), action: AnyAction) => {
  const {payload} = action;
  switch (action.type) {
    case METER_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    case METER_SUCCESS:
      const {meters: {result, entities}} = payload;
      return {
        isFetching: false,
        total: result.length,
        result,
        entities: entities.meters,
      };
    case METER_FAILURE:
      return {
        ...state,
        isFetching: false,
        error: {...payload},
      };
    default:
      return state;
  }
};
