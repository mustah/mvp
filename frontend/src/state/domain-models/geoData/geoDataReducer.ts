import {AnyAction, combineReducers} from 'redux';
import {
  GEO_DATA_FAILURE, GEO_DATA_REQUEST, GEO_DATA_SUCCESS, SIDEBAR_TREE_FAILURE, SIDEBAR_TREE_REQUEST,
  SIDEBAR_TREE_SUCCESS
} from './geoDataActions';
import {AddressState, GeoDataState, IdNamedState} from './geoDataModels';

export const initialState: IdNamedState = {
  result: [],
  entities: {},
  isFetching: false,
  total: 0,
};

export const initialAddressState: AddressState = {
  result: [],
  entities: {},
  isFetching: false,
  total: 0,
};

const geoDataReducerFor = (entity: string, state: IdNamedState, action: AnyAction): any => {
  const {payload} = action;

  switch (action.type) {
    case GEO_DATA_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    case GEO_DATA_SUCCESS:
      return {
        ...state,
        isFetching: false,
        entities: payload.entities[entity],
        result: payload.result[entity],
        total: payload.result[entity].length,
      };
    case GEO_DATA_FAILURE:
      return {
        ...state,
        isFetching: false,
        error: {...payload},
      };
    default:
      return state;
  }
};

export const addresses = (state: AddressState = initialAddressState, action: AnyAction): AddressState =>
  geoDataReducerFor('addresses', state, action);

export const cities = (state: IdNamedState = initialState, action: AnyAction): IdNamedState =>
  geoDataReducerFor('cities', state, action);

export const sidebarTree = (state = {entities: {}, result: []}, action: AnyAction) => {
  const {payload} = action;

  switch (action.type) {
    case SIDEBAR_TREE_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    case SIDEBAR_TREE_SUCCESS:
      return {
        ...state,
        isFetching: false,
        ...payload,
      };
    case SIDEBAR_TREE_FAILURE:
      return {
        ...state,
        isFetching: false,
        error: {...payload},
      };
    default:
      return state;
  }
};

export const geoData = combineReducers<GeoDataState>({addresses, cities, sidebarTree});
