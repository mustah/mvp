import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {filterToUri, restClient} from '../../services/restClient';
import {NormalizedRows} from '../common/components/table/table/Table';
import {Category, Gateway} from './models/Collections';

export const COLLECTION_REQUEST = 'COLLECTION_REQUEST';
export const COLLECTION_SUCCESS = 'COLLECTION_SUCCESS';
export const COLLECTION_FAILURE = 'COLLECTION_FAILURE';

export const GATEWAY_REQUEST = 'GATEWAY_REQUEST';
export const GATEWAY_SUCCESS = 'GATEWAY_SUCCESS';
export const GATEWAY_FAILURE = 'GATEWAY_FAILURE';

export const COLLECTION_SET_FILTER = 'COLLECTION_SET_FILTER';
export const COLLECTION_ADD_FILTER = 'COLLECTION_ADD_FILTER';
export const COLLECTION_REMOVE_FILTER = 'COLLECTION_REMOVE_FILTER';

const collectionRequest = createEmptyAction(COLLECTION_REQUEST);
const collectionSuccess = createPayloadAction(COLLECTION_SUCCESS);
const collectionFailure = createPayloadAction(COLLECTION_FAILURE);

const gatewayRequest = createEmptyAction(GATEWAY_REQUEST);
const gatewaySuccess = createPayloadAction(GATEWAY_SUCCESS);
const gatewayFailure = createPayloadAction(GATEWAY_FAILURE);

// TODO: should be a backend request and not a frontend filter.
export const collectionSetFilter = createPayloadAction(COLLECTION_SET_FILTER);
export const collectionRemoveFilter = createPayloadAction(COLLECTION_REMOVE_FILTER);


export const collectionAddFilter = (filterCriteria) => {
  return (dispatch) => {
    // make sure that the wanted collection filter is set in the global state
    dispatch(createPayloadAction(COLLECTION_ADD_FILTER)(filterCriteria));

    // request new data for the table
    dispatch(fetchGateways(filterCriteria));
  };
};


export const fetchCollections = () => {
  return (dispatch) => {
    dispatch(collectionRequest());

    restClient.get('/collections')
      .then(response => response.data)
      .then(collections => dispatch(collectionSuccess(normalizeCategories(collections))))
      .catch(error => dispatch(collectionFailure(error)));
  };
};

export const fetchGateways = (filter) => {
  return (dispatch) => {
    dispatch(gatewayRequest());

    restClient.get(filterToUri('/gateways', filter))
      .then(response => response.data)
      .then(gateways => dispatch(gatewaySuccess(normalizeGateways(gateways))))
      .catch(error => dispatch(gatewayFailure(error)));
  };
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
