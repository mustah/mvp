import {AnyAction} from 'redux';
import {uuid} from '../../types/Types';
import {SearchOptions, SearchResult} from './models/searchModels';
import {
  DESELECT_SEARCH_OPTION,
  SEARCH_OPTIONS_FAILURE,
  SEARCH_OPTIONS_REQUEST,
  SEARCH_OPTIONS_SUCCESS,
  SELECT_SEARCH_OPTION,
} from './searchActions';

export interface SearchState extends SearchOptions {
  isFetching: boolean;
  selected: SearchResult;
}

export const initialState: SearchState = {
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

export const search = (state: SearchState = initialState, action: AnyAction): SearchState => {
  const {payload} = action;
  switch (action.type) {
    case SEARCH_OPTIONS_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    case SEARCH_OPTIONS_SUCCESS:
      return {
        ...state,
        isFetching: false,
        ...payload,
      };
    case SEARCH_OPTIONS_FAILURE:
      return {
        ...state,
        isFetch: false,
        ...payload,
      };
    case SELECT_SEARCH_OPTION:
      return {
        ...state,
        selected: {
          ...state.selected,
          [payload.entity]: [...state.selected[payload.entity], payload.id],
        },
      };
    case DESELECT_SEARCH_OPTION:
      return {
        ...state,
        selected: {
          ...state.selected,
          [payload.entity]: filterOutUnselected(state.selected[payload.entity], payload.id),
        },
      };
    default:
      return state;
  }
};
