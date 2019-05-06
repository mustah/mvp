import {ActionType, getType} from 'typesafe-actions';
import {isOnMeterDetailsPage} from '../../../app/routes';
import {Period, TemporalResolution} from '../../../components/dates/dateModels';
import {locationChange} from '../../../state/location/locationActions';
import {logoutUser} from '../../auth/authActions';
import * as actions from './meterDetailActions';
import {MeterDetailState} from './meterDetailModels';

export const initialState: MeterDetailState = {
  isDirty: false,
  resolution: TemporalResolution.hour,
  timePeriod: {period: Period.latest},
};

type ActionTypes = ActionType<typeof actions | typeof logoutUser | typeof locationChange>;

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
    case getType(locationChange):
      return isOnMeterDetailsPage(action.payload.pathname) ? state : initialState;
    case getType(logoutUser):
      return initialState;
    default:
      return state;
  }
};
