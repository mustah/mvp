import {Dispatch} from 'react-redux';
import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {EmptyAction, PayloadAction} from 'ts-redux-actions';
import {makeUrl} from '../../helpers/urlFactory';
import {GetState, RootState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {InvalidToken, restClient} from '../../services/restClient';
import {firstUpperTranslated} from '../../services/translationService';
import {ErrorResponse, Identifiable, uuid} from '../../types/Types';
import {logout} from '../../usecases/auth/authActions';
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
  success: (payload) => PayloadAction<string, T>;
  failure: (payload) => PayloadAction<string, SingleEntityFailure>;
}

interface AsyncRequestEntity<DATA> extends PaginatedRequestEntityHandler<DATA>, RequestCallbacks<DATA> {
  requestFunc: (requestData: uuid) => any;
  dispatch: Dispatch<RootState>;
  id: uuid;
}

export const getRequestEntityOf = <T>(endPoint: EndPoints): PaginatedRequestEntityHandler<T> => ({
  request: createEmptyAction<string>(domainModelsPaginatedEntityRequest(endPoint)),
  success: createPayloadAction<string, T>(domainModelsPaginatedEntitySuccess(endPoint)),
  failure: createPayloadAction<string, SingleEntityFailure>(domainModelsPaginatedEntityFailure(endPoint)),
});

const asyncRequestEntity = async <DAT>(
  {
    request,
    success,
    failure,
    afterSuccess,
    afterFailure,
    requestFunc,
    id,
    dispatch,
  }: AsyncRequestEntity<DAT>) => {
  try {
    dispatch(request());
    const {data: entityData} = await requestFunc(id);
    dispatch(success(entityData));
    if (afterSuccess) {
      afterSuccess(entityData, dispatch);
    }
  } catch (error) {
    if (error instanceof InvalidToken) {
      await dispatch(logout(error));
    } else {
      const {response} = error;
      const data: ErrorResponse = response && response.data ||
        {message: firstUpperTranslated('an unexpected error occurred')};
      dispatch(failure({id, ...data}));
      if (afterFailure) {
        afterFailure(data, dispatch);
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
) =>
  (id: uuid) =>
    (dispatch, getState: GetState) => {
      const {paginatedDomainModels} = getState();

      if (shouldFetchEntity(id, paginatedDomainModels[entityType])) {
        const requestFunc = (requestData: uuid) =>
          restClient.get(makeUrl(`${endPoint}/${encodeURIComponent(requestData.toString())}`));
        return asyncRequestEntity<T>({
          ...getRequestEntityOf<T>(endPoint),
          requestFunc,
          id,
          dispatch,
        });
      } else {
        return null;
      }
    };
