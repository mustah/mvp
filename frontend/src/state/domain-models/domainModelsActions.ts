import {normalize} from 'normalizr';
import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {restClient} from '../../services/restClient';
import {ErrorResponse, IdNamed} from '../../types/Types';
import {Normalized} from './domainModelsModels';
import {selectionsSchema} from './domainModelsSchemas';

export const DOMAIN_MODELS_REQUEST = 'DOMAIN_MODELS_REQUEST';
export const DOMAIN_MODELS_SUCCESS = 'DOMAIN_MODELS_SUCCESS';
export const DOMAIN_MODELS_FAILURE = 'DOMAIN_MODELS_FAILURE';

export const domainModelRequest = createEmptyAction(DOMAIN_MODELS_REQUEST);
export const domainModelSuccess = createPayloadAction<string, Normalized<IdNamed>>(DOMAIN_MODELS_SUCCESS);
export const domainModelFailure = createPayloadAction<string, ErrorResponse>(DOMAIN_MODELS_FAILURE);

export const fetchDomainModel = () =>
  async (dispatch) => {
    try {
      dispatch(domainModelRequest());
      const {data: domainModelData} = await restClient.get('/selections');
      dispatch(domainModelSuccess(normalize(domainModelData, selectionsSchema)));
    } catch (error) {
      const {response: {data}} = error;
      dispatch(domainModelFailure(data));
    }
  };
