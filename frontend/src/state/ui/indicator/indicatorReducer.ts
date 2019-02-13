import {EmptyAction} from 'react-redux-typescript';
import {Action} from '../../../types/Types';
import {LOGOUT_USER} from '../../../usecases/auth/authActions';
import {REMOVE_SELECTED_LIST_ITEMS, SET_SELECTED_ENTRIES} from '../../../usecases/report/reportActions';
import {SelectedReportEntries} from '../../../usecases/report/reportModels';
import {getThresholdMedia} from '../../selection-tree/selectionTreeSelectors';
import {RESET_SELECTION, SELECT_SAVED_SELECTION, SET_THRESHOLD} from '../../user-selection/userSelectionActions';
import {ThresholdQuery, UserSelection} from '../../user-selection/userSelectionModels';
import {Medium, Quantity} from '../graph/measurement/measurementModels';
import {SET_REPORT_INDICATOR_WIDGETS, SET_SELECTED_QUANTITIES} from './indicatorActions';

export interface IndicatorState {
  selectedIndicators: {
    report: Medium[],
  };
  selectedQuantities: Quantity[];
}

export const initialState: IndicatorState = {
  selectedIndicators: {report: []},
  selectedQuantities: [],
};

type ActionTypes =
  | Action<Medium[] | Quantity[] | SelectedReportEntries | UserSelection | ThresholdQuery>
  | EmptyAction<string>;

export const indicator = (state: IndicatorState = initialState, action: ActionTypes): IndicatorState => {
  switch (action.type) {
    case SET_REPORT_INDICATOR_WIDGETS:
      return {
        ...state,
        selectedIndicators: {
          report: [...((action as Action<Medium[]>).payload)],
        },
      };
    case SET_SELECTED_ENTRIES:
      const {
        indicatorsToSelect,
        quantitiesToSelect
      }: SelectedReportEntries = (action as Action<SelectedReportEntries>).payload;
      return {
        ...state,
        selectedIndicators: {
          report: [...indicatorsToSelect],
        },
        selectedQuantities: quantitiesToSelect,
      };
    case SET_SELECTED_QUANTITIES:
      return {
        ...state,
        selectedQuantities: (action as Action<Quantity[]>).payload,
      };
    case SET_THRESHOLD:
      const thresholdQuery: ThresholdQuery = (action as Action<ThresholdQuery>).payload;
      return {
        ...state,
        selectedIndicators: {
          report: getThresholdMedia(thresholdQuery),
        },
        selectedQuantities: [thresholdQuery.quantity],
      };
    case REMOVE_SELECTED_LIST_ITEMS:
    case SELECT_SAVED_SELECTION:
    case RESET_SELECTION:
    case LOGOUT_USER:
      return initialState;
    default:
      return state;
  }
};
