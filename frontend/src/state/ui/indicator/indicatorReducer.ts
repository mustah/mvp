import {EmptyAction} from 'react-redux-typescript';
import {Action} from '../../../types/Types';
import {LOGOUT_USER} from '../../../usecases/auth/authActions';
import {SET_SELECTED_ENTRIES} from '../../../usecases/report/reportActions';
import {SelectedReportEntriesPayload} from '../../../usecases/report/reportModels';
import {RESET_SELECTION, SELECT_SAVED_SELECTION} from '../../user-selection/userSelectionActions';
import {UserSelection} from '../../user-selection/userSelectionModels';
import {Medium, Quantity} from '../graph/measurement/measurementModels';
import {SET_REPORT_INDICATOR_WIDGETS, SET_SELECTED_QUANTITIES} from './indicatorActions';

export interface IndicatorState {
  selectedIndicators: {
    report: Medium[],
  };
  selectedQuantities: Quantity[];
}

export const initialState: IndicatorState = {
  selectedIndicators: {
    report: [],
  },
  selectedQuantities: [],
};

type ActionTypes =
  | Action<Medium[]>
  | Action<Quantity[]>
  | Action<SelectedReportEntriesPayload>
  | Action<UserSelection>
  | EmptyAction<string>;

export const indicator = (state: IndicatorState = initialState, action: ActionTypes): IndicatorState => {
  switch (action.type) {
    case SET_REPORT_INDICATOR_WIDGETS:
      return {
        ...state,
        selectedIndicators: {
          report: [...((action as Action<Medium[]>).payload as Medium[])],
        },
      };
    case SET_SELECTED_ENTRIES:
      const payload: SelectedReportEntriesPayload = (action as Action<SelectedReportEntriesPayload>).payload;
      if (!payload.ids.length) {
        return {...initialState};
      }
      return {
        ...state,
        selectedIndicators: {
          report: [...payload.indicatorsToSelect],
        },
        selectedQuantities: [...payload.quantitiesToSelect],
      };
    case SET_SELECTED_QUANTITIES:
      return {
        ...state,
        selectedQuantities: (action as Action<Quantity[]>).payload as Quantity[],
      };
    case SELECT_SAVED_SELECTION:
    case RESET_SELECTION:
    case LOGOUT_USER:
      return {...initialState};
    default:
      return state;
  }
};
