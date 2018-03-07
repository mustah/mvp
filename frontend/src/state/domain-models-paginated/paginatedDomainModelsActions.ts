import {normalize, Schema} from 'normalizr';
import {Dispatch} from 'react-redux';
import {createPayloadAction, PayloadAction} from 'react-redux-typescript';
import {makeUrl} from '../../helpers/urlFactory';
import {GetState, RootState} from '../../reducers/rootReducer';
import {restClient} from '../../services/restClient';
import {firstUpperTranslated} from '../../services/translationService';
import {ErrorResponse, Identifiable} from '../../types/Types';
import {EndPoints, HttpMethod} from '../domain-models/domainModels';
import {
  HasPageNumber,
  NormalizedPaginated,
  NormalizedPaginatedState,
  PaginatedDomainModelsState,
  RestGetPaginated,
} from './paginatedDomainModels';

export const domainModelsPaginatedRequest = (endPoint: EndPoints) => `DOMAIN_MODELS_PAGINATED_REQUEST${endPoint}`;
export const domainModelsPaginatedGetSuccess = (endPoint: EndPoints) =>
  `DOMAIN_MODELS_PAGINATED_${HttpMethod.GET}_SUCCESS${endPoint}`;
export const domainModelsPaginatedFailure = (endPoint: EndPoints) => `DOMAIN_MODELS_PAGINATED_FAILURE${endPoint}`;
export const domainModelPaginatedClearError = (endPoint: EndPoints) =>
  `DOMAIN_MODELS_PAGINATED_CLEAR_ERROR${endPoint}`;

const clearError = (endPoint: EndPoints) =>
  createPayloadAction<string, HasPageNumber>(domainModelPaginatedClearError(endPoint));

export const clearErrorMeters = clearError(EndPoints.meters);

interface RestRequestHandlePaginated<T> {
  request: (payload) => PayloadAction<string, number>;
  success: (payload) => PayloadAction<string, T>;
  failure: (payload) => PayloadAction<string, ErrorResponse & HasPageNumber>;
}

export const requestMethodPaginated = <T>(endPoint: EndPoints): RestRequestHandlePaginated<T> => ({
  request: createPayloadAction<string, number>(domainModelsPaginatedRequest(endPoint)),
  success: createPayloadAction<string, T>(domainModelsPaginatedGetSuccess(endPoint)),
  failure: createPayloadAction<string, ErrorResponse & HasPageNumber>(domainModelsPaginatedFailure(endPoint)),
});

interface RestCallbacks<T> {
  afterSuccess?: (domainModel: T, dispatch: Dispatch<RootState>) => void;
  afterFailure?: (error: ErrorResponse, dispatch: Dispatch<RootState>) => void;
}

interface AsyncRequest<REQ, DAT> extends RestRequestHandlePaginated<DAT>, RestCallbacks<DAT> {
  requestFunc: (requestData?: REQ) => any;
  formatData?: (data: any) => DAT;
  requestData?: REQ;
  page: number;
  dispatch: Dispatch<RootState>;
}

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
    page,
    dispatch,
  }: AsyncRequest<REQ, DAT>) => {
  try {
    dispatch(request(page));
    const {data: domainModelData} = await requestFunc(requestData);
    const formattedData = formatData(domainModelData);
    dispatch(success(formattedData));
    if (afterSuccess) {
      afterSuccess(formattedData, dispatch);
    }
  } catch (error) {
    const {response} = error;
    const data: ErrorResponse = response && response.data ||
      {message: firstUpperTranslated('an unexpected error occurred')};
    dispatch(failure({...data, page}));
    if (afterFailure) {
      afterFailure(data, dispatch);
    }
  }
};

const shouldFetch = (page: number, {result}: NormalizedPaginatedState<Identifiable>): boolean =>
  !result[page]
  || (!result[page].isSuccessfullyFetched && !result[page].isFetching && !result[page].error);

export const restGetIfNeeded = <T extends Identifiable>(
  endPoint: EndPoints,
  schema: Schema,
  entityType: keyof PaginatedDomainModelsState,
  restCallbacks?: RestCallbacks<NormalizedPaginated<T>>,
): RestGetPaginated => {

  const requestGet = requestMethodPaginated<NormalizedPaginated<T>>(endPoint);
  const requestFunc = (requestData: string) => restClient.get(makeUrl(endPoint, requestData));

  return (page: number, requestData?: string) =>
    (dispatch: Dispatch<RootState>, getState: GetState) => {

      const {paginatedDomainModels} = getState();
      if (shouldFetch(page, paginatedDomainModels[entityType])) {
        return asyncRequest<string, NormalizedPaginated<T>>({
          ...requestGet,
          formatData: (data) => ({...normalize(data, schema), page}),
          requestFunc,
          requestData,
          page,
          ...restCallbacks,
          dispatch,
        });
      } else {
        return null;
      }
    };
};
