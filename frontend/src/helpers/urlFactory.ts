import {SelectionItem} from '../state/domain-models/domainModels';
import {Pagination} from '../state/ui/pagination/paginationModels';
import {SelectedParameters, SelectionInterval} from '../state/user-selection/userSelectionModels';
import {EncodedUriParameters, Omit, uuid} from '../types/Types';
import {toPeriodApiParameters} from './dateHelpers';
import {Maybe} from './Maybe';

interface ParameterNames {
  [key: string]: string;
}

const baseParameterNames: ParameterNames = {
  addresses: 'address',
  alarms: 'alarm',
  cities: 'city',
  facilities: 'facility',
  gatewayIds: 'gatewayId',
  gatewaySerials: 'gatewaySerial',
  manufacturers: 'manufacturer',
  media: 'medium',
  productModels: 'productModel',
  reported: 'reported',
  secondaryAddresses: 'secondaryAddress',
};

const gatewayParameterNames: ParameterNames = {
  ...baseParameterNames,
};

const meterParameterNames: ParameterNames = {
  ...baseParameterNames,
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

export type EntityApiParametersFactory =
  (selectionParameters: Omit<SelectedParameters, 'dateRange'>) => EncodedUriParameters[];

export const toEntityApiParametersMeters =
  (selectionParameters: Omit<SelectedParameters, 'dateRange'>): EncodedUriParameters[] =>
    toEntityApiParameters(selectionParameters, meterParameterNames);

export const toEntityApiParametersGateways =
  (selectionParameters: Omit<SelectedParameters, 'dateRange'>): EncodedUriParameters[] =>
    toEntityApiParameters(selectionParameters, gatewayParameterNames);

const makeParameter = (parameterNames: ParameterNames, parameter: string, id: uuid): string =>
  `${parameterNames[parameter]}=${encodeURIComponent(id.toString())}`;

const toEntityApiParameters = (
  selectionParameters: Omit<SelectedParameters, 'dateRange'>,
  parameterNames: ParameterNames,
): EncodedUriParameters[] =>
  Object.keys(selectionParameters)
    .reduce((prev: EncodedUriParameters[], parameter: string) =>
      [
        ...prev,
        ...selectionParameters[parameter]
          .filter(({id}: SelectionItem) => id !== undefined)
          .filter((_) => parameterNames[parameter] !== undefined)
          .map(({id}: SelectionItem) => makeParameter(parameterNames, parameter, id)),
      ], []);

const toMeterIdParameters = (id: uuid) => makeParameter(meterParameterNames, 'meterIds', id);

export const toMeterIdsApiParameters = (ids: uuid[]): string =>
  encodedUriParametersFrom(ids.map(toMeterIdParameters));

export const toGatewayIdsApiParameters = (ids: uuid[], gatewayId: uuid): string =>
  encodedUriParametersFrom([makeParameter(meterParameterNames, 'gatewayIds', gatewayId)]);

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
