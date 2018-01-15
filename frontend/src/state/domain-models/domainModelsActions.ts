import {normalize, Schema} from 'normalizr';
import {createEmptyAction, createPayloadAction, EmptyAction, PayloadAction} from 'react-redux-typescript';
import {makeUrl} from '../../helpers/urlFactory';
import {restClient} from '../../services/restClient';
import {firstUpperTranslated} from '../../services/translationService';
import {ErrorResponse, IdNamed, uuid} from '../../types/Types';
import {showMessage} from '../ui/message/messageActions';
import {EndPoints, Normalized} from './domainModels';
import {selectionsSchema} from './domainModelsSchemas';
import {Gateway} from './gateway/gatewayModels';
import {gatewaySchema} from './gateway/gatewaySchema';
import {meterSchema} from './meter/meterSchema';
import {User} from './user/userModels';
import {userSchema} from './user/userSchema';

export const DOMAIN_MODELS_REQUEST = 'DOMAIN_MODELS_REQUEST';
export const DOMAIN_MODELS_FAILURE = 'DOMAIN_MODELS_FAILURE';

export const DOMAIN_MODELS_GET_SUCCESS = 'DOMAIN_MODELS_GET_SUCCESS';
export const DOMAIN_MODELS_POST_SUCCESS = 'DOMAIN_MODELS_POST_SUCCESS';
export const DOMAIN_MODELS_PUT_SUCCESS = 'DOMAIN_MODELS_PUT_SUCCESS';
export const DOMAIN_MODELS_DELETE_SUCCESS = 'DOMAIN_MODELS_DELETE_SUCCESS';

interface RestRequestHandle<T> {
  request: () => EmptyAction<string>;
  success: (payload: T) => PayloadAction<string, T>;
  failure: (payload: ErrorResponse) => PayloadAction<string, ErrorResponse>;
}

interface RestCallbacks<T> {
  afterSuccess?: (domainModel: T) => void;
  afterFailure?: (error: ErrorResponse) => void;
}

export enum RestRequestTypes {
  GET = 'GET',
  POST = 'POST',
  PUT = 'PUT',
  DELETE = 'DELETE',
}

export const requestHandle = <T>(endPoint: EndPoints, requestType: RestRequestTypes): RestRequestHandle<T> => ({
  request: createEmptyAction<string>(DOMAIN_MODELS_REQUEST.concat(endPoint)),
  success: createPayloadAction<string, T>(`DOMAIN_MODELS_${requestType}_SUCCESS`.concat(endPoint)),
  failure: createPayloadAction<string, ErrorResponse>(DOMAIN_MODELS_FAILURE.concat(endPoint)),
});

interface Signature<P, T> extends RestRequestHandle<T>, RestCallbacks<T> {
  requestFunc: (props?: P) => any;
  formatData?: (data: any) => T;
  requestData?: P;
}

export const asyncRequest = <P, T>({
                      request, success, failure, afterSuccess, afterFailure,
                      requestFunc, requestData, formatData = (id) => id,
                    }: Signature<P, T>) =>
  async (dispatch) => {
    try {
      dispatch(request());
      const {data: domainModelData} = await requestFunc(requestData);
      dispatch(success(formatData(domainModelData)));
      if (afterSuccess) {
        dispatch(afterSuccess(domainModelData));
      }
    } catch (error) {
      const {response: {data}} = error;
      dispatch(failure(data));
      if (afterFailure) {
        dispatch(afterFailure(data.message));
      }
    }
  };

const restGET = <T>(endPoint: EndPoints, schema: Schema) => {
  type RequestSignature = string;
  const requestGet = requestHandle<Normalized<T>>(endPoint, RestRequestTypes.GET);
  const formatData = (data) => normalize(data, schema);
  const requestFunc = (requestData: RequestSignature) => restClient.get(makeUrl(endPoint, requestData));

  return (requestData?: RequestSignature) => asyncRequest<RequestSignature, Normalized<T>>({
    ...requestGet,
    formatData,
    requestFunc,
    requestData,
  });
};

const restPOST = <T>(endPoint: EndPoints, restCallbacks: RestCallbacks<T>) => {
  type RequestSignature = T;
  const requestPost = requestHandle<T>(endPoint, RestRequestTypes.POST);
  const requestFunc = (requestData: RequestSignature) => restClient.post(makeUrl(endPoint), requestData);

  return (requestData: T) => asyncRequest<T, T>({...requestPost, requestFunc, requestData, ...restCallbacks});
};

const restDELETE = <T>(endPoint: EndPoints, restCallbacks: RestCallbacks<T>) => {
  type RequestSignature = uuid;
  const requestDelete = requestHandle<T>(endPoint, RestRequestTypes.DELETE);
  const requestFunc = (requestData: RequestSignature) =>
    restClient.delete(makeUrl(`${endPoint}/${encodeURIComponent(requestData.toString())}`));

  return (requestData: uuid) => asyncRequest<uuid, T>({...requestDelete, requestFunc, requestData, ...restCallbacks});
};

export const fetchSelections = restGET<IdNamed>(EndPoints.selections, selectionsSchema);
export const fetchGateways = restGET<Gateway>(EndPoints.gateways, gatewaySchema);
export const fetchMeters = restGET<Gateway>(EndPoints.meters, meterSchema);
export const fetchUsers = restGET<User>(EndPoints.users, userSchema);

export const addUser = restPOST<User>(EndPoints.users, {
  afterSuccess: (domainModel: User) =>
    showMessage(firstUpperTranslated('successfully created the user {{name}} ({{email}})', {...domainModel})),
  afterFailure: (error: ErrorResponse) =>
    showMessage(firstUpperTranslated('failed to create user: {{error}}', {error})),
});

export const deleteUser = restDELETE<User>(
  EndPoints.users,
  {
    afterSuccess: (domainModel: User) =>
      showMessage(firstUpperTranslated('successfully deleted the user {{name}} ({{email}})', {...domainModel})),
    afterFailure: (error: ErrorResponse) =>
      showMessage(firstUpperTranslated('failed to delete the user: {{error}}', {error})),
  },
);
