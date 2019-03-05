import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/types';
import {Period, TemporalResolution} from '../../components/dates/dateModels';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {Action} from '../../types/Types';
import {logoutUser} from '../auth/authActions';
import {selectResolution, setReportTimePeriod, toggleComparePeriod} from './reportActions';
import {TemporalReportState} from './reportModels';

type ActionTypes = Action<TemporalResolution | SelectionInterval | undefined> | EmptyAction<string>;

export const initialState: TemporalReportState = {
  resolution: TemporalResolution.hour,
  shouldComparePeriod: false,
  timePeriod: {period: Period.latest},
};

export const temporal =
  (state: TemporalReportState = initialState, action: ActionTypes): TemporalReportState => {
    switch (action.type) {
      case getType(setReportTimePeriod):
        return {...state, timePeriod: {...(action as Action<SelectionInterval>).payload}};
      case getType(selectResolution):
        return {...state, resolution: (action as Action<TemporalResolution>).payload};
      case getType(toggleComparePeriod):
        return {...state, shouldComparePeriod: !state.shouldComparePeriod};
      case getType(logoutUser):
        return initialState;
      default:
        return state;
    }
  };
