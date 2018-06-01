import {Dispatch} from 'react-redux';
import {createEmptyAction, EmptyAction, PayloadAction} from 'react-redux-typescript';
import {InvalidToken} from '../../exceptions/InvalidToken';
import {makeUrl} from '../../helpers/urlFactory';
import {GetState, RootState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {restClient, wasRequestCanceled} from '../../services/restClient';
import {
  emptyActionOf,
  EncodedUriParameters,
  ErrorResponse,
  Identifiable,
  payloadActionOf,
  uuid,
} from '../../types/Types';
import {logout} from '../../usecases/auth/authActions';
import {noInternetConnection, responseMessageOrFallback} from '../api/apiActions';
import {DomainModelsState, Normalized, NormalizedState, RequestType} from './domainModels';

type ActionTypeFactory = (endPoint: EndPoints) => string;

const domainModelsSuccess = (requestType: RequestType) => (endPoint: EndPoints) =>
  `DOMAIN_MODELS_${requestType}_SUCCESS${endPoint}`;

export const domainModelsRequest = (endPoint: EndPoints) => `DOMAIN_MODELS_REQUEST${endPoint}`;
export const domainModelsFailure = (endPoint: EndPoints) => `DOMAIN_MODELS_FAILURE${endPoint}`;
export const domainModelsGetSuccess: ActionTypeFactory = domainModelsSuccess(RequestType.GET);
export const domainModelsGetEntitySuccess: ActionTypeFactory = domainModelsSuccess(RequestType.GET_ENTITY);
export const domainModelsPostSuccess: ActionTypeFactory = domainModelsSuccess(RequestType.POST);
export const domainModelsPutSuccess: ActionTypeFactory = domainModelsSuccess(RequestType.PUT);
export const domainModelsDeleteSuccess: ActionTypeFactory = domainModelsSuccess(RequestType.DELETE);

export const domainModelsClearError = (endPoint: EndPoints) => `DOMAIN_MODELS_CLEAR_ERROR${endPoint}`;

export const clearError = (endPoint: EndPoints) =>
  createEmptyAction<string>(domainModelsClearError(endPoint));

export interface RequestCallbacks<T> {
  afterSuccess?: (domainModel: T, dispatch: Dispatch<RootState>) => void;
  afterFailure?: (error: ErrorResponse, dispatch: Dispatch<RootState>) => void;
}

export type DataFormatter = (data?: any) => any;

interface RequestHandler<T> {
  request: () => EmptyAction<string>;
  success: (payload: T) => PayloadAction<string, T>;
  failure: (payload: ErrorResponse) => PayloadAction<string, ErrorResponse>;
}

interface AsyncRequest<REQUEST_MODEL, DATA> extends RequestHandler<DATA>, RequestCallbacks<DATA> {
  requestFunc: (requestData?: REQUEST_MODEL) => any;
  formatData?: DataFormatter;
  requestData?: REQUEST_MODEL;
  dispatch: Dispatch<RootState>;
}

// TODO: Add tests for this function? yes. what about not wrapping afterSuccess() in the same
// try-catch?
export const asyncRequest = async <REQUEST_MODEL, DATA>(
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
  }: AsyncRequest<REQUEST_MODEL, DATA>) => {
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
    } else if (!error.response) {
      dispatch(failure(noInternetConnection()));
    } else if (wasRequestCanceled(error)) {
      return;
    } else {
      const errorResponse: ErrorResponse = responseMessageOrFallback(error.response);
      dispatch(failure(errorResponse));
      if (afterFailure) {
        afterFailure(errorResponse, dispatch);
      }
    }
  }
};

const shouldFetch = ({isSuccessfullyFetched, isFetching, error}: NormalizedState<Identifiable>): boolean =>
  !isSuccessfullyFetched && !isFetching && !error;

const shouldFetchEntity = (
  id: uuid,
  {isSuccessfullyFetched, isFetching, error, entities}: NormalizedState<Identifiable>,
): boolean =>
  !isSuccessfullyFetched && !isFetching && !error && !entities[id];

