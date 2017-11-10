import {AnyAction} from 'redux';
import {Period, uuid} from '../../../types/Types';
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
import {SelectionState} from './selectionModels';

export const initialState: SelectionState = {
  id: -1,
  name: 'all',
  isChanged: false,
  selected: {
    cities: [],
    addresses: [],
    statuses: [],
    period: Period.currentMonth,
  },
};

const filterOutUnselected = (selected: uuid[], id: uuid): uuid[] => selected.filter(sel => sel !== id);

export const selection = (state: SelectionState = initialState, action: AnyAction): SelectionState => {
  const {payload} = action;

  switch (action.type) {
    case RESET_SELECTION:
      return {
        ...initialState,
      };
    case SET_SELECTION:
      return {
        ...state,
        isChanged: true,
        selected: {
          ...state.selected,
          [payload.parameter]: [...state.selected[payload.parameter], payload.id],
        },
      };
    case DESELECT_SELECTION:
      return {
        ...state,
        isChanged: true,
        selected: {
          ...state.selected,
          [payload.parameter]: filterOutUnselected(state.selected[payload.parameter], payload.id),
        },
      };
    case SELECT_PERIOD:
      return {
        ...state,
        isChanged: true,
        selected: {
          ...state.selected,
          period: payload,
        },
      };
    case UPDATE_SELECTION:
    case SELECT_SAVED_SELECTION:
      return {
        ...state,
        ...payload,
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

const updateSelectionById = (state: SelectionState[] = [], {payload}: AnyAction): SelectionState[] => {
  const index = state.findIndex((selection: SelectionState) => selection.id === payload.id);
  if (index !== -1) {
    state[index] = {...payload};
    return [...state];
  } else {
    return state;
  }
};

export const saved = (state: SelectionState[] = [], action: AnyAction): SelectionState[] => {
  const {payload, type} = action;

  switch (type) {
    case SAVE_SELECTION:
      return [...state, payload];
    case UPDATE_SELECTION:
      return updateSelectionById(state, action);
    default:
      return state;
  }
};
