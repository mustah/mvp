import {normalize, Schema} from 'normalizr';
import {Dispatch} from 'react-redux';
import {createEmptyAction, createPayloadAction, EmptyAction, PayloadAction} from 'react-redux-typescript';
import {makeUrl} from '../../helpers/urlFactory';
import {GetState, RootState} from '../../reducers/rootReducer';
import {restClient} from '../../services/restClient';
import {firstUpperTranslated} from '../../services/translationService';
import {ErrorResponse, HasId, IdNamed, uuid} from '../../types/Types';
import {authSetUser} from '../../usecases/auth/authActions';
import {Meter} from '../domain-models-paginated/meter/meterModels';
import {metersAllSchema} from '../domain-models-paginated/meter/meterSchema';
import {NormalizedPaginatedResult} from '../domain-models-paginated/paginatedDomainModels';
import {showFailMessage, showSuccessMessage} from '../ui/message/messageActions';
import {paginationUpdateMetaData} from '../ui/pagination/paginationActions';
import {limit} from '../ui/pagination/paginationReducer';
import {DomainModelsState, EndPoints, HttpMethod, Normalized, NormalizedState} from './domainModels';
import {selectionsSchema} from './domainModelsSchemas';
import {Gateway} from './gateway/gatewayModels';
import {gatewaySchema} from './gateway/gatewaySchema';
import {Measurement} from './measurement/measurementModels';
import {measurementSchema} from './measurement/measurementSchema';
import {Organisation, User} from './user/userModels';
import {userSchema} from './user/userSchema';

export const DOMAIN_MODELS_REQUEST = (endPoint: EndPoints) => `DOMAIN_MODELS_REQUEST${endPoint}`;
export const DOMAIN_MODELS_FAILURE = (endPoint: EndPoints) => `DOMAIN_MODELS_FAILURE${endPoint}`;

const DOMAIN_MODELS_SUCCESS = (httpMethod: HttpMethod) => (endPoint: EndPoints) =>
  `DOMAIN_MODELS_${httpMethod}_SUCCESS${endPoint}`;

export const DOMAIN_MODELS_GET_SUCCESS = DOMAIN_MODELS_SUCCESS(HttpMethod.GET);
export const DOMAIN_MODELS_GET_ENTITY_SUCCESS = DOMAIN_MODELS_SUCCESS(HttpMethod.GET_ENTITY);
export const DOMAIN_MODELS_POST_SUCCESS = DOMAIN_MODELS_SUCCESS(HttpMethod.POST);
export const DOMAIN_MODELS_PUT_SUCCESS = DOMAIN_MODELS_SUCCESS(HttpMethod.PUT);
export const DOMAIN_MODELS_DELETE_SUCCESS = DOMAIN_MODELS_SUCCESS(HttpMethod.DELETE);

export const DOMAIN_MODELS_CLEAR = 'DOMAIN_MODELS_CLEAR';
export const domainModelsClear = createEmptyAction<string>(DOMAIN_MODELS_CLEAR);

interface RestRequestHandle<T> {
  request: () => EmptyAction<string>;
  success: (payload: T) => PayloadAction<string, T>;
  failure: (payload: ErrorResponse) => PayloadAction<string, ErrorResponse>;
}

interface RestCallbacks<T> {
  afterSuccess?: (domainModel: T, dispatch: Dispatch<RootState>) => void;
  afterFailure?: (error: ErrorResponse, dispatch: Dispatch<RootState>) => void;
}

export const requestMethod = <T>(endPoint: EndPoints, requestType: HttpMethod): RestRequestHandle<T> => ({
  request: createEmptyAction<string>(DOMAIN_MODELS_REQUEST(endPoint)),
  success: createPayloadAction<string, T>(DOMAIN_MODELS_SUCCESS(requestType)(endPoint)),
  failure: createPayloadAction<string, ErrorResponse>(DOMAIN_MODELS_FAILURE(endPoint)),
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
    const {response} = error;
    const data: ErrorResponse = response && response.data ||
      {message: firstUpperTranslated('an unexpected error occurred')};
    dispatch(failure(data));
    if (afterFailure) {
      afterFailure(data, dispatch);
    }
  }
};

const isFetchingOrExistingOrError = ({isSuccessfullyFetched, isFetching, error}: NormalizedState<HasId>) =>
  isSuccessfullyFetched || isFetching || error;

