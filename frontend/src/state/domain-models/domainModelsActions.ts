import {normalize, Schema} from 'normalizr';
import {Dispatch} from 'react-redux';
import {createEmptyAction, createPayloadAction, EmptyAction, PayloadAction} from 'react-redux-typescript';
import {makeUrl} from '../../helpers/urlFactory';
import {RootState} from '../../reducers/rootReducer';
import {restClient} from '../../services/restClient';
import {firstUpperTranslated} from '../../services/translationService';
import {ErrorResponse, IdNamed, uuid} from '../../types/Types';
import {authSetUser} from '../../usecases/auth/authActions';
import {showMessage} from '../ui/message/messageActions';
import {EndPoints, HttpMethod, Normalized} from './domainModels';
import {selectionsSchema} from './domainModelsSchemas';
import {Gateway} from './gateway/gatewayModels';
import {gatewaySchema} from './gateway/gatewaySchema';
import {meterSchema} from './meter/meterSchema';
import {User} from './user/userModels';
import {userSchema} from './user/userSchema';

export const DOMAIN_MODELS_REQUEST = 'DOMAIN_MODELS_REQUEST';
export const DOMAIN_MODELS_FAILURE = 'DOMAIN_MODELS_FAILURE';

export const DOMAIN_MODELS_GET_SUCCESS = 'DOMAIN_MODELS_GET_SUCCESS';
export const DOMAIN_MODELS_POST_SUCCESS = 'DOMAIN_MODELS_POST_SUCCESS';
export const DOMAIN_MODELS_PUT_SUCCESS = 'DOMAIN_MODELS_PUT_SUCCESS';
export const DOMAIN_MODELS_DELETE_SUCCESS = 'DOMAIN_MODELS_DELETE_SUCCESS';

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
  request: createEmptyAction<string>(DOMAIN_MODELS_REQUEST.concat(endPoint)),
  success: createPayloadAction<string, T>(`DOMAIN_MODELS_${requestType}_SUCCESS`.concat(endPoint)),
  failure: createPayloadAction<string, ErrorResponse>(DOMAIN_MODELS_FAILURE.concat(endPoint)),
});

interface AsyncRequest<REQ, DAT> extends RestRequestHandle<DAT>, RestCallbacks<DAT> {
  requestFunc: (requestData?: REQ) => any;
  formatData?: (data: any) => DAT;
  requestData?: REQ;
}

// TODO: Add tests for this function?
const asyncRequest = <REQ, DAT>({
                                  request,
                                  success,
                                  failure,
                                  afterSuccess,
                                  afterFailure,
                                  requestFunc,
                                  requestData,
                                  formatData = (id) => id,
                                }: AsyncRequest<REQ, DAT>) =>
  async (dispatch) => {
    try {
      dispatch(request());
      const {data: domainModelData} = await requestFunc(requestData);
      dispatch(success(formatData(domainModelData)));
      if (afterSuccess) {
        afterSuccess(domainModelData, dispatch);
      }
    } catch (error) {
      const {response: {data}} = error;
      dispatch(failure(data));
      if (afterFailure) {
        // TODO: Could this be a source of failure if there is no message field in "data"?
        afterFailure(data.message, dispatch);
      }
    }
  };

const restGet = <T>(endPoint: EndPoints, schema: Schema) => {
  const requestGet = requestMethod<Normalized<T>>(endPoint, HttpMethod.GET);
  const formatData = (data) => normalize(data, schema);
  const requestFunc = (requestData: string) => restClient.get(makeUrl(endPoint, requestData));

  return (requestData?: string) => asyncRequest<string, Normalized<T>>({
    ...requestGet,
    formatData,
    requestFunc,
    requestData,
  });
};

const restPost = <T>(endPoint: EndPoints, restCallbacks: RestCallbacks<T>) => {
  const requestPost = requestMethod<T>(endPoint, HttpMethod.POST);
  const requestFunc = (requestData: T) => restClient.post(makeUrl(endPoint), requestData);

  return (requestData: T) => asyncRequest<T, T>({...requestPost, requestFunc, requestData, ...restCallbacks});
};

const restPut = <T>(endPoint: EndPoints, restCallbacks: RestCallbacks<T>) => {
  const requestPut = requestMethod<T>(endPoint, HttpMethod.PUT);
  const requestFunc = (requestData: T) => restClient.put(makeUrl(endPoint), requestData);

  return (requestData: T) => asyncRequest<T, T>({...requestPut, requestFunc, requestData, ...restCallbacks});
};

const restDelete = <T>(endPoint: EndPoints, restCallbacks: RestCallbacks<T>) => {
  const requestDelete = requestMethod<T>(endPoint, HttpMethod.DELETE);
  const requestFunc = (requestData: uuid) =>
    restClient.delete(makeUrl(`${endPoint}/${encodeURIComponent(requestData.toString())}`));

  return (requestData: uuid) => asyncRequest<uuid, T>({...requestDelete, requestFunc, requestData, ...restCallbacks});
};

export const fetchSelections = restGet<IdNamed>(EndPoints.selections, selectionsSchema);
export const fetchGateways = restGet<Gateway>(EndPoints.gateways, gatewaySchema);
export const fetchMeters = restGet<Gateway>(EndPoints.meters, meterSchema);
export const fetchUsers = restGet<User>(EndPoints.users, userSchema);

export const addUser = restPost<User>(EndPoints.users, {
  afterSuccess: (user: User, dispatch: Dispatch<RootState>) => {
    dispatch(showMessage(firstUpperTranslated('successfully created the user {{name}} ({{email}})', {...user})));
  },
  afterFailure: (error: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showMessage(firstUpperTranslated('failed to create user: {{error}}', {error})));
  },
});

export const modifyUser = restPut<User>(EndPoints.users, {
  afterSuccess: (user: User, dispatch: Dispatch<RootState>) => {
    dispatch(showMessage(firstUpperTranslated('successfully updated user {{name}} ({{email}})', {...user})));
  },
  afterFailure: (error: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showMessage(firstUpperTranslated('failed to update user: {{error}}', {error})));
  },
});

export const modifyProfile = restPut<User>(EndPoints.users, {
  afterSuccess: (user: User, dispatch: Dispatch<RootState>) => {
    dispatch(showMessage(firstUpperTranslated('successfully updated profile', {...user})));
    dispatch(authSetUser(user));
  },
  afterFailure: (error: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showMessage(firstUpperTranslated('failed to update profile: {{error}}', {error})));
  },
});

export const deleteUser = restDelete<User>(EndPoints.users, {
    afterSuccess: (user: User, dispatch: Dispatch<RootState>) => {
      dispatch(showMessage(firstUpperTranslated('successfully deleted the user {{name}} ({{email}})', {...user})));
    },
    afterFailure: (error: ErrorResponse, dispatch: Dispatch<RootState>) => {
      dispatch(showMessage(firstUpperTranslated('failed to delete the user: {{error}}', {error})));
    },
  },
);
