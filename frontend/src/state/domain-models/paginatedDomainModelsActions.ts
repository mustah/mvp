import {normalize, Schema} from 'normalizr';
import {Dispatch} from 'react-redux';
import {createPayloadAction, PayloadAction} from 'react-redux-typescript';
import {makeUrl} from '../../helpers/urlFactory';
import {RootState} from '../../reducers/rootReducer';
import {restClient} from '../../services/restClient';
import {firstUpperTranslated} from '../../services/translationService';
import {ErrorResponse, HasId} from '../../types/Types';
import {showFailMessage} from '../ui/message/messageActions';
import {paginationUpdateMetaData} from '../ui/pagination/paginationActions';
import {EndPoints, HttpMethod} from './domainModels';
import {Measurement} from './measurement/measurementModels';
import {measurementSchema} from './measurement/measurementSchema';
import {Meter} from './meter/meterModels';
import {meterSchema} from './meter/meterSchema';
import {HasPageNumber, NormalizedPaginated, PaginatedDomainModelsState} from './paginatedDomainModels';

export const DOMAIN_MODELS_PAGINATED_REQUEST = (endPoint: EndPoints) => `DOMAIN_MODELS_PAGINATED_REQUEST${endPoint}`;
export const DOMAIN_MODELS_PAGINATED_GET_SUCCESS = (endPoint: EndPoints) =>
  `DOMAIN_MODELS_PAGINATED_${HttpMethod.GET}_SUCCESS${endPoint}`;
export const DOMAIN_MODELS_PAGINATED_FAILURE = (endPoint: EndPoints) => `DOMAIN_MODELS_PAGINATED_FAILURE${endPoint}`;
export const DOMAIN_MODELS_PAGINATED_CLEAR = (endPoint: EndPoints) => `DOMAIN_MODELS_PAGINATED_CLEAR${endPoint}`;

interface RestRequestHandlePaginated<T> {
  request: (payload) => PayloadAction<string, number>;
  success: (payload) => PayloadAction<string, T>;
  failure: (payload) => PayloadAction<string, ErrorResponse & HasPageNumber>;
}

export const requestMethodPaginated = <T>(endPoint: EndPoints): RestRequestHandlePaginated<T> => ({
  request: createPayloadAction<string, number>(DOMAIN_MODELS_PAGINATED_REQUEST(endPoint)),
  success: createPayloadAction<string, T>(DOMAIN_MODELS_PAGINATED_GET_SUCCESS(endPoint)),
  failure: createPayloadAction<string, ErrorResponse & HasPageNumber>(DOMAIN_MODELS_PAGINATED_FAILURE(endPoint)),
});

interface RestCallbacks<T> {
  afterSuccess?: (domainModel: T, dispatch: Dispatch<RootState>) => void;
  afterFailure?: (error: ErrorResponse, dispatch: Dispatch<RootState>) => void;
}

interface AsyncRequest<REQ, DAT> extends RestRequestHandlePaginated<DAT>, RestCallbacks<DAT> {
  requestFunc: (requestData?: REQ) => any;
  formatData?: (data: any) => DAT;
  requestData?: REQ;
  page: number;
  model: keyof PaginatedDomainModelsState;
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
    page,
    model,
  }: AsyncRequest<REQ, DAT>) =>
  async (dispatch, getState: () => RootState) => {
    const {paginatedDomainModels: models} = getState();
    const shouldFetch = !(models[model].result[page] && models[model].result[page].result);
    // TODO: Perhaps move this check one level up to restGetIfNeeded
    if (shouldFetch) {
      try {
        dispatch(request(page));
        const {data: domainModelData} = await requestFunc(requestData);
        const formattedData = formatData(domainModelData);
        dispatch(success(formattedData));
        if (afterSuccess) {
          afterSuccess(formattedData, dispatch);
        }
      } catch (error) {
        const {response} = error;
        const data: ErrorResponse = response.data || {message: firstUpperTranslated('an unexpected error occurred')};
        dispatch(failure({...data, page}));
        if (afterFailure) {
          afterFailure(data, dispatch);
        }
      }
    }
  };

const restGetIfNeeded = <T extends HasId>(
  endPoint: EndPoints,
  schema: Schema,
  model: keyof PaginatedDomainModelsState,
  restCallbacks?: RestCallbacks<NormalizedPaginated<T>>,
) => {
  const requestGet = requestMethodPaginated<NormalizedPaginated<T>>(endPoint);
  const requestFunc = (requestData: string) => restClient.get(makeUrl(endPoint, requestData));

  return (page: number, requestData?: string) => asyncRequest<string, NormalizedPaginated<T>>({
    ...requestGet,
    formatData: (data) => ({...normalize(data, schema), page}),
    requestFunc,
    requestData,
    page,
    ...restCallbacks,
    model,
  });
};

// TODO: Add tests to both fetchMeasurements and fetchMeters
export const fetchMeasurements =
  restGetIfNeeded<Measurement>(EndPoints.measurements, measurementSchema, 'measurements');
export const fetchMeters = restGetIfNeeded<Meter>(EndPoints.meters, meterSchema, 'meters', {
  afterSuccess: ({result}: NormalizedPaginated<Meter>, dispatch) => dispatch(paginationUpdateMetaData(result)),
  afterFailure: (
    {message}: ErrorResponse,
    dispatch,
  ) => dispatch(showFailMessage(firstUpperTranslated('error: {{message}}', {message}))),
});
