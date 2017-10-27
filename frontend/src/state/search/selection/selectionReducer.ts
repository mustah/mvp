import {AnyAction} from 'redux';
import {uuid} from '../../../types/Types';
import {SelectionOptions, SelectedIds} from './selectionModels';
import {
  DESELECT_SELECTION,
  SELECTION_FAILURE,
  SELECTION_REQUEST,
  SELECTION_SUCCESS,
  SET_SELECTION,
} from './selectionActions';

export interface SelectionState extends SelectionOptions {
  isFetching: boolean;
  selected: SelectedIds;
}

export const initialState: SelectionState = {
  isFetching: false,
  entities: {},
  result: {
    cities: [],
    addresses: [],
  },
  selected: {
    cities: [],
    addresses: [],
  },
};

const filterOutUnselected = (selected: uuid[], id: uuid): uuid[] => selected.filter(sel => sel !== id);

export const selection = (state: SelectionState = initialState, action: AnyAction): SelectionState => {
  const {payload} = action;
  switch (action.type) {
    case SELECTION_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    case SELECTION_SUCCESS:
      return {
        ...state,
        isFetching: false,
        ...payload,
      };
    case SELECTION_FAILURE:
      return {
        ...state,
        isFetch: false,
        ...payload,
      };
    case SET_SELECTION:
      return {
        ...state,
        selected: {
          ...state.selected,
          [payload.attribute]: [...state.selected[payload.attribute], payload.id],
        },
      };
    case DESELECT_SELECTION:
      return {
        ...state,
        selected: {
          ...state.selected,
          [payload.attribute]: filterOutUnselected(state.selected[payload.attribute], payload.id),
        },
      };
    default:
      return state;
  }
};
