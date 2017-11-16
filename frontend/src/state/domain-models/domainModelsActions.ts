import {normalize, Schema} from 'normalizr';
import {createEmptyAction, createPayloadAction, EmptyAction, PayloadAction} from 'react-redux-typescript';
import {restClient} from '../../services/restClient';
import {makeUrl} from '../../services/urlFactory';
import {ErrorResponse, IdNamed} from '../../types/Types';
import {EndPoints, Normalized} from './domainModels';
import {selectionsSchema} from './domainModelsSchemas';

export const DOMAIN_MODELS_REQUEST = 'DOMAIN_MODELS_REQUEST';
export const DOMAIN_MODELS_SUCCESS = 'DOMAIN_MODELS_SUCCESS';
export const DOMAIN_MODELS_FAILURE = 'DOMAIN_MODELS_FAILURE';

interface RestRequest<T> {
  request: () => EmptyAction<string>;
  success: (payload: Normalized<T>) => PayloadAction<string, Normalized<T>>;
  failure: (payload: ErrorResponse) => PayloadAction<string, ErrorResponse>;
}

const domainModelRequest = <T>(endPoint: EndPoints): RestRequest<T> => ({
  request: createEmptyAction(DOMAIN_MODELS_REQUEST.concat(endPoint)),
  success: createPayloadAction<string, Normalized<T>>(DOMAIN_MODELS_SUCCESS.concat(endPoint)),
  failure: createPayloadAction<string, ErrorResponse>(DOMAIN_MODELS_FAILURE.concat(endPoint)),
});

const fetchDomainModel = <T>(endPoint: EndPoints, {request, success, failure}: RestRequest<T>, schema: Schema) =>
  (encodedUriParameters?: string) => async (dispatch) => {
  try {
      dispatch(request());
      const {data: domainModelData} = await restClient.get(makeUrl(endPoint, encodedUriParameters));
      dispatch(success(normalize(domainModelData, schema)));
    } catch (error) {
      const {response: {data}} = error;
      dispatch(failure(data));
    }
  };

export const selectionsRequest = domainModelRequest<IdNamed>(EndPoints.selections);
export const fetchSelections = fetchDomainModel<IdNamed>(EndPoints.selections, selectionsRequest, selectionsSchema);
