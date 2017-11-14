import {AnyAction} from 'redux';
import {IndicatorType} from '../../../usecases/common/components/indicators/models/widgetModels';
import {SELECT_INDICATOR_WIDGET} from './indicatorActions';

export interface SelectedIndicators {
  dashboard?: IndicatorType | null;
  report?: IndicatorType | null;
}

export interface IndicatorState {
  selectedIndicators: SelectedIndicators;
}

export const initialState = {
  selectedIndicators: {
    dashboard: null,
    report: null,
  },
};

export const indicator = (state: IndicatorState = initialState, action: AnyAction): IndicatorState => {
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
