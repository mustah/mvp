import {AnyAction} from 'redux';
import {ErrorResponse, IdNamed, uuid} from '../../../types/Types';
import {
  DESELECT_SELECTION,
  SELECTION_FAILURE,
  SELECTION_REQUEST,
  SELECTION_SUCCESS,
  SET_SELECTION,
} from './selectionActions';
import {SelectedIds, SelectionNormalized} from './selectionModels';

export interface SelectionState extends SelectionNormalized {
  isFetching: boolean;
  selected: SelectedIds;
  error?: ErrorResponse;
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

export const addCityEntity = (state: SelectionState, city: IdNamed): SelectionState => {
  return {
    ...state,
    entities: {
      ...state.entities,
      cities: {
        ...state.entities.cities,
        [city.id]: {...city},
      },
    },
  };
};

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
        isFetching: false,
        error: {...payload},
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
