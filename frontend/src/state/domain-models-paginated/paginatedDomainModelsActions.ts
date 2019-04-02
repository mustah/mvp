import {Dispatch} from 'react-redux';
import {createStandardAction} from 'typesafe-actions';
import {InvalidToken} from '../../exceptions/InvalidToken';
import {makeUrl} from '../../helpers/urlFactory';
import {GetState, RootState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../services/restClient';
import {Action, ErrorResponse, FetchPaginated, Identifiable, payloadActionOf} from '../../types/Types';
import {logout} from '../../usecases/auth/authActions';
import {noInternetConnection, requestTimeout, responseMessageOrFallback} from '../api/apiActions';
import {RequestType} from '../domain-models/domainModels';
import {DataFormatter, RequestCallbacks} from '../domain-models/domainModelsActions';
import {ApiRequestSortingOptions} from '../ui/pagination/paginationModels';
import {
  NormalizedPaginated,
  NormalizedPaginatedState,
  PageNumbered,
  PaginatedDomainModelsState,
} from './paginatedDomainModels';

export const domainModelsPaginatedRequest = (endPoint: EndPoints) => `DOMAIN_MODELS_PAGINATED_REQUEST${endPoint}`;
export const domainModelsPaginatedGetSuccess = (endPoint: EndPoints) =>
  `DOMAIN_MODELS_PAGINATED_${RequestType.GET}_SUCCESS${endPoint}`;
export const domainModelsPaginatedFailure = (endPoint: EndPoints) => `DOMAIN_MODELS_PAGINATED_FAILURE${endPoint}`;
export const domainModelPaginatedClearError = (endPoint: EndPoints) =>
  `DOMAIN_MODELS_PAGINATED_CLEAR_ERROR${endPoint}`;

export const sortTableAction = (endPoint: EndPoints) =>
  createStandardAction(`SORT_TABLE${endPoint}`)<ApiRequestSortingOptions[] | undefined>();

export const clearError = (endPoint: EndPoints) =>
  payloadActionOf<PageNumbered>(domainModelPaginatedClearError(endPoint));

interface PaginatedRequestHandler<T> {
  request: (payload: number) => Action<number>;
  success: (payload: T) => Action<T>;
  failure: (payload: ErrorResponse & PageNumbered) => Action<ErrorResponse & PageNumbered>;
}

interface AsyncRequest<REQUEST_MODEL, DATA> extends PaginatedRequestHandler<DATA>, RequestCallbacks<DATA> {
  requestFunc: (requestModel?: REQUEST_MODEL) => any;
  formatData?: DataFormatter<DATA>;
  requestData?: REQUEST_MODEL;
  page: number;
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
    page,
    dispatch,
  }: AsyncRequest<REQUEST_MODEL, DATA>) => {
  try {
    dispatch(request(page));
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
      dispatch(failure({...requestTimeout(), page}));
    } else if (!error.response) {
      dispatch(failure({...noInternetConnection(), page}));
    } else {
      const errorResponse: ErrorResponse = responseMessageOrFallback(error.response);
      dispatch(failure({...errorResponse, page}));
      if (afterFailure) {
        afterFailure(errorResponse, dispatch);
      }
    }
  }
};

const needAnotherPage = (page: number, {result}: NormalizedPaginatedState<Identifiable>): boolean =>
  !result[page]
  || (!result[page].isSuccessfullyFetched && !result[page].isFetching && !result[page].error);

export const makeRequestActionsOf = <T>(endPoint: EndPoints): PaginatedRequestHandler<T> => ({
  request: payloadActionOf<number>(domainModelsPaginatedRequest(endPoint)),
  success: payloadActionOf<T>(domainModelsPaginatedGetSuccess(endPoint)),
  failure: payloadActionOf<ErrorResponse & PageNumbered>(domainModelsPaginatedFailure(endPoint)),
});

export const fetchIfNeeded = <T extends Identifiable>(
  endPoint: EndPoints,
  formatData: DataFormatter<NormalizedPaginated<T>>,
  entityType: keyof PaginatedDomainModelsState,
  requestCallbacks?: RequestCallbacks<NormalizedPaginated<T>>,
): FetchPaginated =>
  (page: number, requestData?: string) =>
    (dispatch: Dispatch<RootState>, getState: GetState) => {
      const {paginatedDomainModels} = getState();
      if (needAnotherPage(page, paginatedDomainModels[entityType])) {
        const requestFunc = (requestData: string) => restClient.get(makeUrl(endPoint, requestData));
        return asyncRequest<string, NormalizedPaginated<T>>({
          ...makeRequestActionsOf<NormalizedPaginated<T>>(endPoint),
          formatData: (data) => ({...formatData(data), page}),
          requestFunc,
          requestData,
          page,
          ...requestCallbacks,
          dispatch,
        });
      } else {
        return null;
      }
    };
