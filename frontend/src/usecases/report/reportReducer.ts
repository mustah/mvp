import {EmptyAction} from 'react-redux-typescript';
import {TemporalResolution} from '../../components/dates/dateModels';
import {toggle} from '../../helpers/collections';
import {resetReducer} from '../../state/domain-models/domainModelsReducer';
import {NormalizedSelectionTree} from '../../state/selection-tree/selectionTreeModels';
import {SELECT_PERIOD, SET_CUSTOM_DATE_RANGE} from '../../state/user-selection/userSelectionActions';
import {Action, uuid} from '../../types/Types';
import {
  HIDE_ALL_LINES,
  REMOVE_SELECTED_LIST_ITEMS,
  SELECT_RESOLUTION,
  SET_SELECTED_ENTRIES,
  TOGGLE_LINE
} from './reportActions';
import {ReportState, SelectedReportEntries} from './reportModels';

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
  | Action<SelectedReportEntries | string[] | uuid | TemporalResolution | NormalizedSelectionTree>
  | EmptyAction<string>;

export const report = (state: ReportState = initialState, action: ActionTypes): ReportState => {
  switch (action.type) {
    case SET_SELECTED_ENTRIES:
      return {
        ...state,
        selectedListItems: [...(action as Action<SelectedReportEntries>).payload.ids],
      };
    case SELECT_RESOLUTION:
      return {
        ...state,
        resolution: (action as Action<TemporalResolution>).payload,
      };
    case TOGGLE_LINE:
      return toggleLine(state, (action as Action<uuid>));
    case HIDE_ALL_LINES:
      return {...state, hiddenLines: [...state.selectedListItems]};
    case REMOVE_SELECTED_LIST_ITEMS:
      return {...state, selectedListItems: [], hiddenLines: []};
    case SELECT_PERIOD:
    case SET_CUSTOM_DATE_RANGE:
      return state;
    default:
      return resetReducer<ReportState>(state, action, initialState);
  }
};
