import {normalize, Schema} from 'normalizr';
import {createEmptyAction, createPayloadAction, EmptyAction, PayloadAction} from 'react-redux-typescript';
import {makeUrl} from '../../helpers/urlFactory';
import {restClient} from '../../services/restClient';
import {ErrorResponse, uuid} from '../../types/Types';
import {EndPoints, HttpMethod} from './domainModels';
import {asyncRequest} from './domainModelsActions';
import {Measurement} from './measurement/measurementModels';
import {measurementSchema} from './measurement/measurementSchema';
import {Meter} from './meter/meterModels';
import {meterSchema} from './meter/meterSchema';
import {NormalizedPaginated} from './paginatedDomainModels';

export const DOMAIN_MODELS_PAGINATED_REQUEST = 'DOMAIN_MODELS_PAGINATED_REQUEST';
export const DOMAIN_MODELS_PAGINATED_GET_SUCCESS = `DOMAIN_MODELS_PAGINATED_${HttpMethod.GET}_SUCCESS`;

export const DOMAIN_MODELS_PAGINATED_FAILURE = 'DOMAIN_MODELS_PAGINATED_FAILURE';

interface RestRequestHandle<T> {
  request: () => EmptyAction<string>;
  success: (payload: T) => PayloadAction<string, T>;
  failure: (payload: ErrorResponse) => PayloadAction<string, ErrorResponse>;
}

export const requestMethodPaginated = <T>(endPoint: EndPoints, requestType: HttpMethod): RestRequestHandle<T> => ({
  request: createEmptyAction<string>(DOMAIN_MODELS_PAGINATED_REQUEST.concat(endPoint)),
  success: createPayloadAction<string, T>(`DOMAIN_MODELS_PAGINATED_${requestType}_SUCCESS`.concat(endPoint)),
  failure: createPayloadAction<string, ErrorResponse>(DOMAIN_MODELS_PAGINATED_FAILURE.concat(endPoint)),
});

const restPaginatedGet = <T>(endPoint: EndPoints, schema: Schema) => {
  const requestGet = requestMethodPaginated<NormalizedPaginated<T>>(endPoint, HttpMethod.GET);
  const formatData = (data) => normalize(data, schema);
  const requestFunc = (requestData: string) => restClient.get(makeUrl(endPoint, requestData));

  return (component: uuid, requestData?: string) => asyncRequest<string, NormalizedPaginated<T>>({
    ...requestGet,
    formatData,
    requestFunc,
    requestData,
  });
};

// TODO support pagination, i.e. "fetch page 2 of this query"
export const fetchMeasurements = restPaginatedGet<Measurement>(EndPoints.measurements, measurementSchema);
export const fetchMeters = restPaginatedGet<Meter>(EndPoints.meters, meterSchema);
