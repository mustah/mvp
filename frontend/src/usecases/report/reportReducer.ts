import {EmptyAction} from 'react-redux-typescript';
import {TemporalResolution} from '../../components/dates/dateModels';
import {toggle} from '../../helpers/collections';
import {resetReducer} from '../../state/domain-models/domainModelsReducer';
import {NormalizedSelectionTree} from '../../state/selection-tree/selectionTreeModels';
import {SELECT_PERIOD, SET_CUSTOM_DATE_RANGE} from '../../state/user-selection/userSelectionActions';
import {Action, uuid} from '../../types/Types';
import {LOGOUT_USER} from '../auth/authActions';
import {SELECT_RESOLUTION, SET_SELECTED_ENTRIES, TOGGLE_LINE} from './reportActions';
import {ReportState, SelectedReportEntriesPayload} from './reportModels';

export const initialState: ReportState = {
  selectedListItems: [],
  hiddenLines: [],
  resolution: TemporalResolution.hour,
};

const toggleLine = (state: ReportState, {payload}: Action<uuid>): ReportState => ({
  ...state,
  hiddenLines: toggle(payload, state.hiddenLines)
});

type ActionTypes =
  | Action<SelectedReportEntriesPayload | string[] | uuid | TemporalResolution | NormalizedSelectionTree>
  | EmptyAction<string>;

export const report = (state: ReportState = initialState, action: ActionTypes): ReportState => {
  switch (action.type) {
    case SET_SELECTED_ENTRIES:
      return {
        ...state,
        selectedListItems: [...(action as Action<SelectedReportEntriesPayload>).payload.ids],
      };
    case SELECT_RESOLUTION:
      return {
        ...state,
        resolution: (action as Action<TemporalResolution>).payload,
      };
    case TOGGLE_LINE:
      return toggleLine(state, (action as Action<uuid>));
    case LOGOUT_USER:
      return initialState;
    case SELECT_PERIOD:
    case SET_CUSTOM_DATE_RANGE:
      return state;
    default:
      return resetReducer<ReportState>(state, action, initialState);
  }
};
