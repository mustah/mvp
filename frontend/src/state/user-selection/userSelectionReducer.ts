import {EmptyAction} from 'react-redux-typescript';
import {DateRange, Period} from '../../components/dates/dateModels';
import {EndPoints} from '../../services/endPoints';
import {Action, uuid} from '../../types/Types';
import {
  domainModelsDeleteSuccess,
  domainModelsPostSuccess,
  domainModelsPutSuccess,
} from '../domain-models/domainModelsActions';
import {
  ADD_PARAMETER_TO_SELECTION,
  DESELECT_SELECTION,
  RESET_SELECTION,
  SELECT_PERIOD,
  SELECT_SAVED_SELECTION, SET_CUSTOM_DATE_RANGE,
  SET_SELECTION,
} from './userSelectionActions';
import {FilterParam, SelectionParameter, UserSelection, UserSelectionState} from './userSelectionModels';

export const initialState: UserSelectionState = {
  userSelection: {
    id: -1,
    name: 'all',
    isChanged: false,
    selectionParameters: {
      cities: [],
      addresses: [],
      meterStatuses: [],
      gatewayStatuses: [],
      alarms: [],
      manufacturers: [],
      productModels: [],
      dateRange: {period: Period.latest},
    },
  },
};

const addSelected = (
  state: UserSelectionState,
  {payload: {parameter, id}}: Action<SelectionParameter>,
): UserSelectionState => {
  const selectedIds = new Set<FilterParam>(state.userSelection.selectionParameters[parameter] as FilterParam[]);
  Array.isArray(id) ? id.forEach((filterParam) => selectedIds.add(filterParam)) : selectedIds.add(id);
  return {
    ...state,
    userSelection: {
      ...state.userSelection,
      isChanged: true,
      selectionParameters: {
        ...state.userSelection.selectionParameters,
        [parameter]: Array.from(selectedIds),
      },
    },
  };
};

const setSelected = (
  state: UserSelectionState,
  {payload: {parameter, id}}: Action<SelectionParameter>,
): UserSelectionState => {
  const selected = Array.isArray(id) ? [...id] : [id];
  return {
    ...state,
    userSelection: {
      ...state.userSelection,
      isChanged: true,
      selectionParameters: {
        ...state.userSelection.selectionParameters,
        [parameter]: selected,
      },
    },
  };
};

const selectSaved = (state: UserSelectionState, {payload}: Action<UserSelection>): UserSelectionState => ({
  ...state,
  userSelection: {
    ...payload,
    isChanged: false,
  },
});

const updatePeriod = (state: UserSelectionState, {payload}: Action<Period>): UserSelectionState => (
  {
    ...state,
    userSelection: {
      ...state.userSelection,
      isChanged: true,
      selectionParameters: {
        ...state.userSelection.selectionParameters,
        dateRange: {
          ...state.userSelection.selectionParameters.dateRange,
          period: payload,
        },
      },
    },
  });

const removeSelected = (state: UserSelectionState, {payload}: Action<SelectionParameter>): UserSelectionState => {
  const {parameter, id} = payload;
  const selectedIds = state.userSelection.selectionParameters[parameter]! as uuid[];
  return {
    ...state,
    userSelection: {
      ...state.userSelection,
      isChanged: true,
      selectionParameters: {
        ...state.userSelection.selectionParameters,
        [parameter]: selectedIds.filter((selectedId) => selectedId !== id),
      },
    },
  };
};

type ActionTypes =
  | EmptyAction<string>
  | Action<SelectionParameter>
  | Action<UserSelection>
  | Action<Period>
  | Action<DateRange>;

export const userSelection = (state: UserSelectionState = initialState, action: ActionTypes): UserSelectionState => {
  switch (action.type) {
    case RESET_SELECTION:
      return {...initialState};
    case ADD_PARAMETER_TO_SELECTION:
      return addSelected(state, action as Action<SelectionParameter>);
    case SET_SELECTION:
      return setSelected(state, action as Action<SelectionParameter>);
    case DESELECT_SELECTION:
      return removeSelected(state, action as Action<SelectionParameter>);
    case SELECT_PERIOD:
      return updatePeriod(state, action as Action<Period>);
    case SET_CUSTOM_DATE_RANGE:
      return {
        ...state,
        userSelection: {
          ...state.userSelection,
          isChanged: true,
          selectionParameters: {
            ...state.userSelection.selectionParameters,
            dateRange: {
              ...state.userSelection.selectionParameters.dateRange,
              period: Period.custom,
              customDateRange: (action as Action<DateRange>).payload,
            },
          },
        },
      };
    case SELECT_SAVED_SELECTION:
    case domainModelsPostSuccess(EndPoints.userSelections):
    case domainModelsPutSuccess(EndPoints.userSelections):
      return selectSaved(state, action as Action<UserSelection>);
    case domainModelsDeleteSuccess(EndPoints.userSelections):
      const payload: UserSelection = (action as Action<UserSelection>).payload;
      return payload.id === state.userSelection.id ? initialState : state;
    default:
      return state;
  }
};
