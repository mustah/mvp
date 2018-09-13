import {PersistedState} from 'redux-persist';
import {
  mapSelectedIdToAddress,
  mapSelectedIdToCity,
} from '../state/domain-models/selections/selectionsApiActions';
import {OldSelectionParameters, UserSelection} from '../state/user-selection/userSelectionModels';
import {IdNamed, toIdNamed, uuid} from '../types/Types';

const convert = (name: keyof OldSelectionParameters, selectionParameters): IdNamed[] => {
  return selectionParameters[name]
    ? selectionParameters[name].map((id: uuid) => ({...toIdNamed(id as string)}))
    : [];
};

export const oldParameterNames: Array<keyof OldSelectionParameters> = [
  'facilities',
  'gatewaySerials',
  'media',
  'secondaryAddresses',
];

export const migrateFromUuidToIdNamed = (selectionParameters) =>
  oldParameterNames.reduce((accumulated, name) =>
    ({
      ...accumulated,
      [name]: [...convert(name as keyof OldSelectionParameters, selectionParameters)],
    }), {});

export const migrateUserSelection = (oldUserSelection): UserSelection => {
  const {selectionParameters} = oldUserSelection;
  return {
    ...oldUserSelection,
    selectionParameters: {
      ...selectionParameters,
      ...migrateFromUuidToIdNamed(selectionParameters),
      addresses: [...selectionParameters.addresses.map(mapSelectedIdToAddress)],
      cities: [...selectionParameters.cities.map(mapSelectedIdToCity)],
    },
  };
};

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
  3: (state: PersistedState | any) => {
    const {userSelection: {userSelection}} = state;
    return {
      ...state,
      userSelection: {
        userSelection: migrateUserSelection(userSelection),
      },
    };
  },
};

export const currentVersion: number = 3;
