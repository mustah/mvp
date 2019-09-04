import {Omit} from 'utility-types';
import {Period} from '../components/dates/dateModels';
import {mapSelectedIdToAddress, mapSelectedIdToCity} from '../state/domain-models/selections/selectionsApiActions';
import {initialState} from '../state/ui/tabs/tabsReducer';
import {ToolbarState, ToolbarView} from '../state/ui/toolbar/toolbarModels';
import {OldSelectionParameters, UserSelection} from '../state/user-selection/userSelectionModels';
import {IdNamed, toIdNamed, uuid} from '../types/Types';

const convert = (name: keyof OldSelectionParameters, selectionParameters): IdNamed[] =>
  selectionParameters[name]
    ? selectionParameters[name].map((id: uuid) => ({...toIdNamed(id as string)}))
    : [];

const toolbarState: Omit<ToolbarState, 'meterMeasurement'> = {
  measurement: {view: ToolbarView.graph},
  collection: {view: ToolbarView.graph},
  meterCollection: {view: ToolbarView.graph},
  selectionReport: {view: ToolbarView.graph},
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

export const toNewPeriod = (period: string): string => {
  switch (period) {
    case 'current_month':
      return Period.currentMonth;
    case 'previous_month':
      return Period.previousMonth;
    case 'previous_7_days':
      return Period.previous7Days;
    case 'current_week':
    case 'latest':
    default:
      return Period.yesterday;
  }
};

export const migrations = {
  1: (state) => ({
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
  2: (state) => {
    const selectedQuantities = [...state.ui.measurements.selectedQuantities];
    const newUiState = {...state.ui};
    delete newUiState.measurements;
    return {
      ...state,
      ui: {
        ...newUiState,
        indicator: {
          ...state.ui.indicator,
          selectedQuantities,
        },
      },
    };
  },
  3: (state) => {
    const {userSelection: {userSelection}} = state;
    return {
      ...state,
      userSelection: {
        userSelection: migrateUserSelection(userSelection),
      },
    };
  },
  4: (state) =>
    ({
      ...state,
      ui: {
        ...state.ui,
        tabs: {
          ...state.ui.tabs,
          report: initialState.report.selectedTab,
        },
      },
    }),
  5: (state) => {
    const {userSelection: {userSelection}} = state;
    const selectionParameters = userSelection.selectionParameters;
    return {
      ...state,
      userSelection: {
        userSelection: {
          ...userSelection,
          selectionParameters: {
            ...selectionParameters,
            threshold: selectionParameters.threshold
              ? {...selectionParameters.threshold, dateRange: selectionParameters.dateRange}
              : undefined
          }
        }
      },
    };
  },
  6: (state) => {
    const {userSelection: {userSelection}} = state;
    const selectionParameters = userSelection.selectionParameters;
    return {
      ...state,
      userSelection: {
        userSelection: {
          ...userSelection,
          selectionParameters: {
            ...selectionParameters,
            dateRange: {period: Period.now}
          }
        }
      },
    };
  },
  7: (state) => {
    const {ui} = state;
    const toolbar = ui.toolbar;
    return {
      ...state,
      ui: {
        ...ui,
        toolbar: {
          ...toolbar,
          meterMeasurement: toolbar.meterMeasurement || {view: ToolbarView.table}
        }
      }
    };
  },
  8: (state) => {
    const {ui} = state;
    const toolbar = ui.toolbar || toolbarState;
    return {
      ...state,
      ui: {
        ...ui,
        toolbar: {
          ...toolbar,
          meterMeasurement: toolbar.meterMeasurement || {view: ToolbarView.table},
        }
      }
    };
  },
  9: (state) => {
    const {ui} = state;
    const toolbar = ui.toolbar || toolbarState;
    return {
      ...state,
      ui: {
        ...ui,
        toolbar: {
          ...toolbar,
          selectionReport: toolbar.selectionReport || {view: ToolbarView.graph},
        }
      }
    };
  },
  10: (state) => {
    const {ui} = state;
    const toolbar = ui.toolbar;
    return {
      ...state,
      ui: {
        ...ui,
        toolbar: {
          ...toolbar,
          meterCollection: toolbar.meterCollection || {view: ToolbarView.graph}
        }
      }
    };
  },
  11: (state) => {
    const paginationState = {
      page: 0,
      size: 20,
      totalElements: -1,
      totalPages: -1,
    };

    const {ui} = state;

    return {
      ...state,
      ui: {
        ...ui,
        pagination: {
          meters: {...paginationState},
          gateways: {...paginationState},
          collectionStatFacilities: {...paginationState},
          meterCollectionStatFacilities: {...paginationState},
        }
      }
    };
  },
  12: (state) => {
    const paginationState = {
      page: 0,
      size: 50,
      totalElements: -1,
      totalPages: -1,
    };

    const {ui} = state;

    return {
      ...state,
      ui: {
        ...ui,
        pagination: {
          meters: {...paginationState},
          gateways: {...paginationState},
          collectionStatFacilities: {...paginationState},
          meterCollectionStatFacilities: {...paginationState},
        }
      }
    };
  },
  13: (state) => {
    const {userSelection: {userSelection}} = state;
    const selectionParameters = userSelection.selectionParameters;
    const {collectionDateRange, dateRange, reportDateRange} = selectionParameters;
    return {
      ...state,
      userSelection: {
        userSelection: {
          ...userSelection,
          selectionParameters: {
            ...selectionParameters,
            collectionDateRange: {...collectionDateRange, period: toNewPeriod(collectionDateRange.period)},
            dateRange: {...dateRange, period: toNewPeriod(dateRange.period)},
            reportDateRange: {...reportDateRange, period: toNewPeriod(reportDateRange.period)},
          }
        }
      },
    };
  },
  14: (state) => {
    const paginationState = {
      page: 0,
      size: 50,
      totalElements: -1,
      totalPages: -1,
    };

    const {ui} = state;

    return {
      ...state,
      ui: {
        ...ui,
        pagination: {
          ...ui.pagination,
          batchReferences: {...paginationState},
        }
      }
    };
  },
};

export const currentVersion: number = 14;
