import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {RootState} from '../../../reducers/rootReducer';
import {filterToUri, restClient} from '../../../services/restClient';
import {normalize} from 'normalizr';
import {gatewaySchema} from './gatewaySchema';

export const GATEWAY_REQUEST = 'GATEWAY_REQUEST';
export const GATEWAY_SUCCESS = 'GATEWAY_SUCCESS';
export const GATEWAY_FAILURE = 'GATEWAY_FAILURE';

const gatewayRequest = createEmptyAction(GATEWAY_REQUEST);
const gatewaySuccess = createPayloadAction(GATEWAY_SUCCESS);
const gatewayFailure = createPayloadAction(GATEWAY_FAILURE);

export const collectionRemoveFilter = (filterCategory, value) => {
  return (dispatch, getState: () => RootState) => {
    const {collection: {pagination: filter}} = getState();

    if (filter.hasOwnProperty(filterCategory)) {
      filter[filterCategory].delete(value);
    }

    dispatch(fetchGateways(filter));
  };
};

export const collectionAddFilter = (filterToAdd) => {
  return (dispatch, getState: () => RootState) => {
    const {collection: {pagination: filter}} = getState();

    Object.keys(filterToAdd).forEach((category) => {
      if (!filter.hasOwnProperty(category)) {
        filter[category] = new Set();
      }
      filter[category].add(filterToAdd[category]);
    });

    dispatch(fetchGateways(filter));
  };
};

export const fetchGateways = (filter) => {
  return (dispatch) => {
    dispatch(gatewayRequest());

    const parameters = {
      ...filter,
    };

    restClient.get(filterToUri('/gateways', parameters))
      .then(response => {
        const gateways = response.data;
        const total = response.headers['X-Total-Count'];

        return dispatch(gatewaySuccess({
          total,
          gateways: normalize(gateways, gatewaySchema),
        }));
      })
      .catch(error => dispatch(gatewayFailure(error)));
  };
};
