import {AxiosPromise} from 'axios';
import {Dispatch} from 'react-redux';
import {EmptyAction, PayloadAction} from 'typesafe-actions/dist/type-helpers';
import {InvalidToken} from '../../exceptions/InvalidToken';
import {makeUrl} from '../../helpers/urlFactory';
import {GetState, RootState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../services/restClient';
import {
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

type ActionTypeFactory = (endPoint: EndPoints) => string;

const domainModelsSuccess =
  (requestType: RequestType) =>
    (endPoint: EndPoints) =>
      `DOMAIN_MODELS_${requestType}_SUCCESS${endPoint}`;

export const domainModelsRequest = (endPoint: EndPoints) => `DOMAIN_MODELS_REQUEST${endPoint}`;
export const domainModelsFailure = (endPoint: EndPoints) => `DOMAIN_MODELS_FAILURE${endPoint}`;
export const domainModelsGetSuccess: ActionTypeFactory = domainModelsSuccess(RequestType.GET);
export const domainModelsGetEntitySuccess: ActionTypeFactory = domainModelsSuccess(RequestType.GET_ENTITY);
export const domainModelsGetEntitiesSuccess: ActionTypeFactory = domainModelsSuccess(RequestType.GET_ENTITIES);
export const domainModelsPostSuccess: ActionTypeFactory = domainModelsSuccess(RequestType.POST);
export const domainModelsPutSuccess: ActionTypeFactory = domainModelsSuccess(RequestType.PUT);
export const domainModelsDeleteSuccess: ActionTypeFactory = domainModelsSuccess(RequestType.DELETE);

export const domainModelsClearError = (endPoint: EndPoints): string => `DOMAIN_MODELS_CLEAR_ERROR${endPoint}`;

export const clearError = (endPoint: EndPoints) => emptyActionOf(domainModelsClearError(endPoint));

export interface RequestCallbacks<T> {
  afterSuccess?: (domainModel: T, dispatch: Dispatch<RootState>) => void;
  afterFailure?: (error: ErrorResponse, dispatch: Dispatch<RootState>) => void;
}

export type DataFormatter<T> = (data?: any) => T;

interface RequestHandler<T> {
  request: () => EmptyAction<string>;
  success: (payload: T) => PayloadAction<string, T>;
  failure: (payload: ErrorResponse) => PayloadAction<string, ErrorResponse>;
}

interface AsyncRequest<REQUEST_MODEL, DATA> extends RequestHandler<DATA>, RequestCallbacks<DATA> {
  requestFunc: (requestData?: REQUEST_MODEL) => any;
  formatData?: DataFormatter<DATA>;
  requestData?: REQUEST_MODEL;
  dispatch: Dispatch<RootState>;
}

const asyncRequest = async <REQUEST_MODEL, DATA>(
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

export const fetchIfNeeded = <T extends Identifiable>(
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

export const fetchEntitiesIfNeeded = <T extends Identifiable>(
  endPoint: EndPoints,
  entityType: keyof Omit<DomainModelsState, 'meterDetailMeasurement'>,
  formatData: DataFormatter<Normalized<T>>,
  requestDataFactory: (meterIds: uuid[], gatewayId?: uuid) => string,
) =>
  (meterIds: uuid[], parameters: EncodedUriParameters, gatewayId?: uuid) =>
    (dispatch, getState: GetState) => {
      const idsToFetch: uuid[] = meterIds
        .filter((id: uuid) => shouldFetchEntity(id, getState().domainModels[entityType]));
      if (idsToFetch.length) {
        const requestFunc = (requestData: string) => restClient.get(makeUrl(endPoint, requestData));
        return asyncRequest<string, Normalized<T>>({
          ...getEntitiesRequestOf<Normalized<T>>(endPoint),
          formatData,
          requestFunc,
          requestData: `${requestDataFactory(idsToFetch, gatewayId)}&${parameters}`,
          dispatch,
        });
      } else {
        return null;
      }
    };

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
    (dispatch) =>
      asyncRequest<T, T>({
        ...postRequestOf<T>(endPoint),
        requestFunc: (requestData: T) => restClient.post(url(urlParameters), requestData),
        requestData,
        ...requestCallbacks,
        dispatch,
      });

export const putRequestToUrl = <T, D, P>(
  endPoint: EndPoints,
  requestCallbacks: RequestCallbacks<T>,
  url: (parameters: P) => string
) =>
  (requestData: D, urlParameters: P) =>
    (dispatch) =>
      asyncRequest<D, T>({
        ...putRequestOf<T>(endPoint),
        requestFunc: (requestData: D) => restClient.put(url(urlParameters), requestData),
        requestData,
        ...requestCallbacks,
        dispatch,
      });

export const putRequest = <T, D>(
  endPoint: EndPoints,
  requestCallbacks: RequestCallbacks<T>
) =>
  (requestData: D) =>
    (dispatch) =>
      asyncRequest<D, T>({
        ...putRequestOf<T>(endPoint),
        requestFunc: (requestData: D) => restClient.put(endPoint, requestData),
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

export const getEntitiesRequestOf = <T>(endpoint: EndPoints) =>
  makeRequestActionsOf<T>(endpoint, RequestType.GET_ENTITIES);

export const postRequestOf = <T>(endpoint: EndPoints) =>
  makeRequestActionsOf<T>(endpoint, RequestType.POST);

export const putRequestOf = <T>(endpoint: EndPoints) =>
  makeRequestActionsOf<T>(endpoint, RequestType.PUT);

export const deleteRequestOf = <T>(endpoint: EndPoints) =>
  makeRequestActionsOf<T>(endpoint, RequestType.DELETE);
