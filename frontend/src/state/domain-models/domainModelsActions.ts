import {normalize, Schema} from 'normalizr';
import {Dispatch} from 'react-redux';
import {createEmptyAction, createPayloadAction, EmptyAction, PayloadAction} from 'react-redux-typescript';
import {makeUrl} from '../../helpers/urlFactory';
import {GetState, RootState} from '../../reducers/rootReducer';
import {InvalidToken, restClient} from '../../services/restClient';
import {firstUpperTranslated} from '../../services/translationService';
import {ErrorResponse, HasId, uuid} from '../../types/Types';
import {logout} from '../../usecases/auth/authActions';
import {NormalizedPaginatedResult} from '../domain-models-paginated/paginatedDomainModels';
import {limit} from '../ui/pagination/paginationReducer';
import {DomainModelsState, EndPoints, HttpMethod, Normalized, NormalizedState} from './domainModels';

type DomainModelTypeCreator = (endPoint: EndPoints) => string;

const domainModelsSuccess = (httpMethod: HttpMethod) => (endPoint: EndPoints) =>
  `DOMAIN_MODELS_${httpMethod}_SUCCESS${endPoint}`;

export const domainModelsRequest = (endPoint: EndPoints) => `DOMAIN_MODELS_REQUEST${endPoint}`;
export const domainModelsFailure = (endPoint: EndPoints) => `DOMAIN_MODELS_FAILURE${endPoint}`;
export const domainModelsGetSuccess: DomainModelTypeCreator = domainModelsSuccess(HttpMethod.GET);
export const domainModelsGetEntitySuccess: DomainModelTypeCreator = domainModelsSuccess(HttpMethod.GET_ENTITY);
export const domainModelsPostSuccess: DomainModelTypeCreator = domainModelsSuccess(HttpMethod.POST);
export const domainModelsPutSuccess: DomainModelTypeCreator = domainModelsSuccess(HttpMethod.PUT);
export const domainModelsDeleteSuccess: DomainModelTypeCreator = domainModelsSuccess(HttpMethod.DELETE);

export const domainModelsClearError = (endPoint: EndPoints) => `DOMAIN_MODELS_CLEAR_ERROR${endPoint}`;

export const clearError = (endPoint: EndPoints) =>
  createEmptyAction<string>(domainModelsClearError(endPoint));

interface RestRequestHandle<T> {
  request: () => EmptyAction<string>;
  success: (payload: T) => PayloadAction<string, T>;
  failure: (payload: ErrorResponse) => PayloadAction<string, ErrorResponse>;
}

interface RestCallbacks<T> {
  afterSuccess?: (domainModel: T, dispatch: Dispatch<RootState>) => void;
  afterFailure?: (error: ErrorResponse, dispatch: Dispatch<RootState>) => void;
}

export const requestMethod = <T>(
  endPoint: EndPoints,
  requestType: HttpMethod,
): RestRequestHandle<T> => ({
  request: createEmptyAction<string>(domainModelsRequest(endPoint)),
  success: createPayloadAction<string, T>(domainModelsSuccess(requestType)(endPoint)),
  failure: createPayloadAction<string, ErrorResponse>(domainModelsFailure(endPoint)),
});

interface AsyncRequest<REQ, DAT> extends RestRequestHandle<DAT>, RestCallbacks<DAT> {
  requestFunc: (requestData?: REQ) => any;
  formatData?: (data: any) => DAT;
  requestData?: REQ;
  dispatch: Dispatch<RootState>;
}

