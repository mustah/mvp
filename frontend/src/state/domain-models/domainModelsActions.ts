import {AxiosPromise} from 'axios';
import {EmptyAction, PayloadAction} from 'typesafe-actions/dist/type-helpers';
import {InvalidToken} from '../../exceptions/InvalidToken';
import {makeUrl} from '../../helpers/urlFactory';
import {GetState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../services/restClient';
import {
  ActionKey,
  Dispatch,
  emptyActionOf,
  EncodedUriParameters,
  ErrorResponse,
  Identifiable,
  Omit,
  payloadActionOf,
  uuid,
} from '../../types/Types';
import {logout} from '../../usecases/auth/authActions';
import {noInternetConnection, requestTimeout, responseMessageOrFallback} from '../api/apiActions';
import {DomainModelsState, Normalized, NormalizedState, RequestsHttp, RequestType} from './domainModels';

type ActionTypeFactory = (endPoint: EndPoints | string) => string;

const domainModelsSuccess =
  (requestType: RequestType) =>
    (actionKey: ActionKey) =>
      `DOMAIN_MODELS_${requestType}_SUCCESS_${actionKey}`;

export const domainModelsRequest = (actionKey: ActionKey) => `DOMAIN_MODELS_REQUEST_${actionKey}`;
export const domainModelsFailure = (actionKey: ActionKey) => `DOMAIN_MODELS_FAILURE_${actionKey}`;
export const domainModelsGetSuccess: ActionTypeFactory = domainModelsSuccess(RequestType.GET);
export const domainModelsGetEntitySuccess: ActionTypeFactory = domainModelsSuccess(RequestType.GET_ENTITY);
export const domainModelsPostSuccess: ActionTypeFactory = domainModelsSuccess(RequestType.POST);
export const domainModelsPutSuccess: ActionTypeFactory = domainModelsSuccess(RequestType.PUT);
export const domainModelsDeleteSuccess: ActionTypeFactory = domainModelsSuccess(RequestType.DELETE);

export const domainModelsClearError = (actionKey: ActionKey): string =>
  `DOMAIN_MODELS_CLEAR_ERROR${actionKey}`;

export const clearError = (actionKey: ActionKey) =>
  emptyActionOf(domainModelsClearError(actionKey));

export const domainModelsClear = (actionKey: ActionKey): string =>
  `DOMAIN_MODELS_CLEAR_${actionKey}`;

export interface RequestCallbacks<ResponseData> {
  afterSuccess?: (domainModel: ResponseData, dispatch: Dispatch) => void;
  afterFailure?: (error: ErrorResponse, dispatch: Dispatch) => void;
}

export type DataFormatter<T> = (data?: any) => T;

interface RequestHandler<T> {
  request: () => EmptyAction<string>;
  success: (payload: T) => PayloadAction<string, T>;
  failure: (payload: ErrorResponse) => PayloadAction<string, ErrorResponse>;
}

interface AsyncRequest<REQUEST_DATA, T> extends RequestHandler<T>, RequestCallbacks<T> {
  requestFunc: (requestData?: REQUEST_DATA) => any;
  formatData?: DataFormatter<T>;
  requestData?: REQUEST_DATA;
  dispatch: Dispatch;
}

const asyncRequest = async <REQUEST_DATA, T>(
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
  }: AsyncRequest<REQUEST_DATA, T>) => {
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
    } else if (wasRequestCanceled(error)) {
      return;
    } else if (isTimeoutError(error)) {
      dispatch(failure(requestTimeout()));
    } else if (!error.response) {
      dispatch(failure(noInternetConnection()));
    } else {
      const errorResponse: ErrorResponse = responseMessageOrFallback(error.response);
      dispatch(failure(errorResponse));
      if (afterFailure) {
        afterFailure(errorResponse, dispatch);
      }
    }
  }
};

export const shouldFetch = ({isSuccessfullyFetched, isFetching, error}: RequestsHttp): boolean =>
  !isSuccessfullyFetched && !isFetching && !error;

const shouldFetchEntity = (
  id: uuid,
  {isSuccessfullyFetched, isFetching, error, entities}: NormalizedState<Identifiable>,
): boolean =>
  !isSuccessfullyFetched && !isFetching && !error && !entities[id];

