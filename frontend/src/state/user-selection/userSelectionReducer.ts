import {EmptyAction} from 'react-redux-typescript';
import {DateRange, Period} from '../../components/dates/dateModels';
import {EndPoints} from '../../services/endPoints';
import {Action, IdNamed} from '../../types/Types';
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
  SELECT_SAVED_SELECTION,
  SET_CUSTOM_DATE_RANGE,
  SET_THRESHOLD,
} from './userSelectionActions';
import {
  initialSelectionId,
  ParameterName,
  SelectionItem,
  SelectionParameter,
  ThresholdQuery,
  UserSelection,
  UserSelectionState
} from './userSelectionModels';

export const initialState: UserSelectionState = {
  userSelection: {
    id: initialSelectionId,
    name: 'all',
    isChanged: false,
    selectionParameters: {
      addresses: [],
      cities: [],
      dateRange: {period: Period.latest},
      facilities: [],
      gatewaySerials: [],
      media: [],
      reported: [],
      secondaryAddresses: [],
    },
  },
};

const getSelectionItems = (state: UserSelectionState, parameter: ParameterName): SelectionItem[] =>
  state.userSelection.selectionParameters[parameter];

const addSelected = (
  state: UserSelectionState,
  {payload: {parameter, item}}: Action<SelectionParameter>,
): UserSelectionState => {
  const {userSelection} = state;
  const selected = new Set<SelectionItem>(getSelectionItems(state, parameter)).add(item);
  return {
    ...state,
    userSelection: {
      ...userSelection,
      isChanged: true,
      selectionParameters: {
        ...userSelection.selectionParameters,
        [parameter]: Array.from(selected),
      },
    },
  };
};

const selectSaved = (
  state: UserSelectionState,
  {payload}: Action<UserSelection>,
): UserSelectionState => ({
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

const removeSelected = (
  state: UserSelectionState,
  {payload}: Action<SelectionParameter>,
): UserSelectionState => {
  const {parameter, item} = payload;
  return {
    ...state,
    userSelection: {
      ...state.userSelection,
      isChanged: true,
      selectionParameters: {
        ...state.userSelection.selectionParameters,
        [parameter]: getSelectionItems(state, parameter)
          .filter((it: IdNamed) => it.id !== item.id),
      },
    },
  };
};

const updateCustomDateRange = (
  state: UserSelectionState,
  {payload}: Action<DateRange>,
): UserSelectionState => {
  const {userSelection} = state;
  return {
    ...state,
    userSelection: {
      ...userSelection,
      isChanged: true,
      selectionParameters: {
        ...userSelection.selectionParameters,
        dateRange: {
          ...userSelection.selectionParameters.dateRange,
          period: Period.custom,
          customDateRange: payload,
        },
      },
    },
  };
};

const applyThreshold = (
  state: UserSelectionState,
  threshold: ThresholdQuery,
): UserSelectionState => ({
  ...state,
  userSelection: {
    ...state.userSelection,
    isChanged: true,
    selectionParameters: {
      ...state.userSelection.selectionParameters,
      threshold,
    }
  }
});

type ActionTypes =
  | EmptyAction<string>
  | Action<SelectionParameter>
  | Action<UserSelection>
  | Action<Period>
  | Action<DateRange>
  | Action<ThresholdQuery>;

export const userSelection = (
  state: UserSelectionState = initialState,
  action: ActionTypes,
): UserSelectionState => {
  switch (action.type) {
    case RESET_SELECTION:
      return initialState;
    case ADD_PARAMETER_TO_SELECTION:
      return addSelected(state, action as Action<SelectionParameter>);
    case DESELECT_SELECTION:
      return removeSelected(state, action as Action<SelectionParameter>);
    case SELECT_PERIOD:
      return updatePeriod(state, action as Action<Period>);
    case SET_CUSTOM_DATE_RANGE:
      return updateCustomDateRange(state, action as Action<DateRange>);
    case SET_THRESHOLD:
      return applyThreshold(state, (action as Action<ThresholdQuery>).payload);
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
