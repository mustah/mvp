import {normalize, Schema} from 'normalizr';
import {createEmptyAction, createPayloadAction, EmptyAction, PayloadAction} from 'react-redux-typescript';
import {makeUrl} from '../../helpers/urlFactory';
import {restClient} from '../../services/restClient';
import {ErrorResponse, IdNamed} from '../../types/Types';
import {EndPoints, Normalized} from './domainModels';
import {selectionsSchema} from './domainModelsSchemas';
import {Gateway} from './gateway/gatewayModels';
import {gatewaySchema} from './gateway/gatewaySchema';
import {meterSchema} from './meter/meterSchema';
import {User} from './user/userModels';
import {userSchema} from './user/userSchema';

export const DOMAIN_MODELS_REQUEST = 'DOMAIN_MODELS_REQUEST';
export const DOMAIN_MODELS_SUCCESS = 'DOMAIN_MODELS_SUCCESS';
export const DOMAIN_MODELS_FAILURE = 'DOMAIN_MODELS_FAILURE';

export const DOMAIN_MODELS_CREATE_REQUEST = 'DOMAIN_MODELS_CREATE_REQUEST';
export const DOMAIN_MODELS_CREATE_SUCCESS = 'DOMAIN_MODELS_CREATE_SUCCESS';
export const DOMAIN_MODELS_CREATE_FAILURE = 'DOMAIN_MODELS_CREATE_FAILURE';

interface RestGet<T> {
  request: () => EmptyAction<string>;
  success: (payload: Normalized<T>) => PayloadAction<string, Normalized<T>>;
  failure: (payload: ErrorResponse) => PayloadAction<string, ErrorResponse>;
}

// TODO generalize over all models, not only user (maybe through an enum)
interface RestPost<T> {
  request: (payload: Partial<User>) => PayloadAction<string, Partial<User>>;
  success: (payload: Normalized<T>) => PayloadAction<string, Normalized<T>>;
  failure: (payload: ErrorResponse) => PayloadAction<string, ErrorResponse>;
}

const domainModelGetRequest = <T>(endPoint: EndPoints): RestGet<T> => ({
  request: createEmptyAction(DOMAIN_MODELS_REQUEST.concat(endPoint)),
  success: createPayloadAction<string, Normalized<T>>(DOMAIN_MODELS_SUCCESS.concat(endPoint)),
  failure: createPayloadAction<string, ErrorResponse>(DOMAIN_MODELS_FAILURE.concat(endPoint)),
});

// TODO derive endpoint from T instead of requiring it on the side (T = User and Endpoints.meters is a conflict)
const domainModelPostRequest = <T>(endPoint: EndPoints): RestPost<T> => ({
  request: createPayloadAction<string, Partial<User>>(DOMAIN_MODELS_CREATE_REQUEST.concat(endPoint)),
  success: createPayloadAction<string, Normalized<T>>(DOMAIN_MODELS_CREATE_SUCCESS.concat(endPoint)),
  failure: createPayloadAction<string, ErrorResponse>(DOMAIN_MODELS_CREATE_FAILURE.concat(endPoint)),
});

// TODO derive schema from T instead of requiring it on the side (T = User and metersSchema is a conflict)
const fetchDomainModel = <T>(endPoint: EndPoints, {request, success, failure}: RestGet<T>, schema: Schema) =>
  (encodedUriParameters?: string) =>
    async (dispatch) => {
      try {
        dispatch(request());
        const {data: domainModelData} = await restClient.get(makeUrl(endPoint, encodedUriParameters));
        dispatch(success(normalize(domainModelData, schema)));
      } catch (error) {
        const {response: {data}} = error;
        dispatch(failure(data));
      }
    };

// TODO adapt to all models (bundle them in an enum or such)
const createDomainModel = <T>(endPoint: EndPoints, {request, success, failure}: RestPost<T>, schema: Schema) =>
  (domainModel: Partial<User>) =>
    async (dispatch) => {
      try {
        dispatch(request(domainModel));
        const {data: domainModelData} = await restClient.post(makeUrl(endPoint), domainModel);
        dispatch(success(normalize(domainModelData, schema)));
      } catch (error) {
        const {response: {data}} = error;
        dispatch(failure(data));
      }
    };

export const selectionsRequest = domainModelGetRequest<IdNamed>(EndPoints.selections);
export const fetchSelections = fetchDomainModel<IdNamed>(EndPoints.selections, selectionsRequest, selectionsSchema);

export const gatewayRequest = domainModelGetRequest<Gateway>(EndPoints.gateways);
export const fetchGateways = fetchDomainModel<Gateway>(EndPoints.gateways, gatewayRequest, gatewaySchema);

export const meterRequest = domainModelGetRequest<Gateway>(EndPoints.meters);
export const fetchMeters = fetchDomainModel<Gateway>(EndPoints.meters, meterRequest, meterSchema);

export const userRequest = domainModelGetRequest<User>(EndPoints.users);
export const fetchUsers = fetchDomainModel<User>(EndPoints.users, userRequest, userSchema);

const userPostRequest = domainModelPostRequest<User>(EndPoints.users);
export const addUser = createDomainModel<User>(EndPoints.users, userPostRequest, userSchema);
