import {ActionType, getType} from 'typesafe-actions';
import {defaultPeriodResolution, Period, TemporalResolution} from '../../components/dates/dateModels';
import {Maybe} from '../../helpers/Maybe';
import {Action} from '../../types/Types';
import {logoutUser} from '../../usecases/auth/authActions';
import {selectSavedSelectionAction, setThreshold} from '../user-selection/userSelectionActions';
import {SelectionInterval, ThresholdQuery, UserSelection} from '../user-selection/userSelectionModels';
import {selectResolution, setReportTimePeriod, toggleComparePeriod} from './reportActions';
import {ReportSector, TemporalReportState} from './reportModels';

type ActionTypes =
  ActionType<typeof selectSavedSelectionAction | typeof setThreshold | typeof logoutUser>
  | Action<TemporalResolution | SelectionInterval>;

export const initialState: TemporalReportState = {
  resolution: TemporalResolution.hour,
  shouldComparePeriod: false,
  timePeriod: {period: Period.yesterday},
};

const fromThreshold = (state: TemporalReportState, threshold?: ThresholdQuery): TemporalReportState =>
  Maybe.maybe(threshold)
    .filter(it => it.value !== '')
    .map(({dateRange: timePeriod}) => ({...state, timePeriod}))
    .orElse(initialState);

export const temporalReducerFor = (sector: ReportSector) =>
  (state: TemporalReportState = initialState, action: ActionTypes): TemporalReportState => {
    switch (action.type) {
      case getType(setReportTimePeriod(sector)):
        const timePeriod = (action as Action<SelectionInterval>).payload;
        return {...state, timePeriod, resolution: defaultPeriodResolution[timePeriod.period]};
      case getType(selectResolution(sector)):
        return {...state, resolution: (action as Action<TemporalResolution>).payload};
      case getType(toggleComparePeriod(sector)):
        return {...state, shouldComparePeriod: !state.shouldComparePeriod};
      case getType(selectSavedSelectionAction):
        return fromThreshold(state, (action as Action<UserSelection>).payload.selectionParameters.threshold);
      case getType(setThreshold):
        return fromThreshold(state, (action as Action<ThresholdQuery>).payload);
      case getType(logoutUser):
        return initialState;
      default:
        return state;
    }
  };
