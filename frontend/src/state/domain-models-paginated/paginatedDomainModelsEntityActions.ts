import {Dispatch} from 'react-redux';
import {EmptyAction} from 'react-redux-typescript';
import {InvalidToken} from '../../exceptions/InvalidToken';
import {makeUrl} from '../../helpers/urlFactory';
import {GetState, RootState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../services/restClient';
import {Action, emptyActionOf, ErrorResponse, Identifiable, payloadActionOf, uuid} from '../../types/Types';
import {logout} from '../../usecases/auth/authActions';
import {noInternetConnection, requestTimeout, responseMessageOrFallback} from '../api/apiActions';
import {RequestCallbacks} from '../domain-models/domainModelsActions';
import {NormalizedPaginatedState, PaginatedDomainModelsState, SingleEntityFailure} from './paginatedDomainModels';

export const domainModelsPaginatedEntityRequest = (endPoint: EndPoints) =>
  `DOMAIN_MODELS_PAGINATED_ENTITY_REQUEST${endPoint}`;
export const domainModelsPaginatedEntitySuccess = (endPoint: EndPoints) =>
  `DOMAIN_MODELS_PAGINATED_ENTITY_SUCCESS${endPoint}`;
export const domainModelsPaginatedEntityFailure = (endPoint: EndPoints) =>
  `DOMAIN_MODELS_PAGINATED_ENTITY_FAILURE${endPoint}`;

interface PaginatedRequestEntityHandler<T> {
  request: () => EmptyAction<string>;
  success: (payload: T) => Action<T>;
  failure: (payload: SingleEntityFailure) => Action<SingleEntityFailure>;
}

interface AsyncRequestEntity<DATA> extends PaginatedRequestEntityHandler<DATA>, RequestCallbacks<DATA> {
  requestFunc: () => any;
  formatData?: (response) => any;
  dispatch: Dispatch<RootState>;
  id?: uuid;
}

export const makeEntityRequestActionsOf = <T>(endPoint: EndPoints): PaginatedRequestEntityHandler<T> => ({
  request: emptyActionOf(domainModelsPaginatedEntityRequest(endPoint)),
  success: payloadActionOf<T>(domainModelsPaginatedEntitySuccess(endPoint)),
  failure: payloadActionOf<SingleEntityFailure>(domainModelsPaginatedEntityFailure(endPoint)),
});

const asyncRequestEntities = async <DAT>(
  {
    request,
    success,
    failure,
    afterSuccess,
    afterFailure,
    requestFunc,
    formatData = (identity) => identity,
    id = -1,
    dispatch,
  }: AsyncRequestEntity<DAT>) => {
  try {
    dispatch(request());
    const {data} = await requestFunc();
    const formattedData = formatData(data);
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
      dispatch(failure({id, ...requestTimeout()}));
    } else if (!error.response) {
      dispatch(failure({id, ...noInternetConnection()}));
    } else {
      const errorResponse: ErrorResponse = responseMessageOrFallback(error.response);
      dispatch(failure({id, ...errorResponse}));
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

const shouldFetchEntities = (
  ids: uuid[],
  {entities, isFetchingSingle}: NormalizedPaginatedState<Identifiable>,
): boolean => {
  const isMissingEntity = ids.reduce((prev, id) => prev || !entities[id], false);
  return isMissingEntity && !isFetchingSingle;
};

const idRequestParams = (ids: uuid[]): string => ids.map((id: uuid) => `id=${id.toString()}`).join('&');

export const fetchEntityDetailsIfNeeded = <T>(
  endPoint: EndPoints,
  entityType: keyof PaginatedDomainModelsState,
  formatData: (values: T[]) => any = (identity) => identity,
) =>
  (ids: uuid[]) =>
    (dispatch, getState: GetState) => {
      const {paginatedDomainModels} = getState();

      if (shouldFetchEntities(ids, paginatedDomainModels[entityType])) {
        const requestFunc = () => restClient.get(makeUrl(`${endPoint}/details`, idRequestParams(ids)));
        return asyncRequestEntities<T[]>({
          ...makeEntityRequestActionsOf<T[]>(endPoint),
          formatData: (data) => formatData(data.content),
          requestFunc,
          dispatch,
        });
      } else {
        return null;
      }
    };
