import {Pagination} from '../state/ui/pagination/paginationModels';
import {
  isValidThreshold,
  ParameterName,
  SelectedParameters,
  SelectionInterval,
  ThresholdQuery
} from '../state/user-selection/userSelectionModels';
import {EncodedUriParameters, IdNamed, Omit, uuid} from '../types/Types';
import {getId} from './collections';
import {isDefined} from './commonUtils';
import {queryParametersOfDateRange, toPeriodApiParameters} from './dateHelpers';
import {Maybe} from './Maybe';

type ParameterNames = {
  [key in ParameterName]: RequestParameter;
};

interface MeterParameterNames extends ParameterNames {
  meterIds: string;
}

export type RequestParameters = Partial<{
  [key in RequestParameter]: string | string [];
}>;

export enum RequestParameter {
  after = 'after',
  address = 'address',
  alarm = 'alarm',
  before = 'before',
  city = 'city',
  facility = 'facility',
  gatewayId = 'gatewayId',
  gatewaySerial = 'gatewaySerial',
  label = 'label',
  logicalMeterId = 'logicalMeterId',
  manufacturer = 'manufacturer',
  medium = 'medium',
  organisation = 'organisation',
  productModel = 'productModel',
  reported = 'reported',
  quantity = 'quantity',
  secondaryAddress = 'secondaryAddress',
  sort = 'sort',
  threshold = 'threshold',
  resolution = 'resolution',
}

const requestParametersBySelectionParameters: ParameterNames = {
  addresses: RequestParameter.address,
  alarms: RequestParameter.alarm,
  cities: RequestParameter.city,
  facilities: RequestParameter.facility,
  gatewayIds: RequestParameter.gatewayId,
  gatewaySerials: RequestParameter.gatewaySerial,
  manufacturers: RequestParameter.manufacturer,
  media: RequestParameter.medium,
  organisations: RequestParameter.organisation,
  productModels: RequestParameter.productModel,
  reported: RequestParameter.reported,
  secondaryAddresses: RequestParameter.secondaryAddress,
  sort: RequestParameter.sort,
  threshold: RequestParameter.threshold,
};

const gatewayParameters: ParameterNames = {
  ...requestParametersBySelectionParameters,
};

export const meterParameters: ParameterNames & MeterParameterNames = {
  ...requestParametersBySelectionParameters,
  meterIds: 'id',
};

const mapRequestParameters =
  (selectedParameter: keyof SelectedParameters, value: any): RequestParameters => {
    if (selectedParameter === 'threshold') {
      return isValidThreshold(value)
        ? {
          [RequestParameter.threshold]: thresholdAsString(value as ThresholdQuery)
        }
        : {};
    }

    if (selectedParameter === 'dateRange') {
      return queryParametersOfDateRange(value as SelectionInterval);
    }

    const apiParameter = requestParametersBySelectionParameters[selectedParameter];
    if (apiParameter && value.length) {
      return {
        [apiParameter]: (value as IdNamed[]).map((value: IdNamed) => value.id.toString()),
      };
    }

    return {};
  };

export const requestParametersFrom = (parameters: SelectedParameters): RequestParameters =>
  Object.keys(parameters)
    .reduce(
      (allParameters: RequestParameters, selectedParameter: keyof SelectedParameters) => ({
        ...allParameters,
        ...mapRequestParameters(
          selectedParameter,
          parameters[selectedParameter]
        )
      }),
      {}
    );

export const encodeRequestParameters = (parameters: RequestParameters): EncodedUriParameters =>
  Object.keys(parameters)
    .map((parameter: RequestParameter): EncodedUriParameters => {
      const value: string | string[] = parameters[parameter]!;
      return Array.isArray(value)
        ? value.map((singleValue: string) => `${parameter}=${encodeURIComponent(singleValue)}`).join('&')
        : `${parameter}=${encodeURIComponent(value)}`;
    })
    .join('&');

export const encodedUriParametersFrom = (
  uriParams: EncodedUriParameters[],
): EncodedUriParameters => uriParams.length ? uriParams.join('&') : '';

export const toPaginationApiParameters = ({page, size}: Pagination) => [
  `size=${encodeURIComponent(size.toString())}`,
  `page=${encodeURIComponent(page.toString())}`,
];

export const toWildcardApiParameter = (query?: string): string[] => query ? [`w=${query}`] : [];

type SelectedParametersById = Omit<SelectedParameters, 'dateRange' | 'threshold'>;
export type EntityApiParametersFactory =
  (selectionParameters: SelectedParametersById) => EncodedUriParameters[];

const thresholdAsString = (threshold: ThresholdQuery): string =>
  `${threshold.quantity} ${threshold.relationalOperator} ${threshold.value} ${threshold.unit}${
    threshold.duration ? ' for ' + threshold.duration + ' days' : ''
    }`;

export const toThresholdParameter = (threshold: ThresholdQuery | undefined): EncodedUriParameters[] =>
  isValidThreshold(threshold)
    ? ['threshold=' + encodeURIComponent(thresholdAsString(threshold!))]
    : [];

export const toEntityApiParametersMeters =
  (selectionParameters: SelectedParametersById): EncodedUriParameters[] =>
    parametersById(selectionParameters, meterParameters);

export const toEntityApiParametersGateways =
  (selectionParameters: SelectedParametersById): EncodedUriParameters[] =>
    parametersById(selectionParameters, gatewayParameters);

const makeParameter = (parameterNames: ParameterNames, parameter: string, value: string): string =>
  `${parameterNames[parameter]}=${encodeURIComponent(value)}`;

const parametersById =
  (selectionParameters: SelectedParametersById, parameterNames: ParameterNames): EncodedUriParameters[] =>
    Object.keys(selectionParameters)
      .reduce((prev: EncodedUriParameters[], parameter: string) =>
        [
          ...prev,
          ...selectionParameters[parameter]
            .map(getId)
            .filter(isDefined)
            .filter((_) => parameterNames[parameter] !== undefined)
            .map((id: uuid) => makeParameter(parameterNames, parameter, id.toString())),
        ], []);

const toMeterIdParameters = (id: uuid) => makeParameter(meterParameters, 'meterIds', id.toString());

export const toMeterIdsApiParameters = (ids: uuid[]): string =>
  encodedUriParametersFrom(ids.map(toMeterIdParameters));

export const toGatewayIdsApiParameters = (ids: uuid[], gatewayId: uuid): string =>
  encodedUriParametersFrom([makeParameter(meterParameters, 'gatewayIds', gatewayId.toString())]);

export const makeApiParametersOf =
  ({period, customDateRange}: SelectionInterval): EncodedUriParameters =>
    toPeriodApiParameters({period, customDateRange: Maybe.maybe(customDateRange)}).join('&');

export const makeUrl =
  (endpoint: string, parameters?: EncodedUriParameters): EncodedUriParameters =>
    parameters && parameters.length
      ? `${endpoint}?${parameters}`
      : endpoint;
