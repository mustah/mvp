import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {restClient} from '../../api/restClient';
import {COLLECTION_FAILURE, COLLECTION_REQUEST, COLLECTION_SUCCESS} from '../../types/ActionTypes';

const collectionRequest = createEmptyAction(COLLECTION_REQUEST);
const collectionSuccess = createPayloadAction(COLLECTION_SUCCESS);
const collectionFailure = createPayloadAction(COLLECTION_FAILURE);

export const fetchCollections = () => {
  return (dispatch) => {
    dispatch(collectionRequest());

    restClient.get('/collections')
      .then(response => response.data)
      .then(collections => dispatch(collectionSuccess(collections)))
      .catch(error => dispatch(collectionFailure(error)));
  };
};
