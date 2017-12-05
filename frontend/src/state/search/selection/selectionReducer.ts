import {EmptyAction} from 'react-redux-typescript';
import {Action, Period, uuid} from '../../../types/Types';
import {
  CLOSE_SELECTION_PAGE,
  DESELECT_SELECTION,
  RESET_SELECTION,
  SAVE_SELECTION,
  SELECT_PERIOD,
  SELECT_SAVED_SELECTION,
  SET_SELECTION,
  UPDATE_SELECTION,
} from './selectionActions';
import {FilterParam, SelectionParameter, SelectionState} from './selectionModels';

export const initialState: SelectionState = {
  id: -1,
  name: 'all',
  isChanged: false,
  selected: {
    cities: [],
    addresses: [],
    meterStatuses: [],
    gatewayStatuses: [],
    alarms: [],
    manufacturers: [],
    productModels: [],
    period: Period.latest,
  },
};

interface SelectionActionModel {
  payload: SelectionState;
  type: string;
}

const updateSelected = (state: SelectionState = initialState, action: Action<SelectionParameter>): SelectionState => {
  const {payload: {parameter, id}} = action;
  const selectedIds: FilterParam[] = state.selected[parameter] as FilterParam[];
  return {
    ...state,
    isChanged: true,
    selected: {
      ...state.selected,
      [parameter]: Array.from(new Set([...selectedIds]).add(id)),
    },
  };
};

const updatePeriod = (state: SelectionState = initialState, action: Action<Period>): SelectionState => {
  const {payload} = action;
  return {
    ...state,
    isChanged: true,
    selected: {
      ...state.selected,
      period: payload,
    },
  };
};

const removeSelected = (state: SelectionState = initialState, action: Action<SelectionParameter>): SelectionState => {
  const {payload: {parameter, id}} = action;
  const selectedIds: uuid[] = state.selected[parameter]! as uuid[];
  return {
    ...state,
    isChanged: true,
    selected: {
      ...state.selected,
      [parameter]: selectedIds.filter(selectedId => selectedId !== id),
    },
  };
};

type ActionTypes =
  | EmptyAction<string>
  | Action<SelectionParameter>
  | Action<SelectionState>
  | Action<Period>;

export const selection = (state: SelectionState = initialState, action: ActionTypes): SelectionState => {
  switch (action.type) {
    case RESET_SELECTION:
      return {...initialState};
    case SET_SELECTION:
      return updateSelected(state, action as Action<SelectionParameter>);
    case DESELECT_SELECTION:
      return removeSelected(state, action as Action<SelectionParameter>);
    case SELECT_PERIOD:
      return updatePeriod(state, action as Action<Period>);
    case UPDATE_SELECTION:
    case SELECT_SAVED_SELECTION:
      return {
        ...state,
        ...(action as SelectionActionModel).payload,
        isChanged: false,
      };
    case SAVE_SELECTION:
    case CLOSE_SELECTION_PAGE:
      return {
        ...state,
        isChanged: false,
      };
    default:
      return state;
  }
};