// TODO: Add tests for this function? yes. what about not wrapping afterSuccess() in the same try-catch?
const asyncRequest = async <REQ, DAT>(
  {
    request,
    success,
    failure,
    afterSuccess,
    afterFailure,
    requestFunc,
    formatData = (id) => id,
    requestData,
    dispatch,
  }: AsyncRequest<REQ, DAT>) => {
  try {
    dispatch(request());
    const {data: domainModelData} = await requestFunc(requestData);
    const formattedData = formatData(domainModelData);
    dispatch(success(formattedData));
    if (afterSuccess) {
      afterSuccess(formattedData, dispatch);
    }
  } catch (error) {
    if (error instanceof InvalidToken) {
      await dispatch(logout(error));
    } else {
      const {response} = error;
      const data: ErrorResponse = response && response.data ||
        {message: firstUpperTranslated('an unexpected error occurred')};
      dispatch(failure(data));
      if (afterFailure) {
        afterFailure(data, dispatch);
      }
    }
  }
};
const shouldFetch = ({isSuccessfullyFetched, isFetching, error}: NormalizedState<HasId>): boolean =>
  !isSuccessfullyFetched && !isFetching && !error;
const shouldFetchEntity = (id: uuid, {isFetching, error, entities}: NormalizedState<HasId>): boolean =>
  !isFetching && !error && !entities[id];

export const restGetIfNeeded = <T extends HasId>(
  endPoint: EndPoints,
  schema: Schema,
  entityType: keyof DomainModelsState,
  restCallbacks?: RestCallbacks<Normalized<T>>,
) => {
  const requestGet = requestMethod<Normalized<T>>(endPoint, HttpMethod.GET);
  const formatData = (data) => normalize(data, schema);
  const requestFunc = (requestData: string) => restClient.get(makeUrl(endPoint, requestData));

  return (requestData?: string) => (dispatch, getState: GetState) => {
    const {domainModels} = getState();
    if (shouldFetch(domainModels[entityType])) {
      return asyncRequest<string, Normalized<T>>({
        ...requestGet,
        formatData,
        requestFunc,
        requestData,
        ...restCallbacks,
        dispatch,
      });
    } else {
      return null;
    }
  };
};
export const restGetEntityIfNeeded = <T>(endPoint: EndPoints, entityType: keyof DomainModelsState) => {
  const requestGet = requestMethod<T>(endPoint, HttpMethod.GET_ENTITY);
  const requestFunc = (requestData: uuid) =>
    restClient.get(makeUrl(`${endPoint}/${encodeURIComponent(requestData.toString())}`));

  return (id: uuid) => (dispatch, getState: GetState) => {
    const {domainModels} = getState();
    if (shouldFetchEntity(id, domainModels[entityType])) {
      return asyncRequest<uuid, T>({
        ...requestGet,
        requestFunc,
        requestData: id,
        dispatch,
      });
    } else {
      return null;
    }
  };
};
export const restPost = <T>(endPoint: EndPoints, restCallbacks: RestCallbacks<T>) => {
  const requestPost = requestMethod<T>(endPoint, HttpMethod.POST);
  const requestFunc = (requestData: T) => restClient.post(makeUrl(endPoint), requestData);

  return (requestData: T) => (dispatch) => asyncRequest<T, T>({
    ...requestPost,
    requestFunc,
    requestData, ...restCallbacks,
    dispatch,
  });
};
export const restPut = <T>(endPoint: EndPoints, restCallbacks: RestCallbacks<T>) => {
  const requestPut = requestMethod<T>(endPoint, HttpMethod.PUT);
  const requestFunc = (requestData: T) => restClient.put(makeUrl(endPoint), requestData);

  return (requestData: T) => (dispatch) => asyncRequest<T, T>({
    ...requestPut,
    requestFunc,
    requestData, ...restCallbacks,
    dispatch,
  });
};
export const restDelete = <T>(endPoint: EndPoints, restCallbacks: RestCallbacks<T>) => {
  const requestDelete = requestMethod<T>(endPoint, HttpMethod.DELETE);
  const requestFunc = (requestData: uuid) =>
    restClient.delete(makeUrl(`${endPoint}/${encodeURIComponent(requestData.toString())}`));

  return (requestData: uuid) => (dispatch) => asyncRequest<uuid, T>({
    ...requestDelete,
    requestFunc,
    requestData, ...restCallbacks,
    dispatch,
  });
};

export const paginationMetaDataFromResult = (result: uuid[]): NormalizedPaginatedResult => ({
  content: result,
  totalPages: Math.ceil(result.length / limit),
  totalElements: result.length,
});
