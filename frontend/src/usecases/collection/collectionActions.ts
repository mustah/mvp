import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {RootState} from '../../reducers/index';
import {filterToUri, restClient} from '../../services/restClient';
import {Gateway} from './models/Collections';

export const COLLECTION_REQUEST = 'COLLECTION_REQUEST';
export const COLLECTION_SUCCESS = 'COLLECTION_SUCCESS';
export const COLLECTION_FAILURE = 'COLLECTION_FAILURE';

export const GATEWAY_REQUEST = 'GATEWAY_REQUEST';
export const GATEWAY_SUCCESS = 'GATEWAY_SUCCESS';
export const GATEWAY_FAILURE = 'GATEWAY_FAILURE';

export const COLLECTION_SET_FILTER = 'COLLECTION_SET_FILTER';
export const COLLECTION_ADD_FILTER = 'COLLECTION_ADD_FILTER';

const collectionRequest = createEmptyAction(COLLECTION_REQUEST);
const collectionSuccess = createPayloadAction(COLLECTION_SUCCESS);
const collectionFailure = createPayloadAction(COLLECTION_FAILURE);

const gatewayRequest = createEmptyAction(GATEWAY_REQUEST);
const gatewaySuccess = createPayloadAction(GATEWAY_SUCCESS);
const gatewayFailure = createPayloadAction(GATEWAY_FAILURE);

export const collectionChangePage = (page) => {
  return (dispatch, getState: () => RootState) => {
    const {collection: {filter, pagination: {limit}}} = getState();
    return dispatch(fetchGateways(filter, page, limit));
  };
};

export const collectionRemoveFilter = (filterCategory, value) => {
  return (dispatch, getState: () => RootState) => {
    const {collection: {pagination: {limit}, filter}} = getState();

    if (filter.hasOwnProperty(filterCategory)) {
      filter[filterCategory].delete(value);
    }

    dispatch(fetchGateways(filter, 1, limit));
  };
};

export const collectionAddFilter = (filterToAdd) => {
  return (dispatch, getState: () => RootState) => {
    const {collection: {pagination: {limit}, filter}} = getState();

    Object.keys(filterToAdd).forEach((category) => {
      if (!filter.hasOwnProperty(category)) {
        filter[category] = new Set();
      }
      filter[category].add(filterToAdd[category]);
    });

    dispatch(fetchGateways(filter, 1, limit));
  };
};

export const fetchCollections = () => {
  return (dispatch) => {
    dispatch(collectionRequest());

    restClient.get('/collections')
      .then(response => response.data)
      .then(collections => dispatch(collectionSuccess(collections)))
      .catch(error => dispatch(collectionFailure(error)));
  };
};

export const fetchGateways = (filter, page, limit) => {
  return (dispatch) => {
    dispatch(gatewayRequest());

    const parameters = {
      ...filter,
      _page: page,
      _limit: limit,
    };

    restClient.get(filterToUri('/gateways', parameters))
      .then(response => {
        const gateways = response.data;

        // JSON server exposes links to first, last, prev, next, but
        // we're not sure the real backend implementation does that,
        // so we're "normalizing" it a tiny bit. If our real implementation
        // actually does provide those links - sweet - let's use them!

        // an example Link header is (without the line breaks, sorry - linter):
        // Link: <http://localhost:8080/api/gateways?_page=1&_limit=20>; rel="first",
        // <http://localhost:8080/api/gateways?_page=2&_limit=20>; rel="next",
        // <http://localhost:8080/api/gateways?_page=500&_limit=20>; rel="last"

        // TODO the following calculation breaks if the query includes a ',',
        // but so does all the implementations I've seen online as well for JS..
        const total = response.headers.link.split(',').reduce((result, header) => {
          const matches = header.trim().match(/<.*_page=(\d+)&_limit=(\d+)>; rel="last"/);
          if (matches) {
            return parseInt(matches[1], 10) * parseInt(matches[2], 10);
          }
          return result;
        }, '');

        return dispatch(gatewaySuccess({
          gateways: normalizeGateways(gateways),
          filter,
          page,
          limit,
          total,
        }));
      })
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