export const fetchIfNeededForSector = <T extends Identifiable>(
  actionKey: ActionKey,
  endPoint: EndPoints,
  entityType: keyof Omit<DomainModelsState, 'meterDetailMeasurement'>,
  formatData: DataFormatter<Normalized<T>>,
  requestCallbacks?: RequestCallbacks<Normalized<T>>,
) =>
  (requestData?: EncodedUriParameters) =>
    (dispatch, getState: GetState) => {
      if (shouldFetch(getState().domainModels[entityType])) {
        const requestFunc = (requestData: EncodedUriParameters): AxiosPromise<T> =>
          restClient.get(makeUrl(endPoint, requestData));
        return asyncRequest<string, Normalized<T>>({
          ...getRequestOf<Normalized<T>>(actionKey),
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

export const fetchIfNeeded = <T extends Identifiable>(
  endPoint: EndPoints,
  entityType: keyof Omit<DomainModelsState, 'meterDetailMeasurement'>,
  formatData: DataFormatter<Normalized<T>>,
  requestCallbacks?: RequestCallbacks<Normalized<T>>,
) =>
  fetchIfNeededForSector<T>(endPoint, endPoint, entityType, formatData, requestCallbacks);

export const fetchEntityIfNeeded = <T extends Identifiable>(
  endPoint: EndPoints,
  entityType: keyof Omit<DomainModelsState, 'meterDetailMeasurement'>,
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

export const postRequest = <T>(
  endPoint: EndPoints,
  requestCallbacks: RequestCallbacks<T>
) =>
  (requestData: T) =>
    (dispatch) =>
      asyncRequest<T, T>({
        ...postRequestOf<T>(endPoint),
        requestFunc: (requestData: T) => restClient.post(endPoint, requestData),
        requestData,
        ...requestCallbacks,
        dispatch,
      });

export const postRequestToUrl = <T, P>(
  endPoint: EndPoints,
  requestCallbacks: RequestCallbacks<T>,
  url: (parameters: P) => string
) =>
  (requestData: T, urlParameters: P) =>
    (dispatch: Dispatch) =>
      asyncRequest<T, T>({
        ...postRequestOf<T>(endPoint),
        requestFunc: (requestData: T) => restClient.post(url(urlParameters), requestData),
        requestData,
        ...requestCallbacks,
        dispatch,
      });

export const putRequestToUrl = <T, REQUEST_DATA, URL_PARAMETERS>(
  endPoint: EndPoints,
  requestCallbacks: RequestCallbacks<T>,
  url: (parameters: URL_PARAMETERS) => string
) =>
  (requestData: REQUEST_DATA, urlParameters: URL_PARAMETERS) =>
    (dispatch) =>
      asyncRequest<REQUEST_DATA, T>({
        ...putRequestOf<T>(endPoint),
        requestFunc: (requestData: REQUEST_DATA) => restClient.put(url(urlParameters), requestData),
        requestData,
        ...requestCallbacks,
        dispatch,
      });

export const putRequest = <T, REQUEST_DATA>(
  endPoint: EndPoints,
  requestCallbacks: RequestCallbacks<T>
) =>
  (requestData: REQUEST_DATA) =>
    (dispatch) =>
      asyncRequest<REQUEST_DATA, T>({
        ...putRequestOf<T>(endPoint),
        requestFunc: (requestData: REQUEST_DATA) => restClient.put(endPoint, requestData),
        requestData,
        ...requestCallbacks,
        dispatch,
      });

export const putFile = <URL_PARAMETERS>(
  endPoint: EndPoints,
  requestCallbacks: RequestCallbacks<undefined>,
  url: (parameters: URL_PARAMETERS) => string
) =>
  (requestData: FormData, urlParameters: URL_PARAMETERS) =>
    (dispatch) =>
      asyncRequest<FormData, undefined>({
        ...putRequestOf<undefined>(endPoint),
        requestFunc: (requestData: FormData) =>
          restClient.put(
            url(urlParameters),
            requestData,
            {
              headers: {
                'content-type': 'multipart/form-data',
              },
            }
          ),
        requestData,
        ...requestCallbacks,
        dispatch,
      });

export const deleteRequest = <T>(endPoint: EndPoints, requestCallbacks: RequestCallbacks<T>) =>
  (requestData: uuid) =>
    (dispatch) =>
      asyncRequest<uuid, T>({
        ...deleteRequestOf<T>(endPoint),
        requestFunc: (requestData: uuid) =>
          restClient.delete(makeUrl(`${endPoint}/${encodeURIComponent(requestData.toString())}`)),
        requestData,
        ...requestCallbacks,
        dispatch,
      });

export const deleteRequestToUrl = <T, REQUEST_DATA>(
  endPoint: EndPoints,
  requestCallbacks: RequestCallbacks<T>,
  url: (parameters: REQUEST_DATA) => string
) =>
  (requestData: REQUEST_DATA) =>
    (dispatch) =>
      asyncRequest<REQUEST_DATA, T>({
        ...deleteRequestOf<T>(endPoint),
        requestFunc: (requestData: REQUEST_DATA) =>
          restClient.delete(url(requestData)),
        requestData,
        ...requestCallbacks,
        dispatch,
      });

const makeRequestActionsOf = <T>(
  actionKey: ActionKey,
  requestType: RequestType,
): RequestHandler<T> => ({
  request: emptyActionOf(domainModelsRequest(actionKey)),
  success: payloadActionOf<T>(domainModelsSuccess(requestType)(actionKey)),
  failure: payloadActionOf<ErrorResponse>(domainModelsFailure(actionKey)),
});

export const getRequestOf = <T>(actionKey: ActionKey) =>
  makeRequestActionsOf<T>(actionKey, RequestType.GET);

export const getEntityRequestOf = <T>(endpoint: EndPoints) =>
  makeRequestActionsOf<T>(endpoint, RequestType.GET_ENTITY);

export const postRequestOf = <T>(endpoint: EndPoints) =>
  makeRequestActionsOf<T>(endpoint, RequestType.POST);

export const putRequestOf = <T>(endpoint: EndPoints) =>
  makeRequestActionsOf<T>(endpoint, RequestType.PUT);

export const deleteRequestOf = <T>(endpoint: EndPoints) =>
  makeRequestActionsOf<T>(endpoint, RequestType.DELETE);
