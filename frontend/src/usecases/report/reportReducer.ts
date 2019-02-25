import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/types';
import {Period, TemporalResolution} from '../../components/dates/dateModels';
import {toggle} from '../../helpers/collections';
import {Maybe} from '../../helpers/Maybe';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {Action, uuid} from '../../types/Types';
import {LOGOUT_USER} from '../auth/authActions';
import {
  HIDE_ALL_LINES,
  REMOVE_SELECTED_LIST_ITEMS,
  SELECT_RESOLUTION,
  SET_SELECTED_ITEMS,
  setReportTimePeriod,
  TOGGLE_LINE
} from './reportActions';
import {LegendItem, Report, ReportState, SelectedReportPayload} from './reportModels';

export const initialState: ReportState = {
  isAllLinesHidden: false,
  hiddenLines: [],
  resolution: TemporalResolution.hour,
  savedReports: {},
  timePeriod: {
    period: Period.latest,
  },
};

const toggleLine = (state: ReportState, {payload}: Action<uuid>): ReportState => ({
  ...state,
  hiddenLines: toggle(payload, state.hiddenLines)
});

type ActionTypes =
  | Action<SelectedReportPayload | string[] | uuid | TemporalResolution | SelectionInterval>
  | EmptyAction<string>;

export const report = (state: ReportState = initialState, action: ActionTypes): ReportState => {
  switch (action.type) {
    case SET_SELECTED_ITEMS:
      const payload = (action as Action<SelectedReportPayload>).payload;
      return {
        ...state,
        savedReports: {
          meterPage: {
            id: 'meterPage',
            meters: payload.items,
          }
        }
      };
    case getType(setReportTimePeriod):
      return {
        ...state,
        timePeriod: {
          ...(action as Action<SelectionInterval>).payload,
        }
      };
    case SELECT_RESOLUTION:
      return {
        ...state,
        resolution: (action as Action<TemporalResolution>).payload,
      };
    case TOGGLE_LINE:
      return toggleLine(state, (action as Action<uuid>));
    case HIDE_ALL_LINES:
      const isAllLinesHidden = !state.isAllLinesHidden;
      const meterIds: uuid[] = Maybe.maybe(state.savedReports.meterPage)
        .map(({meters}: Report) => meters)
        .orElse([])
        .map(({id}: LegendItem) => id);
      return {...state, hiddenLines: isAllLinesHidden ? [...meterIds] : [], isAllLinesHidden};
    case REMOVE_SELECTED_LIST_ITEMS:
      return {...state, hiddenLines: [], savedReports: {}, isAllLinesHidden: false};
    case LOGOUT_USER:
      return initialState;
    default:
      return state;
  }
};
