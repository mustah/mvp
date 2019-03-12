import {ActionType, getType} from 'typesafe-actions';
import {Period} from '../../../components/dates/dateModels';
import {logoutUser} from '../../auth/authActions';
import {setMeterDetailsTimePeriod} from './meterDetailActions';
import {MeterDetailState} from './meterDetailModels';

const initialState: MeterDetailState = {
  timePeriod: {period: Period.latest},
  isTimePeriodDefault: true,
};

type ActionTypes = ActionType<typeof setMeterDetailsTimePeriod | typeof logoutUser>;

export const meterDetail = (state: MeterDetailState = initialState, action: ActionTypes): MeterDetailState => {
  switch (action.type) {
    case getType(setMeterDetailsTimePeriod):
      return {
        ...state,
        timePeriod: action.payload,
        isTimePeriodDefault: false,
      };
    case getType(logoutUser):
      return initialState;
    default:
      return state;
  }
};