const restGetIfNeeded = <T extends HasId>(
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
    const shouldFetch = !isFetchingOrExistingOrError(domainModels[entityType]);

    if (shouldFetch) {
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

const restGetEntity = <T>(endPoint: EndPoints) => {
  const requestGet = requestMethod<T>(endPoint, HttpMethod.GET_ENTITY);
  const requestFunc = (requestData: uuid) =>
    restClient.get(makeUrl(`${endPoint}/${encodeURIComponent(requestData.toString())}`));

  return (requestData: uuid) => (dispatch) => asyncRequest<uuid, T>({
    ...requestGet,
    requestFunc,
    requestData,
    dispatch,
  });
};

const restPost = <T>(endPoint: EndPoints, restCallbacks: RestCallbacks<T>) => {
  const requestPost = requestMethod<T>(endPoint, HttpMethod.POST);
  const requestFunc = (requestData: T) => restClient.post(makeUrl(endPoint), requestData);

  return (requestData: T) => (dispatch) => asyncRequest<T, T>({
    ...requestPost,
    requestFunc,
    requestData, ...restCallbacks,
    dispatch,
  });
};

const restPut = <T>(endPoint: EndPoints, restCallbacks: RestCallbacks<T>) => {
  const requestPut = requestMethod<T>(endPoint, HttpMethod.PUT);
  const requestFunc = (requestData: T) => restClient.put(makeUrl(endPoint), requestData);

  return (requestData: T) => (dispatch) => asyncRequest<T, T>({
    ...requestPut,
    requestFunc,
    requestData, ...restCallbacks,
    dispatch,
  });
};

const restDelete = <T>(endPoint: EndPoints, restCallbacks: RestCallbacks<T>) => {
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

const paginationMetaDataFromResult = (result: uuid[]): NormalizedPaginatedResult => ({
  content: result,
  totalPages: Math.ceil(result.length / limit),
  totalElements: result.length,
});

// TODO: Since 'selections' isn't part of the DomainModelsState 'cities' is selected to check if anything
// have been fetched from 'selections', should perhaps come up with a better way of doing this.
export const fetchSelections = restGetIfNeeded<IdNamed>(EndPoints.selections, selectionsSchema, 'cities');
export const fetchMetersAll = restGetIfNeeded<Meter>(EndPoints.metersAll, metersAllSchema, 'metersAll');

export const fetchGateways = restGetIfNeeded<Gateway>(EndPoints.gateways, gatewaySchema, 'gateways', {
  afterSuccess: (
    {result},
    dispatch,
  ) => dispatch(paginationUpdateMetaData({entityType: 'gateways', ...paginationMetaDataFromResult(result)})),
});
export const fetchUsers = restGetIfNeeded<User>(EndPoints.users, userSchema, 'users');
export const fetchMeasurements =
  restGetIfNeeded<Measurement>(EndPoints.measurements, measurementSchema, 'measurements');
export const fetchUser = restGetEntity<User>(EndPoints.users);
// TODO: Add tests ^

export const addUser = restPost<User>(EndPoints.users, {
  afterSuccess: (user: User, dispatch: Dispatch<RootState>) => {
    dispatch(showSuccessMessage(firstUpperTranslated('successfully created the user {{name}} ({{email}})', {...user})));
  },
  afterFailure: (error: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showFailMessage(firstUpperTranslated('failed to create user: {{error}}', {error: error.message})));
  },
});

export const addOrganisation = restPost<Organisation>(EndPoints.organisations, {
  afterSuccess: (organisation: Organisation, dispatch: Dispatch<RootState>) => {
    dispatch(showSuccessMessage(
      firstUpperTranslated('successfully created the organisation {{name}} ({{code}})', {...organisation}),
    ));
  },
  afterFailure: (error: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showFailMessage(firstUpperTranslated('failed to create organisation: {{error}}', {error: error.message})));
  },
});

export const modifyUser = restPut<User>(EndPoints.users, {
  afterSuccess: (user: User, dispatch: Dispatch<RootState>) => {
    dispatch(showSuccessMessage(firstUpperTranslated('successfully updated user {{name}} ({{email}})', {...user})));
  },
  afterFailure: (error: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showFailMessage(firstUpperTranslated('failed to update user: {{error}}', {error: error.message})));
  },
});

export const modifyProfile = restPut<User>(EndPoints.users, {
  afterSuccess: (user: User, dispatch: Dispatch<RootState>) => {
    dispatch(showSuccessMessage(firstUpperTranslated('successfully updated profile', {...user})));
    dispatch(authSetUser(user));
  },
  afterFailure: (error: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showFailMessage(firstUpperTranslated('failed to update profile: {{error}}', {error: error.message})));
  },
});

export const deleteUser = restDelete<User>(EndPoints.users, {
    afterSuccess: (user: User, dispatch: Dispatch<RootState>) => {
      dispatch(
        showSuccessMessage(firstUpperTranslated('successfully deleted the user {{name}} ({{email}})', {...user})),
      );
    },
    afterFailure: (error: ErrorResponse, dispatch: Dispatch<RootState>) => {
      dispatch(showFailMessage(firstUpperTranslated('failed to delete the user: {{error}}', {error: error.message})));
    },
  },
);
