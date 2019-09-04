import {createStandardAction} from 'typesafe-actions';
import {InvalidToken} from '../../exceptions/InvalidToken';
import {makeUrl} from '../../helpers/urlFactory';
import {GetState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../services/restClient';
import {
  Action,
  ActionKey,
  Dispatch,
  ErrorResponse,
  FetchPaginated,
  Identifiable,
  payloadActionOf
} from '../../types/Types';
import {logout} from '../../usecases/auth/authActions';
import {noInternetConnection, requestTimeout, responseMessageOrFallback} from '../api/apiActions';
import {RequestType} from '../domain-models/domainModels';
import {DataFormatter, RequestCallbacks} from '../domain-models/domainModelsActions';
import {SortOption} from '../ui/pagination/paginationModels';
import {
  NormalizedPaginated,
  NormalizedPaginatedState,
  PageNumbered,
  PaginatedDomainModelsState,
} from './paginatedDomainModels';

export const domainModelsPaginatedRequest = (actionKey: ActionKey) =>
  `DOMAIN_MODELS_PAGINATED_REQUEST_${actionKey}`;

export const domainModelsPaginatedGetSuccess = (actionKey: ActionKey) =>
  `DOMAIN_MODELS_PAGINATED_${RequestType.GET}_SUCCESS_${actionKey}`;

export const domainModelsPaginatedFailure = (actionKey: ActionKey) =>
  `DOMAIN_MODELS_PAGINATED_FAILURE_${actionKey}`;

export const domainModelPaginatedClearError = (actionKey: ActionKey) =>
  `DOMAIN_MODELS_PAGINATED_CLEAR_ERROR_${actionKey}`;

export const sortBatchReferences = createStandardAction(`SORT_BATCH_REFERENCES`)<SortOption[] | undefined>();
export const sortMeters = createStandardAction(`SORT_METERS`)<SortOption[] | undefined>();
export const sortCollectionStats = createStandardAction(`SORT_COLLECTION_STATS`)<SortOption[] | undefined>();
export const sortMeterCollectionStats = createStandardAction(`SORT_METER_COLLECTION_STATS`)<SortOption[] | undefined>();

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
  dispatch: Dispatch;
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

export const makeRequestActionsOf =
  <T>(actionKey: ActionKey): PaginatedRequestHandler<T> => ({
    request: payloadActionOf<number>(domainModelsPaginatedRequest(actionKey)),
    success: payloadActionOf<T>(domainModelsPaginatedGetSuccess(actionKey)),
    failure: payloadActionOf<ErrorResponse & PageNumbered>(domainModelsPaginatedFailure(actionKey)),
  });

export const fetchIfNeeded = <T extends Identifiable>(
  actionKey: ActionKey,
  endPoint: EndPoints,
  formatData: DataFormatter<NormalizedPaginated<T>>,
  entityType: keyof PaginatedDomainModelsState,
  requestCallbacks?: RequestCallbacks<NormalizedPaginated<T>>,
): FetchPaginated =>
  (page: number, requestData?: string) =>
    (dispatch: Dispatch, getState: GetState) => {
      const {paginatedDomainModels} = getState();
      if (needAnotherPage(page, paginatedDomainModels[entityType])) {
        const requestFunc = (requestData: string) => restClient.get(makeUrl(endPoint, requestData));
        return asyncRequest<string, NormalizedPaginated<T>>({
          ...makeRequestActionsOf<NormalizedPaginated<T>>(actionKey),
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
