import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {restClient} from '../../services/restClient';

export const COLLECTION_REQUEST = 'COLLECTION_REQUEST';
export const COLLECTION_SUCCESS = 'COLLECTION_SUCCESS';
export const COLLECTION_FAILURE = 'COLLECTION_FAILURE';

export const COLLECTION_CHANGE_PAGE = 'COLLECTION_CHANGE_PAGE';

const collectionRequest = createEmptyAction(COLLECTION_REQUEST);
const collectionSuccess = createPayloadAction(COLLECTION_SUCCESS);
const collectionFailure = createPayloadAction(COLLECTION_FAILURE);

export const collectionChangePage = createPayloadAction(COLLECTION_CHANGE_PAGE);

export const fetchCollections = () => {
  return (dispatch) => {
    dispatch(collectionRequest());

    restClient.get('/collections')
      .then(response => response.data)
      .then(collections => dispatch(collectionSuccess(collections)))
      .catch(error => dispatch(collectionFailure(error)));
  };
};
