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

export const DOMAIN_MODELS_GET_REQUEST = 'DOMAIN_MODELS_GET_REQUEST';
export const DOMAIN_MODELS_GET_SUCCESS = 'DOMAIN_MODELS_GET_SUCCESS';
export const DOMAIN_MODELS_GET_FAILURE = 'DOMAIN_MODELS_GET_FAILURE';

export const DOMAIN_MODELS_POST_REQUEST = 'DOMAIN_MODELS_POST_REQUEST';
export const DOMAIN_MODELS_POST_SUCCESS = 'DOMAIN_MODELS_POST_SUCCESS';
export const DOMAIN_MODELS_POST_FAILURE = 'DOMAIN_MODELS_POST_FAILURE';

export const DOMAIN_MODELS_PUT_REQUEST = 'DOMAIN_MODELS_PUT_REQUEST';
export const DOMAIN_MODELS_PUT_SUCCESS = 'DOMAIN_MODELS_PUT_SUCCESS';
export const DOMAIN_MODELS_PUT_FAILURE = 'DOMAIN_MODELS_PUT_FAILURE';

export const DOMAIN_MODELS_DELETE_REQUEST = 'DOMAIN_MODELS_DELETE_REQUEST';
export const DOMAIN_MODELS_DELETE_SUCCESS = 'DOMAIN_MODELS_DELETE_SUCCESS';
export const DOMAIN_MODELS_DELETE_FAILURE = 'DOMAIN_MODELS_DELETE_FAILURE';

interface RestRequestHandle<T> {
  request: () => EmptyAction<string>;
  success: (payload: T) => PayloadAction<string, T>;
  failure: (payload: ErrorResponse) => PayloadAction<string, ErrorResponse>;
}

interface RestCallbacks<T> {
  afterSuccess?: (domainModel: T) => void;
  afterFailure?: (error: ErrorResponse) => void;
}

enum RestRequestTypes {
  GET = 'GET',
  POST = 'POST',
  PUT = 'PUT',
  DELETE = 'DELETE',
}

const domainModelRequestHandle = <T>(endPoint: EndPoints, requestType: RestRequestTypes): RestRequestHandle<T> => ({
  request: createEmptyAction<string>(`DOMAIN_MODELS_${requestType}_REQUEST`.concat(endPoint)),
  success: createPayloadAction<string, T>(`DOMAIN_MODELS_${requestType}_SUCCESS`.concat(endPoint)),
  failure: createPayloadAction<string, ErrorResponse>(`DOMAIN_MODELS_${requestType}_FAILURE`.concat(endPoint)),
});

const domainModelGetRequestHandle = <T>(endPoint: EndPoints): RestRequestHandle<Normalized<T>> =>
  domainModelRequestHandle<Normalized<T>>(endPoint, RestRequestTypes.GET);

const domainModelPostRequestHandle = <T>(endPoint: EndPoints): RestRequestHandle<T> =>
  domainModelRequestHandle(endPoint, RestRequestTypes.POST);

const domainModelPutRequestHandle = <T>(endPoint: EndPoints): RestRequestHandle<T> =>
  domainModelRequestHandle(endPoint, RestRequestTypes.PUT);

const domainModelDeleteRequestHandle = <T>(endPoint: EndPoints): RestRequestHandle<T> =>
  domainModelRequestHandle(endPoint, RestRequestTypes.DELETE);

const fetchDomainModel =
  <T>(endPoint: EndPoints, {request, success, failure}: RestRequestHandle<Normalized<T>>, schema: Schema) =>
    (encodedUriParameters?: string) =>
      async (dispatch) => {
        try {
          dispatch(request());
          const {data: domainModelData} = await restClient.get(makeUrl(endPoint, encodedUriParameters));
          dispatch(success(normalize(domainModelData, schema)));
        } catch (error) {
          const {response: {data}} = error;
          dispatch(failure(data));
        }
      };

// TODO adapt to all models (bundle them in an enum or such)
const postDomainModel =
  <T>(endPoint: EndPoints,
      {request, success, failure}: RestRequestHandle<T>,
      {afterSuccess, afterFailure}: RestCallbacks<T>) =>
    (domainModel: T) =>
      async (dispatch) => {
        try {
          dispatch(request());
          const {data: domainModelData} = await restClient.post(makeUrl(endPoint), domainModel);
          dispatch(success(domainModelData));
          if (afterSuccess) {
            dispatch(afterSuccess(domainModelData));
          }
        } catch (error) {
          const {response: {data: {message}}} = error;
          dispatch(failure(message));
          if (afterFailure) {
            dispatch(afterFailure(message));
          }
        }
      };

const deleteDomainModel =
  <T>(endPoint: EndPoints,
      {request, success, failure}: RestRequestHandle<T>,
      {afterSuccess, afterFailure}: RestCallbacks<T>) =>
    (modelId: uuid) =>
      async (dispatch) => {
        try {
          dispatch(request());
          const {data: domainModel} =
            await restClient.delete(makeUrl(`${endPoint}/${encodeURIComponent(modelId.toString())}`));
          dispatch(success(domainModel));
          if (afterSuccess) {
            dispatch(afterSuccess(domainModel));
          }
        } catch (error) {
          const {response: {data}} = error;
          dispatch(failure(data));
          if (afterFailure) {
            dispatch(afterFailure(data));
          }
        }
      };

export const selectionsRequest = domainModelGetRequestHandle<IdNamed>(EndPoints.selections);
export const fetchSelections = fetchDomainModel<IdNamed>(EndPoints.selections, selectionsRequest, selectionsSchema);

export const gatewayRequest = domainModelGetRequestHandle<Gateway>(EndPoints.gateways);
export const fetchGateways = fetchDomainModel<Gateway>(EndPoints.gateways, gatewayRequest, gatewaySchema);

export const meterRequest = domainModelGetRequestHandle<Gateway>(EndPoints.meters);
export const fetchMeters = fetchDomainModel<Gateway>(EndPoints.meters, meterRequest, meterSchema);

export const userRequest = domainModelGetRequestHandle<User>(EndPoints.users);
export const fetchUsers = fetchDomainModel<User>(EndPoints.users, userRequest, userSchema);

// TODO we might want to pass afterSuccess and afterFailure to the action from the application's containers
const userPostRequest = domainModelPostRequestHandle<User>(EndPoints.users);
export const addUser = postDomainModel<User>(EndPoints.users, userPostRequest, {
  afterSuccess: (domainModel: User) =>
    showMessage(firstUpperTranslated('successfully created the user {{name}} ({{email}})', {...domainModel})),
  afterFailure: (error: ErrorResponse) =>
    showMessage(firstUpperTranslated('failed to create user: {{error}}', {error})),
});

const userDeleteRequest = domainModelDeleteRequestHandle(EndPoints.users);
export const deleteUser = deleteDomainModel(
  EndPoints.users,
  userDeleteRequest,
  {
    afterSuccess: (domainModel: User) =>
      showMessage(firstUpperTranslated('successfully deleted the user {{name}} ({{email}})', {...domainModel})),
    afterFailure: (error: ErrorResponse) =>
      showMessage(firstUpperTranslated('failed to delete the user: {{error}}', {error})),
  },
);
