import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/type-helpers';
import {Period, TemporalResolution} from '../../components/dates/dateModels';
import {Action} from '../../types/Types';
import {logoutUser} from '../../usecases/auth/authActions';
import {SelectionInterval} from '../user-selection/userSelectionModels';
import {selectResolution, setReportTimePeriod, toggleComparePeriod} from './reportActions';
import {ReportSector, TemporalReportState} from './reportModels';

type ActionTypes = Action<TemporalResolution | SelectionInterval> | EmptyAction<string>;

export const initialState: TemporalReportState = {
  resolution: TemporalResolution.hour,
  shouldComparePeriod: false,
  timePeriod: {period: Period.latest},
};

export const temporalReducerFor =
  (sector: ReportSector) =>
    (state: TemporalReportState = initialState, action: ActionTypes): TemporalReportState => {
      switch (action.type) {
        case getType(setReportTimePeriod(sector)):
          return {...state, timePeriod: {...(action as Action<SelectionInterval>).payload}};
        case getType(selectResolution(sector)):
          return {...state, resolution: (action as Action<TemporalResolution>).payload};
        case getType(toggleComparePeriod(sector)):
          return {...state, shouldComparePeriod: !state.shouldComparePeriod};
        case getType(logoutUser):
          return initialState;
        default:
          return state;
      }
    };
