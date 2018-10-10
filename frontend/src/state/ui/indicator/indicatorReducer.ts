import {Medium} from '../../../components/indicators/indicatorWidgetModels';
import {Action} from '../../../types/Types';
import {SelectedEntriesPayload, SET_SELECTED_ENTRIES} from '../../../usecases/report/reportActions';
import {Quantity} from '../graph/measurement/measurementModels';
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

type ActionTypes = Action<Medium[]> | Action<Quantity[]> | Action<SelectedEntriesPayload>;

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
      const selectedEntries: SelectedEntriesPayload = action.payload as SelectedEntriesPayload;
      if (!selectedEntries.ids.length) {
        return {...initialState};
      }
      return {
        ...state,
        selectedIndicators: {
          report: [...selectedEntries.indicatorsToSelect],
        },
        selectedQuantities: [...selectedEntries.quantitiesToSelect],
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
