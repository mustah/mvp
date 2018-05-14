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
};

export const currentVersion: number = 1;
