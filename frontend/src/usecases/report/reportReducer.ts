import {EmptyAction} from 'react-redux-typescript';
import {TemporalResolution} from '../../components/dates/dateModels';
import {toggle} from '../../helpers/collections';
import {Maybe} from '../../helpers/Maybe';
import {Action, uuid} from '../../types/Types';
import {LOGOUT_USER} from '../auth/authActions';
import {
  HIDE_ALL_LINES,
  REMOVE_SELECTED_LIST_ITEMS,
  SELECT_RESOLUTION,
  SET_SELECTED_ITEMS,
  TOGGLE_LINE
} from './reportActions';
import {LegendItem, Report, ReportState, SelectedReportPayload} from './reportModels';

export const initialState: ReportState = {
  hiddenLines: [],
  resolution: TemporalResolution.hour,
  savedReports: {},
};

const toggleLine = (state: ReportState, {payload}: Action<uuid>): ReportState => ({
  ...state,
  hiddenLines: toggle(payload, state.hiddenLines)
});

type ActionTypes =
  | Action<SelectedReportPayload | string[] | uuid | TemporalResolution>
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
    case SELECT_RESOLUTION:
      return {
        ...state,
        resolution: (action as Action<TemporalResolution>).payload,
      };
    case TOGGLE_LINE:
      return toggleLine(state, (action as Action<uuid>));
    case HIDE_ALL_LINES:
      const meterIds: uuid[] = Maybe.maybe(state.savedReports.meterPage)
        .map(({meters}: Report) => meters)
        .orElse([])
        .map(({id}: LegendItem) => id);
      return {...state, hiddenLines: [...meterIds]};
    case REMOVE_SELECTED_LIST_ITEMS:
      return {...state, hiddenLines: [], savedReports: {}};
    case LOGOUT_USER:
      return initialState;
    default:
      return state;
  }
};
