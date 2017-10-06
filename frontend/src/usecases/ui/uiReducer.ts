import {AnyAction} from 'redux';
import {IndicatorType} from '../common/components/indicators/models/IndicatorModels';
import {SELECT_INDICATOR_WIDGET} from './uiActions';

export interface SelectedIndicators {
  dashboard?: IndicatorType | null;
  report?: IndicatorType | null;
}

export interface UiState {
  selectedIndicators: SelectedIndicators;
}

export const initialState = {
  selectedIndicators: {
    dashboard: null,
    report: null,
  },
};

export const ui = (state: UiState = initialState, action: AnyAction): UiState => {
  const {payload, type} = action;

  switch (type) {
    case SELECT_INDICATOR_WIDGET:
      return {
        ...state,
        selectedIndicators: {
          ...state.selectedIndicators,
          ...payload,
        },
      };
    default:
      return state;
  }
};
