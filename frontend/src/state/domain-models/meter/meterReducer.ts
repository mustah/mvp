import {AnyAction} from 'redux';
import {MetersState} from './meterModels';
import {METER_REQUEST, METER_SUCCESS} from './meterActions';

const initialState: MetersState = {
  isFetching: false,
  total: 0,
  result: [],
  entities: {meters: {}},
};

export const meters = (state: MetersState = initialState, action: AnyAction) => {
  switch (action.type) {
    case METER_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    case METER_SUCCESS:
      const {meters} = action.payload;
      return {
        isFetching: false,
        total: meters.result.length, // TODO: a work around since we don't use pagination form db.json.
        // Got total from that before
        ...meters,
      };
    default:
      return state;
  }
};
