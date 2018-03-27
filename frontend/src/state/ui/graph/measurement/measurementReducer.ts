import {Action} from '../../../../types/Types';
import {SAVE_SELECTED_QUANTITIES} from './measurementActions';
import {Quantity} from './measurementModels';

export interface MeasurementState {
  isFetching: boolean;
  selectedQuantities: Quantity[];
}

export const initialMeasurementState: MeasurementState = {
  isFetching: false,
  selectedQuantities: [],
};

type MeasurementAction =
  Action<Quantity[]>;

export const measurements =
  (state: MeasurementState = initialMeasurementState, action: MeasurementAction): MeasurementState => {
    switch (action.type) {
      case SAVE_SELECTED_QUANTITIES:
        return {
          ...state,
          selectedQuantities: action.payload as Quantity[],
        };
      default:
        return state;
    }
  };
