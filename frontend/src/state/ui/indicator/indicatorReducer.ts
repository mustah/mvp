import {Medium} from '../../../components/indicators/indicatorWidgetModels';
import {Action} from '../../../types/Types';
import {Quantity} from '../graph/measurement/measurementModels';
import {SET_SELECTED_QUANTITIES, SET_REPORT_INDICATOR_WIDGETS} from './indicatorActions';

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

type ActionTypes = Action<Medium[]> | Action<Quantity[]>;

export const indicator = (state: IndicatorState = initialState, action: ActionTypes): IndicatorState => {
  switch (action.type) {
    case SET_REPORT_INDICATOR_WIDGETS:
      return {
        ...state,
        selectedIndicators: {
          report: [...(action.payload as Medium[])],
        },
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
