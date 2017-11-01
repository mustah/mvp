import {AnyAction} from 'redux';
import {ErrorResponse, IdNamed, Period, uuid} from '../../../types/Types';
import {
  DESELECT_SELECTION, SELECT_PERIOD,
  SELECTION_FAILURE,
  SELECTION_REQUEST,
  SELECTION_SUCCESS,
  SET_SELECTION,
} from './selectionActions';
import {SelectedParameters, SelectionNormalized} from './selectionModels';

export interface SelectionState extends SelectionNormalized {
  isFetching: boolean;
  selected: SelectedParameters;
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
    statuses: [],
    period: Period.now,
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
    default:
      return state;
  }
};
