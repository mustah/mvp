import {AnyAction} from 'redux';
import {COLLECTION_REQUEST, COLLECTION_SUCCESS, GATEWAY_REQUEST, GATEWAY_SUCCESS} from '../../types/ActionTypes';
import {NormalizedRows} from '../common/components/table/table/Table';
import {Category, CollectionState, Gateway} from './models/Collections';

const initialState: CollectionState = {
  title: 'CollectionState',
  records: [],
  isFetching: false,
  gateways: {allIds: [], byId: {}},
  categories: {
    handled: {allIds: [], byId: {}},
    unhandled: {allIds: [], byId: {}},
  },
};

// TODO the mapping between back and front end needs to be more formal
const normalizeGateways = (gatewaysFromBackend): Gateway => {
  const gatewaysById = {};
  gatewaysFromBackend.map((g) => gatewaysById[g.id] = g);
  return {
    allIds: gatewaysFromBackend.map((g) => g.id),
    byId: gatewaysById,
  };
};

const normalizeCategories = (categoriesFromBackEnd): Category => {
  const {handled, unhandled} = categoriesFromBackEnd;

  const normalize = (category): NormalizedRows => {
    const byId = {};
    const allIds: any = [];
    Object.keys(category).map((c, i) => {
      allIds.push(i);
      byId[i] = category[i];
    });
    return {
      allIds,
      byId,
    };
  };
  const normalizedHandled = normalize(handled);
  const normalizedUnhandled = normalize(unhandled);
  return {
    handled: {
      ...normalizedHandled,
    },
    unhandled: {
      ...normalizedUnhandled,
    },
  };
};

export const collection = (state: CollectionState = initialState, action: AnyAction): CollectionState => {
  // TODO make sure to handle failed HTTP requests as well (*_FAILURE)
  switch (action.type) {
    case COLLECTION_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    case COLLECTION_SUCCESS:
      return {
        ...state,
        categories: normalizeCategories(action.payload),
        isFetching: false,
      };
    case GATEWAY_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    case GATEWAY_SUCCESS:
      return {
        ...state,
        gateways: normalizeGateways(action.payload),
        isFetching: false,
      };
    default:
      return state;
  }
};
