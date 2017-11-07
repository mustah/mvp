import {AnyAction} from 'redux';
import {idGenerator} from '../../../services/idGenerator';
import {Period, uuid} from '../../../types/Types';
import {
  DESELECT_SELECTION,
  SAVE_SELECTION,
  SELECT_PERIOD,
  SELECT_SAVED_SELECTION,
  SET_SELECTION,
} from './selectionActions';
import {SelectionState} from './selectionModels';

export const initialState: SelectionState = {
  id: idGenerator.uuid(),
  name: 'all',
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
    case SET_SELECTION:
      return {
        ...state,
        selected: {
          ...state.selected,
          [payload.parameter]: [...state.selected[payload.parameter], payload.id],
        },
      };
    case DESELECT_SELECTION:
      return {
        ...state,
        selected: {
          ...state.selected,
          [payload.parameter]: filterOutUnselected(state.selected[payload.parameter], payload.id),
        },
      };
    case SELECT_PERIOD:
      return {
        ...state,
        selected: {
          ...state.selected,
          period: payload,
        },
      };
    case SELECT_SAVED_SELECTION:
      return {
        ...payload,
      };
    default:
      return state;
  }
};

export const saved = (state: SelectionState[] = [], action: AnyAction): SelectionState[] => {
  switch (action.type) {
    case SAVE_SELECTION:
      return [...state, action.payload];
    default:
      return state;
  }
};
