import {Dispatch} from 'react-redux';
import {EmptyAction} from 'typesafe-actions/dist/type-helpers';
import {InvalidToken} from '../../exceptions/InvalidToken';
import {makeUrl} from '../../helpers/urlFactory';
import {GetState, RootState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../services/restClient';
import {Action, emptyActionOf, ErrorResponse, Identifiable, payloadActionOf, uuid} from '../../types/Types';
import {logout} from '../../usecases/auth/authActions';
import {noInternetConnection, requestTimeout, responseMessageOrFallback} from '../api/apiActions';
import {RequestCallbacks} from '../domain-models/domainModelsActions';
import {
  NormalizedPaginatedState,
  PageNumbered,
  PaginatedDomainModelsState,
  SingleEntityFailure
} from './paginatedDomainModels';

export const domainModelsPaginatedEntityRequest = (endPoint: EndPoints | string) =>
  `DOMAIN_MODELS_PAGINATED_ENTITY_REQUEST${endPoint}`;

export const domainModelsPaginatedEntitySuccess = (endPoint: EndPoints | string) =>
  `DOMAIN_MODELS_PAGINATED_ENTITY_SUCCESS${endPoint}`;

export const domainModelsPaginatedEntityFailure = (endPoint: EndPoints | string) =>
  `DOMAIN_MODELS_PAGINATED_ENTITY_FAILURE${endPoint}`;

export const domainModelsPaginatedDeleteRequest = (endPoint: EndPoints | string) =>
  `DOMAIN_MODELS_PAGINATED_DELETE_REQUEST${endPoint}`;

export const domainModelsPaginatedDeleteSuccess = (endPoint: EndPoints | string) =>
  `DOMAIN_MODELS_PAGINATED_DELETE_SUCCESS${endPoint}`;

export const domainModelsPaginatedDeleteFailure = (endPoint: EndPoints | string) =>
  `DOMAIN_MODELS_PAGINATED_DELETE_FAILURE${endPoint}`;

interface PaginatedRequestEntityHandler<T> {
  request: () => EmptyAction<string>;
  success: (payload: T) => Action<T>;
  failure: (payload: SingleEntityFailure) => Action<SingleEntityFailure>;
}

interface AsyncRequestEntity<DATA> extends PaginatedRequestEntityHandler<DATA>, RequestCallbacks<DATA> {
  requestFunc: (id?: uuid) => any;
  formatData?: (response) => any;
  dispatch: Dispatch<RootState>;
  id?: uuid;
  page?: number;
}

export const makeEntityRequestActionsOf = <T>(endPoint: EndPoints): PaginatedRequestEntityHandler<T> => ({
  request: emptyActionOf(domainModelsPaginatedEntityRequest(endPoint)),
  success: payloadActionOf<T>(domainModelsPaginatedEntitySuccess(endPoint)),
  failure: payloadActionOf<SingleEntityFailure>(domainModelsPaginatedEntityFailure(endPoint)),
});

const asyncRequestEntities = async <DATA>({
  request,
  success,
  failure,
  afterSuccess,
  afterFailure,
  requestFunc,
  formatData = (identity) => identity,
  id,
  dispatch,
  page
}: AsyncRequestEntity<DATA>) => {
  try {
    dispatch(request());
    const {data} = await requestFunc(id);
    const formattedData = formatData(data);
    dispatch(success({...formattedData, page}));
    if (afterSuccess) {
      afterSuccess(formattedData, dispatch);
    }
  } catch (error) {
    if (error instanceof InvalidToken) {
      await dispatch(logout(error));
    } else if (wasRequestCanceled(error)) {
      return;
    } else if (isTimeoutError(error)) {
      dispatch(failure({id: id || -1, ...requestTimeout()}));
    } else if (!error.response) {
      dispatch(failure({id: id || -1, ...noInternetConnection()}));
    } else {
      const errorResponse: ErrorResponse = responseMessageOrFallback(error.response);
      dispatch(failure({id: id || -1, ...errorResponse}));
      if (afterFailure) {
        afterFailure(errorResponse, dispatch);
      }
    }
  }
};

const shouldFetchEntity = (
  id: uuid,
  {entities, isFetchingSingle, nonExistingSingles}: NormalizedPaginatedState<Identifiable>,
): boolean =>
  !entities[id] && !isFetchingSingle && !nonExistingSingles[id];

export const fetchEntityIfNeeded = <T>(
  endPoint: EndPoints,
  entityType: keyof PaginatedDomainModelsState,
  formatData: (value: T) => any = (identity) => identity,
) =>
  (id: uuid) =>
    (dispatch, getState: GetState) => {
      const {paginatedDomainModels} = getState();

      if (shouldFetchEntity(id, paginatedDomainModels[entityType])) {
        const requestFunc = () =>
          restClient.get(makeUrl(`${endPoint}/${encodeURIComponent(id.toString())}`));
        return asyncRequestEntities<T>({
          ...makeEntityRequestActionsOf<T>(endPoint),
          requestFunc,
          formatData: (data) => formatData(data),
          id,
          dispatch,
        });
      } else {
        return null;
      }
    };

export const makePaginatedDeleteRequestActions =
  <T>(endPoint: EndPoints): PaginatedRequestEntityHandler<T> => ({
    request: emptyActionOf(domainModelsPaginatedDeleteRequest(endPoint)),
    success: payloadActionOf<T & PageNumbered>(domainModelsPaginatedDeleteSuccess(endPoint)),
    failure: payloadActionOf<SingleEntityFailure>(domainModelsPaginatedDeleteFailure(endPoint)),
  });

export const paginatedDeleteRequest = <T>(endPoint: EndPoints, requestCallbacks: RequestCallbacks<T>) =>
  (id: uuid, page: number) =>
    (dispatch) =>
      asyncRequestEntities<T>({
        ...makePaginatedDeleteRequestActions<T & PageNumbered>(endPoint),
        requestFunc: (id: uuid) => restClient.delete(makeUrl(`${endPoint}/${encodeURIComponent(id.toString())}`)),
        id,
        ...requestCallbacks,
        dispatch,
        page,
      });
