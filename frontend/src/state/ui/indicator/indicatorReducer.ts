import {Medium} from '../../../components/indicators/indicatorWidgetModels';
import {Action, uuid} from '../../../types/Types';
import {SET_SELECTED_ENTRIES} from '../../../usecases/report/reportActions';
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

type ActionTypes = Action<Medium[]> | Action<Quantity[]> | Action<uuid[]>;

export const indicator = (state: IndicatorState = initialState, action: ActionTypes): IndicatorState => {
  // TODO listen in on SET_SELECTED_ENTRIES and auto toggle some indicators/quantities if needed..
  //   modify the payload to include what's needed to select indicators
  switch (action.type) {
    case SET_REPORT_INDICATOR_WIDGETS:
      return {
        ...state,
        selectedIndicators: {
          report: [...(action.payload as Medium[])],
        },
      };
    case SET_SELECTED_ENTRIES:
      return (action.payload as uuid[]).length ? state : {...initialState};
    case SET_SELECTED_QUANTITIES:
      return {
        ...state,
        selectedQuantities: action.payload as Quantity[],
      };
    default:
      return state;
  }
};
