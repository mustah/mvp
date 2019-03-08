import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/types';
import {Period, TemporalResolution} from '../../../components/dates/dateModels';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {Action} from '../../../types/Types';
import {logoutUser} from '../../auth/authActions';
import {setMeterDetailsTimePeriod} from './meterDetailActions';
import {MeterDetailState} from './meterDetailModels';

const initialState: MeterDetailState = {
  timePeriod: {period: Period.latest},
};

type ActionTypes = Action<TemporalResolution | SelectionInterval> | EmptyAction<string>;

export const meterDetail = (
  state: MeterDetailState = initialState,
  action: ActionTypes
): MeterDetailState => {
  switch (action.type) {
    case getType(setMeterDetailsTimePeriod):
      return {...state, timePeriod: {...(action as Action<SelectionInterval>).payload}};
    case getType(logoutUser):
      return initialState;
    default:
      return state;
  }
};
