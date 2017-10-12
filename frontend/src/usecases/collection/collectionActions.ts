import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {restClient} from '../../services/restClient';
import {
  COLLECTION_FAILURE,
  COLLECTION_REQUEST,
  COLLECTION_SUCCESS,
  GATEWAY_FAILURE,
  GATEWAY_REQUEST,
  GATEWAY_SUCCESS,
} from '../../types/ActionTypes';
import {NormalizedRows} from '../common/components/table/table/Table';
import {Category, Gateway} from './models/Collections';

const collectionRequest = createEmptyAction(COLLECTION_REQUEST);
const collectionSuccess = createPayloadAction(COLLECTION_SUCCESS);
const collectionFailure = createPayloadAction(COLLECTION_FAILURE);

const gatewayRequest = createEmptyAction(GATEWAY_REQUEST);
const gatewaySuccess = createPayloadAction(GATEWAY_SUCCESS);
const gatewayFailure = createPayloadAction(GATEWAY_FAILURE);

export const fetchCollections = () => {
  return (dispatch) => {
    dispatch(collectionRequest());

    restClient.get('/collections')
      .then(response => response.data)
      .then(collections => dispatch(collectionSuccess(normalizeCategories(collections))))
      .catch(error => dispatch(collectionFailure(error)));
  };
};

export const fetchGateways = () => {
  return (dispatch) => {
    dispatch(gatewayRequest());

    restClient.get('/gateways')
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
