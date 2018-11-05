import {Action} from '../../../types/Types';
import {SET_SELECTED_ENTRIES} from '../../../usecases/report/reportActions';
import {SelectedReportEntriesPayload} from '../../../usecases/report/reportModels';
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

type ActionTypes = Action<Medium[]> | Action<Quantity[]> | Action<SelectedReportEntriesPayload>;

export const indicator = (state: IndicatorState = initialState, action: ActionTypes): IndicatorState => {
  switch (action.type) {
    case SET_REPORT_INDICATOR_WIDGETS:
      return {
        ...state,
        selectedIndicators: {
          report: [...(action.payload as Medium[])],
        },
      };
    case SET_SELECTED_ENTRIES:
      const payload: SelectedReportEntriesPayload = action.payload as SelectedReportEntriesPayload;
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
        selectedQuantities: action.payload as Quantity[],
      };
    default:
      return state;
  }
};
