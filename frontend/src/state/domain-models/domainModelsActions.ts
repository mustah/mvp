import {normalize} from 'normalizr';
import {createEmptyAction, createPayloadAction, EmptyAction, PayloadAction} from 'react-redux-typescript';
import {restClient} from '../../services/restClient';
import {ErrorResponse, IdNamed} from '../../types/Types';
import {EndPoints, Normalized} from './domainModelsModels';
import {selectionsSchema} from './domainModelsSchemas';

export const DOMAIN_MODELS_REQUEST: string = 'DOMAIN_MODELS_REQUEST';
export const DOMAIN_MODELS_SUCCESS: string = 'DOMAIN_MODELS_SUCCESS';
export const DOMAIN_MODELS_FAILURE: string = 'DOMAIN_MODELS_FAILURE';

interface RestRequest {
  request: () => EmptyAction<string>;
  success: (payload: Normalized<IdNamed>) => PayloadAction<string, Normalized<IdNamed>>;
  failure: (payload: ErrorResponse) => PayloadAction<string, ErrorResponse>;
}

export const selectionsRequest: RestRequest = {
  request: createEmptyAction(DOMAIN_MODELS_REQUEST.concat(EndPoints.selections)),
  success: createPayloadAction<string, Normalized<IdNamed>>(DOMAIN_MODELS_SUCCESS.concat(EndPoints.selections)),
  failure: createPayloadAction<string, ErrorResponse>(DOMAIN_MODELS_FAILURE.concat(EndPoints.selections)),
};

export const fetchDomainModel = (endPoint: EndPoints, {request, success, failure}: RestRequest, schema) =>
  () => async (dispatch) => {
    try {
      dispatch(request());
      const {data: domainModelData} = await restClient.get(endPoint);
      dispatch(success(normalize(domainModelData, schema)));
    } catch (error) {
      const {response: {data}} = error;
      dispatch(failure(data));
    }
  };

export const fetchSelections = fetchDomainModel(EndPoints.selections, selectionsRequest, selectionsSchema);