export const fetchIfNeeded = <T extends Identifiable>(
  endPoint: EndPoints,
  entityType: keyof DomainModelsState,
  formatData: DataFormatter,
  requestCallbacks?: RequestCallbacks<Normalized<T>>,
) =>
  (requestData?: string) =>
    (dispatch, getState: GetState) => {
      const {domainModels} = getState();
      if (shouldFetch(domainModels[entityType])) {
        const requestFunc = (requestData: string) => restClient.get(makeUrl(endPoint, requestData));
        return asyncRequest<string, Normalized<T>>({
          ...getRequestOf<Normalized<T>>(endPoint),
          formatData,
          requestFunc,
          requestData,
          ...requestCallbacks,
          dispatch,
        });
      } else {
        return null;
      }
    };

export const fetchEntityIfNeeded = <T extends Identifiable>(
  endPoint: EndPoints,
  entityType: keyof DomainModelsState,
) =>
  (id: uuid, parameters?: EncodedUriParameters) =>
    (dispatch, getState: GetState) => {
      const {domainModels} = getState();
      if (shouldFetchEntity(id, domainModels[entityType])) {
        const requestFunc = (id: uuid) =>
          restClient.get(makeUrl(`${endPoint}/${encodeURIComponent(id.toString())}`, parameters));
        return asyncRequest<uuid, T>({
          ...getEntityRequestOf<T>(endPoint),
          formatData: (data?: any) => data || ({id}),
          requestFunc,
          requestData: id,
          dispatch,
        });
      } else {
        return null;
      }
    };

export const postRequest = <T>(endPoint: EndPoints, requestCallbacks: RequestCallbacks<T>) => {
  const requestFunc = (requestData: T) => restClient.post(makeUrl(endPoint), requestData);

  return (requestData: T) =>
    (dispatch) =>
      asyncRequest<T, T>({
        ...postRequestOf<T>(endPoint),
        requestFunc,
        requestData,
        ...requestCallbacks,
        dispatch,
      });
};

export const putRequest = <T>(endPoint: EndPoints, requestCallbacks: RequestCallbacks<T>) => {
  const requestFunc = (requestData: T) => restClient.put(makeUrl(endPoint), requestData);

  return (requestData: T) =>
    (dispatch) =>
      asyncRequest<T, T>({
        ...putRequestOf<T>(endPoint),
        requestFunc,
        requestData,
        ...requestCallbacks,
        dispatch,
      });
};

export const deleteRequest = <T>(endPoint: EndPoints, requestCallbacks: RequestCallbacks<T>) => {
  const requestFunc = (requestData: uuid) =>
    restClient.delete(makeUrl(`${endPoint}/${encodeURIComponent(requestData.toString())}`));

  return (requestData: uuid) =>
    (dispatch) =>
      asyncRequest<uuid, T>({
        ...deleteRequestOf<T>(endPoint),
        requestFunc,
        requestData,
        ...requestCallbacks,
        dispatch,
      });
};

const makeRequestActionsOf = <T>(
  endPoint: EndPoints,
  requestType: RequestType,
): RequestHandler<T> => ({
  request: emptyActionOf(domainModelsRequest(endPoint)),
  success: payloadActionOf<T>(domainModelsSuccess(requestType)(endPoint)),
  failure: payloadActionOf<ErrorResponse>(domainModelsFailure(endPoint)),
});

export const getRequestOf = <T>(endpoint: EndPoints) =>
  makeRequestActionsOf<T>(endpoint, RequestType.GET);

export const getEntityRequestOf = <T>(endpoint: EndPoints) =>
  makeRequestActionsOf<T>(endpoint, RequestType.GET_ENTITY);

export const postRequestOf = <T>(endpoint: EndPoints) =>
  makeRequestActionsOf<T>(endpoint, RequestType.POST);

export const putRequestOf = <T>(endpoint: EndPoints) =>
  makeRequestActionsOf<T>(endpoint, RequestType.PUT);

export const deleteRequestOf = <T>(endpoint: EndPoints) =>
  makeRequestActionsOf<T>(endpoint, RequestType.DELETE);
