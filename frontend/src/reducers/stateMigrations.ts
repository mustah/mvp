import {PersistedState} from 'redux-persist';

export const migrations = {
  1: (state: PersistedState | any) => ({
    ...state,
    ui: {
      ...state.ui,
      indicator: {
        selectedIndicators: {
          report: [],
        },
      },
    },
  }),
  2: (state: PersistedState | any) => {
    const quantities = [...state.ui.measurements.selectedQuantities];
    const newUiState = {...state.ui};
    delete newUiState.measurements;
    return {
      ...state,
      ui: {
        ...newUiState,
        indicator: {
          ...state.ui.indicator,
          selectedQuantities: quantities,
        },
      },
    };
  },
};

export const currentVersion: number = 2;
