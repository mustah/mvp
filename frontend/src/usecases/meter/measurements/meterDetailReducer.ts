import {last} from 'lodash';
import {ActionType, getType} from 'typesafe-actions';
import {isOnMeterDetailsPage} from '../../../app/routes';
import {defaultPeriodResolution, Period, TemporalResolution} from '../../../components/dates/dateModels';
import {locationChange} from '../../../state/location/locationActions';
import {logoutUser} from '../../auth/authActions';
import {selectResolution, setTimePeriod} from './meterDetailMeasurementActions';
import {MeterDetailState} from './meterDetailModels';

export const initialState: MeterDetailState = {
  isDirty: false,
  resolution: TemporalResolution.hour,
  timePeriod: {period: Period.yesterday},
};

type ActionTypes = ActionType<typeof setTimePeriod
  | typeof selectResolution
  | typeof logoutUser
  | typeof locationChange>;

export const meterDetail = (state: MeterDetailState = initialState, action: ActionTypes): MeterDetailState => {
  switch (action.type) {
    case getType(setTimePeriod):
      const timePeriod = action.payload;
      return {
        ...state,
        isDirty: true,
        timePeriod,
        resolution: defaultPeriodResolution[timePeriod.period],
      };
    case getType(selectResolution):
      return {
        ...state,
        isDirty: true,
        resolution: action.payload,
      };
    case getType(locationChange):
      return isOnMeterDetailsPage(action.payload.location.pathname)
        ? {...state, selectedMeterId: last(action.payload.location.pathname.split('/'))}
        : {...initialState, selectedMeterId: state.selectedMeterId};
    case getType(logoutUser):
      return initialState;
    default:
      return state;
  }
};
