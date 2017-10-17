import {AnyAction} from 'redux';
import {SearchOptionResult, SearchOptions} from './models/searchModels';
import {
  SEARCH_OPTIONS_FAILURE,
  SEARCH_OPTIONS_REQUEST,
  SEARCH_OPTIONS_SUCCESS,
  SELECT_SEARCH_OPTION,
} from './searchActions';

export interface SearchState extends SearchOptions {
  isFetching: boolean;
  selected: SearchOptionResult;
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
      const {entity, id} = payload;
      return {
        ...state,
        selected: {
          ...state.selected,
          [entity]: [...state.selected[entity], id],
        },
      };
    default:
      return state;
  }
};
