import {normalize, Schema} from 'normalizr';
import {Dispatch} from 'react-redux';
import {createPayloadAction, PayloadAction} from 'react-redux-typescript';
import {makeUrl} from '../../helpers/urlFactory';
import {RootState} from '../../reducers/rootReducer';
import {restClient} from '../../services/restClient';
import {ErrorResponse, HasId, uuid} from '../../types/Types';
import {paginationUpdateMetaData} from '../ui/pagination/paginationActions';
import {EndPoints, HttpMethod} from './domainModels';
import {Measurement} from './measurement/measurementModels';
import {measurementSchema} from './measurement/measurementSchema';
import {Meter} from './meter/meterModels';
import {meterSchema} from './meter/meterSchema';
import {HasComponentId, NormalizedPaginated} from './paginatedDomainModels';

export const DOMAIN_MODELS_PAGINATED_REQUEST = (endPoint: EndPoints) => `DOMAIN_MODELS_PAGINATED_REQUEST${endPoint}`;
export const DOMAIN_MODELS_PAGINATED_GET_SUCCESS = (endPoint: EndPoints) =>
  `DOMAIN_MODELS_PAGINATED_${HttpMethod.GET}_SUCCESS${endPoint}`;
export const DOMAIN_MODELS_PAGINATED_FAILURE = (endPoint: EndPoints) => `DOMAIN_MODELS_PAGINATED_FAILURE${endPoint}`;

export interface RestRequestHandlePaginated<T> {
  request: (payload) => PayloadAction<string, uuid>;
  success: (payload) => PayloadAction<string, T>;
  failure: (payload) => PayloadAction<string, ErrorResponse & HasComponentId>;
}

export const requestMethodPaginated = <T>(endPoint: EndPoints): RestRequestHandlePaginated<T> => ({
  request: createPayloadAction<string, uuid>(DOMAIN_MODELS_PAGINATED_REQUEST(endPoint)),
  success: createPayloadAction<string, T>(DOMAIN_MODELS_PAGINATED_GET_SUCCESS(endPoint)),
  failure: createPayloadAction<string, ErrorResponse & HasComponentId>(DOMAIN_MODELS_PAGINATED_FAILURE(endPoint)),
});

interface RestCallbacks<T> {
  afterSuccess?: (domainModel: T, dispatch: Dispatch<RootState>) => void;
  afterFailure?: (error: ErrorResponse, dispatch: Dispatch<RootState>) => void;
}

interface AsyncRequest<REQ, DAT> extends RestRequestHandlePaginated<DAT>, RestCallbacks<DAT> {
  requestFunc: (requestData?: REQ) => any;
  formatData?: (data: any) => DAT;
  requestData?: REQ;
  componentId: uuid;
}

const asyncRequest = <REQ, DAT>(
  {
    request,
    success,
    failure,
    afterSuccess,
    afterFailure,
    requestFunc,
    formatData = (id) => id,
    requestData,
    componentId,
  }: AsyncRequest<REQ, DAT>) =>
  async (dispatch) => {
    try {
      dispatch(request(componentId));
      const {data: domainModelData} = await requestFunc(requestData);
      const formattedData = formatData(domainModelData);
      dispatch(success(formattedData));
      if (afterSuccess) {
        afterSuccess(formattedData, dispatch);
      }
    } catch (error) {
      const {response: {data}} = error;
      dispatch(failure({...data, componentId}));
      if (afterFailure) {
        afterFailure(data.message, dispatch);
      }
    }
  };

const restPaginatedGet = <T extends HasId>(
  endPoint: EndPoints,
  schema: Schema,
  restCallbacks?: RestCallbacks<NormalizedPaginated<T>>,
) => {
  const requestGet = requestMethodPaginated<NormalizedPaginated<T>>(endPoint);
  const requestFunc = (requestData: string) => restClient.get(makeUrl(endPoint, requestData));

  return (componentId: uuid, requestData?: string) => asyncRequest<string, NormalizedPaginated<T>>({
    ...requestGet,
    formatData: (data) => ({...normalize(data, schema), componentId}),
    requestFunc,
    requestData,
    componentId,
    ...restCallbacks,
  });
};

export const fetchMeasurements = restPaginatedGet<Measurement>(EndPoints.measurements, measurementSchema);
export const fetchMeters = restPaginatedGet<Meter>(EndPoints.meters, meterSchema, {
  afterSuccess: (
    {componentId, result: {content, number, ...pagination}}: NormalizedPaginated<Meter>,
    dispatch,
  ) => dispatch(paginationUpdateMetaData({
    componentId,
    page: {...pagination, currentPage: number, requestedPage: number},
  })),
});
