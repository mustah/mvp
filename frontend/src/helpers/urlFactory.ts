import {Pagination} from '../state/ui/pagination/paginationModels';
import {
  isValidThreshold,
  ParameterName,
  SelectedParameters,
  SelectionInterval,
  SelectionItem,
  ThresholdQuery
} from '../state/user-selection/userSelectionModels';
import {EncodedUriParameters, IdNamed, Omit, uuid} from '../types/Types';
import {queryParametersOfDateRange, toPeriodApiParameters} from './dateHelpers';
import {Maybe} from './Maybe';

type ParameterNames = {
  [key in ParameterName]: BackendParameter;
};

interface MeterParameterNames extends ParameterNames {
  meterIds: string;
}

export type BackendParameters = Partial<{
  [key in BackendParameter]: string | string [];
}>;

export enum BackendParameter {
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
  threshold = 'threshold',
}

const frontendToApiParameters: ParameterNames = {
  addresses: BackendParameter.address,
  alarms: BackendParameter.alarm,
  cities: BackendParameter.city,
  facilities: BackendParameter.facility,
  gatewayIds: BackendParameter.gatewayId,
  gatewaySerials: BackendParameter.gatewaySerial,
  manufacturers: BackendParameter.manufacturer,
  media: BackendParameter.medium,
  organisations: BackendParameter.organisation,
  productModels: BackendParameter.productModel,
  reported: BackendParameter.reported,
  secondaryAddresses: BackendParameter.secondaryAddress,
  threshold: BackendParameter.threshold,
};

const gatewayParameters: ParameterNames = {
  ...frontendToApiParameters,
};

export const meterParameters: ParameterNames & MeterParameterNames = {
  ...frontendToApiParameters,
  meterIds: 'id',
};

const frontendValueToBackendParameter =
  (frontendParameter: keyof SelectedParameters, value: any): BackendParameters => {
    if (frontendParameter === 'threshold') {
      return isValidThreshold(value)
        ? {
          [BackendParameter.threshold]: thresholdAsString(value as ThresholdQuery)
        }
        : {};
    }

    if (frontendParameter === 'dateRange') {
      return queryParametersOfDateRange(value as SelectionInterval);
    }

    const backendParameter = frontendToApiParameters[frontendParameter];
    if (backendParameter && value.length) {
      return {
        [backendParameter]: (value as IdNamed[]).map((value: IdNamed) => value.id.toString()),
      };
    }

    return {};
  };

export const queryParametersOfSelectedParameters = (parameters: SelectedParameters): BackendParameters =>
  Object.keys(parameters)
    .reduce(
      (allParameters: BackendParameters, frontendParameter: keyof SelectedParameters) => ({
        ...allParameters,
        ...frontendValueToBackendParameter(
          frontendParameter,
          parameters[frontendParameter]
        )
      }),
      {}
    );

export const encodeBackendParameters = (parameters: BackendParameters): EncodedUriParameters =>
  Object.keys(parameters)
    .map((parameter: BackendParameter): EncodedUriParameters => {
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

export const toQueryApiParameters = (query?: string): string[] => query ? [`w=${query}`] : [];

type ParametersThatAreLists = Omit<SelectedParameters, 'dateRange' | 'threshold'>;
export type EntityApiParametersFactory =
  (selectionParameters: ParametersThatAreLists) => EncodedUriParameters[];

const thresholdAsString = (threshold: ThresholdQuery): string =>
  `${threshold.quantity} ${threshold.relationalOperator} ${threshold.value} ${threshold.unit}`;

export const toThresholdParameter = (threshold: ThresholdQuery | undefined): EncodedUriParameters[] =>
  isValidThreshold(threshold)
    ? ['threshold=' + encodeURIComponent(thresholdAsString(threshold!))]
    : [];

export const toEntityApiParametersMeters =
  (selectionParameters: ParametersThatAreLists): EncodedUriParameters[] =>
    listsToParametersById(selectionParameters, meterParameters);

export const toEntityApiParametersGateways =
  (selectionParameters: ParametersThatAreLists): EncodedUriParameters[] =>
    listsToParametersById(selectionParameters, gatewayParameters);

const makeParameter = (parameterNames: ParameterNames, parameter: string, value: string): string =>
  `${parameterNames[parameter]}=${encodeURIComponent(value)}`;

const listsToParametersById =
  (selectionParameters: ParametersThatAreLists, parameterNames: ParameterNames): EncodedUriParameters[] =>
    Object.keys(selectionParameters)
      .reduce((prev: EncodedUriParameters[], parameter: string) =>
        [
          ...prev,
          ...selectionParameters[parameter]
            .filter(({id}: SelectionItem) => id !== undefined)
            .filter((_) => parameterNames[parameter] !== undefined)
            .map(({id}: SelectionItem) => makeParameter(parameterNames, parameter, id.toString())),
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
