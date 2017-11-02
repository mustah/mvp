import {AnyAction, combineReducers} from 'redux';
import {GEO_DATA_FAILURE, GEO_DATA_REQUEST, GEO_DATA_SUCCESS} from './geoDataActions';
import {GeoDataState, NormalizedState} from './geoDataModels';

export const initialState: NormalizedState = {
  result: [],
  entities: {},
  isFetching: false,
  total: 0,
};

const geoDataReducerFor = (entity: string, state: NormalizedState, action: AnyAction): NormalizedState => {
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

export const addresses = (state: NormalizedState = initialState, action: AnyAction): NormalizedState =>
  geoDataReducerFor('addresses', state, action);

export const cities = (state: NormalizedState = initialState, action: AnyAction): NormalizedState =>
  geoDataReducerFor('cities', state, action);

export const geoData = combineReducers<GeoDataState>({addresses, cities});
