import {ActionType, getType} from 'typesafe-actions';
import {Period, TemporalResolution} from '../../../components/dates/dateModels';
import {logoutUser} from '../../auth/authActions';
import * as actions from './meterDetailActions';
import {MeterDetailState} from './meterDetailModels';

const initialState: MeterDetailState = {
  timePeriod: {period: Period.latest},
  resolution: TemporalResolution.hour,
  isDirty: false,
};

type ActionTypes = ActionType<typeof actions | typeof logoutUser>;

export const meterDetail = (state: MeterDetailState = initialState, action: ActionTypes): MeterDetailState => {
  switch (action.type) {
    case getType(actions.setTimePeriod):
      return {
        ...state,
        isDirty: true,
        timePeriod: action.payload,
      };
    case getType(actions.selectResolution):
      return {
        ...state,
        isDirty: true,
        resolution: action.payload,
      };
    case getType(logoutUser):
      return initialState;
    default:
      return state;
  }
};
