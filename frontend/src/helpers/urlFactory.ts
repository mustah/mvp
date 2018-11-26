import {Pagination} from '../state/ui/pagination/paginationModels';
import {
  ParameterName,
  SelectedParameters,
  SelectionInterval,
  SelectionItem,
  ThresholdQuery
} from '../state/user-selection/userSelectionModels';
import {EncodedUriParameters, Omit, uuid} from '../types/Types';
import {toPeriodApiParameters} from './dateHelpers';
import {Maybe} from './Maybe';

type ParameterNames = {
  [key in ParameterName]: string;
};

interface MeterParameterNames extends ParameterNames {
  meterIds: string;
}

const frontendToApiParameters: ParameterNames = {
  addresses: 'address',
  alarms: 'alarm',
  cities: 'city',
  facilities: 'facility',
  gatewayIds: 'gatewayId',
  gatewaySerials: 'gatewaySerial',
  manufacturers: 'manufacturer',
  media: 'medium',
  organisations: 'organisation',
  productModels: 'productModel',
  reported: 'reported',
  secondaryAddresses: 'secondaryAddress',
  threshold: 'threshold',
};

const gatewayParameters: ParameterNames = {
  ...frontendToApiParameters,
};

export const meterParameters: ParameterNames & MeterParameterNames = {
  ...frontendToApiParameters,
  meterIds: 'id',
};

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

export const toThresholdParameter = (threshold: ThresholdQuery | undefined): EncodedUriParameters[] =>
  threshold
    ? [
      'threshold=' + encodeURIComponent(
        `${threshold.quantity} ${threshold.comparator} ${threshold.value} ${threshold.unit}`
      )
    ]
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
  (selectionInterval: SelectionInterval): EncodedUriParameters =>
    toPeriodApiParameters({
      period: selectionInterval.period,
      customDateRange: Maybe.maybe(selectionInterval.customDateRange),
    }).join('&');

export const makeUrl =
  (endpoint: string, parameters?: EncodedUriParameters): EncodedUriParameters =>
    parameters && parameters.length
      ? `${endpoint}?${parameters}`
      : endpoint;
